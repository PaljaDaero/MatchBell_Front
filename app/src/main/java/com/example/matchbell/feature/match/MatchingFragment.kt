package com.example.matchbell.feature.match

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import com.example.matchbell.databinding.FragmentMatchingBinding
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI 표시를 위한 통합 데이터 모델
data class MatchUiItem(
    val userId: Long,
    val nickname: String,
    val infoText: String,
    val score: Int,
    val isMatched: Boolean,
    val avatarUrl: String?
)

@AndroidEntryPoint
class MatchingFragment : Fragment() {

    private var _binding: FragmentMatchingBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var authApi: AuthApi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMatchingData()
    }

    private fun loadMatchingData() {
        val token = context?.let { TokenManager.getAccessToken(it) }
        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val headerToken = "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sentDeferred = async { authApi.getSentCurious(headerToken) }
                val receivedDeferred = async { authApi.getReceivedCurious(headerToken) }
                val matchesDeferred = async { authApi.getMatches(headerToken) }

                val sentResponse = sentDeferred.await()
                val receivedResponse = receivedDeferred.await()
                val matchesResponse = matchesDeferred.await()

                val uiList = mutableListOf<MatchUiItem>()

                // (1) 매칭 완료
                if (matchesResponse.isSuccessful) {
                    matchesResponse.body()?.forEach { match ->
                        uiList.add(
                            MatchUiItem(
                                userId = match.userId,
                                nickname = match.nickname,
                                infoText = "${match.region} | ${match.job}",
                                score = 0,
                                isMatched = true,
                                avatarUrl = match.avatarUrl
                            )
                        )
                    }
                }

                // (2) 받은 궁금해요
                if (receivedResponse.isSuccessful) {
                    receivedResponse.body()?.forEach { curious ->
                        uiList.add(
                            MatchUiItem(
                                userId = curious.userId,
                                nickname = curious.nickname,
                                infoText = "나에게 궁금해요를 보냄",
                                score = 0,
                                isMatched = false,
                                avatarUrl = curious.avatarUrl
                            )
                        )
                    }
                }

                // (3) 보낸 궁금해요
                if (sentResponse.isSuccessful) {
                    sentResponse.body()?.forEach { curious ->
                        uiList.add(
                            MatchUiItem(
                                userId = curious.userId,
                                nickname = curious.nickname,
                                infoText = "내가 궁금해요를 보냄",
                                score = 0,
                                isMatched = false,
                                avatarUrl = curious.avatarUrl
                            )
                        )
                    }
                }

                addMatchItems(uiList)

            } catch (e: Exception) {
                Log.e("MatchingFragment", "Error loading matching data", e)
                Toast.makeText(context, "데이터를 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addMatchItems(users: List<MatchUiItem>) {
        val container = binding.llMatchingItemsContainer
        container.removeAllViews()

        val inflater = LayoutInflater.from(context)

        users.forEach { user ->
            val itemView = inflater.inflate(R.layout.item_matching_list, container, false)

            // 1. 텍스트 데이터 바인딩
            if (user.score > 0) {
                itemView.findViewById<TextView>(R.id.tv_score_value).text = " ${user.score}점"
            } else {
                itemView.findViewById<TextView>(R.id.tv_score_value).text = " 궁합 ?"
            }

            itemView.findViewById<TextView>(R.id.tv_affiliation).text = "${user.nickname}\n${user.infoText}"

            // 2. [수정] 잠금 버튼 클릭 시 상세 페이지로 이동 (데이터 전달 포함)
            val btnLockProfile = itemView.findViewById<ImageButton>(R.id.btn_lock)
            btnLockProfile.setOnClickListener {
                // ProfileDetailFragment에서 받을 데이터를 Bundle에 담습니다.
                val bundle = Bundle().apply {
                    putLong("targetUserId", user.userId) // [필수] ID
                    putString("targetName", user.nickname)
                    putString("targetRegion", user.infoText) // 임시로 infoText를 넘김
                    putInt("targetScore", user.score)
                }

                // 네비게이션 이동 (Bundle 전달)
                findNavController().navigate(
                    R.id.action_matchingFragment_to_profileDetailFragment,
                    bundle
                )
            }

            // 3. 하트 상태 처리
            val tvLikeText = itemView.findViewById<TextView>(R.id.tv_like_text)
            val ivHeartIcon = itemView.findViewById<ImageView>(R.id.iv_heart_icon)

            if (user.isMatched) {
                tvLikeText.text = "매칭완료"
                ivHeartIcon.setImageResource(R.drawable.ic_heart)
            } else {
                tvLikeText.text = "궁금해요"
                ivHeartIcon.setImageResource(R.drawable.ic_unheart)
            }

            container.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}