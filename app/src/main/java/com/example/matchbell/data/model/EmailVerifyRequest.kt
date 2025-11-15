package com.example.matchbell.data.model

data class EmailVerifyRequest(
    val email: String,
    val code: String
)