package com.example.matchbell.feature.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.navigation.NavController // NavController를 사용하기 위해 추가
import com.example.matchbell.R // 네비게이션 ID를 사용하기 위해 추가
import android.util.Log // <--- 추가
import android.widget.Toast // <--- 추가

object TokenManager {

    private const val PREFS_NAME = "secure_app_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    // 암호화된 SharedPreferences 생성 (수정 X)
    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // 토큰 저장
    fun saveTokens(context: Context, accessToken: String, refreshToken: String = "") {
        getEncryptedPrefs(context).edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            if (refreshToken.isNotEmpty()) {
                putString(KEY_REFRESH_TOKEN, refreshToken)
            }
            apply()
        }
    }

    // 토큰 가져오기
    fun getAccessToken(context: Context): String? {
        return getEncryptedPrefs(context).getString(KEY_ACCESS_TOKEN, null)
    }

    // 토큰 삭제 (로그아웃)
    fun clearTokens(context: Context) {
        getEncryptedPrefs(context).edit().clear().apply()
    }
    // [추가] 토큰 만료 시 로그아웃 처리 및 로그인 화면으로 이동
    // NavController는 Fragment에서 받아와야 하므로, 함수 인수로 받습니다.
    fun handleTokenExpired(context: Context, navController: NavController) {
        clearTokens(context) // 저장된 토큰 삭제

        // 로그인 화면으로 이동 (로그인 Fragment의 Destination ID 필요)
        // R.id.loginFragment 또는 R.id.splashFragment 등 시작 지점으로 이동하도록 수정 필요
        // 여기서는 예시로 R.id.loginFragment로 이동한다고 가정합니다.
        try {
            // 모든 백 스택을 제거하고 로그인 화면으로 이동
            navController.navigate(R.id.loginFragment)
            // 또는 findNavController().navigate(R.id.action_global_loginFragment) 같은 global action 사용 가능

            // 사용자에게 안내 메시지 표시 (Fragment의 context를 사용)
            Toast.makeText(context, "세션이 만료되어 다시 로그인해야 합니다.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // NavController 오류 처리 (예: 이미 로그인 화면에 있거나 네비게이션 경로가 잘못된 경우)
            Log.e("TokenManager", "로그인 화면으로 이동 실패: ${e.message}")
        }
    }
}