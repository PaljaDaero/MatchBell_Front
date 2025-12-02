package com.example.matchbell.feature

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

// 레이더 응답의 사용자 목록 항목
data class RadarUserData(
    val userId: Long,
    val nickname: String,
    val gender: String,
    val age: Int,
    val distanceMeters: Double,
    val region: String,
    val avatarUrl: String?,
    val originalScore: Double,
    val finalScore: Double,
    val stressScore: Double,
    val tendency0: List<String>,
    val tendency1: List<String>
)

// 레이더 전체 응답 구조
data class RadarResponse(
    val me: MyLocationData, // me 필드 데이터 모델
    val users: List<RadarUserData>
)

// me 필드의 위치 데이터
data class MyLocationData(
    val lat: Double,
    val lng: Double,
    val region: String
)

// [추가] 궁금해요 (보냄/받음) 유저 요약 정보
data class CuriousUserSummary(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String?,
    val createdAt: String
)

// [추가] 매칭된 유저 정보
data class MatchSummary(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String?,
    val age: Int,
    val region: String,
    val job: String,
    val matchedAt: String
)

// [추가] 상대방 상세 프로필 조회 응답 모델 (JSON 명세 반영)
data class MatchProfileResponse(
    val userId: Long,
    val nickname: String,
    val intro: String?,
    val gender: String, // "MALE" or "FEMALE"
    val birth: String,
    val region: String,
    val job: String,
    val avatarUrl: String?,
    val tendency: String,
    val compat: CompatResult // 아래에 정의된 중첩 객체
)

// [추가] 상세 프로필 내 궁합 점수 데이터
data class CompatResult(
    val originalScore: Double,
    val finalScore: Double,
    val stressScore: Double,
    val sal0: List<Double>,
    val sal1: List<Double>,
    val person0: List<Int>,
    val person1: List<Int>,
    val tendency0: List<String>,
    val tendency1: List<String>
)

// [추가] 쿠키 사용 요청 모델
data class CookieSpendRequest(
    val amount: Int,
    val reason: String = "프로필 잠금 해제" // 기본값 설정
)

// [추가] 나만의 궁합 요청 모델
data class MyCompatRequest(
    val name: String,
    val gender: String, // "MALE" or "FEMALE"
    val birth: String   // "YYYY-MM-DD"
)

// [추가] 나만의 궁합 응답 모델 (점수 반환 가정)
// 200 OK라고만 적혀있어서, 통상적으로 점수와 설명을 반환한다고 가정하고 작성합니다.
data class MyCompatResponse(
    val score: Int,
    val description: String? // 궁합 설명 (없으면 null)
)

// [추가] 랭킹 아이템 모델
data class RankingItem(
    val rank: Int,
    val userAId: Long,
    val userBId: Long,
    val userANickname: String,
    val userBNickname: String,
    val finalScore: Double,
    val stressScore: Double
)

// [추가] 랭킹 목록 응답 모델
data class RankingListResponse(
    val items: List<RankingItem>
)