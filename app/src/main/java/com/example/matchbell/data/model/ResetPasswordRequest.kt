package com.example.matchbell.data.model

data class ResetPasswordRequest(
    val email: String, // 인증받은 이메일 (또는 아이디)
    val newPassword: String
)