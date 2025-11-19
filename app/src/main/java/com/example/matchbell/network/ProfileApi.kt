package com.example.matchbell.network

import com.example.matchbell.data.model.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT

interface ProfileApi {
    // 프로필 수정 요청 (PUT 방식)
    @PUT("/users/me/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<Unit>
}