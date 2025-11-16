package com.example.matchbell.feature.auth

import android.os.Bundle
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

        // '인증번호 확인' 버튼 클릭 시 -> ViewModel 호출
        authConfirmButton.setOnClickListener {
            val email = emailInput.text.toString()
            val code = authCodeInput.text.toString()
            viewModel.onVerifyEmailClicked(email, code)
        }

        // '최종 확인' 버튼 클릭 시 -> ViewModel 호출
        nextButton.setOnClickListener {
            val pw = pwInput.text.toString()
            val pwConfirm = pwConfirmInput.text.toString()

            // 1. 프론트에서 먼저 비밀번호 일치 검사
            if (pw.isEmpty() || pwConfirm.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pw != pwConfirm) {
                Toast.makeText(context, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. 검사 통과 시 ViewModel에게 회원가입 명령
            viewModel.onSignupButtonClicked(
                idInput.text.toString(),
                pw,
                emailInput.text.toString()
            )
        }

        // 3. ViewModel의 신호(결과) 받기
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