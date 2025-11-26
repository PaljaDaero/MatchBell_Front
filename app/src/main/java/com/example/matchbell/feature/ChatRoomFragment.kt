package com.example.matchbell.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.matchbell.R
import com.example.matchbell.databinding.FragmentChatRoomBinding
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRoomFragment : Fragment() {

    private lateinit var rvChatMessages: RecyclerView
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: Button
    private lateinit var tvUserName: TextView
    private lateinit var tvMatchScore: TextView
    private lateinit var ivProfile: CircleImageView
    private lateinit var messageAdapter: MessageAdapter

    // 채팅방 및 사용자 정보 (ChatListFragment에서 Bundle로 받아옴)
    private var roomId: String? = null
    private var otherUserId: String = "unknown_user" // 기본값 설정
    private var otherUserName: String? = null
    private var otherProfileUrl: String? = null

    private val myUserId = "current_user_id_123" // 현재 사용자 ID

    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    // 메시지 데이터 구조 정의 (로컬/Adapter용)
    data class Message(
        val messageId: String = UUID.randomUUID().toString(),
        val senderId: String,
        val content: String,
        val timestamp: Long = System.currentTimeMillis(),
        var isMine: Boolean // 내가 보낸 메시지인지 구분
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bundle에서 데이터 추출
        arguments?.let {
            roomId = it.getString("ROOM_ID")
            // null이 아닐 경우에만 할당, null이면 초기값 "unknown_user" 유지
            otherUserId = it.getString("USER_ID") ?: otherUserId
            otherUserName = it.getString("USER_NAME")
            otherProfileUrl = it.getString("PROFILE_URL")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. UI 요소 바인딩
        tvUserName = binding.tvUserName
        tvMatchScore = binding.tvMatchScore
        ivProfile = binding.ivProfileChatRoom
        rvChatMessages = binding.rvChatMessages
        etMessageInput = binding.etMessageInput
        btnSend = binding.btnSend

        // 2. 상단바 정보 설정
        tvUserName.text = otherUserName ?: "상대방"
        tvMatchScore.text = "나와의 궁합 92점!" // 하드코딩 유지
        Glide.with(this)
            .load(otherProfileUrl)
            .placeholder(R.drawable.bg_profile_image)
            .error(R.drawable.bg_profile_image)
            .into(ivProfile)

        // 3. [테스트용] 초기 메시지 기록 로드
        val initialMessages = mutableListOf(
            Message(senderId = otherUserId, content = "안녕하세요 ;)", timestamp = System.currentTimeMillis() - 40000, isMine = false),
            Message(senderId = myUserId, content = "네, 안녕하세요. 오늘 뭐하세요?", timestamp = System.currentTimeMillis() - 30000, isMine = true),
            Message(senderId = otherUserId, content = "오늘은 집에서 쉬려고요. 당신은요?", timestamp = System.currentTimeMillis() - 20000, isMine = false),
            Message(senderId = myUserId, content = "저는 오랜만에 친구 만나러 나왔어요.", timestamp = System.currentTimeMillis() - 10000, isMine = true)
        )

        // 4. Adapter 초기화 및 RecyclerView 설정
        messageAdapter = MessageAdapter(initialMessages, myUserId)
        rvChatMessages.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
        rvChatMessages.scrollToPosition(messageAdapter.itemCount - 1)

        // 홈 버튼: 네비게이션 스택을 팝함
        view.findViewById<View>(R.id.iv_home).setOnClickListener {
            findNavController().popBackStack()
        }

        // 차단 버튼: dialog를 띄워 버튼 선택하게 함
        binding.btnReport.setOnClickListener {
            showReportDialog()
        }

        // 더보기 버튼: 상세페이지(fragment_profile_detail)로 이동하게 함
        binding.btnMore.setOnClickListener {
            findNavController().navigate(R.id.action_chatRoomFragment_to_profileDetailFragment)
        }

        // 5. 이벤트 리스너
        btnSend.setOnClickListener { sendMessage() }

        // 6. 시스템 뒤로가기 버튼 처리: 수동 FragmentManager 대신 findNavController() 사용
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })
    }

    // WebSocket: 메시지 전송 및 로컬 시뮬레이션
    private fun sendMessage() {
        val content = etMessageInput.text.toString().trim()
        if (content.isEmpty()) return

        // 1. 내가 보낸 메시지를 UI에 즉시 추가
        val myMessage = Message(
            senderId = myUserId,
            content = content,
            isMine = true
        )
        messageAdapter.addMessage(myMessage)
        etMessageInput.text.clear()
        rvChatMessages.scrollToPosition(messageAdapter.itemCount - 1)

        // 2. [테스트용] 상대방이 메시지를 받았고 응답하는 것을 시뮬레이션 (딜레이 후 실행)
        lifecycleScope.launch {
            delay(1500) // 1.5초 딜레이

            val autoReply = when {
                content.contains("안녕") -> "안녕하세요! ${otherUserName ?: "상대방"}입니다."
                content.contains("오늘") -> "오늘은 좀 바쁘네요. 내일은 시간 괜찮아요?"
                else -> "네, 알겠습니다! 좋은 하루 보내세요."
            }

            val receivedMessage = Message(
                senderId = otherUserId,
                content = autoReply,
                isMine = false
            )

            // 메인 스레드에서 UI 업데이트
            messageAdapter.addMessage(receivedMessage)
            rvChatMessages.scrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    private fun showReportDialog() {
        // 1. AlertDialog Builder 생성
        val builder = AlertDialog.Builder(requireContext())

        // 2. Custom Layout 인플레이트 (R.layout.dialog_ranking 사용)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report, null)
        builder.setView(dialogView)

        // 3. Dialog 생성
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 4. 다이얼로그 내 버튼 클릭 리스너 설정
        val chargeButton = dialogView.findViewById<Button>(R.id.btn_dialog_report)
        val closeButton = dialogView.findViewById<Button>(R.id.btn_dialog_close)

        // 차단 버튼 로직
        chargeButton.setOnClickListener {
        }

        // 닫기 버튼 로직
        closeButton.setOnClickListener {
            dialog.dismiss() // 다이얼로그 닫기
        }

        // 5. 다이얼로그 표시
        dialog.show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // WebSocket 관련 정리 코드 제거
    }
}