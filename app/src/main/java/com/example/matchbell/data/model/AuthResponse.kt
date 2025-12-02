package com.example.matchbell.data.model

// 로그인, 회원가입 성공 시 서버가 주는 데이터
data class AuthResponse(
    val jwt: String,       // 토큰
    val user: UserInfo   // 사용자 정보 객체
)

data class UserInfo(
    val id: Int,
    val email: String
)