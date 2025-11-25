package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint
// ⬇️ [ADD] 백엔드 연동 시 주석 해제 (ViewModel 사용을 위해)
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {

    // ⬇️ [ADD] 백엔드 연동 시 주석 해제 (ViewModel 연결)
    //private val viewModel: ResetPasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [보안] 캡처 방지 (유지)
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // ⬇️ [ADD] 이전 화면(비밀번호 찾기)에서 보내준 아이디 받기 (매우 중요!)
        // 백엔드에 "이 아이디(userId)의 비번을 바꿔줘"라고 말해야 하기 때문입니다.
        val userId = arguments?.getString("userId") ?: ""

        val newPwInput = view.findViewById<EditText>(R.id.et_new_pw)
        val confirmPwInput = view.findViewById<EditText>(R.id.et_new_pw_confirm)
        val changeButton = view.findViewById<Button>(R.id.btn_change_pw)

        changeButton.setOnClickListener {
            val newPw = newPwInput.text.toString().trim()
            val confirmPw = confirmPwInput.text.toString().trim()

            // 1. 빈칸 검사
            if (newPw.isEmpty() || confirmPw.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. 비밀번호 일치 검사
            if (newPw != confirmPw) {
                Toast.makeText(context, "비밀번호가 서로 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. [요청하신 보안 로직] 정규식 검사
            if (!isValidPassword(newPw)) {
                Toast.makeText(context, "비밀번호 형식이 올바르지 않습니다.\n(영문+숫자+특수문자 포함 8자 이상)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // ⬇️⬇️⬇️ [DELETE START] 백엔드 연동 시 삭제 (가짜 변경 및 강제 이동) ⬇️⬇️⬇️
            // 지금은 서버 연결 없이 MockServer만 업데이트하고 바로 이동합니다.
            if (userId.isNotEmpty()) {
                MockServer.updatePassword(userId, newPw)
                println("임시 비밀번호 변경: $userId -> $newPw")
            }

            Toast.makeText(context, "비밀번호가 변경되었습니다! 로그인해주세요.", Toast.LENGTH_SHORT).show()
            // 로그인 화면으로 이동
            findNavController().navigate(R.id.loginFragment)

            return@setOnClickListener // 여기서 함수 강제 종료
            // ⬆️⬆️⬆️ [DELETE END] ⬆️⬆️⬆️


            // ⬇️⬇️⬇️ [ADD] 백엔드 연동 시 주석 해제 (실제 서버 요청) ⬇️⬇️⬇️
/*
            if (userId.isEmpty()) {
                Toast.makeText(context, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ViewModel에게 "이 유저($userId) 비밀번호를 이걸($newPw)로 바꿔줘" 요청
            viewModel.resetPassword(userId, newPw)
*/
        }

        // ⬇️⬇️⬇️ [ADD] 백엔드 연동 시 주석 해제 (서버 응답 관찰자) ⬇️⬇️⬇️
/*
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                if (event == "SUCCESS") {
                    Toast.makeText(context, "비밀번호 변경 완료! 로그인해주세요.", Toast.LENGTH_SHORT).show()
                    // 서버가 성공했다고 하면 그때 이동!
                    findNavController().navigate(R.id.loginFragment)
                } else {
                    Toast.makeText(context, "변경 실패: $event", Toast.LENGTH_SHORT).show()
                }
            }
        }
*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    // ⭐⭐⭐ 요청하신 보안 검증 함수 ⭐⭐⭐
    private fun isValidPassword(password: String): Boolean {
        // ⬇️⬇️⬇️ [DELETE] 백엔드 구축 완료 시 제거할 부분 ⬇️⬇️⬇️
        if (password == "admin") return true
        // ⬆️⬆️⬆️ [DELETE END] ⬆️⬆️⬆️

        // 영문, 숫자, 특수문자 포함 8~20자
        val regex = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$"
        return password.matches(regex.toRegex())
    }
}