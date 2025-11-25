package com.example.matchbell.feature.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

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
}