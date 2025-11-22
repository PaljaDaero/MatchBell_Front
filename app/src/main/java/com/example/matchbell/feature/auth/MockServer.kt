package com.example.matchbell.feature.auth

// 앱 어디서든 접근 가능한 '가짜 서버' (앱 끄면 날아감)
object MockServer {
    // 아이디와 비밀번호를 저장하는 맵 (Key: 아이디, Value: 비밀번호)
    val userDatabase = mutableMapOf<String, String>()

    // 회원가입 함수
    fun register(id: String, pw: String) {
        userDatabase[id] = pw
    }

    // 로그인 검사 함수 (성공하면 true, 실패하면 false)
    fun checkLogin(id: String, pw: String): Boolean {
        // 1. 아이디가 있는지 확인
        if (userDatabase.containsKey(id)) {
            // 2. 비밀번호가 맞는지 확인
            val savedPw = userDatabase[id]
            return savedPw == pw
        }
        return false
    }
}