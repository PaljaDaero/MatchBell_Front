package com.example.matchbell.feature

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.matchbell.R
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private val chatRooms: List<ChatRoomData>,
    private val onItemClicked: (ChatRoomData) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatRoomViewHolder>() {

    // [추가] API 형식의 시간 문자열("2025-11-29T18:20:15")을 표시 형식으로 변환하는 유틸리티
    private fun formatApiTime(apiTime: String): String {
        return try {
            val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = apiFormat.parse(apiTime) ?: return apiTime // 파싱 실패 시 원본 반환

            val now = Date()
            val dayInMs = 1000 * 60 * 60 * 24

            return if (now.time - date.time < dayInMs) {
                // 오늘: "오후 6:20"
                SimpleDateFormat("a h:mm", Locale.getDefault()).format(date)
            } else {
                // 오늘 이전: "11/29"
                SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            apiTime
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_list, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val chatRoom = chatRooms[position]
        holder.bind(chatRoom)
        holder.itemView.setOnClickListener {
            onItemClicked(chatRoom)
        }
    }

    override fun getItemCount(): Int = chatRooms.size

    inner class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.iv_profile)
        private val userName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val lastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        private val unreadCount: TextView = itemView.findViewById(R.id.tv_unread_count)
        private val timestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        private val unreadContainer: CardView = itemView.findViewById(R.id.cv_unread_count)

        fun bind(chatRoom: ChatRoomData) {
            val context = itemView.context

            // 1. [수정] 프로필 이미지 로드 (URL 처리 - Glide 사용)
            if (!chatRoom.userProfileUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(chatRoom.userProfileUrl)
                    .placeholder(R.drawable.bg_profile_image) // 로딩 중 기본 이미지 (R.drawable.bg_profile_image가 있어야 함)
                    .error(R.drawable.bg_profile_image) // 로드 실패 시 이미지
                    .into(profileImage)
            } else {
                // URL이 없는 경우 로컬 더미 이미지 사용
                profileImage.setImageResource(R.drawable.bg_profile_image)
            }

            // 2. 닉네임
            userName.text = chatRoom.userName

            // 3. 마지막 메시지
            lastMessage.text = chatRoom.lastMessage

            // 4. [수정] 마지막 메시지 시간 포맷
            timestamp.text = formatApiTime(chatRoom.timestamp)

            // 5. 읽지 않은 메시지 수
            if (chatRoom.unreadCount > 0) {
                unreadContainer.visibility = View.VISIBLE
                unreadCount.text = chatRoom.unreadCount.toString()
            } else {
                unreadContainer.visibility = View.GONE
            }
        }
    }
}