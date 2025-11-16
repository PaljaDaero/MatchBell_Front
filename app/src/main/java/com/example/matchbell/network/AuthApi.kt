package com.example.matchbell.network // 1. 패키지 이름 수정

// 2. 데이터 모델 import 경로 수정
import com.example.matchbell.data.model.LoginRequest
import com.example.matchbell.data.model.LoginResponse
import com.example.matchbell.data.model.SignupRequest
import com.example.matchbell.data.model.SignupResponse
import com.example.matchbell.data.model.EmailRequest
import com.example.matchbell.data.model.EmailVerifyRequest
import retrofit2.Response // 이 부분을 수정하세요
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    // [추가] 1. 회원가입 API
    @POST("/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>

    // [추가] 2. 이메일 인증번호 요청 API
    @POST("/auth/email/send")
    suspend fun sendEmailVerification(@Body request: EmailRequest): Response<Unit>

    // [추가] 3. 이메일 인증번호 확인 API
    @POST("/auth/email/verify")
    suspend fun verifyEmail(@Body request: EmailVerifyRequest): Response<Unit>
}
