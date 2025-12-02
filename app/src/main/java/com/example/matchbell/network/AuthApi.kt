package com.example.matchbell.network

import com.example.matchbell.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
// 2. 데이터 모델 import 경로 수정
import com.example.matchbell.data.model.ChangePasswordRequest
import com.example.matchbell.data.model.CookieBalanceResponse
import com.example.matchbell.data.model.CookieChargeRequest
import com.example.matchbell.data.model.EmailRequest
import com.example.matchbell.data.model.EmailVerifyRequest
import com.example.matchbell.data.model.LocationRequest
import com.example.matchbell.data.model.LoginRequest
import com.example.matchbell.data.model.LoginResponse
import com.example.matchbell.data.model.ProfileResponse
import com.example.matchbell.data.model.ResetPasswordRequest
import com.example.matchbell.data.model.SignupRequest
import com.example.matchbell.data.model.SignupResponse
import com.example.matchbell.data.model.VerifyCodeRequest
import com.example.matchbell.data.model.VerifyResponse
import com.example.matchbell.feature.RadarResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

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
    // 1. [수정됨] 프로필 사진 업로드 (회원가입 직후 & 프로필 수정 공용)
    @Multipart
    @POST("/me/profile/image")
    suspend fun uploadProfileImage(
        @Header("Authorization") token: String, // "Bearer 토큰"
        @Part file: MultipartBody.Part
    ): Response<UserProfileResponse> // 성공하면 변경된 정보 전체를 줌

    // 2. [수정됨] 내 프로필 정보 수정 (텍스트만)
    @PATCH("/me/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileUpdateRequest
    ): Response<UserProfileResponse> // 성공하면 변경된 정보 전체를 줌

    // 3. [추가] 내 프로필 조회 (마이페이지 들어갈 때)
    @GET("/me/profile")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>
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

    // network/AuthApi.kt
    @POST("auth/password/change") // 주소는 백엔드가 알려줌
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    // [추가] 쿠키 잔액 조회 API
    // GET /cookie/balance
    @GET("/cookie/balance")
    suspend fun getCookieBalance(): Response<CookieBalanceResponse>

    // [추가] 쿠키 충전 API
    // POST /cookie/charge
    @POST("/cookie/charge")
    suspend fun chargeCookie(@Body request: CookieChargeRequest): Response<CookieBalanceResponse>

    // [추가] 내 프로필 정보 조회 API
    // GET /users/me (주소는 백엔드에 따라 변경될 수 있음)
    @GET("/users/me")
    suspend fun getMyProfile(): Response<ProfileResponse>

    @GET("/radar")
    suspend fun getRadarUsers(): Response<RadarResponse>

    // [추가] 현위치 업데이트 API: POST /me/location
    @POST("/me/location")
    suspend fun updateMyLocation(@Body request: LocationRequest): Response<Unit>
}
