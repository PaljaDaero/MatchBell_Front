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

// [수정] chatRooms를 var로 바꾸고, updateList 함수를 추가하여 목록 업데이트를 처리합니다.
class ChatListAdapter(
    private var chatRooms: List<ChatRoomData>, // private var로 변경하여 목록 업데이트 가능하게 함
    private val onItemClicked: (ChatRoomData) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatRoomViewHolder>() {

    // [추가] 목록을 업데이트하고 RecyclerView에 알리는 함수
    fun updateList(newChatRooms: List<ChatRoomData>) {
        // [핵심] 기존 목록을 새 목록으로 교체
        chatRooms = newChatRooms
        // 변경 사항 전체를 알립니다. (DiffUtil을 사용하면 더 효율적일 수 있습니다.)
        notifyDataSetChanged()
    }

    // [추가] API 형식의 시간 문자열("2025-11-29T18:20:15")을 표시 형식으로 변환하는 유틸리티
    private fun formatApiTime(apiTime: String): String {
        return try {
            val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = apiFormat.parse(apiTime) ?: return apiTime // 파싱 실패 시 원본 반환

            val now = Date()
            val dayInMs = 1000 * 60 * 60 * 24

            return if (now.time - date.time < dayInMs) {
                // 오늘: "오전/오후 3:30" 형식
                SimpleDateFormat("a h:mm", Locale.getDefault()).format(date)
            } else {
                // 오늘 이전: "11/29" 형식
                SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
            }
        } catch (e: Exception) {
            // 오류 시 원본 문자열 반환
            apiTime
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
        // [주의: XML 레이아웃 ID와 일치하는지 확인해야 합니다]
        private val profileImage: CircleImageView = itemView.findViewById(R.id.iv_profile_image)
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
                unreadCount.text = chatRoom.unreadCount.toString()
                unreadContainer.visibility = View.VISIBLE
            } else {
                unreadContainer.visibility = View.GONE
            }

            // 6. 클릭 리스너
            itemView.setOnClickListener {
                onItemClicked(chatRoom)
            }
        }
    }
}