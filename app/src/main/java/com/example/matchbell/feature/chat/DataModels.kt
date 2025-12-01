package com.example.matchbell.feature.chat

/**
 * 채팅방 목록에 표시될 각 채팅방의 데이터를 담는 모델입니다.
 */

// [추가] 채팅 히스토리 및 실시간 수신 메시지 모델 (API Response 형식)
data class ChatMessageResponse(
    val id: Long,        // 메시지 고유 ID
    val matchId: Long,   // 채팅방 ID
    val senderId: Long,  // 메시지 보낸 사용자 ID (API 명세 기준 Long)
    val content: String, // 메시지 내용
    val sentAt: String,  // 메시지 전송 시간 ("2025-11-29T16:00:00")
    val status: String   // 메시지 상태 (예: "READ", "SENT")
)

// [추가] 메시지 전송 요청 시 사용할 모델 (STOMP Destination /app/chat.send Body)
data class ChatMessageSendRequest(
    val matchId: Long,
    val content: String
)

// [추가] 내 채팅방 목록 조회 API의 응답 모델
data class ChatRoomListResponse(
    val matchId: Long,
    val otherUserId: Long,
    val otherNickname: String,
    val otherAvatarUrl: String?, // URL (Nullable)
    val lastMessage: String,
    val lastMessageTime: String, // "2025-11-29T18:20:15"
    val unreadCount: Int
)

/**
 * [수정] 어댑터와 프래그먼트에서 사용되는 로컬 채팅방 데이터 모델
 * API 응답 필드에 맞게 수정합니다.
 */
data class ChatRoomData(
    val roomId: String,          // matchId.toString()
    val userId: String,          // otherUserId.toString()
    val userName: String,        // otherNickname
    val userProfileUrl: String?, // otherAvatarUrl
    val lastMessage: String,     // lastMessage
    val unreadCount: Int,        // unreadCount
    val timestamp: String        // lastMessageTime
)

// [추가] API 응답을 로컬 모델로 변환하는 확장 함수
fun ChatRoomListResponse.toChatRoomData(): ChatRoomData {
    return ChatRoomData(
        roomId = this.matchId.toString(),
        userId = this.otherUserId.toString(),
        userName = this.otherNickname,
        userProfileUrl = this.otherAvatarUrl,
        lastMessage = this.lastMessage,
        unreadCount = this.unreadCount,
        timestamp = this.lastMessageTime // API 시간 문자열을 그대로 전달
    )
}