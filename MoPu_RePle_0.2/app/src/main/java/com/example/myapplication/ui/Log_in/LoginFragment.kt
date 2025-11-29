package com.example.myapplication.ui.Log_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLoginBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import android.util.Log
import com.example.myapplication.ui.Log_in.HttpClientProvider
import com.example.myapplication.ui.Log_in.TokenStore

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 로그인 버튼
        binding.loginButton.setOnClickListener {
            val value_login_ID = binding.loginID.text.toString()
            val value_login_PW = binding.loginPW.text.toString()

            while (true) {
                if (value_login_ID.isBlank()) {
                    AlertDialog.Builder(requireContext()).run {
                        setTitle("ID 입력칸이 비어있습니다")
                        setIcon(android.R.drawable.ic_dialog_alert)
                        setMessage("ID를 입력하고 다시 시도해주세요")
                        setPositiveButton("OK", null)
                        show()
                    }
                    break
                }
                if (value_login_PW.isBlank()) {
                    AlertDialog.Builder(requireContext()).run {
                        setTitle("비밀번호 입력칸이 비어있습니다")
                        setIcon(android.R.drawable.ic_dialog_alert)
                        setMessage("비밀번호를 입력하고 다시 시도해주세요")
                        setPositiveButton("OK", null)
                        show()
                    }
                    break
                }

                POST_login_request(value_login_ID, value_login_PW)
                Toast.makeText(
                    requireContext(),
                    "ID == $value_login_ID, PW == $value_login_PW",
                    Toast.LENGTH_SHORT
                ).show()
                break
            }
        }

        binding.loginMovetoSignup.setOnClickListener {
            findNavController().navigate(R.id.move_login_to_signup)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun POST_login_request(ID: String, Password: String) {
        val client = HttpClientProvider.get(requireContext())

        val json = JSONObject().apply {
            put("studentId", ID)
            put("password", Password)
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://43.202.225.195:8080/api/auth/login")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    AlertDialog.Builder(requireContext()).run {
                        setTitle("서버 연결 실패")
                        setMessage("네트워크 오류: ${e.localizedMessage}")
                        setPositiveButton("OK", null)
                        show()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()

                Log.d(
                    "로그인 return 코드값",
                    "로그인 return 코드값 = ${response.code}, body = $bodyStr"
                )

                activity?.runOnUiThread {
                    if (response.isSuccessful && bodyStr != null) {
                        try {
                            val obj = JSONObject(bodyStr)
                            val token = obj.optString("accessToken")
                            val nickname = obj.optString("nickname")
                            val role = obj.optString("role")

                            Log.d("LOGIN_RESPONSE", "accessToken = $token")
                            Log.d("LOGIN_RESPONSE", "nickname = $nickname")
                            Log.d("LOGIN_RESPONSE", "role = $role")

                            TokenStore.saveAll(requireContext(), token, nickname, role)
                            Toast.makeText(
                                requireContext(),
                                "로그인 성공: $nickname",
                                Toast.LENGTH_SHORT
                            ).show()

                            when (role.uppercase()) {
                                "ADMIN" -> {
                                    findNavController().navigate(
                                        R.id.move_login_to_admin_main
                                    )
                                }
                                "STUDENT" -> {
                                    findNavController().navigate(
                                        R.id.move_login_to_home
                                    )
                                }
                                else -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "알 수 없는 권한: $role",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (t: Throwable) {
                            AlertDialog.Builder(requireContext()).run {
                                setTitle("응답 파싱 실패")
                                setMessage("서버 응답 형식을 확인해주세요\n$bodyStr")
                                setPositiveButton("OK", null)
                                show()
                            }
                        }
                    } else {
                        AlertDialog.Builder(requireContext()).run {
                            setTitle("로그인 실패")
                            setMessage("code: ${response.code}\n${bodyStr ?: ""}")
                            setPositiveButton("OK", null)
                            show()
                        }
                    }
                }
            }
        })
    }
}
