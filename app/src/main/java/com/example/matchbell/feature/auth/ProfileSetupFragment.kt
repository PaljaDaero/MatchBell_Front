package com.example.matchbell.feature.auth

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class ProfileSetupFragment : Fragment(R.layout.fragment_profile_setup) {

    private lateinit var profileImageView: ImageView

    // 1. 갤러리에서 사진 가져오기 위한 '런처' 설정
    // (ActivityResultContracts.GetContent()는 이미지를 가져오는 표준 방식입니다)
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // 사진을 선택했을 경우, 이미지뷰에 바로 보여줌
            profileImageView.setImageURI(uri)

            // 주의: 실제 앱에서는 여기서 선택된 uri를 파일로 변환해 서버로 전송해야 합니다.
            // 지금은 화면에 보여주는 것까지만 구현됨.
        } else {
            Toast.makeText(context, "사진 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 찾아오기
        profileImageView = view.findViewById(R.id.iv_profile_image) // 멤버 변수에 할당
        val nicknameInput = view.findViewById<EditText>(R.id.et_nickname)
        val birthDateTextView = view.findViewById<TextView>(R.id.tv_birth_value) // 생년월일 텍스트뷰
        val btnFinish = view.findViewById<Button>(R.id.btn_finish_signup)

        // -------------------------------------------------------
        // 기능 1: 프로필 사진 클릭 시 갤러리 열기
        // -------------------------------------------------------
        profileImageView.setOnClickListener {
            // "image/*"는 모든 이미지 파일을 보여달라는 뜻입니다.
            pickImageLauncher.launch("image/*")
        }

        // -------------------------------------------------------
        // 기능 2: 생년월일 클릭 시 달력(DatePicker) 띄우기
        // -------------------------------------------------------
        birthDateTextView.setOnClickListener {
            // 현재 날짜를 기준으로 달력을 엽니다.
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // DatePickerDialog 생성
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    // 날짜 선택 후 '확인' 눌렀을 때 실행되는 코드
                    // selectedMonth는 0부터 시작하므로 +1 해줘야 함
                    val formattedDate = "${selectedYear}년 ${selectedMonth + 1}월 ${selectedDay}일"
                    birthDateTextView.text = formattedDate
                },
                year, month, day
            )

            // 달력 표시
            datePickerDialog.show()
        }

        // -------------------------------------------------------
        // 완료 버튼 로직
        // -------------------------------------------------------
        btnFinish.setOnClickListener {
            val nickname = nicknameInput.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (nickname.length > 10) {
                Toast.makeText(context, "닉네임은 10자 이하로 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: 서버 전송 로직 (ViewModel 연동 필요)

            Toast.makeText(context, "회원가입 완료! 권한 설정으로 이동", Toast.LENGTH_SHORT).show()

            // 다음 화면으로 이동 (네비게이션 그래프 ID 확인 필요)
            // findNavController().navigate(R.id.action_profileSetupFragment_to_permissionFragment)
        }
    }
}