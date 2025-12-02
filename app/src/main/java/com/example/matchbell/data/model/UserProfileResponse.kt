package com.example.matchbell.data.model

data class UserProfileResponse(
    val nickname: String,
    val intro: String?,      // 자기소개 (없을 수도 있음)
    val gender: String,
    val birth: String,
    val region: String?,     // 지역
    val job: String?,        // 직업
    val avatarUrl: String?,  // 프로필 사진 주소
    val tendency: String?    // 성향 (열정/에너지 등)
)