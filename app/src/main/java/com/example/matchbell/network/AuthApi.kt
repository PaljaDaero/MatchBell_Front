package com.example.matchbell.network

import com.example.matchbell.data.model.AuthResponse
import com.example.matchbell.data.model.ChangePasswordRequest
import com.example.matchbell.data.model.CookieBalanceResponse
import com.example.matchbell.data.model.CookieChargeRequest
import com.example.matchbell.data.model.EmailRequest
import com.example.matchbell.data.model.EmailVerifyRequest
import com.example.matchbell.data.model.LocationRequest
import com.example.matchbell.data.model.LoginRequest
import com.example.matchbell.data.model.ProfileUpdateRequest
import com.example.matchbell.data.model.ResetPasswordRequest
import com.example.matchbell.data.model.SignupRequest
import com.example.matchbell.data.model.UserProfileResponse
import com.example.matchbell.data.model.VerifyCodeRequest
import com.example.matchbell.data.model.VerifyResponse
import com.example.matchbell.feature.CuriousUserSummary
import com.example.matchbell.feature.MatchSummary
import com.example.matchbell.feature.RadarResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

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


    // [수정됨] 쿠키 잔액 조회 (토큰 필요!)
    @GET("/me/cookie") // /api 붙었는지 확인!
    suspend fun getCookieBalance(
        @Header("Authorization") token: String // 👈 이게 꼭 있어야 합니다!
    ): Response<CookieBalanceResponse>

    // [수정됨] 쿠키 충전 (토큰 필요!)
    @POST("/me/cookie/earn") // /api 붙었는지 확인!
    suspend fun chargeCookie(
        @Header("Authorization") token: String, // 👈 이것도 토큰 필요!
        @Body request: CookieChargeRequest
    ): Response<CookieBalanceResponse>
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

    // [추가] 사용자가 보낸 궁금해요 리스트
    @GET("/me/curious/sent")
    suspend fun getSentCurious(
        @Header("Authorization") token: String
    ): Response<List<CuriousUserSummary>>

    // [추가] 사용자가 받은 궁금해요 리스트
    @GET("/me/curious/received")
    suspend fun getReceivedCurious(
        @Header("Authorization") token: String
    ): Response<List<CuriousUserSummary>>

    // [추가] 매칭 리스트 (매칭 완료)
    @GET("/me/matches")
    suspend fun getMatches(
        @Header("Authorization") token: String
    ): Response<List<MatchSummary>>
}