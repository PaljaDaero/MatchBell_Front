package com.example.matchbell.data.model

data class LoginRequest(
    val email: String,
    val pwd: String        // password -> pwd 로 변경됨
)