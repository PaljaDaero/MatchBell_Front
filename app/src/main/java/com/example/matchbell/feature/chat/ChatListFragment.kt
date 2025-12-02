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

    // [수정] 채팅방 목록을 API에서 불러오는 함수 (토큰 적용 및 Context 안전성 강화)
    private fun loadChatRooms() {
        // [추가] TokenManager를 통해 토큰을 가져와 유효성 검사
        // [수정] context 대신 requireContext()를 사용하여 안정적으로 토큰을 가져옵니다.
        val token = try {
            TokenManager.getAccessToken(requireContext())
        } catch (e: Exception) {
            // requireContext() 실패 시 (예: Fragment가 attach 안 된 상태)
            null
        }

        if (token.isNullOrEmpty()) {
            // [수정] Toast에 requireContext() 사용
            Toast.makeText(requireContext(), "로그인 정보가 없어 채팅 목록을 불러올 수 없습니다.", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                // [수정] API 호출 시 "Bearer $token" 문자열을 인자로 전달
                val response = chatApi.getChatRooms("Bearer $token")

                // [수정] Fragment가 아직 붙어 있는지 확인 후 UI 업데이트
                if (isAdded && response.isSuccessful && response.body() != null) {
                    val chatRoomListResponse = response.body()!!
                    // List<ChatRoomListResponse> -> List<ChatRoomData>로 변환
                    val chatRoomsData = chatRoomListResponse.map { it.toChatRoomData() }
                    setupAdapter(chatRoomsData)

                } else if (isAdded) {
                    // isAdded를 확인하여 안전하게 Toast 표시
                    Log.e("ChatListFragment", "Failed to load chat list: ${response.code()}")
                    Toast.makeText(requireContext(), "채팅방 목록을 불러오는 데 실패했습니다 (${response.code()})", Toast.LENGTH_LONG).show()
                    // 실패 시 빈 목록 설정
                    setupAdapter(emptyList())
                }
            } catch (e: Exception) {
                // [수정] isAdded를 확인하여 안전하게 Toast 표시
                if (isAdded) {
                    Log.e("ChatListFragment", "Network error when loading chat list", e)
                    Toast.makeText(requireContext(), "네트워크 오류로 채팅방 목록을 불러올 수 없습니다.", Toast.LENGTH_LONG).show()
                    // 네트워크 오류 시 빈 목록 설정
                    setupAdapter(emptyList())
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
        // 메모리 누수 방지를 위해 어댑터 참조를 제거할 수 있습니다 (필요 시)
        // rvChatList.adapter = null
    }
}