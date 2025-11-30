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

    // 최종 회원가입 진행 함수 (순서: 가입 -> 토큰저장 -> 사진업로드)
    fun signup(signupData: SignupRequest, imageFile: MultipartBody.Part?, context: android.content.Context) {
        viewModelScope.launch {
            try {
                // 1단계: 회원가입 정보(JSON) 전송
                val response = authApi.signup(signupData)

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    val jwtToken = authData.jwt

                    // 2단계: 받은 토큰(JWT) 저장 (TokenManager 사용)
                    // (Refresh Token이 없으므로 두 번째 인자는 비워둠)
                    TokenManager.saveTokens(context, jwtToken, "")

                    // 3단계: 프로필 사진이 있다면 업로드 (토큰 필요!)
                    if (imageFile != null) {
                        try {
                            // 헤더에 "Bearer 토큰" 형태로 넣어서 보냄
                            val imageResponse = authApi.uploadProfileImage("Bearer $jwtToken", imageFile)
                            if (!imageResponse.isSuccessful) {
                                // 사진 업로드 실패해도 가입은 성공했으니 로그만 찍음
                                println("사진 업로드 실패: ${imageResponse.code()}")
                            }
                        } catch (e: Exception) {
                            println("사진 업로드 오류: ${e.message}")
                        }
                    }

                    // 모든 과정 완료!
                    _event.emit("SIGNUP_SUCCESS")

                } else {
                    // 가입 실패 (409: 중복 등)
                    // 에러 메시지(response.errorBody())를 파싱하면 좋지만 일단 코드로
                    _event.emit("가입 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                _event.emit("에러 발생: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // (기존 이메일 인증 관련 함수들은 필요하면 유지, 아니면 삭제)
}