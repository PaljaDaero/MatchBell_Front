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
import com.example.matchbell.feature.CookieSpendRequest
import com.example.matchbell.feature.CuriousUserSummary
import com.example.matchbell.feature.MatchProfileResponse
import com.example.matchbell.feature.MatchSummary
import com.example.matchbell.feature.MyCompatRequest
import com.example.matchbell.feature.MyCompatResponse
import com.example.matchbell.feature.ProfileUnlockResponse
import com.example.matchbell.feature.RadarResponse
import com.example.matchbell.feature.RankingListResponse
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
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("/me/profile")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>

    @PATCH("/me/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileUpdateRequest
    ): Response<UserProfileResponse>

    @Multipart
    @POST("/me/profile/image")
    suspend fun uploadProfileImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<UserProfileResponse>

    @DELETE("/auth/withdraw")
    suspend fun withdrawAccount(): Response<Unit>

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

    @GET("/me/cookie")
    suspend fun getCookieBalance(
        @Header("Authorization") token: String
    ): Response<CookieBalanceResponse>

    @POST("/me/cookie/earn")
    suspend fun chargeCookie(
        @Header("Authorization") token: String,
        @Body request: CookieChargeRequest
    ): Response<CookieBalanceResponse>

    @POST("/me/location")
    suspend fun updateMyLocation(
        @Header("Authorization") token: String,
        @Body request: LocationRequest
    ): Response<Unit>

    @GET("/radar")
    suspend fun getRadarUsers(
        @Header("Authorization") token: String
    ): Response<RadarResponse>

    @GET("/me/curious/sent")
    suspend fun getSentCurious(
        @Header("Authorization") token: String
    ): Response<List<CuriousUserSummary>>

    @GET("/me/curious/received")
    suspend fun getReceivedCurious(
        @Header("Authorization") token: String
    ): Response<List<CuriousUserSummary>>

    @GET("/me/matches")
    suspend fun getMatches(
        @Header("Authorization") token: String
    ): Response<List<MatchSummary>>

    @POST("/me/curious/{targetUserId}")
    suspend fun sendLike(
        @Header("Authorization") token: String,
        @Path("targetUserId") targetUserId: Long
    ): Response<Unit>

    // [수정] 상대방 상세 프로필 조회 (MatchProfileResponse 모델 변경됨)
    @GET("/profiles/{targetUserId}")
    suspend fun getMatchProfile(
        @Header("Authorization") token: String,
        @Path("targetUserId") targetUserId: Long
    ): Response<MatchProfileResponse>

    // [수정] 프로필 잠금 해제 (Body 추가 -> 쿠키 차감 필수!)
    @POST("/me/matches/{targetUserId}/profile/unlock")
    suspend fun unlockProfile(
        @Header("Authorization") token: String,
        @Path("targetUserId") targetUserId: Long,
        @Body request: CookieSpendRequest
    ): Response<ProfileUnlockResponse>

    // ==========================================
    // 6. 나만의 궁합 & 랭킹
    // ==========================================

    @POST("/my-compat")
    suspend fun postMyCompat(
        @Header("Authorization") token: String,
        @Body request: MyCompatRequest
    ): Response<MyCompatResponse>

    @GET("/compat/ranking")
    suspend fun getRanking(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 100
    ): Response<RankingListResponse>
}