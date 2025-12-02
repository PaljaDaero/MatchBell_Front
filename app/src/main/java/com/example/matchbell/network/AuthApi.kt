package com.example.matchbell.network

import com.example.matchbell.data.model.*
import com.example.matchbell.feature.RadarResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    // ==========================================
    // 1. 로그인 / 회원가입 (Auth)
    // ==========================================

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>


    // ==========================================
    // 2. 프로필 / 내 정보 (Profile)
    // ==========================================

    // 내 프로필 조회 (토큰 필요)
    @GET("/me/profile")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>

    // 내 프로필 정보 수정 (텍스트)
    @PATCH("/me/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileUpdateRequest
    ): Response<UserProfileResponse>

    // 프로필 사진 업로드 (이미지)
    @Multipart
    @POST("/me/profile/image")
    suspend fun uploadProfileImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<UserProfileResponse>

    // 회원 탈퇴 (토큰 필요할 수 있음 -> 필요시 @Header 추가)
    @DELETE("/auth/withdraw")
    suspend fun withdrawAccount(): Response<Unit>

    // 비밀번호 변경
    @POST("/auth/password/change")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>


    // ==========================================
    // 3. 이메일 인증 / 비밀번호 찾기
    // ==========================================

    @POST("/auth/email/send")
    suspend fun sendEmailVerification(@Body request: EmailRequest): Response<Unit>

    @POST("/auth/email/verify")
    suspend fun verifyEmail(@Body request: EmailVerifyRequest): Response<Unit>

    @POST("/auth/password/email/send")
    suspend fun sendPasswordResetCode(@Body body: EmailRequest): Response<Unit>

    @POST("/auth/password/email/verify")
    suspend fun verifyPasswordResetCode(@Body body: VerifyCodeRequest): Response<VerifyResponse>

    @POST("/auth/password/reset")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<Unit>


    // ==========================================
    // 4. 기타 기능 (쿠키, 위치, 레이더)
    // ==========================================

    @GET("/cookie/balance")
    suspend fun getCookieBalance(): Response<CookieBalanceResponse>

    @POST("/cookie/charge")
    suspend fun chargeCookie(@Body request: CookieChargeRequest): Response<CookieBalanceResponse>

    // [수정됨] 현위치 업데이트 (토큰 헤더 추가)
    @POST("/me/location")
    suspend fun updateMyLocation(
        @Header("Authorization") token: String, // 👈 토큰 추가됨
        @Body request: LocationRequest
    ): Response<Unit>

    // [수정됨] 레이더 유저 조회 (토큰 헤더 추가)
    @GET("/radar")
    suspend fun getRadarUsers(
        @Header("Authorization") token: String // 👈 토큰 추가됨
    ): Response<RadarResponse>

    // (참고: 중복된 getMyProfile()은 삭제했습니다.)
}