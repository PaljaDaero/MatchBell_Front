package com.example.matchbell.feature.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.matchbell.R
import com.example.matchbell.feature.ChatRoomData
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.feature.toChatRoomData
import com.example.matchbell.network.ChatApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

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

        // 1. RecyclerView 설정
        rvChatList = view.findViewById(R.id.rv_chat_list)
        // [수정] context 대신 requireContext()를 사용합니다.
        rvChatList.layoutManager = LinearLayoutManager(requireContext())

        // 2. 데이터 로드 시작
        loadChatRooms()
    }

    // 채팅방 목록을 API에서 불러오는 함수 (토큰 적용 및 Context 안전성 강화)
    private fun loadChatRooms() {
        // [수정] context가 null이면 즉시 리턴 (안전 장치 1)
        val context = context ?: return

        val token = TokenManager.getAccessToken(context)
        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = chatApi.getChatRooms("Bearer $token")

                // [수정] 응답 받은 시점에 Fragment가 살아있는지 확인 (안전 장치 2)
                if (!isAdded || view == null) return@launch

                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.map { it.toChatRoomData() }
                    setupAdapter(list)
                } else {
                    Log.e("ChatList", "Error: ${response.code()}")
                    setupAdapter(emptyList()) // 빈 리스트라도 띄워서 로딩 끝냄
                }
            } catch (e: Exception) {
                if (isAdded) { // 살아있을 때만 토스트 띄움
                    Log.e("ChatList", "Network Error", e)
                    // Toast.makeText(requireContext(), "오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // [추가] 어댑터를 초기화하고 클릭 리스너를 설정하는 함수
    private fun setupAdapter(chatRoomsData: List<ChatRoomData>) {
        // [수정] Toast에 requireContext() 사용
        chatListAdapter = ChatListAdapter(chatRoomsData) { chatRoom ->
            // 채팅방 아이템 클릭 시 이벤트 처리
            Toast.makeText(requireContext(), "${chatRoom.userName} 님과의 채팅방으로 이동", Toast.LENGTH_SHORT).show()

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

    override fun onDestroyView() {
        super.onDestroyView()
        // rvChatList.adapter = null
    }
}