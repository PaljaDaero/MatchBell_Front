package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf // [필수] 데이터 묶음
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController // ⭐ [필수] 이 줄이 없어서 에러난 겁니다! ⭐
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupInfoFragment : Fragment(R.layout.fragment_register_info) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [보안] 캡처 방지
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // 1. 뷰 찾기
        val emailInput = view.findViewById<EditText>(R.id.et_signup_email)
        val pwInput = view.findViewById<EditText>(R.id.et_signup_pw)
        val pwConfirmInput = view.findViewById<EditText>(R.id.et_signup_pw_confirm)
        val nextButton = view.findViewById<Button>(R.id.btn_next_step)

        // 2. '다음' 버튼 클릭
        nextButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val pw = pwInput.text.toString().trim()
            val pwConfirm = pwConfirmInput.text.toString().trim()

            // --- 유효성 검사 ---
            if (email.isEmpty()) {
                Toast.makeText(context, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(pw)) {
                Toast.makeText(context, "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (pw != pwConfirm) {
                Toast.makeText(context, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. 데이터 포장 (이메일, 비번)
            val bundle = bundleOf(
                "email" to email,
                "password" to pw
            )

            // 4. 다음 화면으로 이동
            try {
                // NavGraph ID가 맞는지 확인하세요
                findNavController().navigate(R.id.action_signupInfoFragment_to_profileSetupFragment2, bundle)
            } catch (e: Exception) {
                Toast.makeText(context, "이동 실패! Nav Graph ID를 확인하세요.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun isValidPassword(password: String): Boolean {
        val regex = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$"
        return password.matches(regex.toRegex())
    }
}