package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // 💡 ViewModel 사용
import androidx.lifecycle.lifecycleScope // 💡 Coroutine 사용
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch // 💡 Coroutine 사용

@AndroidEntryPoint
class FindPasswordFragment : Fragment(R.layout.fragment_find_password) {

    // [수정] ViewModel 연결
    private val viewModel: FindPasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 껍데기(XML)에서 부품 찾아오기
        val emailInput = view.findViewById<TextInputEditText>(R.id.et_find_email)
        val authCodeInput = view.findViewById<TextInputEditText>(R.id.et_find_auth_code)
        val submitButton = view.findViewById<Button>(R.id.btn_find_pw_submit)

        // '인증하기' 버튼 클릭 시 -> ViewModel 호출
        submitButton.setOnClickListener {
            val email = emailInput.text.toString()
            val code = authCodeInput.text.toString()
            viewModel.onVerifyClicked(email, code)
        }

        // [추가] ViewModel의 신호(결과) 받기
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                if (event == "PASSWORD_RESET_SUCCESS") {
                    Toast.makeText(context, "인증 성공! 새 비밀번호 설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()

                    // TODO: '새 비밀번호' 입력 화면을 만들고 연결
                    // findNavController().navigate(R.id.action_findPasswordFragment_to_resetPasswordFragment)
                } else {
                    Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}