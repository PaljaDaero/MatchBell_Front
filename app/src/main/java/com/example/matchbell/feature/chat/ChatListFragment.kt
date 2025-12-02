package com.example.matchbell.feature.chat

import android.os.Bundle
import android.util.Log // Log import
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // lifecycleScope import
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.matchbell.R
import com.example.matchbell.feature.ChatRoomData
import com.example.matchbell.feature.toChatRoomData
import com.example.matchbell.network.ChatApi // ChatApi import
import dagger.hilt.android.AndroidEntryPoint // Hilt import
import kotlinx.coroutines.launch
import javax.inject.Inject // Inject import

// [Hilt 사용을 위해 추가]
@AndroidEntryPoint
class ChatListFragment : Fragment() {

    // [추가] ChatApi 주입
    @Inject
    lateinit var chatApi: ChatApi

    private lateinit var rvChatList: RecyclerView
    private lateinit var chatListAdapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. RecyclerView 참조
        rvChatList = view.findViewById(R.id.rv_chat_list)
        rvChatList.layoutManager = LinearLayoutManager(context)

        // 초기 어댑터 설정 (로딩 중 상태 또는 빈 목록을 표시)
        chatListAdapter = ChatListAdapter(emptyList()) { /* 클릭 이벤트는 loadChatRooms에서 설정 */ }
        rvChatList.adapter = chatListAdapter

        // 2. [수정] API 호출로 데이터 로드
        loadChatRooms()
    }

    // [추가] 채팅방 목록 API를 호출하고 데이터를 처리하는 함수
    private fun loadChatRooms() {
        lifecycleScope.launch {
            try {
                val response = chatApi.getChatRooms()

                if (response.isSuccessful) {
                    val apiRooms = response.body() ?: emptyList()

                    // API 응답 모델을 ChatRoomData로 변환
                    val chatRoomsData = apiRooms.map { it.toChatRoomData() }

                    // 어댑터에 데이터 설정 및 클릭 리스너 재설정
                    setupAdapter(chatRoomsData)
                } else {
                    Log.e("ChatListFragment", "Failed to load chat list: ${response.code()}")
                    Toast.makeText(context, "채팅방 목록을 불러오는 데 실패했습니다 (${response.code()})", Toast.LENGTH_LONG).show()
                    // 실패 시 빈 목록 설정
                    setupAdapter(emptyList())
                }
            } catch (e: Exception) {
                Log.e("ChatListFragment", "Network error when loading chat list", e)
                Toast.makeText(context, "네트워크 오류로 채팅방 목록을 불러올 수 없습니다.", Toast.LENGTH_LONG).show()
                // 네트워크 오류 시 빈 목록 설정
                setupAdapter(emptyList())
            }
        }
    }

    // [추가] 어댑터를 초기화하고 클릭 리스너를 설정하는 함수
    private fun setupAdapter(chatRoomsData: List<ChatRoomData>) {
        chatListAdapter = ChatListAdapter(chatRoomsData) { chatRoom ->
            // 채팅방 아이템 클릭 시 이벤트 처리
            Toast.makeText(context, "${chatRoom.userName} 님과의 채팅방으로 이동", Toast.LENGTH_SHORT).show()

            // Navigation Component를 사용하여 ChatRoomFragment로 이동
            val bundle = Bundle().apply {
                putString("ROOM_ID", chatRoom.roomId) // matchId (String)
                putString("USER_ID", chatRoom.userId) // otherUserId (String)
                putString("USER_NAME", chatRoom.userName) // otherNickname
                putString("PROFILE_URL", chatRoom.userProfileUrl) // otherAvatarUrl (URL)
            }

            // NavGraph에 정의된 Action ID를 사용하여 이동
            findNavController().navigate(R.id.action_chatListFragment_to_chatRoomFragment, bundle)
        }
        rvChatList.adapter = chatListAdapter
    }
}