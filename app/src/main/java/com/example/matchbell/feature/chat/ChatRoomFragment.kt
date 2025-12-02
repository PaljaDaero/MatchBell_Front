package com.example.matchbell.feature.chat

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.matchbell.R
import com.example.matchbell.databinding.FragmentChatRoomBinding
import com.example.matchbell.feature.ChatMessageResponse
import com.example.matchbell.feature.ChatMessageSendRequest
import com.example.matchbell.feature.auth.TokenManager // [필수] TokenManager import
import com.example.matchbell.network.ChatApi
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ChatRoomFragment : Fragment() {

    // ChatApi 주입
    @Inject
    lateinit var chatApi: ChatApi

    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var tvUserName: TextView
    private lateinit var tvMatchScore: TextView
    private lateinit var ivProfile: CircleImageView
    private lateinit var messageAdapter: MessageAdapter

    // 채팅방 및 사용자 정보 (ChatListFragment에서 Bundle로 받아옴)
    private var roomId: String? = null
    private var otherUserId: String = "unknown_user" // 기본값 설정
    private var otherUserName: String? = null
    private var otherProfileUrl: String? = null

    private val myUserId = "current_user_id_123" // 현재 사용자 ID (String 유지)

    // 메시지 데이터 구조 정의 (로컬/Adapter용) - API 응답 및 로직에 맞춤
    data class Message(
        val messageId: Long,    // API의 id와 매핑
        val matchId: Long,      // API의 matchId와 매핑
        val senderId: String,   // 비교를 위해 String 유지
        val content: String,
        val timestamp: Long,    // API의 sentAt을 파싱한 Long값
        var isMine: Boolean     // 내가 보낸 메시지인지 구분
    )

    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bundle에서 데이터 추출
        arguments?.let {
            roomId = it.getString("ROOM_ID")
            otherUserId = it.getString("USER_ID") ?: otherUserId
            otherUserName = it.getString("USER_NAME")
            otherProfileUrl = it.getString("PROFILE_URL")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. UI 요소 바인딩
        tvUserName = binding.tvUserName
        tvMatchScore = binding.tvMatchScore
        ivProfile = binding.ivProfileChatRoom
        rvChatMessages = binding.rvChatMessages
        etMessageInput = binding.etMessageInput
        btnSend = binding.btnSend // 전송 버튼

        // [추가된 버튼들 참조]
        val btnHome: ImageButton = binding.btnHome // 홈 버튼
        val btnReport: ImageButton = binding.btnReport // 차단/신고 버튼
        val btnMore: ImageButton = binding.btnMore // 더보기 버튼

        // 2. Adapter 초기화 및 RecyclerView 설정 - [수정됨: 초기 메시지 목록 비우기]
        messageAdapter = MessageAdapter(mutableListOf(), myUserId)
        rvChatMessages.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }

        // NavigationComponent 뒤로가기 버튼 처리 (기존 로직 유지)

        // 3. [추가] 채팅방 히스토리 로드
        loadChatHistory()

        // 4. [추가] STOMP 구독 시작 (Placeholder)
        setupStompConnection(roomId)

        // 5. 이벤트 리스너 설정: 버튼 기능 구현

        // A. 홈 버튼: fragment_chat_list로 돌아가기
        btnHome.setOnClickListener {
            // 채팅 목록 화면으로 돌아가기 (chatListFragment를 스택에서 찾을 때까지 팝)
            findNavController().popBackStack(R.id.chatListFragment, false)
        }

        // B. 차단/신고 버튼: 다이얼로그 띄우고 차단 API 호출
        btnReport.setOnClickListener {
            showReportDialog()
        }

        // C. 더보기 버튼: fragment_profile_detail로 이동
        btnMore.setOnClickListener {
            // nav_graph에 정의된 action_chatRoomFragment_to_profileDetailFragment를 사용
            findNavController().navigate(R.id.action_chatRoomFragment_to_profileDetailFragment)
        }

        // D. 전송 버튼
        btnSend.setOnClickListener { sendMessage() }
    }

    /**
     * [수정] 채팅방 히스토리 로드 및 UI 업데이트 (토큰 적용)
     */
    private fun loadChatHistory() {
        val matchIdLong = roomId?.toLongOrNull()
        if (matchIdLong == null) {
            Log.e("ChatRoomFragment", "Invalid matchId: $roomId")
            return
        }

        // [추가] TokenManager를 통해 토큰을 가져와 유효성 검사
        val token = context?.let { TokenManager.getAccessToken(it) }
        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "로그인 정보가 없어 채팅 기록을 불러올 수 없습니다.", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                // [수정] API 호출 시 "Bearer $token" 문자열을 첫 번째 인자로 전달
                val response = chatApi.getChatHistory("Bearer $token", matchIdLong)

                if (response.isSuccessful) {
                    val history = response.body() ?: emptyList()

                    // 응답 데이터를 로컬 Message 모델로 변환
                    val messages = history.map { response ->
                        convertToLocalMessage(response)
                    }

                    messageAdapter.addMessages(messages)
                    rvChatMessages.scrollToPosition(messageAdapter.itemCount - 1)
                } else {
                    // 서버 오류 처리
                    Log.e("ChatRoomFragment", "Failed to load chat history: ${response.code()}")
                    Toast.makeText(context, "채팅 기록을 불러오는 데 실패했습니다 (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // 네트워크 오류 처리
                Log.e("ChatRoomFragment", "Network error loading chat history", e)
                Toast.makeText(context, "네트워크 오류로 채팅 기록을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // [유지] ChatMessageResponse를 로컬 Message 모델로 변환하는 유틸리티
    private fun convertToLocalMessage(response: ChatMessageResponse): Message {
        val timestamp = convertApiDateToTimestamp(response.sentAt)
        val senderIdString = response.senderId.toString()

        // [수정: Message 클래스에 직접 접근]
        return Message(
            messageId = response.id,
            matchId = response.matchId,
            senderId = senderIdString,
            content = response.content,
            timestamp = timestamp,
            isMine = senderIdString == myUserId // 내 ID와 비교
        )
    }

    // [유지] STOMP 연결 및 구독 로직 PlaceHolder
    private fun setupStompConnection(roomId: String?) {
        if (roomId == null) return

        // 1. 토큰 가져오기 (TokenManager 사용)
        val token = context?.let { TokenManager.getAccessToken(it) } ?: "dummy_jwt"

        // 2. WebSocket
        val wsUrl = "ws://3.239.45.21:8080/ws/chat?token=$token"

        // 3. 구독 Destination: /topic/chat.{matchId}
        val subscribeTopic = "/topic/chat.$roomId"

        Log.d("ChatRoomFragment", "WS URL: $wsUrl")
        Log.d("ChatRoomFragment", "Subscribe To: $subscribeTopic")

        // [실제 STOMP 라이브러리 초기화 및 연결 로직 필요]
        Toast.makeText(requireContext(), "WebSocket 연결 및 $subscribeTopic 구독 시도 중...", Toast.LENGTH_SHORT).show()
    }

    // [유지] 메시지 전송 로직
    private fun sendMessage() {
        val content = etMessageInput.text.toString().trim()
        val matchIdLong = roomId?.toLongOrNull()
        if (content.isEmpty() || matchIdLong == null) return

        // 1. 내가 보낸 메시지를 UI에 즉시 추가 (임시 ID 사용)
        // [수정: Message 클래스에 직접 접근]
        val myMessage = Message(
            messageId = System.currentTimeMillis() / 100, // 임시 ID
            matchId = matchIdLong,
            senderId = myUserId,
            content = content,
            timestamp = System.currentTimeMillis(),
            isMine = true
        )
        messageAdapter.addMessage(myMessage)
        etMessageInput.text.clear()
        rvChatMessages.scrollToPosition(messageAdapter.itemCount - 1)

        /// 2. [추가] 실제 WebSocket 전송 로직
        val sendRequest = ChatMessageSendRequest( // 클래스를 찾을 수 없던 오류 해결
            matchId = matchIdLong,
            content = content
        )
        // [STOMP 전송 로직: Gson().toJson(sendRequest)]
        Log.d("ChatRoomFragment", "STOMP Send: /app/chat.send, Body: $sendRequest")
    }

    // [유지] 차단 다이얼로그 표시 및 API 호출 로직
    private fun showReportDialog() {
        val builder = AlertDialog.Builder(requireContext())
        // dialog_report.xml 레이아웃 사용
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val reportButton = dialogView.findViewById<Button>(R.id.btn_dialog_report)
        val closeButton = dialogView.findViewById<Button>(R.id.btn_dialog_close)

        // 차단하기 버튼 로직: API 호출 및 네비게이션
        reportButton.setOnClickListener {
            dialog.dismiss()
            blockAndNavigateHome() // 차단 로직 실행
        }

        // 닫기 버튼 로직
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * [수정] 차단 API 호출 및 채팅 목록으로 복귀 로직 (토큰 적용)
     */
    private fun blockAndNavigateHome() {
        val matchIdLong = roomId?.toLongOrNull()
        if (matchIdLong == null) {
            Toast.makeText(context, "오류: 채팅방 정보가 유효하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // [추가] TokenManager를 통해 토큰을 가져와 유효성 검사
        val token = context?.let { TokenManager.getAccessToken(it) }
        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "로그인 정보가 없어 차단할 수 없습니다.", Toast.LENGTH_LONG).show()
            return
        }

        // API 호출
        lifecycleScope.launch {
            try {
                // [수정] API 호출 시 "Bearer $token" 문자열을 첫 번째 인자로 전달
                val response = chatApi.blockChatRoom("Bearer $token", matchIdLong)

                if (response.isSuccessful) {
                    Toast.makeText(context, "${otherUserName ?: "상대방"}님과의 채팅방이 차단되었습니다.", Toast.LENGTH_LONG).show()

                    // 성공적으로 차단되면 채팅 목록 화면으로 이동
                    findNavController().popBackStack(R.id.chatListFragment, false)
                } else {
                    Log.e("ChatRoomFragment", "Block API failed: ${response.code()}")
                    Toast.makeText(context, "차단 실패: 서버 오류 (${response.code()})", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("ChatRoomFragment", "Block API network error", e)
                Toast.makeText(context, "네트워크 오류로 차단에 실패했습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // [유지] WebSocket으로부터 메시지를 수신했을 때 호출될 함수
    fun handleReceivedMessage(wsMessage: ChatMessageResponse) {
        val message = convertToLocalMessage(wsMessage)

        // UI 업데이트는 메인 스레드에서 실행
        requireActivity().runOnUiThread {
            messageAdapter.addMessage(message)
            rvChatMessages.scrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    // [유지] API의 날짜 문자열을 Kotlin/Java timestamp (Long)으로 변환
    private fun convertApiDateToTimestamp(apiDate: String): Long {
        // "yyyy-MM-dd'T'HH:mm:ss" 형식 파싱
        return try {
            // SimpleDateFormat은 스레드 안전하지 않으므로, Java 8+ API를 사용하거나 스레드 로컬을 쓰는 것이 좋으나,
            // 간단한 예제에서는 이대로 사용합니다.
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.parse(apiDate)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("ChatRoomFragment", "Date parsing error: $apiDate", e)
            System.currentTimeMillis()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // [추가] STOMP 연결 해제 로직 필요
        // disconnectStompClient()
        _binding = null
    }
}