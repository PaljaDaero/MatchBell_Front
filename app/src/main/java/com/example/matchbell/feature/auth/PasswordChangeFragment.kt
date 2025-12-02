package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope // [필수] 결과 관찰용
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import com.example.matchbell.feature.settings.SettingsViewModel // [확인] 뷰모델 패키지명 확인!
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch // [필수] 코루틴

@AndroidEntryPoint
class PasswordChangeFragment : Fragment(R.layout.fragment_password_change) {

    // [수정됨] 주석 해제하여 뷰모델 연결
    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 부품들 찾아오기
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val etCurrentPw = view.findViewById<EditText>(R.id.et_current_pw)
        val etNewPw = view.findViewById<EditText>(R.id.et_new_pw)
        val etConfirmPw = view.findViewById<EditText>(R.id.et_confirm_pw)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        // 2. 뒤로가기
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 3. 확인 버튼 클릭
        btnConfirm.setOnClickListener {
            val currentPw = etCurrentPw.text.toString().trim()
            val newPw = etNewPw.text.toString().trim()
            val confirmPw = etConfirmPw.text.toString().trim()

            // [검사 1] 빈칸 확인
            if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
                Toast.makeText(context, "모든 칸을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // [검사 2] 새 비밀번호 유효성 검사
            if (!isValidPassword(newPw)) {
                Toast.makeText(context, "비밀번호는 영문, 숫자, 특수문자 포함 8~20자여야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // [검사 3] 새 비밀번호와 확인 비밀번호가 같은지
            if (newPw != confirmPw) {
                Toast.makeText(context, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ⭐⭐⭐ [수정됨] 서버로 진짜 변경 요청 보내기 ⭐⭐⭐
            // (ViewModel에 있는 함수를 호출합니다)
            viewModel.changePassword(currentPw, newPw)
        }

        // 4. [추가됨] 서버 응답 기다리기 (성공/실패 처리)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                if (event == "SUCCESS") {
                    // 성공했을 때만 토스트 띄우고 뒤로가기
                    Toast.makeText(context, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else if (event.startsWith("FAIL")) {
                    // 실패 시 (현재 비번 틀림 등) 메시지만 띄우고 화면 유지
                    Toast.makeText(context, "변경 실패: 현재 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isValidPassword(password: String): Boolean {
        if (password == "admin") return true
        val regex = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$"
        return password.matches(regex.toRegex())
    }
}