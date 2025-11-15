package com.example.matchbell.feature.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupInfoFragment : Fragment(R.layout.fragment_register_info) { // XML 이름 확인!

    // [수정] ViewModel 연결
    private val viewModel: SignupInfoViewModel by viewModels()

    // ⭐ 아이디 형식 검사 함수 (LoginFragment와 동일하게) ⭐
    // 6~20자의 영문(소문자/대문자) 및 숫자만 허용하도록 가정
    private fun isValidUsername(username: String): Boolean {
        // [a-zA-Z0-9]{6,20} : 영문 대소문자, 숫자만 허용하며, 6자에서 20자 사이
        return username.matches("^[a-zA-Z0-9]{6,20}$".toRegex())
    }

    // ⭐ 비밀번호 형식 검사 함수 추가 ⭐
    // 최소 8자 이상, 영문, 숫자, 특수문자가 각각 1개 이상 포함되도록 가정
    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$".toRegex()
        return password.matches(passwordRegex)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // XML에서 부품 찾아오기
        val idInput = view.findViewById<EditText>(R.id.et_signup_id)
        val pwInput = view.findViewById<EditText>(R.id.et_signup_pw)
        val pwConfirmInput = view.findViewById<EditText>(R.id.et_signup_pw_confirm)
        val emailInput = view.findViewById<EditText>(R.id.et_signup_email)
        val authCodeInput = view.findViewById<EditText>(R.id.et_auth_code)

        val authConfirmButton = view.findViewById<Button>(R.id.btn_auth_confirm)
        val nextButton = view.findViewById<Button>(R.id.btn_next_step)

        // '인증번호 확인' 버튼 클릭 시 -> ViewModel 호출 (기존 로직 유지)
        authConfirmButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val code = authCodeInput.text.toString().trim()

            // ⭐ 이메일 형식 검사도 추가 (회원가입에서 이메일 인증이 필수이므로) ⭐
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "올바른 이메일 형식을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.onVerifyEmailClicked(email, code)
        }

        // '최종 확인' 버튼 클릭 시 -> ViewModel 호출
        nextButton.setOnClickListener {
            val id = idInput.text.toString().trim()
            val pw = pwInput.text.toString().trim()
            val pwConfirm = pwConfirmInput.text.toString().trim()
            val email = emailInput.text.toString().trim()


            // 1. [수정] 아이디 형식 검사
            if (!isValidUsername(id)) {
                Toast.makeText(context, "아이디는 6~20자의 영문/숫자만 가능합니다.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 2. 비밀번호 유효성 및 일치 검사
            if (pw.isEmpty() || pwConfirm.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pw != pwConfirm) {
                Toast.makeText(context, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. [추가] 비밀번호 규칙 검사
            if (!isValidPassword(pw)) {
                Toast.makeText(context, "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 4. 이메일 유효성 검사 (혹시 최종 버튼 전에 이메일을 수정했을 경우를 대비)
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "유효한 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // 5. 모든 검사 통과 시 ViewModel에게 회원가입 명령
            viewModel.onSignupButtonClicked(id, pw, email)
        }

        // 3. ViewModel의 신호(결과) 받기 (기존 로직 유지)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                if (event == "SIGNUP_SUCCESS") {
                    // 회원가입 성공 시 다음 화면(프로필 설정)으로 이동!
                    Toast.makeText(context, "회원가입 성공! 프로필 설정으로 이동", Toast.LENGTH_SHORT).show()

                    // TODO: 3단계 프로필 설정 화면을 만들고 연결할 예정
                    // findNavController().navigate(R.id.action_signupInfo_to_profileSet)
                } else {
                    // 그 외 모든 메시지는 토스트로 띄우기
                    Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}