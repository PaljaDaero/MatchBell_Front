package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
/*
// ⬇️⬇️⬇️ [UNCOMMENT START] 백엔드 연동 시 아래 주석을 푸세요 ⬇️⬇️⬇️
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
// ⬆️⬆️⬆️ [UNCOMMENT END] ⬆️⬆️⬆️
*/

@AndroidEntryPoint
class SignupInfoFragment : Fragment(R.layout.fragment_register_info) {

    private val viewModel: SignupInfoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [보안] 현재 화면 캡처/녹화 방지 켜기
        requireActivity().window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        // 뷰 찾기
        val idInput = view.findViewById<EditText>(R.id.et_signup_id)
        val pwInput = view.findViewById<EditText>(R.id.et_signup_pw)
        val pwConfirmInput = view.findViewById<EditText>(R.id.et_signup_pw_confirm)
        val emailInput = view.findViewById<EditText>(R.id.et_signup_email)
        val authCodeInput = view.findViewById<EditText>(R.id.et_auth_code)

        val authConfirmButton = view.findViewById<Button>(R.id.btn_auth_confirm)
        val nextButton = view.findViewById<Button>(R.id.btn_next_step)

        // 이메일 인증 버튼
        authConfirmButton.setOnClickListener {
            val email = emailInput.text.toString()
            val code = authCodeInput.text.toString()
            viewModel.onVerifyEmailClicked(email, code)
        }


        // '다음' 버튼 클릭 (회원가입 진행)
        nextButton.setOnClickListener {
            val id = idInput.text.toString().trim() // [보안] 공백 제거
            val pw = pwInput.text.toString().trim()
            val pwConfirm = pwConfirmInput.text.toString().trim()

            // --- [보안 수정 시작] 입력값 유효성 검사 강화 ---

            // 1. 아이디 검사 (6~20자, 영문/숫자)
            if (!isValidUsername(id)) {
                Toast.makeText(context, "아이디는 영문/숫자 포함 6~20자여야 합니다.", Toast.LENGTH_LONG).show()
                idInput.requestFocus()
                return@setOnClickListener
            }

            // 2. 비밀번호 검사 (8~20자, 영문+숫자+특수문자)
            if (!isValidPassword(pw)) {
                Toast.makeText(context, "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다.", Toast.LENGTH_LONG)
                    .show()
                pwInput.requestFocus()
                return@setOnClickListener
            }

            // 3. 비밀번호 일치 검사
            if (pw != pwConfirm) {
                Toast.makeText(context, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show()
                pwConfirmInput.requestFocus()
                return@setOnClickListener
            }
            // --- [보안 수정 끝] ---


            // ⬇️⬇️⬇️ [DELETE START] 백엔드 연동 완료 시 여기서부터 삭제하세요 ⬇️⬇️⬇️
            // 4. 가짜 서버(MockServer)에 아이디/비번 저장
            MockServer.register(id, pw)

            // [보안] 실제 상용 앱에서는 비밀번호를 로그에 찍으면 안됩니다. 개발 단계라 남겨둡니다.
            println("회원가입 저장됨: $id / $pw")


            // 5. 다음 화면(프로필 설정)으로 이동
            try {
                findNavController().navigate(R.id.action_signupInfoFragment_to_profileSetupFragment2)
            } catch (e: Exception) {
                Toast.makeText(context, "이동 실패! Nav Graph ID를 확인하세요.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }



            // ⬆️⬆️⬆️ [DELETE END] 여기까지 삭제하세요 ⬆️⬆️⬆️

            // ⬇️⬇️⬇️ [UNCOMMENT START] 백엔드 연동 시 아래 주석을 푸세요 ⬇️⬇️⬇️
/*
            // 실제 서버로 전송
            viewModel.onSignupButtonClicked(id, pw, emailInput.text.toString())
        }
        // ⬆️⬆️⬆️ [UNCOMMENT END] ⬆️⬆️⬆️
*/

    // ⬇️⬇️⬇️ [UNCOMMENT START] 백엔드 연동 시 아래 주석을 풀어야 결과 처리가 됩니다 ⬇️⬇️⬇️
/*
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                if (event == "SIGNUP_SUCCESS") {
                    Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                    // 성공 시 프로필 설정 화면으로 이동
                    findNavController().navigate(R.id.action_signupInfoFragment_to_profileSetupFragment2)
                } else {
                    // 실패 시 에러 메시지 토스트
                    Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
*/
    // ⬆️⬆️⬆️ [UNCOMMENT END] ⬆️⬆️⬆️

    // [중요] 화면을 벗어날 때는 캡처 방지를 풀어줘야 다른 화면(메인)에서 캡처가 됩니다.
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
    }
    // [보안 기능 추가] 아이디 유효성 검사 함수 (LoginFragment와 동일 조건)
    private fun isValidUsername(username: String): Boolean {
        // 영문 대소문자, 숫자만 허용, 6~20자
        val regex = "^[a-zA-Z0-9]{6,20}$"
        return username.matches(regex.toRegex())
    }

    // [보안 기능 추가] 비밀번호 유효성 검사 함수
    private fun isValidPassword(password: String): Boolean {
        // 최소 8자 ~ 최대 20자
        // (?=.*[a-zA-Z]): 영문자 최소 1개 포함
        // (?=.*[0-9]): 숫자 최소 1개 포함
        // (?=.*[@$!%*?&]): 특수문자 최소 1개 포함
        val regex = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$"
        return password.matches(regex.toRegex())
    }
}
