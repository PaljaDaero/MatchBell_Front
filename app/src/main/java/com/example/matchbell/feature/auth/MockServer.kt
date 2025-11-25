package com.example.matchbell.feature.auth
//벡엔드 구축시 파일 전체삭제
// 앱 어디서든 접근 가능한 '가짜 서버' (앱 끄면 날아감)
object MockServer {
    // 아이디와 비밀번호를 저장하는 맵 (Key: 아이디, Value: 비밀번호)
    val userDatabase = mutableMapOf<String, String>()
    // [추가됨] 이메일 -> 아이디 (이메일로 아이디를 찾기 위해)
    val emailDatabase = mutableMapOf<String, String>()

    // [수정됨] 회원가입 시 이메일도 같이 저장
    fun register(id: String, pw: String, email: String) {
        userDatabase[id] = pw
        emailDatabase[email] = id // 이메일 주인이 누군지 저장
    }

    // [수정됨] 이메일로 아이디 찾기 (비밀번호 찾기용)
    // 이메일이 있으면 아이디(String)를 반환, 없으면 null 반환
    fun findIdByEmail(email: String): String? {
        return emailDatabase[email]
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
    // ⭐⭐⭐ [오류 해결] 비밀번호 변경 기능 ⭐⭐⭐
    // 이 함수가 없어서 빨간 줄이 떴던 겁니다!
    fun updatePassword(id: String, newPw: String) {
        if (userDatabase.containsKey(id)) {
            userDatabase[id] = newPw
        }
    }
}