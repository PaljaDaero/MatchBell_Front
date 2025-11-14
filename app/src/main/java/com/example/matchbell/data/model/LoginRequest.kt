package com.example.matchbell.data.model

// 💡 class 앞에 'data' 키워드를 추가합니다.
data class LoginRequest(
    val email: String,
    val password: String
)