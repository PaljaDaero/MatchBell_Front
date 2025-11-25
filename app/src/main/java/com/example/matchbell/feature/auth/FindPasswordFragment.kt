package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint
// ⬇️ [ADD] 백엔드 연동 시 주석 해제 (ViewModel 사용을 위해)
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import kotlinx.coroutines.launch

@AndroidEntryPoint
class FindPasswordFragment : Fragment(R.layout.fragment_find_password) {

    // ⬇️ [ADD] 백엔드 연동 시 주석 해제 (ViewModel 연결)
     //private val viewModel: FindPasswordViewModel by viewModels()

    // ⬇️ [DELETE] 백엔드 연동 시 삭제 (ViewModel이 데이터를 관리하므로 필요 없음)
    private var verifiedId: String? = null
    // ⬆️⬆️⬆️ [DELETE END] ⬆️⬆️⬆️

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [보안] 캡처 방지 (유지)
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val emailInput = view.findViewById<EditText>(R.id.et_find_email)
        val sendButton = view.findViewById<Button>(R.id.btn_send_code)
        val authCodeInput = view.findViewById<EditText>(R.id.et_auth_code)
        val verifyButton = view.findViewById<Button>(R.id.btn_verify_code)

        // 1. [전송] 버튼 클릭 이벤트
        sendButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(context, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

           // ⬇️⬇️⬇️ [DELETE START] 백엔드 연동 시 삭제 (가짜 서버 로직) ⬇️⬇️⬇️
            // 가입된 이메일인지 확인 (MockServer)
            val foundId = MockServer.findIdByEmail(email)
            if (foundId == null) {
                Toast.makeText(context, "가입되지 않은 이메일입니다.", Toast.LENGTH_SHORT).show()
            } else {
                verifiedId = foundId
                Toast.makeText(context, "인증번호가 전송되었습니다. (테스트: 아무 번호나 입력)", Toast.LENGTH_LONG).show()
            }
            // ⬆️⬆️⬆️ [DELETE END] ⬆️⬆️⬆️

            // ⬇️⬇️⬇️ [ADD] 백엔드 연동 시 주석 해제 (실제 서버 요청) ⬇️⬇️⬇️

            // ViewModel에게 이메일로 인증번호 보내달라고 요청
            //viewModel.requestEmailCode(email)

        }

        // 2. [확인] 버튼 클릭 이벤트
        verifyButton.setOnClickListener {
            val code = authCodeInput.text.toString().trim()
            val email = emailInput.text.toString().trim() // 이메일도 같이 보내야 함

            // ⬇️⬇️⬇️ [DELETE START] 백엔드 연동 시 삭제 (가짜 검증 로직) ⬇️⬇️⬇️
            // 전송 버튼을 먼저 눌러서 아이디를 찾았는지 확인
            if (verifiedId == null) {
                Toast.makeText(context, "먼저 인증번호를 전송해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (code.isEmpty()) {
                Toast.makeText(context, "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // [Mock] 인증번호 검사 (지금은 무조건 통과)
            Toast.makeText(context, "인증되었습니다.", Toast.LENGTH_SHORT).show()

            // 다음 화면(비밀번호 재설정)으로 이동
            val bundle = bundleOf("userId" to verifiedId)
            findNavController().navigate(R.id.action_findPasswordFragment_to_resetPasswordFragment, bundle)
            // ⬆️⬆️⬆️ [DELETE END] ⬆️⬆️⬆️


            // ⬇️⬇️⬇️ [ADD] 백엔드 연동 시 주석 해제 (실제 서버 검증 요청) ⬇️⬇️⬇️
/*
            if (code.isEmpty()) {
                 Toast.makeText(context, "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
            }
            // ViewModel에게 이 번호가 맞는지 물어봄
            viewModel.verifyCode(email, code)
*/
        }

        // ⬇️⬇️⬇️ [ADD] 백엔드 연동 시 주석 해제 (서버 응답 관찰자) ⬇️⬇️⬇️
/*
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                // 예시: event가 성공이면 다음 화면 이동
                if (event is FindPasswordEvent.VerifySuccess) {
                    Toast.makeText(context, "인증 성공!", Toast.LENGTH_SHORT).show()

                    // 서버가 알려준 진짜 유저 아이디를 받아서 넘김
                    val realUserId = event.userId
                    val bundle = bundleOf("userId" to realUserId)
                    findNavController().navigate(R.id.action_findPasswordFragment_to_resetPasswordFragment, bundle)
                }
                else if (event is FindPasswordEvent.CodeSent) {
                    Toast.makeText(context, "인증번호가 발송되었습니다.", Toast.LENGTH_SHORT).show()
                }
                else if (event is FindPasswordEvent.Error) {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}