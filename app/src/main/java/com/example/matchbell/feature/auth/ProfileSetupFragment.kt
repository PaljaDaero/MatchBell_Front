package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
// ⭐⭐⭐ 9행 수정: android:widget.EditText -> android.widget.EditText
import android.widget.EditText
// ⭐⭐⭐ 10행 수정: android:widget.ImageView -> android.widget.ImageView
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileSetupFragment : Fragment(R.layout.fragment_profile_setup) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 껍데기(XML)에서 부품 찾아오기
        val profileImage = view.findViewById<ImageView>(R.id.iv_profile_image)
        val nicknameInput = view.findViewById<EditText>(R.id.et_nickname)
        val btnFinish = view.findViewById<Button>(R.id.btn_finish_signup)

        // '프로필 사진 등록' 클릭 시 (다음 단계 기능)
        profileImage.setOnClickListener {
            // TODO: 갤러리 여는 로직 (ActivityResultLauncher)
            Toast.makeText(context, "갤러리 열기 (다음 단계 구현)", Toast.LENGTH_SHORT).show()
        }

        // '확인(가입 완료)' 버튼 클릭 시
        btnFinish.setOnClickListener {
            val nickname = nicknameInput.text.toString().trim()

            // [유효성 검사] 닉네임 1~10자 검사
            if (nickname.isEmpty()) {
                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // 멈춤
            }
            if (nickname.length > 10) {
                Toast.makeText(context, "닉네임은 10자 이하로 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // 멈춤
            }

            // TODO: [중복 확인] ViewModel을 만들어 서버에 닉네임 중복 확인 API 호출

            // TODO: 모든 정보(닉네임, 성별, 생일 등)를 ViewModel에 전달하여 서버에 최종 저장

            Toast.makeText(context, "회원가입 완료! 권한 설정으로 이동", Toast.LENGTH_SHORT).show()

            // 4단계 '권한 설정' 화면으로 이동
            // findNavController().navigate(R.id.action_profileSetupFragment_to_permissionFragment)
        }
    }
}