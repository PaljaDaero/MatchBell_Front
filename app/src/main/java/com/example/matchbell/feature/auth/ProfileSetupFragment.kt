package com.example.matchbell.feature.auth

import android.app.AlertDialog
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

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            profileImageView.setImageURI(uri)
        } else {
            Toast.makeText(context, "사진 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 찾아오기
        profileImageView = view.findViewById(R.id.iv_profile_image)
        val nicknameInput = view.findViewById<EditText>(R.id.et_nickname)
        val bioInput = view.findViewById<EditText>(R.id.et_bio)
        val birthDateTextView = view.findViewById<TextView>(R.id.tv_birth_value)
        val regionTextView = view.findViewById<TextView>(R.id.tv_region_value)
        val jobInput = view.findViewById<EditText>(R.id.et_job)
        val btnFinish = view.findViewById<Button>(R.id.btn_finish_signup)

        // 1. 프로필 사진
        profileImageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 2. 생년월일 (⭐⭐⭐ 2000~2021년 제한 추가됨 ⭐⭐⭐)
        birthDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()

            // 기본 선택 위치를 2000년 1월 1일로 설정 (편의성)
            val defaultYear = 2000
            val defaultMonth = 0 // 1월
            val defaultDay = 1

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "${selectedYear}년 ${selectedMonth + 1}월 ${selectedDay}일"
                    birthDateTextView.text = formattedDate
                },
                defaultYear, defaultMonth, defaultDay
            )

            // [핵심] 달력 날짜 범위 제한하기
            val minDate = Calendar.getInstance().apply { set(2000, 0, 1) }.timeInMillis // 2000년 1월 1일
            val maxDate = Calendar.getInstance().apply { set(2021, 11, 31) }.timeInMillis // 2021년 12월 31일

            datePickerDialog.datePicker.minDate = minDate
            datePickerDialog.datePicker.maxDate = maxDate

            datePickerDialog.show()
        }
        /*
        // 2. 생년월일
        birthDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "${selectedYear}년 ${selectedMonth + 1}월 ${selectedDay}일"
                    birthDateTextView.text = formattedDate
                },
                year, month, day
            )
            datePickerDialog.show()
        }
         */

        // 3. 지역 선택
        regionTextView.setOnClickListener {
            val regions = arrayOf(
                "서울특별시", "경기도", "인천광역시", "강원도", "충청북도", "충청남도",
                "대전광역시", "경상북도", "경상남도", "대구광역시", "울산광역시", "부산광역시",
                "전라북도", "전라남도", "광주광역시", "제주특별자치도"
            )

            AlertDialog.Builder(requireContext())
                .setTitle("지역을 선택해주세요")
                .setItems(regions) { _, which ->
                    regionTextView.text = regions[which]
                }
                .show()
        }

        // 4. 확인 버튼 (유효성 검사 추가됨!)
        btnFinish.setOnClickListener {
            // 입력된 내용 가져오기 (.trim()은 앞뒤 공백 제거)
            val nickname = nicknameInput.text.toString().trim()
            val bio = bioInput.text.toString().trim()
            val job = jobInput.text.toString().trim()

            // [검사 1] 닉네임 입력 확인
            if (nickname.isEmpty()) {
                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                nicknameInput.requestFocus() // 커서를 닉네임 칸으로 이동
                return@setOnClickListener // 여기서 함수 종료 (밑으로 안내려감)
            }

            // [검사 2] 자기소개 입력 확인
            if (bio.isEmpty()) {
                Toast.makeText(context, "자기소개를 입력해주세요.", Toast.LENGTH_SHORT).show()
                bioInput.requestFocus()
                return@setOnClickListener
            }

            // [검사 3] 직업 입력 확인 (필요 없으면 이 부분 지워도 됨)
            if (job.isEmpty()) {
                Toast.makeText(context, "직업을 입력해주세요.", Toast.LENGTH_SHORT).show()
                jobInput.requestFocus()
                return@setOnClickListener
            }

            // 위 검사를 모두 통과해야만 여기가 실행됨
            Toast.makeText(context, "프로필 설정 완료! 환영합니다 ($nickname)님", Toast.LENGTH_SHORT).show()

            // 아래 코드를 주석 해제하고 사용하세요! (ID가 맞는지 확인 필요)
            findNavController().navigate(R.id.action_profileSetupFragment_to_permissionFragment)
        }
    }
}