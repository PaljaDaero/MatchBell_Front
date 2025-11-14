package com.example.matchbell.feature.auth // 1. 본인 패키지 이름인지 확인

import android.os.Bundle
import android.util.Patterns // 💡 Patterns.EMAIL_ADDRESS 사용을 위한 import
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar // 💡 ProgressBar 사용을 위한 import
import android.widget.Toast
import androidx.core.view.isVisible // 💡 View.isVisible 사용을 위한 import
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    // 두뇌(ViewModel) 연결
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailInput = view.findViewById<EditText>(R.id.et_email)
        val passwordInput = view.findViewById<EditText>(R.id.et_password)
        val loginButton = view.findViewById<Button>(R.id.btn_login)
        val loadingBar = view.findViewById<ProgressBar>(R.id.progress_bar) // 💡 로딩바 추가

        // 1. 버튼 클릭 리스너 (유효성 검사 포함)
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim() // 공백 제거
            val pw = passwordInput.text.toString().trim()

            // [유효성 검사] - "경비원 역할"
            if (email.isEmpty()) {
                Toast.makeText(context, "아이디(이메일)를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // 여기서 멈춤
            }

            // 이메일 형식이 맞는지 검사 (안드로이드 기본 도구 사용)
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pw.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 검사 통과하면 ViewModel 호출!
            viewModel.onLoginButtonClicked(email, pw)
        }

        // 2. 로딩 상태 관찰 (뱅글이 보여주기/숨기기)
        viewLifecycleOwner.lifecycleScope.launch {
            // 💡 ViewModel에서 정의한 isLoading Flow를 관찰
            viewModel.isLoading.collect { isLoading ->
                // 로딩 중이면 뱅글이 보이고, 버튼 숨기기 (또는 비활성화)
                loadingBar.isVisible = isLoading // 로딩바 표시/숨김
                loginButton.isEnabled = !isLoading // 로딩 중엔 버튼 못 누르게 막기
                loginButton.text = if (isLoading) "로딩 중..." else "확인"
            }
        }

        // 3. 로그인 결과 관찰 (기존 로직 유지하며 when을 if-else 형태로 변경)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginEvent.collect { event ->
                when {
                    event == "SUCCESS" -> {
                        Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        // 성공 시 레이더 화면으로 이동 (지도에 radarFragment ID가 있어야 함)
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