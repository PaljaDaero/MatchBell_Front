package com.example.matchbell.network

import com.example.matchbell.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    // 1. 로그인
    // [변경] 반환 타입이 LoginResponse -> AuthResponse 로 변경됨
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // 2. 회원가입
    // [변경] Multipart 제거, JSON 바디 사용, 반환 타입 AuthResponse 로 변경됨
    @POST("/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    // 3. [신규] 프로필 사진 별도 업로드 API
    // 회원가입 성공 후, 발급받은 토큰을 헤더에 넣어서 이미지를 따로 전송합니다.
    @Multipart
    @POST("/me/profile/image")
    suspend fun uploadProfileImage(
        @Header("Authorization") token: String, // "Bearer {access_token}" 형태
        @Part file: MultipartBody.Part
    ): Response<Unit>

    // --- 기존 기능 유지 (필요 없으면 삭제하세요) ---

    // 4. 이메일 인증번호 요청 API
    @POST("/auth/email/send")
    suspend fun sendEmailVerification(@Body request: EmailRequest): Response<Unit>

    // 5. 이메일 인증번호 확인 API
    @POST("/auth/email/verify")
    suspend fun verifyEmail(@Body request: EmailVerifyRequest): Response<Unit>

    // 6. 비밀번호 찾기 - 인증번호 전송 요청
    @POST("auth/password/email/send")
    suspend fun sendPasswordResetCode(@Body body: EmailRequest): Response<Unit>

    // 7. 비밀번호 찾기 - 인증번호 확인 요청
    @POST("auth/password/email/verify")
    suspend fun verifyPasswordResetCode(@Body body: VerifyCodeRequest): Response<VerifyResponse>

    // 8. 비밀번호 재설정 요청
    @POST("auth/password/reset")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<Unit>

    // 9. 비밀번호 변경 (로그인 된 상태)
    @POST("auth/password/change")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    // 10. 회원 탈퇴
    @DELETE("auth/withdraw")
    suspend fun withdrawAccount(): Response<Unit>
}