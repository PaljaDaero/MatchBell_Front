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
import com.example.matchbell.feature.MatchingScore
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val matchingScoreCalculator = MatchingScore()

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

                // (1) 매칭 완료 목록
                if (matchesResponse.isSuccessful) {
                    matchesResponse.body()?.forEach { match ->
                        // 서버가 점수를 안 주면 0.0 -> 결과 0점
                        val finalS = match.finalScore ?: 0.0
                        val stressS = match.stressScore ?: 0.0

                        val realScore = if (finalS == 0.0 && stressS == 0.0) {
                            0
                        } else {
                            matchingScoreCalculator.calculateCompositeScore(finalS, stressS)
                        }

                        uiList.add(
                            MatchUiItem(
                                userId = match.userId,
                                nickname = match.nickname,
                                infoText = "${match.region} | ${match.job}",
                                score = realScore,
                                isMatched = true,
                                avatarUrl = match.avatarUrl
                            )
                        )
                    }
                }

                // (2) 받은 궁금해요
                if (receivedResponse.isSuccessful) {
                    receivedResponse.body()?.forEach { curious ->
                        val finalS = curious.finalScore ?: 0.0
                        val stressS = curious.stressScore ?: 0.0

                        val realScore = if (finalS == 0.0 && stressS == 0.0) {
                            0
                        } else {
                            matchingScoreCalculator.calculateCompositeScore(finalS, stressS)
                        }

                        uiList.add(
                            MatchUiItem(
                                userId = curious.userId,
                                nickname = curious.nickname,
                                infoText = "나에게 궁금해요를 보냄",
                                score = realScore,
                                isMatched = false,
                                avatarUrl = curious.avatarUrl
                            )
                        )
                    }
                }

                // (3) 보낸 궁금해요
                if (sentResponse.isSuccessful) {
                    sentResponse.body()?.forEach { curious ->
                        val finalS = curious.finalScore ?: 0.0
                        val stressS = curious.stressScore ?: 0.0

                        val realScore = if (finalS == 0.0 && stressS == 0.0) {
                            0
                        } else {
                            matchingScoreCalculator.calculateCompositeScore(finalS, stressS)
                        }

                        uiList.add(
                            MatchUiItem(
                                userId = curious.userId,
                                nickname = curious.nickname,
                                infoText = "내가 궁금해요를 보냄",
                                score = realScore,
                                isMatched = false,
                                avatarUrl = curious.avatarUrl
                            )
                        )
                    }
                }

                addMatchItems(uiList)

            } catch (e: Exception) {
                Log.e("MatchingFragment", "Error loading data", e)
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

            // [삭제됨] 점수 관련 로직 (tvScore) 전부 제거

            // 전공/직업/닉네임 표시 (XML에서 폰트 키움)
            // 닉네임과 정보를 줄바꿈(\n)으로 연결해서 보여줌
            itemView.findViewById<TextView>(R.id.tv_affiliation).text = "${user.nickname}\n${user.infoText}"

            // 잠금 버튼 클릭 시 상세 이동 (점수는 계산용으로 일단 넘김)
            val btnLockProfile = itemView.findViewById<ImageButton>(R.id.btn_lock)
            btnLockProfile.setOnClickListener {
                val bundle = Bundle().apply {
                    putLong("targetUserId", user.userId)
                    putString("targetName", user.nickname)
                    putString("targetRegion", user.infoText)
                    putInt("targetScore", user.score)
                    putString("PROFILE_URL", user.avatarUrl)
                }
                findNavController().navigate(
                    R.id.action_matchingFragment_to_profileDetailFragment,
                    bundle
                )
            }

            // 하트 아이콘/텍스트 설정
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