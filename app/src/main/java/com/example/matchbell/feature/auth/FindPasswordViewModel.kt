/*package com.example.matchbell.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 화면에 보낼 신호 종류 정의
sealed class FindPasswordEvent {
    object CodeSent : FindPasswordEvent() // 전송 성공
    data class VerifySuccess(val userId: String) : FindPasswordEvent() // 인증 성공
    data class Error(val message: String) : FindPasswordEvent() // 에러
}

@HiltViewModel
class FindPasswordViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    private val _event = MutableSharedFlow<FindPasswordEvent>()
    val event = _event.asSharedFlow()

    // 1. 인증번호 전송 요청
    fun requestEmailCode(email: String) {
        viewModelScope.launch {
            try {
                // 백엔드 API 호출 (아직 API가 없다면 주석 처리하거나 비워두세요)
                // authApi.sendPasswordResetCode(EmailRequest(email))

                // 성공했다고 가정하고 신호 보냄
                _event.emit(FindPasswordEvent.CodeSent)
            } catch (e: Exception) {
                _event.emit(FindPasswordEvent.Error("전송 실패: ${e.message}"))
            }
        }
    }

    // 2. 인증번호 확인 요청
    fun verifyCode(email: String, code: String) {
        viewModelScope.launch {
            try {
                // 백엔드 API 호출
                // val response = authApi.verifyPasswordResetCode(VerifyCodeRequest(email, code))

                // 성공했다고 가정 (임시)
                _event.emit(FindPasswordEvent.VerifySuccess("temp_user_id"))
            } catch (e: Exception) {
                _event.emit(FindPasswordEvent.Error("인증 실패: ${e.message}"))
            }
        }
    }
}*/