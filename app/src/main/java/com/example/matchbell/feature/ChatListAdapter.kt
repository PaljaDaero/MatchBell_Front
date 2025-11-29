package com.example.matchbell.feature

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.matchbell.R
import de.hdodenhof.circleimageview.CircleImageView

class ChatListAdapter(
    private val chatRooms: List<ChatRoomData>,
    private val onItemClicked: (ChatRoomData) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatRoomViewHolder>() {

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
        private val timestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        private val unreadCountBadge: CardView = itemView.findViewById(R.id.cv_unread_count)
        private val unreadCountText: TextView = itemView.findViewById(R.id.tv_unread_count)

        fun bind(chatRoom: ChatRoomData) {
            profileImage.setImageResource(R.drawable.ic_launcher_foreground)
            userName.text = chatRoom.userName
            lastMessage.text = chatRoom.lastMessage
            timestamp.text = chatRoom.timestamp

            // 1. 읽지 않은 메시지 수 표시/숨김
            if (chatRoom.unreadCount > 0) {
                unreadCountBadge.visibility = View.VISIBLE
                unreadCountText.text = chatRoom.unreadCount.toString()
            } else {
                unreadCountBadge.visibility = View.GONE
            }

            // 2. [로컬 이미지 로드 로직]
            val context = itemView.context

            // ChatListFragment에서 전달된 문자열 리소스 이름(placeholder_profile)을 ID로 변환합니다.
            val resourceId = context.resources.getIdentifier(
                chatRoom.userProfileUrl,
                "drawable",
                context.packageName
            )

            if (resourceId != 0) {
                // 리소스가 발견되면 해당 리소스 로드
                profileImage.setImageResource(resourceId)
            } else {
                // 리소스가 발견되지 않으면 대체 이미지 로드 시도
                try {
                    // R.drawable.bg_profile_image가 정의되어 있다면 이것을 사용
                    profileImage.setImageResource(R.drawable.bg_profile_image)

                    // 만약 이 대체 이미지도 로드에 실패한다면 RuntimeException이 발생할 수 있으므로,
                    // 사용자에게 명시적으로 Toast 메시지를 표시하여 누락된 리소스를 알려줍니다.
                    Toast.makeText(
                        context,
                        "[경고] 더미 이미지 리소스 'placeholder_profile' 또는 'bg_profile_image'를 찾을 수 없습니다.",
                        Toast.LENGTH_LONG // 길게 표시
                    ).show()
                } catch (e: Exception) {
                    // 최종적으로 리소스 로드 실패 시 Toast를 다시 띄웁니다.
                    // 이 경우 아이템의 높이가 0이 되어 안 보일 수 있으므로, 텍스트라도 보이게 하기 위해 배경색을 강제합니다.
                    profileImage.setBackgroundColor(context.resources.getColor(android.R.color.darker_gray, null))
                    Toast.makeText(
                        context,
                        "[오류] 프로필 이미지가 로드되지 않아 목록이 비어 보입니다. 모든 Drawable 리소스를 확인해주세요.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}