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
import android.content.Context
import android.util.Log

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
                Toast.makeText(
                    requireContext(),
                    "ID == $value_login_ID, PW == $value_login_PW",
                    Toast.LENGTH_SHORT
                ).show()
                // 아래 findNav 주석처리 하면, 로그인 실패해도 자동으로 안 넘어가고, 로그인 화면에 머무름
                // findNavController().navigate(R.id.move_login_to_home)
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


        // 버튼 == 화면 이동
        // 회원가입 버튼 == 회원가입 페이지로 이동
        binding.loginMovetoSignup.setOnClickListener {
            findNavController().navigate(R.id.move_login_to_signup)
        }
        // 관리자 화면 버튼 == 관리자 메인 페이지로 이동
        binding.loginMovetoTempAdmin.setOnClickListener {
            findNavController().navigate(R.id.move_login_to_admin_main)
        }

        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    // 로그인 요청 function 함수
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

                // return 코드 확인용 로그캣
                Log.d("로그인 return 코드값", "로그인 return 코드값 = ${response.code}, body = $bodyStr")

                activity?.runOnUiThread {
                    if (response.isSuccessful && bodyStr != null) {
                        try {
                            val obj = JSONObject(bodyStr)
                            val token = obj.optString("accessToken")
                            val nickname = obj.optString("nickname")
                            val role = obj.optString("role")

                            // 로그캣 확인용
                            Log.d("LOGIN_RESPONSE", "accessToken = $token")
                            Log.d("LOGIN_RESPONSE", "nickname = $nickname")
                            Log.d("LOGIN_RESPONSE", "role = $role")

                            // 로그인 성공 (코드: 200) == return값 3개 저장
                            TokenStore.saveAll(requireContext(), token, nickname, role)
                            Toast.makeText(
                                requireContext(),
                                "로그인 성공: $nickname",
                                Toast.LENGTH_SHORT
                            ).show()


                            // ========================================================================
                            // return { role = ADMIN } == 관리자 페이지로 이동
                            // return { role = STUDENT } == Home 화면으로 이동
                            when (role) {
                                "ADMIN" -> {
                                    findNavController().navigate(R.id.move_login_to_admin_main)
                                }
                                "STUDENT" -> {
                                    findNavController().navigate(R.id.move_login_to_home)
                                }
                                else -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "로그인 role 부분에서 문제 발생",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
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
}

object TokenStore {
    private const val SP = "auth"
    private const val KEY_TOKEN = "accessToken"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_ROLE = "role"

    // 3개 값 저장
    fun saveAll(context: Context, token: String?, nickname: String?, role: String?) {
        if (token.isNullOrBlank()) return
        val sp = context.getSharedPreferences(SP, Context.MODE_PRIVATE)
        sp.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_NICKNAME, nickname)
            .putString(KEY_ROLE, role)
            .apply()
    }

    // 기존 Interceptor에서 쓰는 토큰용
    fun getToken(context: Context): String? =
        context.getSharedPreferences(SP, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)

    fun getNickname(context: Context): String? =
        context.getSharedPreferences(SP, Context.MODE_PRIVATE)
            .getString(KEY_NICKNAME, null)

    fun getRole(context: Context): String? =
        context.getSharedPreferences(SP, Context.MODE_PRIVATE)
            .getString(KEY_ROLE, null)

    fun clear(context: Context) {
        context.getSharedPreferences(SP, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}

class AuthInterceptor(private val context: Context) : Interceptor {
    private val excludedPaths = listOf(
        "/api/auth/login",
        "/api/auth/signup"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val urlPath = original.url.encodedPath

        val shouldAttach = excludedPaths.none { urlPath.endsWith(it) }

        val reqBuilder = original.newBuilder()
            .header("Accept", "application/json")

        if (shouldAttach) {
            val token = TokenStore.getToken(context)
            if (!token.isNullOrBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }
        }

        val request = reqBuilder.build()
        val response = chain.proceed(request)

        // API return 코드 로그로
        Log.d(
            "HTTP",
            "url=${response.request.url}, method=${response.request.method}, code=${response.code}"
        )

        if (response.code == 401) {
            TokenStore.clear(context)
        }
        return response
    }
}

object HttpClientProvider {
    @Volatile
    private var client: OkHttpClient? = null

    fun get(context: Context): OkHttpClient {
        return client ?: synchronized(this) {
            client ?: OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context.applicationContext))
                .build()
                .also { client = it }
        }
    }
}