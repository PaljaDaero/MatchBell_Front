package com.example.matchbell.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchbell.data.model.LoginRequest
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow // 💡 새로 추가된 import
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow // 💡 새로 추가된 import
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    // 1. 화면 이동/에러 메시지용 신호 (일회성 이벤트)
    private val _loginEvent = MutableSharedFlow<String>()
    val loginEvent = _loginEvent.asSharedFlow()

    // 2. [추가] 로딩 상태 신호 (상태 유지)
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun onLoginButtonClicked(id: String, pw: String) {
        viewModelScope.launch {
            // 로딩 시작!
            _isLoading.value = true

            try {
                val request = LoginRequest(id, pw)
                val response = authApi.login(request)

                if (response.isSuccessful) {
                    _loginEvent.emit("SUCCESS")
                } else {
                    // response.code()를 사용하여 실패 코드를 전달
                    _loginEvent.emit("FAIL: ${response.code()}")
                }
            } catch (e: Exception) {
                // 네트워크 에러 처리
                _loginEvent.emit("ERROR: ${e.message}")
            } finally {
                // 성공하든 실패하든 로딩 끝!
                _isLoading.value = false
            }
        }
    }
}