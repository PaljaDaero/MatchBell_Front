package com.example.matchbell.data.model

data class UpdateProfileRequest(
    val nickname: String,
    val job: String,
    val bio: String,
    val gender: String,
    val birthdate: String
)