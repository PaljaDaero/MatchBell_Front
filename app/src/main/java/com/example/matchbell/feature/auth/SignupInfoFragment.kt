package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
// [수정 필요] R.layout.fragment_signup_terms를 R.layout.fragment_register_info로 변경
class SignupInfoFragment : Fragment(R.layout.fragment_register_info) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 이 부분은 R.layout.fragment_register_info에서 ID를 올바르게 찾게 되어 정상 작동할 것입니다.
        val idInput = view.findViewById<EditText>(R.id.et_signup_id)
        val pwInput = view.findViewById<EditText>(R.id.et_signup_pw)
        val pwConfirmInput = view.findViewById<EditText>(R.id.et_signup_pw_confirm)
        val btnNext = view.findViewById<Button>(R.id.btn_next_step)


        // '확인(다음 단계)' 버튼 클릭 시
        btnNext.setOnClickListener {
            val id = idInput.text.toString()
            val pw = pwInput.text.toString()
            val pwConfirm = pwConfirmInput.text.toString()

            // 간단한 비밀번호 일치 검사
            if (pw.isEmpty() || pwConfirm.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else if (pw != pwConfirm) {
                Toast.makeText(context, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show()
            } else {
                // 검사 통과 시 다음 화면(프로필 설정)으로 이동!
                Toast.makeText(context, "정보 입력 완료! 프로필 설정으로 이동", Toast.LENGTH_SHORT).show()

                // TODO: 나중에 3단계 프로필 설정 화면을 만들고 연결할 예정
                // findNavController().navigate(R.id.action_signupInfo_to_profileSet)
            }
        }
    }
}