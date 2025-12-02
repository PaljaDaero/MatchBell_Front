package com.example.matchbell.data.model

data class ProfileUpdateRequest(
    val nickname: String,
    val intro: String,
    val region: String,
    val job: String
    // avatarUrl은 보통 서버가 주는 거라 요청엔 안 넣지만,
    // 혹시 백엔드가 '기본 이미지로 변경' 등을 위해 필요하다면 추가하세요.
)