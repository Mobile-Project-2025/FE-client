package com.example.myapplication.ui.Log_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLoginBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

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

        // 여기 위에까지는 기본코드


        // 상단에 아이콘, 텍스트 2개
        binding.loginIcon
        binding.loginRePle1
        binding.loginRePle2

        // 로그인 버튼
        binding.loginButton.setOnClickListener {
            val value_login_ID = binding.loginID.text.toString()  // 아이디 입력칸
            val value_login_PW = binding.loginPW.text.toString()  // 비밀번호 입력칸


            // 아이디, 입력칸을 비워놓지는 않았는가?
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


                // 로그인 성공 == (임시) 토스트로 로그 띄우고, 홈화면으로 이동
                POST_login_request(value_login_ID, value_login_PW)  // 로그인 api 요청
                Toast.makeText(requireContext(), "ID == $value_login_ID, PW == $value_login_PW", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.move_login_to_home)
                break

//                // 로그인 실패 == 다이얼로그 메시지
//                AlertDialog.Builder(requireContext()).run {
//                    setTitle("로그인 실패")
//                    setIcon(android.R.drawable.ic_dialog_alert)
//                    setMessage("학번, 비밀번호를 다시 확인해주세요")
//                    setPositiveButton("OK", null)
//                    show()
//                }
            }
        }


        // 회원가입 하기 버튼 == 회원가입 페이지로 이동
        binding.loginMovetoSignup.setOnClickListener {
            findNavController().navigate(R.id.move_login_to_signup)
        }
        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    // 로그인 요청 function 함수
    private fun POST_login_request(ID: String, Password: String) {
        val client = OkHttpClient()

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
                activity?.runOnUiThread {
                    if (response.isSuccessful && bodyStr != null) {
                        // 응답 예시: {"accessToken":"...","nickname":"...","role":"STUDENT"}
                        try {
                            val obj = JSONObject(bodyStr)
                            val token = obj.optString("accessToken")
                            val nickname = obj.optString("nickname")
                            val role = obj.optString("role")

                            // 토큰 저장 (SharedPreferences)
                            val sp = requireContext().getSharedPreferences("auth", 0)
                            sp.edit()
                                .putString("accessToken", token)
                                .putString("nickname", nickname)
                                .putString("role", role)
                                .apply()

                            Toast.makeText(requireContext(), "로그인 성공: ${nickname}", Toast.LENGTH_SHORT).show()
                            // 홈으로 이동
                            findNavController().navigate(R.id.move_login_to_home)
                        } catch (t: Throwable) {
                            AlertDialog.Builder(requireContext()).run {
                                setTitle("응답 파싱 실패")
                                setMessage("서버 응답 형식을 확인해주세요\n${bodyStr}")
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
    // TIP: 이후 API 호출 시 Authorization 헤더 넣는 방법 (OkHttp 예)
    // val token = requireContext().getSharedPreferences("auth", 0).getString("accessToken", null)
    // val authedRequest = Request.Builder()
    //     .url("http://43.202.225.195:8080/your/api")
    //     .addHeader("Authorization", "Bearer ${token}")
    //     .build()
}