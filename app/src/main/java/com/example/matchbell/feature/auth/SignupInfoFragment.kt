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

@AndroidEntryPoint
class SignupInfoFragment : Fragment(R.layout.fragment_register_info) {

    private val viewModel: SignupInfoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            val id = idInput.text.toString()
            val pw = pwInput.text.toString()
            val pwConfirm = pwConfirmInput.text.toString()

            // 1. 유효성 검사
            if (pw.isEmpty() || pwConfirm.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pw != pwConfirm) {
                Toast.makeText(context, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. 가짜 서버(MockServer)에 아이디/비번 저장
            MockServer.register(id, pw)
            println("회원가입 저장됨: $id / $pw")

            // 3. 다음 화면(프로필 설정)으로 이동
            try {
                // nav_graph에서 화살표 ID가 맞는지 꼭 확인하세요!
                findNavController().navigate(R.id.action_signupInfoFragment_to_profileSetupFragment2)
            } catch (e: Exception) {
                Toast.makeText(context, "이동 실패! Nav Graph ID를 확인하세요.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
}