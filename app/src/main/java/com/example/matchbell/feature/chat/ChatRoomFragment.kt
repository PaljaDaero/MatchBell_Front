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
import com.bumptech.glide.Glide
import com.example.matchbell.R
import com.example.matchbell.databinding.FragmentChatRoomBinding
import com.example.matchbell.feature.ChatMessageResponse
import com.example.matchbell.feature.ChatMessageSendRequest
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.ChatApi
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
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

    // [추가] 이미지 로딩을 위한 기본 URL
    private val BASE_URL = "http://3.239.45.21:8080"

    // [추가] STOMP 관련 변수
    private lateinit var mStompClient: StompClient
    private val compositeDisposable = CompositeDisposable() // 구독 해제를 위한 쓰레기통
    private val gson = Gson() // JSON 변환기

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

        // [추가] 사용자 이름 설정
        tvUserName.text = otherUserName ?: "알 수 없는 사용자"

        // [추가] 프로필 이미지 로드 (Glide 사용)
        if (!otherProfileUrl.isNullOrEmpty()) {
            val fullUrl = if (otherProfileUrl!!.startsWith("http")) {
                otherProfileUrl
            } else {
                "$BASE_URL$otherProfileUrl"
            }

            Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.bg_profile_image) // 로딩 중 표시할 이미지
                .error(R.drawable.bg_profile_image)       // 에러 시 표시할 이미지
                .into(ivProfile)
        } else {
            ivProfile.setImageResource(R.drawable.bg_profile_image)
        }

        // 2. Adapter 초기화 및 RecyclerView 설정 - [수정됨: 초기 메시지 목록 비우기]
        messageAdapter = MessageAdapter(mutableListOf())
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
     *  채팅방 히스토리 로드 및 UI 업데이트 (토큰 적용)
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
     * 차단 API 호출 및 채팅 목록으로 복귀 로직 (토큰 적용)
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

    // ------------------------------------------------------------------------
    // [수정] 4. 실제 STOMP 연결 및 구독 로직
    // ------------------------------------------------------------------------
    private fun setupStompConnection(roomId: String?) {
        if (roomId == null) return

        // 1. 토큰 가져오기
        val token = context?.let { TokenManager.getAccessToken(it) } ?: ""

        // 2. 서버 주소 설정 (ws:// 프로토콜 사용)
        // 주의: 백엔드 설정에 따라 ?token= 쿼리 파라미터가 필요할 수도, 헤더가 필요할 수도 있습니다.
        // 여기서는 작성해주신대로 URL 쿼리 파라미터 방식을 사용합니다.
        val wsUrl = "ws://3.239.45.21:8080/ws/chat?token=$token"

        // 3. 클라이언트 초기화
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)

        // [옵션] 연결 헤더에 토큰을 추가해야 하는 경우 (백엔드 요구사항에 따라 다름)
        // val headerList = arrayListOf<StompHeader>()
        // headerList.add(StompHeader("Authorization", "Bearer $token"))
        // mStompClient.connect(headerList)

        // 4. 연결 상태 모니터링 (로그 확인용)
        val lifecycleDisp = mStompClient.lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> Log.d("STOMP", "연결 성공 (OPENED)")
                    LifecycleEvent.Type.ERROR -> Log.e("STOMP", "연결 에러", lifecycleEvent.exception)
                    LifecycleEvent.Type.CLOSED -> Log.d("STOMP", "연결 종료 (CLOSED)")
                    else -> Log.d("STOMP", "연결 상태: ${lifecycleEvent.message}")
                }
            }
        compositeDisposable.add(lifecycleDisp)

        // 5. 채팅방 구독 (Receive) -> /topic/chat.{matchId}
        val subscribeTopic = "/topic/chat.$roomId"
        val topicDisp = mStompClient.topic(subscribeTopic)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage ->
                // 메시지 수신 시 실행되는 부분
                Log.d("STOMP", "메시지 수신: ${topicMessage.payload}")

                try {
                    // JSON String -> ChatMessageResponse 객체로 변환
                    val receivedMsg = gson.fromJson(topicMessage.payload, ChatMessageResponse::class.java)

                    // 로컬 모델로 변환
                    val messageItem = convertToLocalMessage(receivedMsg)

                    // UI 업데이트 (내가 보낸 게 아닐 때만 추가하거나, 중복 방지 로직 필요)
                    // 여기서는 일단 들어오는 족족 리스트에 추가합니다.
                    // (내가 보낸 메시지는 sendMessage에서 이미 추가했으므로, senderId 비교 필요)
                    if (messageItem.senderId != myUserId) {
                        messageAdapter.addMessage(messageItem)
                        rvChatMessages.scrollToPosition(messageAdapter.itemCount - 1)
                    }
                } catch (e: Exception) {
                    Log.e("STOMP", "메시지 파싱 실패", e)
                }
            }, { throwable ->
                Log.e("STOMP", "구독 에러", throwable)
            })
        compositeDisposable.add(topicDisp)

        // 6. 연결 시작
        mStompClient.connect()
    }

    // ------------------------------------------------------------------------
    // [수정] D. 메시지 전송 로직
    // ------------------------------------------------------------------------
    private fun sendMessage() {
        val content = etMessageInput.text.toString().trim()
        val matchIdLong = roomId?.toLongOrNull()
        if (content.isEmpty() || matchIdLong == null) return

        // 1. 내 화면에 먼저 보여주기 (UX 향상)
        val myMessage = Message(
            messageId = System.currentTimeMillis(), // 임시 ID
            matchId = matchIdLong,
            senderId = myUserId,
            content = content,
            timestamp = System.currentTimeMillis(),
            isMine = true
        )
        messageAdapter.addMessage(myMessage)
        etMessageInput.text.clear()
        rvChatMessages.scrollToPosition(messageAdapter.itemCount - 1)

        // 2. 서버로 전송 (Send) -> /app/chat.send
        val sendRequest = ChatMessageSendRequest(
            matchId = matchIdLong,
            content = content
        )
        // 객체 -> JSON String 변환
        val jsonContent = gson.toJson(sendRequest)

        // 전송 (비동기)
        val sendDisp = mStompClient.send("/app/chat.send", jsonContent)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("STOMP", "메시지 전송 성공")
            }, { error ->
                Log.e("STOMP", "메시지 전송 실패", error)
                Toast.makeText(context, "메시지 전송 실패", Toast.LENGTH_SHORT).show()
            })
        compositeDisposable.add(sendDisp)
    }

    // ------------------------------------------------------------------------
    // [수정] 연결 해제 (메모리 누수 방지)
    // ------------------------------------------------------------------------
    override fun onDestroyView() {
        super.onDestroyView()
        // STOMP 연결 끊기
        if (::mStompClient.isInitialized && mStompClient.isConnected) {
            mStompClient.disconnect()
        }
        // RxJava 구독 해제
        compositeDisposable.clear()
        _binding = null
    }
}