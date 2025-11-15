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

        // 주의: XML 레이아웃에서 아이디 입력 필드의 ID가 R.id.et_email로 되어 있다면
        // 변수명은 emailInput 그대로 두되, 실제로는 '아이디'를 받는 필드임을 인지해야 합니다.
        val idInput = view.findViewById<EditText>(R.id.et_email)
        val passwordInput = view.findViewById<EditText>(R.id.et_password)
        val loginButton = view.findViewById<Button>(R.id.btn_login)
        val loadingBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val signupText = view.findViewById<TextView>(R.id.tv_signup) // 회원가입 글씨 가져오기

        // 1. 로그인 버튼 클릭
        loginButton.setOnClickListener {
            val id = idInput.text.toString().trim() // 이메일 대신 '아이디'로 변수명 변경 (가독성 향상)
            val pw = passwordInput.text.toString().trim()

            if (id.isEmpty()) {
                Toast.makeText(context, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ⭐⭐ 기존 이메일 형식 검사 로직을 아이디 형식 검사 로직으로 대체합니다. ⭐⭐
            if (!isValidUsername(id)) {
                Toast.makeText(context, "올바른 아이디 형식이 아닙니다. (6~20자의 영문, 숫자)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (pw.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // LoginViewModel의 함수명은 email, pw를 인수로 받지만,
            // 실제로는 id, pw를 넘겨주므로, LoginViewModel의 함수명도 id, pw로 변경하는 것을 권장합니다.
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

    /**
     * ⭐ 아이디 형식 검사 함수 ⭐
     * - 아이디는 6자 이상 20자 이하의 영문(소문자/대문자) 및 숫자만 허용하도록 가정합니다.
     * - 이 규칙은 회원가입 시 설정된 규칙과 반드시 일치해야 합니다.
     */
    private fun isValidUsername(username: String): Boolean {
        // [a-zA-Z0-9]{6,20} : 영문 대소문자, 숫자만 허용하며, 6자에서 20자 사이
        return username.matches("^[a-zA-Z0-9]{6,20}$".toRegex())
    }
}