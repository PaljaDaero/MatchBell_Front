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
import com.example.matchbell.R
import com.example.matchbell.databinding.FragmentChatRoomBinding
import com.example.matchbell.feature.ChatMessageResponse
import com.example.matchbell.feature.ChatMessageSendRequest
import com.example.matchbell.feature.MatchingScore
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
import ua.naiksoftware.stomp.dto.StompHeader
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

    private var loadedMatchScore: Int = 0

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

        // [수정] 상단 프로필 이미지도 자물쇠 아이콘(ic_lock)으로 고정
        ivProfile.setImageResource(R.drawable.ic_lock)

        // Adapter 초기화 (빈 리스트로 생성)
        messageAdapter = MessageAdapter(mutableListOf())
        rvChatMessages.apply {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = messageAdapter
        }

        loadChatHistory()
        setupStompConnection(roomId)

        loadMatchScore()

        btnHome.setOnClickListener { findNavController().popBackStack(R.id.chatListFragment, false) }
        btnReport.setOnClickListener { showReportDialog() }

        btnMore.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("userId", otherUserId.toLongOrNull() ?: -1L)
                putString("USER_NAME", otherUserName)
                putString("PROFILE_URL", otherProfileUrl)
                putInt("targetScore", loadedMatchScore)
            }
            findNavController().navigate(R.id.action_chatRoomFragment_to_profileDetailFragment, bundle)
        }

        btnSend.setOnClickListener { sendMessage() }
    }

    // ... (이하 나머지 함수들은 기존과 동일하게 유지) ...

    private fun loadMatchScore() {
        val targetId = otherUserId.toLongOrNull()
        if (targetId == null || targetId == -1L) return

        val token = context?.let { TokenManager.getAccessToken(it) } ?: return

        lifecycleScope.launch {
            try {
                val response = authApi.getMatchProfile("Bearer $token", targetId)
                if (response.isSuccessful) {
                    val profile = response.body()

                    // [수정] basic을 제거하고 바로 compat에 접근합니다.
                    // 기존: val compat = profile?.basic?.compat
                    val compat = profile?.compat

                    val finalS = compat?.finalScore ?: 0.0
                    val stressS = compat?.stressScore ?: 0.0

                    val score = if (finalS == 0.0 && stressS == 0.0) {
                        0
                    } else {
                        matchingScoreCalculator.calculateCompositeScore(finalS, stressS)
                    }

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
        val wsUrl = "ws://3.239.45.21:8080/ws/websocket"

        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)

        val headerList = arrayListOf<StompHeader>()
        headerList.add(StompHeader("Authorization", "Bearer $token"))

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

        mStompClient.connect(headerList)
    }

    private fun sendMessage() {
        val content = etMessageInput.text.toString().trim()
        val matchIdLong = roomId?.toLongOrNull()

        if (content.isEmpty() || matchIdLong == null) return

        val myMessage = Message(
            messageId = System.currentTimeMillis(),
            matchId = matchIdLong,
            senderId = myUserId,
            content = content,
            timestamp = System.currentTimeMillis(),
            isMine = true
        )
        messageAdapter.addMessage(myMessage)
        etMessageInput.text.clear()
        rvChatMessages.scrollToPosition(messageAdapter.itemCount - 1)

        val sendRequest = ChatMessageSendRequest(
            matchId = matchIdLong,
            content = content
        )

        val jsonContent = gson.toJson(sendRequest)
        Log.d("STOMP", "Sending: $jsonContent")

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
                    // [변경] 날짜 바 처리를 위해 setMessages 사용
                    messageAdapter.setMessages(messages)
                    if (messageAdapter.itemCount > 0) {
                        rvChatMessages.scrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            } catch (e: Exception) {}
        }
    }

    private fun convertToLocalMessage(response: ChatMessageResponse): Message {
        return Message(response.id, response.matchId, response.senderId.toString(), response.content, convertApiDateToTimestamp(response.sentAt), response.senderId.toString() == myUserId)
    }

    // [유지] UTC 파싱 로직
    private fun convertApiDateToTimestamp(apiDate: String?): Long {
        if (apiDate.isNullOrEmpty()) return System.currentTimeMillis()
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (pattern in patterns) {
            try {
                val format = SimpleDateFormat(pattern, Locale.KOREA)
                format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val date = format.parse(apiDate)
                if (date != null) return date.time
            } catch (e: Exception) { continue }
        }
        return System.currentTimeMillis()
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