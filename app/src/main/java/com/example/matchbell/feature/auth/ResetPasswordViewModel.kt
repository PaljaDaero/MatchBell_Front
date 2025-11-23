/*package com.example.matchbell.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    // 비밀번호 재설정 요청
    fun resetPassword(userId: String, newPw: String) {
        viewModelScope.launch {
            try {
                // 백엔드 API 호출
                // authApi.resetPassword(ResetPasswordRequest(userId, newPw))

                // 성공 신호
                _event.emit("SUCCESS")
            } catch (e: Exception) {
                _event.emit("변경 실패: ${e.message}")
            }
        }
    }
}*/