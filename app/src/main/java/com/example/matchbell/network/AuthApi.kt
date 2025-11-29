package com.example.matchbell.network // 1. 패키지 이름 수정

// 2. 데이터 모델 import 경로 수정
import com.example.matchbell.data.model.LoginRequest
import com.example.matchbell.data.model.LoginResponse
import com.example.matchbell.data.model.SignupRequest
import com.example.matchbell.data.model.SignupResponse
import com.example.matchbell.data.model.EmailRequest
import com.example.matchbell.data.model.EmailVerifyRequest
import com.example.matchbell.data.model.ResetPasswordRequest
import com.example.matchbell.data.model.VerifyCodeRequest
import com.example.matchbell.data.model.VerifyResponse
import retrofit2.Response // 이 부분을 수정하세요
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.DELETE
import com.example.matchbell.data.model.ChangePasswordRequest
import com.example.matchbell.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

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

    // [추가] 비밀번호 찾기 - 인증번호 전송 요청
    // [나중에 백엔드가 정해준 주소로 변경]
    @POST("auth/password/email/send")
    suspend fun sendPasswordResetCode(@Body body: EmailRequest): Response<Unit>

    // [추가] 비밀번호 찾기 - 인증번호 확인 요청
    // 서버가 응답으로 "이 사람 맞음!" 하고 userId를 돌려준다고 가정
    @POST("auth/password/email/verify")
    suspend fun verifyPasswordResetCode(@Body body: VerifyCodeRequest): Response<VerifyResponse>

    // [추가] 비밀번호 재설정 요청
    @POST("auth/password/reset")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<Unit>

    //백엔드가 주소보내주면 수정하면 됨
    @DELETE("auth/withdraw")
    suspend fun withdrawAccount(): Response<Unit>

    // network/AuthApi.kt
    @POST("auth/password/change") // 주소는 백엔드가 알려줌
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    // [수정] 이렇게 @Multipart가 붙어있어야 에러가 사라집니다!
    @Multipart
    @POST("/members/join")
    suspend fun signup(
        @Part("request") request: RequestBody,
        @Part file: MultipartBody.Part?
    ): Response<SignupResponse>

}
