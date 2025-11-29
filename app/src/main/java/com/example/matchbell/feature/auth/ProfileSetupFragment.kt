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
import androidx.lifecycle.lifecycleScope // [중요] 코루틴 사용
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import com.example.matchbell.data.model.SignupRequest
import com.google.gson.Gson // [중요] JSON 변환
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull // [중요] 미디어 타입
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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

        // 2. 뷰 찾기 (변수 선언을 맨 위로 올려서 빨간 줄 해결!)
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

        // 생년월일 클릭 (2000~2021 제한)
        birthDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    // 화면엔 "2000년 1월 1일" 형식으로 보여줌
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

        // [확인] 버튼 클릭 (서버 전송)
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

            // --- 데이터 준비 (JSON + File) ---

            // 1) JSON 데이터 만들기 (Gson 사용)
            val signupRequest = SignupRequest(email, password, nickname, nickname, birthday, gender)
            val gson = Gson()
            val jsonString = gson.toJson(signupRequest)

            // 서버 전송용 RequestBody 생성
            val requestBody = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

            // 2) 이미지 파일 만들기 (Multipart)
            var imagePart: MultipartBody.Part? = null
            if (selectedImageUri != null) {
                val file = uriToFile(selectedImageUri!!)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    // "file"은 백엔드가 정한 변수명
                    imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                }
            }

            // 3) 전송 (ViewModel 호출)
            viewModel.signup(requestBody, imagePart)
        }

        // 4. 서버 응답 결과 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                if (event == "SIGNUP_SUCCESS") {
                    Toast.makeText(context, "회원가입 완료! 권한 설정으로 이동합니다.", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_profileSetupFragment_to_permissionFragment)
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