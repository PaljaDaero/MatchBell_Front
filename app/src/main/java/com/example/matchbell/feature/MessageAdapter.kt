package com.example.matchbell.feature

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.matchbell.R
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// MessageAdapter.kt
class MessageAdapter(
    private val messages: MutableList<ChatRoomFragment.Message>,
    private val myUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1 // 내가 보낸 메시지
        private const val VIEW_TYPE_RECEIVED = 2 // 상대방이 보낸 메시지
    }

    // 뷰 타입 결정 (어떤 레이아웃을 사용할지)
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isMine) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder.itemViewType) {
            VIEW_TYPE_SENT -> (holder as SentMessageViewHolder).bind(message)
            VIEW_TYPE_RECEIVED -> (holder as ReceivedMessageViewHolder).bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatRoomFragment.Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // 메시지 목록을 한 번에 추가하는 함수 (채팅 기록 로드 시 사용)
    fun addMessages(newMessages: List<ChatRoomFragment.Message>) {
        val startPosition = messages.size
        messages.addAll(newMessages)
        notifyItemRangeInserted(startPosition, newMessages.size)
    }

    // 시간 포맷을 변환하는 유틸리티 함수
    private fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat("a h:mm", Locale.getDefault()).format(Date(timestamp)) // 오전/오후 3:30
    }

    // 1. 내가 보낸 메시지 뷰 홀더
    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tv_message_content_sent)
        private val timestampText: TextView = itemView.findViewById(R.id.tv_timestamp_sent)

        fun bind(message: ChatRoomFragment.Message) {
            messageText.text = message.content
            timestampText.text = formatTimestamp(message.timestamp)
        }
    }

    // 2. 상대방이 보낸 메시지 뷰 홀더
    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.iv_profile_received)
        private val userName: TextView = itemView.findViewById(R.id.tv_user_name_received)
        private val messageText: TextView = itemView.findViewById(R.id.tv_message_content_received)
        private val timestampText: TextView = itemView.findViewById(R.id.tv_timestamp_received)

        fun bind(message: ChatRoomFragment.Message) {
            messageText.text = message.content
            timestampText.text = formatTimestamp(message.timestamp)

            userName.text = "상대방" // 실제로는 Fragment에서 받은 이름을 사용해야 합니다.

            // 프로필 이미지 로드 (더미 이미지 사용)
            // 실제 구현 시: Glide를 사용하여 URL로 로드
            profileImage.setImageResource(R.drawable.bg_profile_image)
        }
    }
}