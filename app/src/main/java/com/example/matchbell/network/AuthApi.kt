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

    // 내 프로필 조회 (토큰 헤더 포함)
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

    // 회원 탈퇴
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

    @POST("/me/location")
    suspend fun updateMyLocation(@Body request: LocationRequest): Response<Unit>

    @GET("/radar")
    suspend fun getRadarUsers(): Response<RadarResponse>

    // [참고] 위쪽에 있는 getMyProfile(token)과 이름이 겹쳐서 에러가 날 수 있습니다.
    // 만약 이 API가 필요 없다면 삭제하시고, 필요하다면 이름을 getMyProfileSimple() 등으로 바꿔주세요.
    // 일단은 /api만 제거해두었습니다.
    @GET("/users/me")
    suspend fun getMyProfile(): Response<ProfileResponse>
}