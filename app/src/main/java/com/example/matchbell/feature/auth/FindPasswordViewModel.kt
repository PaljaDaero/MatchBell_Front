package com.example.matchbell.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchbell.data.model.EmailVerifyRequest
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindPasswordViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    // Fragment로 보낼 신호 (성공/실패)
    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    // '인증하기' 버튼 클릭 시
    fun onVerifyClicked(email: String, code: String) {
        viewModelScope.launch {
            try {
                val response = authApi.verifyPasswordCode(EmailVerifyRequest(email, code))
                if (response.isSuccessful) {
                    _event.emit("PASSWORD_RESET_SUCCESS") // 인증 성공
                } else {
                    _event.emit("인증번호가 틀립니다.")
                }
            } catch (e: Exception) {
                _event.emit("오류: ${e.message}")
            }
        }
    }
}