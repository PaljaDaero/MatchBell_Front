package com.example.matchbell.feature

import com.google.gson.annotations.SerializedName

// ==========================================
// 1. 채팅 관련 모델
// ==========================================

data class ChatRoomData(
    val roomId: String,
    val userId: String,
    val userName: String,
    val userProfileUrl: String?,
    val lastMessage: String,
    val unreadCount: Int,
    val timestamp: String
)

data class ChatRoomListResponse(
    val matchId: Long,
    val otherUserId: Long,
    val otherNickname: String,
    val otherAvatarUrl: String?,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int
)

fun ChatRoomListResponse.toChatRoomData(): ChatRoomData {
    return ChatRoomData(
        roomId = this.matchId.toString(),
        userId = this.otherUserId.toString(),
        userName = this.otherNickname,
        userProfileUrl = this.otherAvatarUrl,
        lastMessage = this.lastMessage,
        unreadCount = this.unreadCount,
        timestamp = this.lastMessageTime
    )
}

data class ChatMessageResponse(
    val id: Long, val matchId: Long, val senderId: Long, val content: String, val sentAt: String, val status: String
)

data class ChatMessageSendRequest(val matchId: Long, val content: String)


// ==========================================
// 2. 프로필 및 매칭 모델 (핵심 수정 부분)
// ==========================================

// [수정] 서버 JSON과 똑같이 Flat하게 변경 (basic 제거됨)
data class MatchProfileResponse(
    val userId: Long,
    val nickname: String,
    val intro: String?,
    val gender: String,
    val birth: String?,
    val region: String?,
    val job: String?,
    @SerializedName("avatarUrl")
    val avatarUrl: String?,
    val tendency: String?,

    // 중첩된 궁합 정보
    val compat: CompatResult?
)

// [수정] compat 내부 구조
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

// [수정] 쿠키 사용 요청 모델 (amount 필수)
data class CookieSpendRequest(
    val amount: Int,
    val reason: String
)

// [수정] 잠금 해제 응답
data class ProfileUnlockResponse(
    val unlocked: Boolean,
    val cost: Int,
    val balanceAfter: Int
)

// [수정] 매칭된 유저 정보 (점수 포함)
data class MatchSummary(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String?,
    val age: Int,
    val region: String,
    val job: String,
    val matchedAt: String,
    val finalScore: Double? = 0.0,
    val stressScore: Double? = 0.0
)

// [수정] 궁금해요 유저 요약 정보 (점수 포함)
data class CuriousUserSummary(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String?,
    val createdAt: String,
    val finalScore: Double? = 0.0,
    val stressScore: Double? = 0.0
)


// ==========================================
// 3. 레이더 / 랭킹 / 나만의 궁합 등 기타
// ==========================================

data class RadarUserData(
    val userId: Long, val nickname: String, val gender: String, val age: Int,
    val distanceMeters: Double, val region: String, val avatarUrl: String?,
    val originalScore: Double, val finalScore: Double, val stressScore: Double,
    val tendency0: List<String>, val tendency1: List<String>
)

data class RadarResponse(
    val me: MyLocationData,
    val users: List<RadarUserData>
)

data class MyLocationData(
    val lat: Double, val lng: Double, val region: String
)

data class MyCompatRequest(
    val name: String, val gender: String, val birth: String
)

// 나만의 궁합 응답용 상세 모델
data class CompatDetail(
    val finalScore: Double,
    val stressScore: Double,
    val tendency0: List<String>?,
    val tendency1: List<String>?
)

data class MyCompatResponse(
    val targetName: String,
    val targetGender: String,
    val targetBirth: String,
    val compat: CompatDetail
)

data class RankingItem(
    val rank: Int, val userAId: Long, val userBId: Long,
    val userANickname: String, val userBNickname: String,
    val finalScore: Double, val stressScore: Double, val compositeScore: Int
)

data class RankingListResponse(
    val items: List<RankingItem>,
    val myBestCompositeScore: Int?,
    val myPercentile: Double?
)