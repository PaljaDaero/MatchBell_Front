package com.example.matchbell.data.model

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)