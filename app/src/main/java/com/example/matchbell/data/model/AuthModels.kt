package com.example.matchbell.data.model

// 1. 인증번호 확인할 때 보낼 데이터
data class VerifyCodeRequest(
    val email: String,
    val code: String
)

// 2. 인증번호 확인 후 서버가 돌려줄 데이터 (userId를 준다고 하셨으므로)
data class VerifyResponse(
    val success: Boolean,
    val message: String,
    val userId: String? = null  // 서버가 유저 아이디를 줄 경우를 대비
)

// 3. 비밀번호 재설정할 때 보낼 데이터
data class ResetPasswordRequest(
    val userId: String,      // 누구의 비밀번호를 바꿀지
    val newPassword: String  // 새 비밀번호
)

data class CookieChargeRequest(
    val amount: Int = 10,
    val reason: String = "쿠키 충전 (10개)"
)

data class CookieBalanceResponse(
    val balance: Int
)
