package com.example.matchbell.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchbell.data.model.LoginRequest
import com.example.matchbell.data.model.AuthResponse // 위에서 만든 파일 import
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 로그인 성공 시 결과를 담을 이벤트 클래스
sealed class LoginEvent {
    data class Success(val tokens: AuthResponse) : LoginEvent()
    data class Error(val message: String) : LoginEvent()         // 실패 (에러 메시지)
    object Loading : LoginEvent()                                // 로딩 중
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    // 로그인 버튼 누르면 실행되는 함수
    fun onLoginButtonClicked(email: String, pw: String) {
        viewModelScope.launch {
            _loginEvent.emit(LoginEvent.Loading) // 로딩 시작

            try {
                // 1. 서버에 로그인 요청 (이메일, 비번)
                val request = LoginRequest(email, pw)
                val response = authApi.login(request)

                if (response.isSuccessful && response.body() != null) {
                    // 2. 성공 시: 토큰을 받아서 화면으로 넘김
                    val tokens = response.body()!!
                    _loginEvent.emit(LoginEvent.Success(tokens))
                } else {
                    // 3. 실패 시 (비번 틀림 등)
                    _loginEvent.emit(LoginEvent.Error("로그인 실패: 아이디나 비밀번호를 확인해주세요."))
                }
            } catch (e: Exception) {
                // 4. 에러 시 (서버 꺼짐, 인터넷 끊김)
                _loginEvent.emit(LoginEvent.Error("네트워크 오류: ${e.message}"))
                e.printStackTrace()
            }
        }
    }
}