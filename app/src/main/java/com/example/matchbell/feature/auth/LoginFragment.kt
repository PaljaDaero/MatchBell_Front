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

        val idInput = view.findViewById<EditText>(R.id.et_id)
        val passwordInput = view.findViewById<EditText>(R.id.et_password)
        val loginButton = view.findViewById<Button>(R.id.btn_login)
        val loadingBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val signupText = view.findViewById<TextView>(R.id.tv_signup)

        // 1. 로그인 버튼 클릭
        loginButton.setOnClickListener {
            val id = idInput.text.toString().trim()
            val pw = passwordInput.text.toString().trim()

            if (id == "admin" && pw == "admin") { // 백엔드 구축 완료 시 제거할 부분
                Toast.makeText(context, "관리자 모드 (테스트)", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.radarFragment)
                return@setOnClickListener
            }

            if (id.isEmpty()) {
                Toast.makeText(context, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidUsername(id)) {
                Toast.makeText(context, "올바른 아이디 형식이 아닙니다. (6~20자의 영문, 숫자)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (pw.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.onLoginButtonClicked(id, pw)
        }

        // 2. 회원가입 텍스트 클릭 (기존 로직 유지)
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
    private fun isValidUsername(username: String): Boolean {
        // [a-zA-Z0-9]{6,20} : 영문 대소문자, 숫자만 허용하며, 6자에서 20자 사이
        if (username == "admin") return true; // 백엔드 구축 완료 시 제거할 부분ㅁ
        return username.matches("^[a-zA-Z0-9]{6,20}$".toRegex())
    }
}