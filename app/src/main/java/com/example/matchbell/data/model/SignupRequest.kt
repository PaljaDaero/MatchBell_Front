package com.example.matchbell.data.model

data class SignupRequest(
    val email: String,
    val pwd: String,       // password -> pwd 로 변경됨
    val nickname: String,
    val birth: String,     // "yyyy-MM-dd" 형식
    val gender: String,    // "FEMALE" or "MALE"
    val job: String        // 새로 추가됨
)