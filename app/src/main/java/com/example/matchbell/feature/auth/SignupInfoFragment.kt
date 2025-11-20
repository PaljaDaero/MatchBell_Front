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
class SignupInfoFragment : Fragment(R.layout.fragment_register_info) {

    private val viewModel: SignupInfoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idInput = view.findViewById<EditText>(R.id.et_signup_id)
        val pwInput = view.findViewById<EditText>(R.id.et_signup_pw)
        val pwConfirmInput = view.findViewById<EditText>(R.id.et_signup_pw_confirm)
        val emailInput = view.findViewById<EditText>(R.id.et_signup_email)
        val authCodeInput = view.findViewById<EditText>(R.id.et_auth_code)

        val authConfirmButton = view.findViewById<Button>(R.id.btn_auth_confirm)
        val nextButton = view.findViewById<Button>(R.id.btn_next_step)

        authConfirmButton.setOnClickListener {
            val email = emailInput.text.toString()
            val code = authCodeInput.text.toString()
            viewModel.onVerifyEmailClicked(email, code)
        }

        // [수정됨] '다음' 버튼 클릭 시 무조건 이동하도록 변경
        nextButton.setOnClickListener {

            // --- [임시 주석 처리 시작] 기존 유효성 검사 및 서버 요청 로직 ---
            /*
            val pw = pwInput.text.toString()
            val pwConfirm = pwConfirmInput.text.toString()

            if (pw.isEmpty() || pwConfirm.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pw != pwConfirm) {
                Toast.makeText(context, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.onSignupButtonClicked(
                idInput.text.toString(),
                pw,
                emailInput.text.toString()
            )
            */
            // --- [임시 주석 처리 끝] ---

            // [임시 추가] 무조건 다음 화면(프로필 설정)으로 이동
            // 주의: nav_graph.xml에 정의된 화살표(Action)의 ID와 정확히 일치해야 합니다.
            // 만약 아래 코드가 빨간줄이 뜬다면, 네비게이션 그래프에서 화살표 이름을 확인하세요.
            // (보통 action_출발_to_도착 형식을 따릅니다)

            try {
                // 기존 주석에 있던 ID를 사용했습니다. 만약 빨간줄이면 action 이름을 확인해주세요.
                findNavController().navigate(R.id.action_signupInfoFragment_to_profileSetupFragment2)
            } catch (e: Exception) {
                // ID가 틀렸을 경우를 대비해 안전장치 로그
                Toast.makeText(context, "네비게이션 Action ID를 확인해주세요!", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        // 3. ViewModel 결과 관찰 (지금은 버튼에서 바로 이동하므로 동작하지 않지만 코드는 남겨둠)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                if (event == "SIGNUP_SUCCESS") {
                    Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}