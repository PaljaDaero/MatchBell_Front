package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
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

        val idInput = view.findViewById<EditText>(R.id.et_id)
        val passwordInput = view.findViewById<EditText>(R.id.et_password)
        val loginButton = view.findViewById<Button>(R.id.btn_login)
        val loadingBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val signupText = view.findViewById<TextView>(R.id.tv_signup)

        // (보내주신 XML에 있는 ID인 tv_find_pw를 사용합니다)
        val findPwText = view.findViewById<TextView>(R.id.tv_find_pw)
        // [보안] 현재 화면 캡처/녹화 방지 켜기
        requireActivity().window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        // 1. 로그인 버튼 클릭
        loginButton.setOnClickListener {
            val id = idInput.text.toString().trim() // [보안] 공백 제거
            val pw = passwordInput.text.toString().trim()


            // ⬇️⬇️⬇️ [DELETE START] 백엔드 연동 시 삭제 (관리자 테스트 모드) ⬇️⬇️⬇️
            // [테스트용] 관리자 계정 (백엔드 연결 시 삭제 요망)
            if (id == "admin" && pw == "admin") {
                Toast.makeText(context, "관리자 모드 (테스트)", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.radarFragment)
                return@setOnClickListener
                // ⬆️⬆️⬆️ [DELETE END] 여기까지 삭제 ⬆️⬆️⬆️
            }

            // --- [보안 수정 시작] 로그인 시에도 형식을 체크하여 불필요한 요청 방지 ---

            if (id.isEmpty()) {
                Toast.makeText(context, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
                idInput.requestFocus()
                return@setOnClickListener
            }

            // 아이디 형식 검사 (회원가입 조건과 일치시킴)
            if (!isValidUsername(id)) {
                Toast.makeText(context, "아이디 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pw.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            // [추가됨] 비밀번호 형식 검사 (회원가입 조건과 일치시킴)
            // 보안상 로그인 때는 "형식이 틀렸다"고 너무 친절하게 알려주지 않는 경우도 있지만,
            // UX를 위해 사용자가 실수한 것을 바로 알 수 있게 추가했습니다.
            if (!isValidPassword(pw)) {
                Toast.makeText(context, "비밀번호 형식이 올바르지 않습니다. (영문+숫자+특수문자)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // --- [보안 수정 끝] ---


            // ⬇️⬇️⬇️ [DELETE START] 백엔드 연동 시 삭제 (가짜 서버 확인 로직) ⬇️⬇️⬇️
            // ⭐⭐⭐ 가짜 서버(MockServer) 먼저 확인하기 ⭐⭐⭐
            if (MockServer.checkLogin(id, pw)) {
                Toast.makeText(context, "로그인 성공! (임시 계정)", Toast.LENGTH_SHORT).show()

                // [임시] 가짜 토큰 저장 (나중에 삭제됨)
                TokenManager.saveTokens(requireContext(), "fake_access_token_12345")
                // 키보드 내리기 등의 처리를 위해 focus 제거 (선택 사항)
                idInput.clearFocus()
                passwordInput.clearFocus()

                findNavController().navigate(R.id.radarFragment)
                return@setOnClickListener
            }
// ⭐⭐⭐ [추가 끝] ⭐⭐⭐
            // ⬆️⬆️⬆️ [DELETE END] 여기까지 삭제 ⬆️⬆️⬆️

            // 실제 서버 요청
            viewModel.onLoginButtonClicked(id, pw)
        }

        // 2. 회원가입 텍스트 클릭
        signupText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupTermsFragment)
        }
        findPwText.setOnClickListener {
            try {
                // nav_graph에서 만든 화살표 ID를 적으세요.
                findNavController().navigate(R.id.action_loginFragment_to_findPasswordFragment)
            } catch (e: Exception) {
                Toast.makeText(context, "네비게이션 연결을 확인해주세요!", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. 로딩 상태 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                loadingBar.isVisible = isLoading
                loginButton.isEnabled = !isLoading
                loginButton.text = if (isLoading) "로딩 중..." else "확인"
            }
        }

        // 4. 로그인 결과 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginEvent.collect { event ->
                when {
                    event == "SUCCESS" -> {
                        Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        // ⬇️⬇️⬇️ [MODIFY] 나중에 여기에 '진짜 토큰' 저장 코드를 추가해야 함 ⬇️⬇️⬇️
                        // 예: TokenManager.saveTokens(requireContext(), event.realToken)
                        // 아마 주석 지우면 realToken에 오류뜰텐데, 이건 벡엔드에서 백엔드에서 받아온 진짜 토큰 변수 이름받아오면 고치면됨.현재event에는 realToken이라는게 없어서 빨간 줄이 뜹니다.
                        findNavController().navigate(R.id.radarFragment)
                    }
                    else -> {
                        Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    // [중요] 화면을 벗어날 때는 캡처 방지를 풀어줘야 다른 화면(메인)에서 캡처가 됩니다.
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
    }

    // [보안] 아이디 검증 함수 (SignupInfoFragment와 동일)
    private fun isValidUsername(username: String): Boolean {
        if (username == "admin") return true // 백엔드 구축 완료 시 제거할 부분ㅁ
        val regex = "^[a-zA-Z0-9]{6,20}$"
        return username.matches(regex.toRegex())
    }

    // [보안] 비밀번호 검증 함수 추가 (SignupInfoFragment와 동일)
    private fun isValidPassword(password: String): Boolean {
        if (password == "admin") return true // 백엔드 구축 완료 시 제거할 부분ㅁ
        // 영문, 숫자, 특수문자 포함 8~20자
        val regex = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$"
        return password.matches(regex.toRegex())
    }
}