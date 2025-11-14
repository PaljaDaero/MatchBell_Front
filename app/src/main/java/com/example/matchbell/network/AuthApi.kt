package com.example.matchbell.network // 1. 패키지 이름 수정

// 2. 데이터 모델 import 경로 수정
import com.example.matchbell.data.model.LoginRequest
import com.example.matchbell.data.model.LoginResponse
import retrofit2.Response // 이 부분을 수정하세요
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
