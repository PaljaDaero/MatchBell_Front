package com.example.matchbell.feature.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.matchbell.R
import com.example.matchbell.feature.ChatRoomData
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatListAdapter(
    private var chatRooms: List<ChatRoomData>,
    private val onItemClicked: (ChatRoomData) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatRoomViewHolder>() {

    // [핵심 수정] UTC 파싱 + 날짜 포맷 로직
    private fun formatApiTime(apiTime: String): String {
        if (apiTime.isNullOrEmpty()) return ""

        // 1. 서버 UTC 시간 파싱
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )

        var parsedDate: Date? = null
        for (pattern in patterns) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.KOREA)
                parser.timeZone = TimeZone.getTimeZone("UTC") // 서버 = UTC
                parsedDate = parser.parse(apiTime)
                if (parsedDate != null) break
            } catch (e: Exception) { continue }
        }

        if (parsedDate == null) return apiTime

        // 2. 오늘인지 확인 (로컬 시간 기준 비교)
        val now = Date()
        val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val isToday = dayFormat.format(parsedDate) == dayFormat.format(now)

        return if (isToday) {
            // 오늘: "오전 3:30"
            SimpleDateFormat("a h:mm", Locale.getDefault()).format(parsedDate)
        } else {
            // 과거: "12월 5일" (년도 제외, 날짜만)
            SimpleDateFormat("M월 d일", Locale.getDefault()).format(parsedDate)
        }
    }

    override fun getItemCount(): Int = chatRooms.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_list, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(chatRooms[position])
    }

    inner class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.iv_profile_image)
        private val userName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val lastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        private val unreadCount: TextView = itemView.findViewById(R.id.tv_unread_count)
        private val timestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        private val unreadContainer: CardView = itemView.findViewById(R.id.cv_unread_count)

        fun bind(chatRoom: ChatRoomData) {
            val context = itemView.context

            // 프로필 사진 -> 자물쇠 고정
            Glide.with(context).clear(profileImage)
            profileImage.setImageResource(R.drawable.ic_lock)

            userName.text = chatRoom.userName
            lastMessage.text = chatRoom.lastMessage
            timestamp.text = formatApiTime(chatRoom.timestamp)

            if (chatRoom.unreadCount > 0) {
                unreadCount.text = chatRoom.unreadCount.toString()
                unreadContainer.visibility = View.VISIBLE
            } else {
                unreadContainer.visibility = View.GONE
            }

            itemView.setOnClickListener { onItemClicked(chatRoom) }
        }
    }
}