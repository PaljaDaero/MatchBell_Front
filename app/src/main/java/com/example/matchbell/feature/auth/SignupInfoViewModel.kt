package com.example.matchbell.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class SignupInfoViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    // [삭제됨] isEmailVerified 변수 삭제
    // [삭제됨] onVerifyEmailClicked 함수 삭제
    // [삭제됨] onSendEmailCodeClicked 함수 삭제

    // [유지] 최종 회원가입 요청 함수 (ProfileSetupFragment에서 씀)
    fun signup(request: RequestBody, file: MultipartBody.Part?) {
        viewModelScope.launch {
            try {
                val response = authApi.signup(request, file)

                if (response.isSuccessful) {
                    _event.emit("SIGNUP_SUCCESS")
                } else {
                    _event.emit("가입 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                _event.emit("에러 발생: ${e.message}")
            }
        }
    }
}