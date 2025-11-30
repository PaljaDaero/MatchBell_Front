package com.example.matchbell.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchbell.data.model.ProfileUpdateRequest
import com.example.matchbell.data.model.UserProfileResponse
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    // 내 정보 데이터 (화면에 띄워줄 내용)
    private val _myProfile = MutableStateFlow<UserProfileResponse?>(null)
    val myProfile = _myProfile.asStateFlow()

    // 성공/실패 알림용
    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    // 1. 내 프로필 조회 (GET /me/profile)
    fun fetchMyProfile(context: Context) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getAccessToken(context)
                if (token == null) {
                    _event.emit("로그인이 필요합니다.")
                    return@launch
                }

                val response = authApi.getMyProfile("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    _myProfile.value = response.body() // 데이터 저장 -> 화면이 자동으로 바뀜
                } else {
                    _event.emit("정보 불러오기 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                _event.emit("네트워크 오류: ${e.message}")
            }
        }
    }

    // 2. 프로필 수정 (텍스트 + 사진)
    fun updateProfile(
        context: Context,
        updateData: ProfileUpdateRequest,
        imageFile: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getAccessToken(context) ?: return@launch

                // (1) 텍스트 정보 수정 (PATCH)
                val textResponse = authApi.updateProfile("Bearer $token", updateData)

                if (!textResponse.isSuccessful) {
                    _event.emit("프로필 수정 실패: ${textResponse.code()}")
                    return@launch
                }

                // (2) 사진이 있다면 사진 업로드 (POST)
                if (imageFile != null) {
                    val imageResponse = authApi.uploadProfileImage("Bearer $token", imageFile)
                    if (!imageResponse.isSuccessful) {
                        _event.emit("사진 업로드 실패")
                    }
                }

                // 성공! -> 최신 정보로 다시 새로고침
                fetchMyProfile(context)
                _event.emit("UPDATE_SUCCESS")

            } catch (e: Exception) {
                _event.emit("에러 발생: ${e.message}")
            }
        }
    }
}