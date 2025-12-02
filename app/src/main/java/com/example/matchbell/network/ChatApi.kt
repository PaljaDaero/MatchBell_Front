package com.example.matchbell.network

import com.example.matchbell.feature.ChatMessageResponse
import com.example.matchbell.feature.ChatRoomListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApi {

    // 채팅방 히스토리 조회 API
    // GET /me/chats/{matchId}/messages
    @GET("/me/chats/{matchId}/messages")
    suspend fun getChatHistory(
        @Header("Authorization") token: String, // [수정] 토큰 헤더 추가
        @Path("matchId") matchId: Long
    ): Response<List<ChatMessageResponse>>

    // 내 채팅방 목록 조회 API
    // GET /me/chats
    @GET("/me/chats")
    suspend fun getChatRooms(
        @Header("Authorization") token: String // [수정] 토큰 헤더 추가
    ): Response<List<ChatRoomListResponse>>

    // 채팅방 차단 API
    // POST /me/chats/{matchId}/block
    @POST("/me/chats/{matchId}/block")
    suspend fun blockChatRoom(
        @Header("Authorization") token: String, // [수정] 토큰 헤더 추가
        @Path("matchId") matchId: Long
    ): Response<Unit>

}