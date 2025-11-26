package com.example.matchbell.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import com.example.matchbell.databinding.FragmentMatchingBinding

// 매칭 아이템 데이터를 위한 데이터 클래스 (Feature 내부 또는 Shared 모듈에 정의)
data class MatchUser(
    val id: Int,
    val score: Int,
    val affiliation: String,
    val name: String,
    var isMutualLike: Boolean = false // 쌍방 궁금해요 상태 (사용자 코드 반영)
)

class MatchingFragment : Fragment() {

    // 뷰 바인딩 사용을 위해 수정
    private var _binding: FragmentMatchingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // R.layout.fragment_matching 대신 Binding 클래스를 사용하여 뷰를 인플레이트합니다.
        _binding = FragmentMatchingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 더미 데이터 생성 (쌍방 궁금해요 상태 포함)
        val dummyMatchData = listOf(
            MatchUser(1, 92, "명지대학교 뮤지컬과", "은우사마", isMutualLike = true),
            MatchUser(2, 85, "명지대학교 컴퓨터공학과", "지민쓰", isMutualLike = false),
            MatchUser(3, 78, "명지대학교 경영학과", "태현K", isMutualLike = false),
            MatchUser(4, 65, "명지대학교 의학과", "수지P", isMutualLike = true) ,
            MatchUser(5, 92, "명지대학교 뮤지컬과", "은우사마", isMutualLike = true),
            MatchUser(2, 85, "명지대학교 컴퓨터공학과", "지민쓰", isMutualLike = false),
            MatchUser(3, 78, "명지대학교 경영학과", "태현K", isMutualLike = false),
            MatchUser(4, 65, "명지대학교 의학과", "수지P", isMutualLike = true),
            MatchUser(1, 92, "명지대학교 뮤지컬과", "은우사마", isMutualLike = true),
            MatchUser(2, 85, "명지대학교 컴퓨터공학과", "지민쓰", isMutualLike = false),
            MatchUser(3, 78, "명지대학교 경영학과", "태현K", isMutualLike = false),
            MatchUser(4, 65, "명지대학교 의학과", "수지P", isMutualLike = true)
        )

        addMatchItems(dummyMatchData)
    }

    private fun addMatchItems(users: List<MatchUser>) {
        val container = binding.llMatchingItemsContainer
        val inflater = LayoutInflater.from(context)

        users.forEach { user ->
            // item_matching_user.xml 레이아웃을 인플레이트합니다. (레이아웃 ID를 item_matching_list로 수정)
            val itemView = inflater.inflate(R.layout.item_matching_list, container, false)

            // 1. 데이터 바인딩
            itemView.findViewById<TextView>(R.id.tv_score_value).text = " ${user.score}점"
            itemView.findViewById<TextView>(R.id.tv_affiliation).text = user.affiliation

            // 2. 잠금 버튼 클릭 리스너 (ProfileActivity로 이동)
            val btnLockProfile = itemView.findViewById<ImageButton>(R.id.btn_lock)
            btnLockProfile.setOnClickListener {
                findNavController().navigate(R.id.action_matchingFragment_to_profileDetailFragment)
            }

            // 3. 하트 버튼 (궁금해요/매칭 완료) 로직
            val flLikeHeart = itemView.findViewById<FrameLayout>(R.id.fl_like_heart)
            val tvLikeText = itemView.findViewById<TextView>(R.id.tv_like_text)

            val ivHeartIcon = itemView.findViewById<ImageView>(R.id.iv_heart_icon)

            // 텍스트, 하트 리소스 설정
            val text: String
            val backgroundResId: Int

            if (user.isMutualLike) {
                backgroundResId = R.drawable.ic_heart
                text = "매칭완료"
            } else {
                backgroundResId = R.drawable.ic_unheart
                text = "궁금해요"
            }

            tvLikeText.text = text
            ivHeartIcon?.setImageResource(backgroundResId)

            container.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}