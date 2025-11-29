package com.example.matchbell.feature

/**
 * 채팅방 목록에 표시될 각 채팅방의 데이터를 담는 모델입니다.
 * 이전에 ApiService.kt에 정의되어 있었으나, 로컬 테스트를 위해 별도 파일로 분리되었습니다.
 */
data class ChatRoomData(
    val roomId: String,          // 채팅방 고유 ID
    val userId: String,          // 상대방 사용자 ID
    val userName: String,        // 상대방 이름
    val userProfileUrl: String,  // 상대방 프로필 이미지 URL (더미 이미지 사용 중)
    val lastMessage: String,     // 마지막 메시지 내용
    val unreadCount: Int,        // 읽지 않은 메시지 수
    val timestamp: String        // 마지막 메시지 시간
)

// 참고: ChatRoomFragment.kt에 이미 정의된 Message 데이터 클래스는 해당 파일 내부에 유지됩니다.