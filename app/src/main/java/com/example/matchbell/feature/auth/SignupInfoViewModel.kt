package com.example.matchbell.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchbell.data.model.EmailRequest
import com.example.matchbell.data.model.EmailVerifyRequest
import com.example.matchbell.data.model.SignupRequest
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupInfoViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    // 화면(Fragment)으로 보낼 신호 (성공/실패/안내)
    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    private var isEmailVerified = false // 이메일 인증 완료 여부 저장

    // 1. '인증번호 확인' 버튼 클릭 시
    fun onVerifyEmailClicked(email: String, code: String) {
        viewModelScope.launch {
            try {
                val response = authApi.verifyEmail(EmailVerifyRequest(email, code))
                if (response.isSuccessful) {
                    isEmailVerified = true
                    _event.emit("이메일 인증 성공!")
                } else {
                    _event.emit("인증번호가 틀립니다.")
                }
            } catch (e: Exception) {
                _event.emit("인증 오류: ${e.message}")
            }
        }
    }

    // 2. 최종 '확인' 버튼 클릭 시
    fun onSignupButtonClicked(id: String, pw: String, email: String) {
        if (!isEmailVerified) {
            viewModelScope.launch { _event.emit("이메일 인증을 먼저 완료해주세요.") }
            return
        }

        viewModelScope.launch {
            try {
                val response = authApi.signup(SignupRequest(id, pw, email))
                if (response.isSuccessful) {
                    _event.emit("SIGNUP_SUCCESS") // 회원가입 성공 신호
                } else {
                    _event.emit("회원가입 실패: ${response.message()}")
                }
            } catch (e: Exception) {
                _event.emit("회원가입 오류: ${e.message}")
            }
        }
    }

    // (보너스) 이메일 입력 후 '인증번호 전송' 버튼 로직 (XML에 버튼이 있다면)
    fun onSendEmailCodeClicked(email: String) {
        viewModelScope.launch {
            try {
                authApi.sendEmailVerification(EmailRequest(email))
                _event.emit("인증번호가 발송되었습니다.")
            } catch (e: Exception) {
                _event.emit("이메일 발송 실패")
            }
        }
    }
}