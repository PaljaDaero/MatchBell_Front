package com.example.matchbell.feature.chat

import android.app.AlertDialog
import android.os.Bundle
import android.util.Base64
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
import com.example.matchbell.feature.MatchingScore // [중요] 점수 계산기 추가
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.AuthApi
import com.example.matchbell.network.ChatApi
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ChatRoomFragment : Fragment() {

    @Inject
    lateinit var chatApi: ChatApi

    @Inject
    lateinit var authApi: AuthApi

    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var tvUserName: TextView
    private lateinit var ivProfile: CircleImageView
    private lateinit var messageAdapter: MessageAdapter

    private var roomId: String? = null
    private var otherUserId: String = "unknown"
    private var otherUserName: String? = null
    private var otherProfileUrl: String? = null

    // 상세 화면 전달용 점수
    private var loadedMatchScore: Int = 0

    // [추가] 점수 계산기 인스턴스 생성
    private val matchingScoreCalculator = MatchingScore()

    private var myUserId: String = ""
    private val BASE_URL = "http://3.239.45.21:8080"

    private lateinit var mStompClient: StompClient
    private val compositeDisposable = CompositeDisposable()
    private val gson = Gson()

    data class Message(
        val messageId: Long,
        val matchId: Long,
        val senderId: String,
        val content: String,
        val timestamp: Long,
        var isMine: Boolean
    )

    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            roomId = it.getString("ROOM_ID")
            otherUserId = it.getString("USER_ID") ?: "unknown"
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

        setupMyUserId()

        tvUserName = binding.tvUserName
        ivProfile = binding.ivProfileChatRoom
        rvChatMessages = binding.rvChatMessages
        etMessageInput = binding.etMessageInput
        btnSend = binding.btnSend

        val btnHome: ImageButton = binding.btnHome
        val btnReport: ImageButton = binding.btnReport
        val btnMore: ImageButton = binding.btnMore

        tvUserName.text = otherUserName ?: "알 수 없는 사용자"
        if (!otherProfileUrl.isNullOrEmpty()) {
            val fullUrl = if (otherProfileUrl!!.startsWith("http")) otherProfileUrl else "$BASE_URL$otherProfileUrl"
            Glide.with(this).load(fullUrl).placeholder(R.drawable.bg_profile_image).into(ivProfile)
        } else {
            ivProfile.setImageResource(R.drawable.bg_profile_image)
        }

        messageAdapter = MessageAdapter(mutableListOf())
        rvChatMessages.apply {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = messageAdapter
        }

        loadChatHistory()
        setupStompConnection(roomId)

        // 궁합 점수 계산 및 로드
        loadMatchScore()

        btnHome.setOnClickListener { findNavController().popBackStack(R.id.chatListFragment, false) }
        btnReport.setOnClickListener { showReportDialog() }

        // 더보기 버튼: 계산된 점수(loadedMatchScore)를 포함하여 전달
        btnMore.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("userId", otherUserId.toLongOrNull() ?: -1L)
                putString("USER_NAME", otherUserName)
                putString("PROFILE_URL", otherProfileUrl)
                putInt("targetScore", loadedMatchScore) // 계산된 점수 전달
            }
            findNavController().navigate(R.id.action_chatRoomFragment_to_profileDetailFragment, bundle)
        }

        btnSend.setOnClickListener { sendMessage() }
    }

    /**
     * [수정] MatchingScore 클래스를 사용하여 정확한 점수 계산
     */
    private fun loadMatchScore() {
        val targetId = otherUserId.toLongOrNull()
        if (targetId == null || targetId == -1L) return

        val token = context?.let { TokenManager.getAccessToken(it) } ?: return

        lifecycleScope.launch {
            try {
                val response = authApi.getMatchProfile("Bearer $token", targetId)
                if (response.isSuccessful) {
                    val profile = response.body()

                    // 1. 점수 재료 추출
                    val compat = profile?.detail?.compat
                    val finalS = compat?.finalScore ?: 0.0
                    val stressS = compat?.stressScore ?: 0.0

                    // 2. 계산기 돌리기
                    val score = matchingScoreCalculator.calculateCompositeScore(finalS, stressS)

                    // 3. UI 및 변수 업데이트
                    loadedMatchScore = score
                    binding.tvMatchScore.text = "${score}점"
                } else {
                    binding.tvMatchScore.text = "0점"
                }
            } catch (e: Exception) {
                Log.e("ChatRoom", "Error loading score", e)
            }
        }
    }

    private fun setupStompConnection(roomId: String?) {
        if (roomId == null) return
        val token = context?.let { TokenManager.getAccessToken(it) } ?: ""
        val wsUrl = "ws://3.239.45.21:8080/ws/websocket?token=$token"

        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)
        val lifecycleDisp = mStompClient.lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> Log.d("STOMP", "✅ 연결 성공")
                    LifecycleEvent.Type.ERROR -> Log.e("STOMP", "❌ 연결 에러", lifecycleEvent.exception)
                    LifecycleEvent.Type.CLOSED -> Log.d("STOMP", "🔒 연결 종료")
                    else -> {}
                }
            }
        compositeDisposable.add(lifecycleDisp)

        val subscribeTopic = "/topic/chat.$roomId"
        val topicDisp = mStompClient.topic(subscribeTopic)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage ->
                try {
                    val receivedMsg = gson.fromJson(topicMessage.payload, ChatMessageResponse::class.java)
                    val messageItem = convertToLocalMessage(receivedMsg)
                    if (messageItem.senderId != myUserId) {
                        messageAdapter.addMessage(messageItem)
                        rvChatMessages.scrollToPosition(messageAdapter.itemCount - 1)
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }, { })
        compositeDisposable.add(topicDisp)
        mStompClient.connect()
    }

    private fun sendMessage() {
        val content = etMessageInput.text.toString().trim()
        val matchIdLong = roomId?.toLongOrNull()

        // 유효성 검사
        if (content.isEmpty() || matchIdLong == null) return

        // 1. 내 화면에 먼저 보여주기 (UX) - 로컬 모델에는 senderId가 필요함 (내꺼니까 myUserId)
        val myMessage = Message(
            messageId = System.currentTimeMillis(),
            matchId = matchIdLong,
            senderId = myUserId, // 로컬 표시용 (전송용 아님)
            content = content,
            timestamp = System.currentTimeMillis(),
            isMine = true
        )
        messageAdapter.addMessage(myMessage)
        etMessageInput.text.clear()
        rvChatMessages.scrollToPosition(messageAdapter.itemCount - 1)

        // 2. 서버로 전송 (SEND) -> /app/chat.send
        // [핵심 수정] 명세서대로 matchId와 content만 보냅니다. senderId 제거!
        val sendRequest = ChatMessageSendRequest(
            matchId = matchIdLong,
            content = content
        )

        val jsonContent = gson.toJson(sendRequest)

        Log.d("STOMP", "Sending: $jsonContent") // 로그 확인: {"matchId":3,"content":"..."} 라고 떠야 함

        val sendDisp = mStompClient.send("/app/chat.send", jsonContent)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("STOMP", "📤 메시지 전송 성공")
            }, { error ->
                Log.e("STOMP", "📤 메시지 전송 실패", error)
            })
        compositeDisposable.add(sendDisp)
    }

    private fun setupMyUserId() {
        val token = context?.let { TokenManager.getAccessToken(it) }
        if (token.isNullOrEmpty()) return
        try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val jsonObject = JSONObject(payload)
                myUserId = when {
                    jsonObject.has("userId") -> jsonObject.getLong("userId").toString()
                    jsonObject.has("id") -> jsonObject.getLong("id").toString()
                    jsonObject.has("sub") -> jsonObject.getString("sub")
                    else -> "unknown"
                }
            }
        } catch (e: Exception) { myUserId = "unknown" }
    }

    private fun loadChatHistory() {
        val matchIdLong = roomId?.toLongOrNull() ?: return
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return
        lifecycleScope.launch {
            try {
                val response = chatApi.getChatHistory("Bearer $token", matchIdLong)
                if (response.isSuccessful) {
                    val messages = response.body()?.map { convertToLocalMessage(it) } ?: emptyList()
                    messageAdapter.addMessages(messages)
                    if (messages.isNotEmpty()) rvChatMessages.scrollToPosition(messages.size - 1)
                }
            } catch (e: Exception) {}
        }
    }

    private fun convertToLocalMessage(response: ChatMessageResponse): Message {
        return Message(response.id, response.matchId, response.senderId.toString(), response.content, convertApiDateToTimestamp(response.sentAt), response.senderId.toString() == myUserId)
    }

    private fun convertApiDateToTimestamp(apiDate: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.parse(apiDate)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) { System.currentTimeMillis() }
    }

    private fun showReportDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btn_dialog_report).setOnClickListener {
            dialog.dismiss()
            blockAndNavigateHome()
        }
        dialogView.findViewById<Button>(R.id.btn_dialog_close).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun blockAndNavigateHome() {
        val matchIdLong = roomId?.toLongOrNull() ?: return
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return

        lifecycleScope.launch {
            try {
                val response = chatApi.blockChatRoom("Bearer $token", matchIdLong)
                if (response.isSuccessful) {
                    Toast.makeText(context, "차단되었습니다.", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack(R.id.chatListFragment, false)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "차단 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::mStompClient.isInitialized && mStompClient.isConnected) mStompClient.disconnect()
        compositeDisposable.clear()
        _binding = null
    }
}