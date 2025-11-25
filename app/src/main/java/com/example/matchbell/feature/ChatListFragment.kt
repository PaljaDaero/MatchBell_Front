package com.example.matchbell.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.matchbell.R

// ChatListFragment.kt
class ChatListFragment : Fragment() {

    private lateinit var rvChatList: RecyclerView
    private lateinit var chatListAdapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_chat_list.xml 레이아웃을 인플레이트합니다.
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. RecyclerView 참조
        rvChatList = view.findViewById(R.id.rv_chat_list)

        // 2. [테스트용] 더미 데이터 생성 및 로드
        val dummyData = listOf(
            ChatRoomData(
                roomId = "room_a", userId = "user001", userName = "차은우",
                // 로컬 리소스 ID의 문자열 이름 사용 (Adapter에서 int ID로 변환)
                userProfileUrl = "placeholder_profile", lastMessage = "오늘 뭐하세요?",
                unreadCount = 0, timestamp = "10월 31일"
            ),
            ChatRoomData(
                roomId = "room_b", userId = "user002", userName = "이도현",
                userProfileUrl = "placeholder_profile", lastMessage = "안녕하세요 ;)",
                unreadCount = 2, timestamp = "10월 31일"
            ),
            ChatRoomData(
                roomId = "room_c", userId = "user003", userName = "하루아",
                userProfileUrl = "placeholder_profile", lastMessage = "누나, 오늘 뭐해요??",
                unreadCount = 4, timestamp = "10월 31일"
            ),
            ChatRoomData(
                roomId = "room_d", userId = "user004", userName = "김민재",
                userProfileUrl = "placeholder_profile", lastMessage = "프로젝트 잘 마무리했습니다.",
                unreadCount = 1, timestamp = "10월 30일"
            )
        )

        // 3. Adapter 초기화 및 클릭 리스너 설정
        chatListAdapter = ChatListAdapter(dummyData) { chatRoom ->
            // 채팅방 아이템 클릭 시 이벤트 처리
            Toast.makeText(context, "${chatRoom.userName} 님과의 채팅방으로 이동", Toast.LENGTH_SHORT).show()

            // Navigation Component를 사용하여 ChatRoomFragment로 이동
            val bundle = Bundle().apply {
                putString("ROOM_ID", chatRoom.roomId)
                putString("USER_ID", chatRoom.userId)
                putString("USER_NAME", chatRoom.userName)
                putString("PROFILE_URL", chatRoom.userProfileUrl) // 이제 URL 대신 로컬 리소스 이름 전달
            }

            // NavGraph에 정의된 Action ID를 사용하여 이동
            findNavController().navigate(R.id.action_chatListFragment_to_chatRoomFragment, bundle)
        }

        // 4. RecyclerView 설정
        rvChatList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatListAdapter
        }
    }
}