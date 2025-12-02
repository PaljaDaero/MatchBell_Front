package com.example.matchbell.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchbell.data.model.SignupRequest
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class SignupInfoViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    fun signup(signupData: SignupRequest, imageFile: MultipartBody.Part?, context: android.content.Context) {
        viewModelScope.launch {
            try {
                // 1. 회원가입 (데이터 전송)
                val response = authApi.signup(signupData)

                if (response.isSuccessful && response.body() != null) {
                    val jwtToken = response.body()!!.jwt

                    // 2. 토큰 저장
                    TokenManager.saveTokens(context, jwtToken, "")

                    // 3. [수정됨] 사진이 있다면 업로드 (주소: /me/profile/image)
                    if (imageFile != null) {
                        try {
                            // 여기 API가 uploadProfileImage로 바뀌었습니다!
                            val imageResponse = authApi.uploadProfileImage("Bearer $jwtToken", imageFile)

                            if (imageResponse.isSuccessful) {
                                println("사진 업로드 성공: ${imageResponse.body()?.avatarUrl}")
                            } else {
                                println("사진 업로드 실패: ${imageResponse.code()}")
                            }
                        } catch (e: Exception) {
                            println("사진 업로드 중 오류: ${e.message}")
                        }
                    }

                    _event.emit("SIGNUP_SUCCESS")

                } else {
                    _event.emit("가입 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                _event.emit("에러: ${e.message}")
            }
        }
    }

    // (기존 이메일 인증 관련 함수들은 필요하면 유지, 아니면 삭제)
}