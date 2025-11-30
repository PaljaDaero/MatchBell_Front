package com.example.matchbell.feature.auth

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.matchbell.R
import com.example.matchbell.data.model.ProfileUpdateRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class ProfileEditFragment : Fragment(R.layout.fragment_profile_edit) {

    private val viewModel: ProfileViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    // 갤러리 런처
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            view?.findViewById<ImageView>(R.id.iv_profile_image)?.setImageURI(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 뷰 찾기
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile_image)
        val etNickname = view.findViewById<EditText>(R.id.et_nickname)
        val etJob = view.findViewById<EditText>(R.id.et_job)
        val etBio = view.findViewById<EditText>(R.id.et_bio)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        // 2. 기존 정보 불러오기 (자동 채우기)
        viewModel.fetchMyProfile(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myProfile.collect { user ->
                if (user != null) {
                    etNickname.setText(user.nickname)
                    etJob.setText(user.job)
                    etBio.setText(user.intro)

                    if (selectedImageUri == null && !user.avatarUrl.isNullOrEmpty()) {
                        Glide.with(this@ProfileEditFragment).load(user.avatarUrl).into(ivProfile)
                    }
                }
            }
        }

        // 3. 사진 변경
        ivProfile.setOnClickListener { pickImageLauncher.launch("image/*") }

        // 4. 뒤로가기
        btnBack.setOnClickListener { findNavController().popBackStack() }

        // 5. 확인(수정) 버튼
        btnConfirm.setOnClickListener {
            val nickname = etNickname.text.toString().trim()
            val job = etJob.text.toString().trim()
            val bio = etBio.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 수정 데이터 객체 생성 (지역은 일단 서울로 고정, 필요시 UI 추가)
            val updateData = ProfileUpdateRequest(
                nickname = nickname,
                job = job,
                intro = bio,
                region = "서울"
            )

            // 이미지 파일 준비
            var imagePart: MultipartBody.Part? = null
            if (selectedImageUri != null) {
                val file = uriToFile(selectedImageUri!!)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                }
            }

            // 서버 전송
            viewModel.updateProfile(requireContext(), updateData, imagePart)
        }

        // 6. 결과 처리 (성공 시 뒤로가기)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                if (event == "UPDATE_SUCCESS") {
                    Toast.makeText(context, "수정되었습니다!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack() // 마이페이지로 복귀
                } else {
                    Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 유틸 함수
    private fun uriToFile(uri: Uri): File? {
        return try {
            val stream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().cacheDir, "temp_edit.jpg")
            val output = FileOutputStream(file)
            stream.copyTo(output)
            stream.close()
            output.close()
            file
        } catch (e: Exception) { null }
    }
}