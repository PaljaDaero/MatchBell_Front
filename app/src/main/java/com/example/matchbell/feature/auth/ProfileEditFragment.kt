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

    // 기존 지역 정보 저장 (기본값: 서울)
    private var currentRegion: String = "서울"

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            view?.findViewById<ImageView>(R.id.iv_profile_image)?.setImageURI(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile_image)
        val etNickname = view.findViewById<EditText>(R.id.et_nickname)
        val etJob = view.findViewById<EditText>(R.id.et_job)
        val etBio = view.findViewById<EditText>(R.id.et_bio)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        // 1. 기존 정보 불러오기
        viewModel.fetchMyProfile(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myProfile.collect { user ->
                if (user != null) {
                    etNickname.setText(user.nickname)
                    etJob.setText(user.job)
                    etBio.setText(user.intro)

                    if (!user.region.isNullOrEmpty()) {
                        currentRegion = user.region
                    }

                    if (selectedImageUri == null && !user.avatarUrl.isNullOrEmpty()) {
                        Glide.with(this@ProfileEditFragment).load(user.avatarUrl).into(ivProfile)
                    }
                }
            }
        }

        // 2. 사진 변경
        ivProfile.setOnClickListener { pickImageLauncher.launch("image/*") }

        // 3. 뒤로가기
        btnBack.setOnClickListener { findNavController().popBackStack() }

        // 4. 확인(수정) 버튼
        btnConfirm.setOnClickListener {
            val nickname = etNickname.text.toString().trim()
            val job = etJob.text.toString().trim()
            val bio = etBio.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateData = ProfileUpdateRequest(
                nickname = nickname,
                job = job,
                intro = bio,
                region = currentRegion
            )

            var imagePart: MultipartBody.Part? = null
            if (selectedImageUri != null) {
                val file = uriToFile(selectedImageUri!!)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                }
            }

            viewModel.updateProfile(requireContext(), updateData, imagePart)
        }

        // 5. 결과 처리
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                if (event == "UPDATE_SUCCESS") {
                    Toast.makeText(context, "수정되었습니다!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ⭐⭐⭐ [수정됨] 이미지 압축 및 리사이징 함수 ⭐⭐⭐
    private fun uriToFile(uri: Uri): File? {
        try {
            val context = requireContext()
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // 크기 줄이기 (1024px)
            val scaledBitmap = resizeBitmap(originalBitmap, 1024)

            val file = File(context.cacheDir, "compressed_edit_profile.jpg")
            val outputStream = FileOutputStream(file)

            // 압축 (JPEG, 80%)
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)

            outputStream.flush()
            outputStream.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // [추가] 리사이징 헬퍼 함수
    private fun resizeBitmap(bitmap: android.graphics.Bitmap, maxSize: Int): android.graphics.Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}