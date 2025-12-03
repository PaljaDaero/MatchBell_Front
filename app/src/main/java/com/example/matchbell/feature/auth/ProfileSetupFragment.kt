package com.example.matchbell.feature.auth

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import com.example.matchbell.data.model.SignupRequest
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

        val email = arguments?.getString("email") ?: ""
        val password = arguments?.getString("password") ?: ""

        // 1. 뷰 찾기 (bio 관련 변수 삭제)
        profileImageView = view.findViewById(R.id.iv_profile_image)
        val nicknameInput = view.findViewById<EditText>(R.id.et_nickname)
        // val bioInput = view.findViewById<EditText>(R.id.et_bio) // 삭제됨
        val birthDateTextView = view.findViewById<TextView>(R.id.tv_birth_value)
        val regionTextView = view.findViewById<TextView>(R.id.tv_region_value)
        val jobInput = view.findViewById<EditText>(R.id.et_job)
        val btnFinish = view.findViewById<Button>(R.id.btn_finish_signup)
        val rgGender = view.findViewById<RadioGroup>(R.id.rg_gender)

        // 2. 리스너 설정

        // 프로필 사진 클릭
        profileImageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 생년월일 클릭
        birthDateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, y, m, d -> birthDateTextView.text = "${y}년 ${m + 1}월 ${d}일" },
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

        // [확인] 버튼 클릭
        btnFinish.setOnClickListener {
            val nickname = nicknameInput.text.toString().trim()
            // val bio = bioInput.text.toString().trim() // 삭제됨
            val job = jobInput.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // if (bio.isEmpty()) ... // 삭제됨

            val birthRaw = birthDateTextView.text.toString()
            val dateParts = birthRaw.split(Regex("[^0-9]")).filter { it.isNotEmpty() }

            if (dateParts.size < 3) {
                Toast.makeText(context, "생년월일을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val year = dateParts[0]
            val month = dateParts[1].padStart(2, '0')
            val day = dateParts[2].padStart(2, '0')
            val birthday = "$year-$month-$day"

            val gender = if (rgGender.checkedRadioButtonId == R.id.rb_male) "MALE" else "FEMALE"

            // --- 데이터 준비 ---
            // (SignupRequest에 bio 필드는 원래 없었으므로 그대로 사용)
            val requestData = SignupRequest(
                email = email,
                pwd = password,
                nickname = nickname,
                birth = birthday,
                gender = gender,
                job = job
            )

            // 이미지 파일 준비
            var imagePart: MultipartBody.Part? = null
            if (selectedImageUri != null) {
                val file = uriToFile(selectedImageUri!!)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                }
            }

            // 전송
            viewModel.signup(requestData, imagePart, requireContext())
        }

        // 결과 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                if (event == "SIGNUP_SUCCESS") {
                    Toast.makeText(context, "회원가입 완료! 권한 설정으로 이동합니다.", Toast.LENGTH_SHORT).show()
                    try {
                        findNavController().navigate(R.id.action_profileSetupFragment_to_permissionFragment)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 이미지 압축 함수 (그대로 유지)
    private fun uriToFile(uri: Uri): File? {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            val scaledBitmap = resizeBitmap(originalBitmap, 1024)
            val file = File(requireContext().cacheDir, "compressed_profile.jpg")
            val outputStream = FileOutputStream(file)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
            return file
        } catch (e: Exception) { return null }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        val ratio = width.toFloat() / height.toFloat()
        if (ratio > 1) { width = maxSize; height = (width / ratio).toInt() }
        else { height = maxSize; width = (height * ratio).toInt() }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}