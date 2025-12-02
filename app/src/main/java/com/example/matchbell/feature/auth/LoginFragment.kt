package com.example.matchbell.feature.auth

import android.os.Bundle
import android.util.Patterns // [추가] 이메일 검사용 도구
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
        val emailInput = view.findViewById<EditText>(R.id.et_email) // ID는 et_id지만 내용은 이메일
        val passwordInput = view.findViewById<EditText>(R.id.et_pwd)
        val loginButton = view.findViewById<Button>(R.id.btn_login)
        val loadingBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val signupText = view.findViewById<TextView>(R.id.tv_signup)
        val findPwText = view.findViewById<TextView>(R.id.tv_find_pw)

        // 1. 로그인 버튼 클릭
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val pwd = passwordInput.text.toString().trim()

            // ⬇️⬇️⬇️ [추가됨] 임시 로그인(백도어) 코드 시작 ⬇️⬇️⬇️
            if (email == "1@m.com" && pwd == "admin") {
                Toast.makeText(context, "관리자 모드(테스트) 접속!", Toast.LENGTH_SHORT).show()

                // [중요] 가짜 토큰이라도 저장해야 다른 화면(마이페이지 등)에서 튕기지 않습니다.
                TokenManager.saveTokens(requireContext(), "fake_admin_token_12345", "")

                // 메인 화면으로 강제 이동
                findNavController().navigate(R.id.radarFragment)
                return@setOnClickListener // 여기서 함수 종료 (서버 요청 안 함)
            }
            // ⬆️⬆️⬆️ [추가됨] 임시 로그인 코드 끝 ⬆️⬆️⬆️
            // 빈칸 검사
            if (email.isEmpty()) {
                Toast.makeText(context, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                emailInput.requestFocus()
                return@setOnClickListener
            }

            // ⭐ [추가] 이메일 형식 검사 ⭐
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (pwd.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            // ViewModel에게 진짜 로그인 요청!
            viewModel.onLoginButtonClicked(email, pwd)
        }

        // 2. 회원가입 버튼 클릭
        signupText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupTermsFragment)
        }
/*
        // 3. 비밀번호 찾기 버튼 클릭
        findPwText.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_loginFragment_to_findPasswordFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
*/
        // 4. 로그인 결과 받기
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

                        // 서버가 준 진짜 토큰 저장!
                        TokenManager.saveTokens(
                            requireContext(),
                            event.tokens.jwt,
                            ""
                        )

                        Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
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