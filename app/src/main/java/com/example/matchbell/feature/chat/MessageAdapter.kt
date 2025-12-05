package com.example.matchbell.feature.chat

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

// [1] 리스트에 들어갈 아이템 타입을 정의 (메시지 or 날짜)
sealed class ChatItem {
    data class MessageItem(val message: ChatRoomFragment.Message) : ChatItem()
    data class DateItem(val timestamp: Long) : ChatItem()
}

class MessageAdapter(
    // [2] 데이터 리스트 타입을 ChatItem으로 변경
    private val items: MutableList<ChatItem> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_DATE = 3 // [추가] 날짜 표시용 타입
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ChatItem.DateItem -> VIEW_TYPE_DATE
            is ChatItem.MessageItem -> {
                if (item.message.isMine) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = inflater.inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = inflater.inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            VIEW_TYPE_DATE -> { // [추가] 날짜 뷰홀더 생성
                val view = inflater.inflate(R.layout.item_chat_date, parent, false)
                DateViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ChatItem.MessageItem -> {
                if (holder is SentMessageViewHolder) holder.bind(item.message)
                else if (holder is ReceivedMessageViewHolder) holder.bind(item.message)
            }
            is ChatItem.DateItem -> {
                if (holder is DateViewHolder) holder.bind(item.timestamp)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // [중요] 메시지 추가 로직: 날짜가 바뀌면 DateItem을 먼저 넣고 메시지를 넣음
    fun addMessage(message: ChatRoomFragment.Message) {
        val lastItem = items.lastOrNull()

        // 날짜가 바뀌었는지 확인
        if (shouldAddDateItem(lastItem, message.timestamp)) {
            items.add(ChatItem.DateItem(message.timestamp))
        }

        items.add(ChatItem.MessageItem(message))
        notifyItemRangeInserted(items.size - ((if(shouldAddDateItem(lastItem, message.timestamp)) 2 else 1)), if(shouldAddDateItem(lastItem, message.timestamp)) 2 else 1)
        // 안전하게 전체 갱신을 원하면: notifyDataSetChanged() 사용 권장
        notifyDataSetChanged()
    }

    // [중요] 대량 추가 로직 (채팅방 입장 시)
    fun setMessages(messages: List<ChatRoomFragment.Message>) {
        items.clear()

        var lastDateString = ""
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        messages.forEach { msg ->
            val currentDateString = dateFormat.format(Date(msg.timestamp))

            // 날짜가 바뀌면 날짜 아이템 추가
            if (currentDateString != lastDateString) {
                items.add(ChatItem.DateItem(msg.timestamp))
                lastDateString = currentDateString
            }
            items.add(ChatItem.MessageItem(msg))
        }
        notifyDataSetChanged()
    }

    // 날짜 비교 헬퍼 함수
    private fun shouldAddDateItem(lastItem: ChatItem?, currentTimestamp: Long): Boolean {
        if (lastItem == null) return true // 첫 메시지면 무조건 날짜 표시
        if (lastItem !is ChatItem.MessageItem) return false // 마지막이 날짜면 패스

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val lastDate = dateFormat.format(Date(lastItem.message.timestamp))
        val currentDate = dateFormat.format(Date(currentTimestamp))

        return lastDate != currentDate
    }

    // --- ViewHolders ---

    // [3] 날짜 뷰홀더
    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.tv_date_display)

        fun bind(timestamp: Long) {
            // "2025년 12월 5일 금요일" 형식
            val format = SimpleDateFormat("yyyy년 MM월 dd일 EEEE", Locale.KOREA)
            dateText.text = format.format(Date(timestamp))
        }
    }

    // 메시지 뷰홀더 (시간만 표시하도록 수정)
    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tv_message_content_sent)
        private val timestampText: TextView = itemView.findViewById(R.id.tv_timestamp_sent)

        fun bind(message: ChatRoomFragment.Message) {
            messageText.text = message.content
            // [수정] 오직 시간만 표시 (오전 3:30)
            timestampText.text = SimpleDateFormat("a h:mm", Locale.getDefault()).format(Date(message.timestamp))
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.iv_profile_received)
        private val userName: TextView = itemView.findViewById(R.id.tv_user_name_received)
        private val messageText: TextView = itemView.findViewById(R.id.tv_message_content_received)
        private val timestampText: TextView = itemView.findViewById(R.id.tv_timestamp_received)

        fun bind(message: ChatRoomFragment.Message) {
            messageText.text = message.content
            // [수정] 오직 시간만 표시
            timestampText.text = SimpleDateFormat("a h:mm", Locale.getDefault()).format(Date(message.timestamp))
            userName.text = "상대방"
            profileImage.setImageResource(R.drawable.ic_lock)
        }
    }
}