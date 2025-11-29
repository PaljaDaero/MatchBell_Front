package com.example.matchbell.data.model

data class SignupRequest(
    val email: String,      // 아이디 대신 이메일 사용
    val password: String,
    val nickname: String,
    val name: String,       // 이름 (실명)
    val birthday: String,   // 생년월일 (예: 2000-01-01)
    val gender: String      // 성별 (MALE/FEMALE)
)