package com.example.matchbell.feature.auth

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import com.example.matchbell.data.model.SignupRequest
// [수정] Gson import 제거됨 (더 이상 안 씀)
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

@AndroidEntryPoint
class ProfileSetupFragment : Fragment(R.layout.fragment_profile_setup) {

    // ViewModel 연결
    private val viewModel: SignupInfoViewModel by viewModels()

    private lateinit var profileImageView: ImageView
    private var selectedImageUri: Uri? = null

    // 갤러리 런처
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            profileImageView.setImageURI(uri)
        } else {
            Toast.makeText(context, "사진 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 이전 화면(회원가입 정보)에서 넘겨준 데이터 받기
        val email = arguments?.getString("email") ?: ""
        val password = arguments?.getString("password") ?: ""

        // 2. 뷰 찾기
        profileImageView = view.findViewById(R.id.iv_profile_image)
        val nicknameInput = view.findViewById<EditText>(R.id.et_nickname)
        val bioInput = view.findViewById<EditText>(R.id.et_bio)
        val birthDateTextView = view.findViewById<TextView>(R.id.tv_birth_value)
        val regionTextView = view.findViewById<TextView>(R.id.tv_region_value)
        val jobInput = view.findViewById<EditText>(R.id.et_job)
        val btnFinish = view.findViewById<Button>(R.id.btn_finish_signup)
        val rgGender = view.findViewById<RadioGroup>(R.id.rg_gender)

        // 3. 리스너 설정

        // 프로필 사진 클릭
        profileImageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 생년월일 클릭
        birthDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    birthDateTextView.text = "${y}년 ${m + 1}월 ${d}일"
                },
                2000, 0, 1
            )
            val minDate = Calendar.getInstance().apply { set(2000, 0, 1) }.timeInMillis
            val maxDate = Calendar.getInstance().apply { set(2021, 11, 31) }.timeInMillis
            datePickerDialog.datePicker.minDate = minDate
            datePickerDialog.datePicker.maxDate = maxDate
            datePickerDialog.show()
        }

        // 지역 선택
        regionTextView.setOnClickListener {
            val regions = arrayOf("서울특별시", "경기도", "인천광역시", "부산광역시", "대구광역시", "광주광역시", "대전광역시")
            AlertDialog.Builder(requireContext())
                .setItems(regions) { _, which -> regionTextView.text = regions[which] }
                .show()
        }

        // [확인] 버튼 클릭 (수정된 부분)
        btnFinish.setOnClickListener {
            val nickname = nicknameInput.text.toString().trim()
            val bio = bioInput.text.toString().trim()
            val job = jobInput.text.toString().trim()

            // 유효성 검사
            if (nickname.isEmpty()) {
                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 날짜 변환 ("2000년 1월 1일" -> "2000-01-01")
            val birthRaw = birthDateTextView.text.toString()
            val birthday = birthRaw.replace("년 ", "-").replace("월 ", "-").replace("일", "")

            // 성별 가져오기
            val gender = if (rgGender.checkedRadioButtonId == R.id.rb_male) "MALE" else "FEMALE"

            // --- [변경] 데이터 준비 (객체 생성 + Multipart) ---

            // 1) 회원가입 요청 객체 생성 (Gson 변환 없이 객체 그대로 사용)
            // 주의: SignupRequest 데이터 클래스의 필드명과 순서를 확인하세요.
            val requestData = SignupRequest(
                email = email,
                pwd = password,   // 데이터 클래스 변수명 확인 (password vs pwd)
                nickname = nickname,
                birth = birthday,
                gender = gender,
                job = job
                // bio 등 필요한 필드가 더 있다면 추가
            )

            // 2) 이미지 파일 만들기 (MultipartBody.Part)
            var imagePart: MultipartBody.Part? = null
            if (selectedImageUri != null) {
                val file = uriToFile(selectedImageUri!!)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    // "file"은 백엔드가 정한 변수명 (AuthApi 참조)
                    imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                }
            }

            // 3) 전송 (ViewModel 호출)
            // Context를 넘기는 이유는 ViewModel에서 토큰 저장을 위해 SharedPreferences를 쓰기 위함입니다.
            viewModel.signup(requestData, imagePart, requireContext())
        }

        // 4. 서버 응답 결과 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                if (event == "SIGNUP_SUCCESS") {
                    Toast.makeText(context, "회원가입 완료! 권한 설정으로 이동합니다.", Toast.LENGTH_SHORT).show()

                    // ⭐⭐⭐ [수정됨] 네비게이션 예외 처리 (안전장치) ⭐⭐⭐
                    try {
                        findNavController().navigate(R.id.action_profileSetupFragment_to_permissionFragment)
                    } catch (e: Exception) {
                        // 경로가 잘못되었거나 연결되지 않았을 때 실행됨
                        Toast.makeText(context, "경로를 다시 확인해주세요. (NavGraph 오류)", Toast.LENGTH_LONG).show()
                        e.printStackTrace() // 로그캣에 자세한 오류 출력
                    }

                } else {
                    Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // [유틸 함수] Uri -> File 변환
    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().cacheDir, "temp_profile.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}