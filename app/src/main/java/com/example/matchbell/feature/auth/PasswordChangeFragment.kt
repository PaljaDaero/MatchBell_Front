package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import com.example.matchbell.feature.settings.SettingsViewModel // (주의) 뷰모델 패키지 확인
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordChangeFragment : Fragment(R.layout.fragment_password_change) {

    // [필수] 백엔드와 대화하려면 뷰모델이 필요합니다. (아직 안 만드셨으면 빨간 줄 뜰 수 있음)
    // private val viewModel: SettingsViewModel by viewModels()

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

        // 3. 확인 버튼 클릭 (여기가 핵심!)
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

            // -------------------------------------------------------------------------
            // [TODO: 백엔드 연결 시 수정할 부분 - 시작]
            // -------------------------------------------------------------------------
            // 설명: 지금은 무조건 성공했다고 치고 화면을 닫지만,
            // 나중에는 "서버야, 비밀번호 바꿔줘"라고 요청하고, "응 바꿨어"라는 대답을 들었을 때만 닫아야 합니다.

            // [현재 코드: 삭제 대상]
            Toast.makeText(context, "비밀번호가 변경되었습니다. (가짜 성공)", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack() // 그냥 뒤로가기


            // [미래 코드: 이렇게 바꾸세요]
            /*
            // 1. 뷰모델에게 요청 (현재비번, 새비번을 줌)
            viewModel.changePassword(currentPw, newPw)

            // 2. 결과 기다리기 (성공했는지 실패했는지)
            // (lifecycleScope.launch 같은 코드가 필요할 수 있음)
            viewModel.passwordChangeEvent.observe(viewLifecycleOwner) { result ->
                if (result == "SUCCESS") {
                     Toast.makeText(context, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                     findNavController().popBackStack() // 성공했을 때만 뒤로가기!
                } else {
                     Toast.makeText(context, "변경 실패: $result", Toast.LENGTH_SHORT).show()
                     // 실패하면 뒤로가지 않고 그대로 둠 (다시 입력하게)
                }
            }
            */
            // -------------------------------------------------------------------------
            // [TODO: 백엔드 연결 시 수정할 부분 - 끝]
            // -------------------------------------------------------------------------
        }
    }

    private fun isValidPassword(password: String): Boolean {
        if (password == "admin") return true
        val regex = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$"
        return password.matches(regex.toRegex())
    }
}