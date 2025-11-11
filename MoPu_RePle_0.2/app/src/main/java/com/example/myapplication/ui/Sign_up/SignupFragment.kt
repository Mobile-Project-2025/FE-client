package com.example.myapplication.ui.Sign_up

import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentSignupBinding
import androidx.core.view.isGone
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!


    // 중복확인 2개 했는가?
    private var value_isCheck_Nickname = false
    private var value_isCheck_StudentID = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 여기 위에까지는 기본코드



        // "회원가입 화면" 텍스트
        binding.signupTop


        // "~ 입력" 텍스트
        binding.signupPlsNickname
        binding.signupPlsStudentID
        binding.signupPlsPassword1
        binding.signupPlsPassword2


        // 중복확인 경고문
        binding.signupPlsChangeNickname.visibility = View.GONE
        binding.signupPlsChangeStudentID.visibility = View.GONE
        // 사용 가능한 ~~입니다 알림문
        binding.signupItsOkNickname.visibility = View.GONE
        binding.signupItsOkStudentID.visibility = View.GONE


        // 닉네임 중복확인 버튼
        binding.signupCheckNickname.setOnClickListener {

            // (임시) 닉네임 중복확인 로직
            if (binding.signupPlsChangeNickname.isGone) {
                binding.signupPlsChangeNickname.visibility = View.VISIBLE
            } else {
                binding.signupPlsChangeNickname.visibility = View.GONE
            }

            value_isCheck_Nickname = true
//            // (api 로직 적용) 닉네임 중복확인 로직
//            if (api이름 == true) {
//                binding.signupItsOkNickname.visibility = View.VISIBLE   // 사용 가능한 닉네임 입니다
//                binding.signupPlsChangeNickname.visibility = View.GONE
//            } else {
//                binding.signupPlsChangeNickname.visibility = View.VISIBLE   // 닉네임 중복입니다!
//                binding.signupItsOkNickname.visibility = View.GONE
//            }
        }

        // 학번 중복확인 버튼
        binding.signupCheckStudentID.setOnClickListener {

            // 학번 중복확인 로직
            if (binding.signupPlsChangeStudentID.isGone) {
                binding.signupPlsChangeStudentID.visibility = View.VISIBLE
            } else {
                binding.signupPlsChangeStudentID.visibility = View.GONE
            }

            value_isCheck_StudentID = true
        }


        // 회원가입 하기 버튼 터치
        binding.signupButton.setOnClickListener {
            val value_signup_Nickname = binding.signupInputNickname.text.toString()
            val value_signup_StudentID = binding.signupInputStudentID.text.toString()
            val value_signup_Password_1 = binding.signupInputPassword1.text.toString()
            val value_signup_Password_2 = binding.signupInputPassword2.text.toString()



            // 검사할 것들 순서 3단계
            // 1. 4개 입력칸 비지는 않았지?
            // 2. 중복확인 2개 다 했지?
            // 3. 비밀번호 입력, 확인 2개가 일치하지?
            while (true) {
                // 1단계 == 4개 입력칸 비지는 않았지?
                if (value_signup_Nickname.isBlank()) {
                    AlertDialog.Builder(requireContext()).run {
                        setTitle("닉네임 입력칸이 비어있습니다")
                        setIcon(android.R.drawable.ic_dialog_alert)
                        setMessage("닉네임을 입력하고 다시 시도해주세요")
                        setPositiveButton("OK", null)
                        show()
                    }
                    break
                }
                if (value_signup_StudentID.isBlank()) {
                    AlertDialog.Builder(requireContext()).run {
                        setTitle("학번 입력칸이 비어있습니다")
                        setIcon(android.R.drawable.ic_dialog_alert)
                        setMessage("학번을 입력하고 다시 시도해주세요")
                        setPositiveButton("OK", null)
                        show()
                    }
                    break
                }
                if (value_signup_Password_1.isBlank()) {
                    AlertDialog.Builder(requireContext()).run {
                        setTitle("비밀번호 입력칸이 비어있습니다")
                        setIcon(android.R.drawable.ic_dialog_alert)
                        setMessage("비밀번호 입력칸을 입력하고 다시 시도해주세요")
                        setPositiveButton("OK", null)
                        show()
                    }
                    break
                }
                if (value_signup_Password_2.isBlank()) {
                    AlertDialog.Builder(requireContext()).run {
                        setTitle("비밀번호 확인칸이 비어있습니다")
                        setIcon(android.R.drawable.ic_dialog_alert)
                        setMessage("비밀번호 확인칸을 입력하고 다시 시도해주세요")
                        setPositiveButton("OK", null)
                        show()
                    }
                    break
                }


                // 2단계 == 중복확인 2개 다 했지?
                if (value_isCheck_Nickname == false) {
                    AlertDialog.Builder(requireContext()).run {
                        setTitle("닉네임 중복확인을 하지 않았습니다")
                        setIcon(android.R.drawable.ic_dialog_alert)
                        setMessage("중복확인 버튼을 눌러서 확인해주세요")
                        setPositiveButton("OK", null)
                        show()
                    }
                    break
                }
                if (value_isCheck_StudentID == false) {
                    AlertDialog.Builder(requireContext()).run {
                        setTitle("학번 중복확인을 하지 않았습니다")
                        setIcon(android.R.drawable.ic_dialog_alert)
                        setMessage("중복확인 버튼을 눌러서 확인해주세요")
                        setPositiveButton("OK", null)
                        show()
                    }
                    break
                }


                // 3단계 == 비밀번호 입력, 확인 틀리지는 않았지?
                if (value_signup_Password_1 != value_signup_Password_2) {
                    AlertDialog.Builder(requireContext()).run {
                        setTitle("비밀번호 확인이 일치하지 않습니다")
                        setIcon(android.R.drawable.ic_dialog_alert)
                        setMessage("비밀번호 입력칸과 확인칸을 확인해주세요")
                        setPositiveButton("OK", null)
                        show()
                    }
                    break
                }


                // 여기까지 break 안 걸렸으면, 회원가입 성공
                POST_signup_request(value_signup_Nickname, value_signup_StudentID, value_signup_Password_1)
                AlertDialog.Builder(requireContext()).run {
                    setTitle("회원가입 완료")
                    setIcon(android.R.drawable.ic_dialog_alert)
                    setMessage("회원가입에 성공했습니다. 로그인 페이지로 이동합니다")
                    setPositiveButton("OK", null)
                    show()
                }
                break
            }
            // 회원가입 성공 시 == 로그인 페이지로 이동
            findNavController().navigate(R.id.move_signup_to_login)
        }
        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    // 회원가입 요청 function 함수
    private fun POST_signup_request(nickname: String, studentId: String, password: String) {
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("nickname", nickname)
            put("studentId", studentId)
            put("password", password)
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://43.202.225.195:8080/api/auth/register")
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
                    if (response.isSuccessful) {
                        AlertDialog.Builder(requireContext()).run {
                            setTitle("회원가입 완료")
                            setMessage("회원가입에 성공했습니다.")
                            setPositiveButton("OK", null)
                            show()
                        }
                    } else {
                        AlertDialog.Builder(requireContext()).run {
                            setTitle("회원가입 실패")
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