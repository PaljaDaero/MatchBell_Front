package com.example.matchbell.feature.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView // TextView 추가
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

        val emailInput = view.findViewById<EditText>(R.id.et_email)
        val passwordInput = view.findViewById<EditText>(R.id.et_password)
        val loginButton = view.findViewById<Button>(R.id.btn_login)
        val loadingBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val signupText = view.findViewById<TextView>(R.id.tv_signup) // 회원가입 글씨 가져오기

        // 1. 로그인 버튼 클릭 (기존 로직 유지)
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val pw = passwordInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(context, "아이디(이메일)를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pw.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.onLoginButtonClicked(email, pw)
        }

        // 2. [여기 수정됨!] 회원가입 텍스트 클릭 시 '약관 동의' 화면으로 이동
        signupText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupTermsFragment)
        }

        // 3. 로딩 상태 관찰 (기존 로직 유지)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                loadingBar.isVisible = isLoading
                loginButton.isEnabled = !isLoading
                loginButton.text = if (isLoading) "로딩 중..." else "확인"
            }
        }

        // 4. 로그인 결과 관찰 (기존 로직 유지)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginEvent.collect { event ->
                when {
                    event == "SUCCESS" -> {
                        Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.radarFragment)
                    }
                    else -> {
                        Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}