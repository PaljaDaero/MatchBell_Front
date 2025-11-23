package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileEditFragment : Fragment(R.layout.fragment_profile_edit) {

    // 갤러리 사진 선택 기능
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            view?.findViewById<ImageView>(R.id.iv_profile_image)?.setImageURI(uri)
            // [TODO: 백엔드 연결 3] 사진을 선택했다면, 이 uri(사진파일)를 서버로 전송하는 코드도 필요할 수 있습니다.
            // 예: viewModel.uploadProfileImage(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 부품 찾기
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile_image)
        val etNickname = view.findViewById<EditText>(R.id.et_nickname)
        val etJob = view.findViewById<EditText>(R.id.et_job)
        val etBio = view.findViewById<EditText>(R.id.et_bio)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        // ---------------------------------------------------------------
        // [TODO: 백엔드 연결 1 - 화면 켜질 때 정보 가져오기]
        // ---------------------------------------------------------------
        loadUserProfile(etNickname, etJob, etBio)


        // 2. 뒤로가기
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 3. 사진 변경
        ivProfile.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // 4. 확인 버튼
        btnConfirm.setOnClickListener {
            val nickname = etNickname.text.toString().trim()
            val job = etJob.text.toString().trim()
            val bio = etBio.text.toString().trim()

            // 빈칸 검사
            if (nickname.isEmpty()) {
                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (job.isEmpty()) {
                Toast.makeText(context, "직업을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (bio.isEmpty()) {
                Toast.makeText(context, "자기소개를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ---------------------------------------------------------------
            // [TODO: 백엔드 연결 2 - 수정된 정보 서버로 보내기]
            // ---------------------------------------------------------------
            // 현재 상태: 그냥 토스트 띄우고 나감
            // 수정 방법: 아래 주석을 참고하여 viewModel 함수 호출로 변경하세요.

            // [예시 코드]
            // viewModel.updateProfile(nickname, job, bio)
            // viewModel.event.collect { result ->
            //     if (result == "성공") {
            //         findNavController().popBackStack()
            //     }
            // }

            // ▼ 나중엔 이 아래 2줄을 지우고 위 코드로 대체하면 됩니다.
            Toast.makeText(context, "프로필이 수정되었습니다. (서버 저장 전)", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    // 정보를 화면에 보여주는 함수
    private fun loadUserProfile(etNickname: EditText, etJob: EditText, etBio: EditText) {
        // ---------------------------------------------------------------
        // [TODO: 백엔드 연결 1 상세 구현]
        // ---------------------------------------------------------------
        // 현재 상태: 가짜 데이터를 직접 입력함
        // 수정 방법: 서버에서 받은 데이터 변수명을 setText 안에 넣으세요.

        // [예시 코드]
        // val user = viewModel.userInfo.value (서버에서 받은 정보)
        // etNickname.setText(user.nickname)
        // etJob.setText(user.job)
        // etBio.setText(user.bio)

        // ▼ 나중엔 이 아래 3줄을 지우고 위 코드로 대체하면 됩니다.
        etNickname.setText("김명지")         // 가짜 데이터 1
        etJob.setText("명지대학교 수학과")     // 가짜 데이터 2
        etBio.setText("운명의 상대를 찾고 있습니다!") // 가짜 데이터 3
    }
}