package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [보안] 캡처 방지
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // 뷰 찾기
        val emailInput = view.findViewById<EditText>(R.id.et_id) // ID는 et_id지만 내용은 이메일
        val passwordInput = view.findViewById<EditText>(R.id.et_password)
        val loginButton = view.findViewById<Button>(R.id.btn_login)
        val loadingBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val signupText = view.findViewById<TextView>(R.id.tv_signup)
        val findPwText = view.findViewById<TextView>(R.id.tv_find_pw) // 비밀번호 찾기 버튼

        // 1. 로그인 버튼 클릭
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val pw = passwordInput.text.toString().trim()

            // 빈칸 검사
            if (email.isEmpty()) {
                Toast.makeText(context, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                emailInput.requestFocus()
                return@setOnClickListener
            }
            if (pw.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            // [실행] ViewModel에게 진짜 로그인 요청!
            viewModel.onLoginButtonClicked(email, pw)
        }

        // 2. 회원가입 버튼 클릭
        signupText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupTermsFragment)
        }

        // 3. 비밀번호 찾기 버튼 클릭
        findPwText.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_loginFragment_to_findPasswordFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 4. [핵심] 로그인 결과 받기 (AuthResponse 대응 수정)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginEvent.collect { event ->
                when (event) {
                    is LoginEvent.Loading -> {
                        loadingBar.isVisible = true
                        loginButton.isEnabled = false
                        loginButton.text = "로그인 중..."
                    }

                    is LoginEvent.Success -> {
                        loadingBar.isVisible = false
                        loginButton.isEnabled = true
                        loginButton.text = "확인"

                        // ⭐⭐⭐ [수정됨] AuthResponse 구조에 맞춰 저장 ⭐⭐⭐
                        // event.tokens는 이제 AuthResponse 타입입니다.
                        // accessToken 대신 jwt 필드를 사용하고, 리프레시 토큰은 없으므로 공백 처리합니다.
                        TokenManager.saveTokens(
                            requireContext(),
                            event.tokens.jwt,  // [변경] accessToken -> jwt
                            ""                 // [변경] refreshToken -> 없음("")
                        )

                        Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()

                        // 메인 화면으로 이동
                        findNavController().navigate(R.id.radarFragment)
                    }

                    is LoginEvent.Error -> {
                        loadingBar.isVisible = false
                        loginButton.isEnabled = true
                        loginButton.text = "확인"
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}