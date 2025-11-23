package com.example.matchbell.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authApi: AuthApi,
    @ApplicationContext private val context: Context // 토큰 매니저 사용을 위해 앱의 맥락(Context)을 받음
) : ViewModel() {

    // 화면에 결과를 알려주는 확성기 (성공/실패 메시지 전달)
    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    // 1. 로그아웃 기능
    fun logout() {
        viewModelScope.launch {
            // 저장된 토큰 삭제
            TokenManager.clearTokens(context)
            _event.emit("LOGOUT_SUCCESS")
        }
    }

    // 2. 회원 탈퇴 기능
    fun withdraw() {
        viewModelScope.launch {
            try {
                // 서버에 "나 탈퇴할래" 요청
                val response = authApi.withdrawAccount()

                if (response.isSuccessful) {
                    // 성공하면 내 폰의 토큰도 삭제
                    TokenManager.clearTokens(context)
                    _event.emit("WITHDRAW_SUCCESS")
                } else {
                    _event.emit("탈퇴 실패: ${response.message()}")
                }
            } catch (e: Exception) {
                // 서버 연결 실패 시 에러 메시지
                _event.emit("오류 발생: ${e.message}")
            }
        }
    }
}