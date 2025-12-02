package com.example.matchbell.feature.match

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
    val infoText: String, // affiliation 대용 (지역 + 직업 등)
    val score: Int,       // API에 점수가 없으므로 기본값 처리하거나 추후 연동
    val isMatched: Boolean, // true: 매칭완료(하트), false: 궁금해요(빈하트)
    val avatarUrl: String?
)

@AndroidEntryPoint
class MatchingFragment : Fragment() {

    private var _binding: FragmentMatchingBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var authApi: AuthApi

    // [서버 주소] - 이미지 로딩용 (NetworkModule과 동일하게 맞춤)
    private val BASE_URL = "http://3.239.45.21:8080"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // API 호출하여 데이터 로드
        loadMatchingData()
    }

    private fun loadMatchingData() {
        // 1. 토큰 가져오기
        val token = context?.let { TokenManager.getAccessToken(it) }
        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val headerToken = "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 3개의 API를 동시에 비동기 호출 (성능 최적화)
                val sentDeferred = async { authApi.getSentCurious(headerToken) }
                val receivedDeferred = async { authApi.getReceivedCurious(headerToken) }
                val matchesDeferred = async { authApi.getMatches(headerToken) }

                val sentResponse = sentDeferred.await()
                val receivedResponse = receivedDeferred.await()
                val matchesResponse = matchesDeferred.await()

                val uiList = mutableListOf<MatchUiItem>()

                // (1) 매칭 완료 리스트 처리 (Heart)
                if (matchesResponse.isSuccessful) {
                    matchesResponse.body()?.forEach { match ->
                        uiList.add(
                            MatchUiItem(
                                userId = match.userId,
                                nickname = match.nickname,
                                infoText = "${match.region} | ${match.job}", // 정보 조합
                                score = 0, // 매칭 API에 점수가 없어서 0으로 표기 (필요시 백엔드 요청)
                                isMatched = true, // 하트 표시
                                avatarUrl = match.avatarUrl
                            )
                        )
                    }
                }

                // (2) 받은 궁금해요 리스트 처리 (Unheart)
                if (receivedResponse.isSuccessful) {
                    receivedResponse.body()?.forEach { curious ->
                        uiList.add(
                            MatchUiItem(
                                userId = curious.userId,
                                nickname = curious.nickname,
                                infoText = "나에게 궁금해요를 보냄",
                                score = 0,
                                isMatched = false, // 빈 하트
                                avatarUrl = curious.avatarUrl
                            )
                        )
                    }
                }

                // (3) 보낸 궁금해요 리스트 처리 (Unheart)
                if (sentResponse.isSuccessful) {
                    sentResponse.body()?.forEach { curious ->
                        uiList.add(
                            MatchUiItem(
                                userId = curious.userId,
                                nickname = curious.nickname,
                                infoText = "내가 궁금해요를 보냄",
                                score = 0,
                                isMatched = false, // 빈 하트
                                avatarUrl = curious.avatarUrl
                            )
                        )
                    }
                }

                // UI에 아이템 추가
                addMatchItems(uiList)

            } catch (e: Exception) {
                Log.e("MatchingFragment", "Error loading matching data", e)
                Toast.makeText(context, "데이터를 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMatchItems(users: List<MatchUiItem>) {
        val container = binding.llMatchingItemsContainer
        container.removeAllViews() // 기존 뷰 초기화 (새로고침 시 중복 방지)

        val inflater = LayoutInflater.from(context)

        users.forEach { user ->
            // item_matching_list.xml 레이아웃 인플레이트
            val itemView = inflater.inflate(R.layout.item_matching_list, container, false)

            // 1. 텍스트 데이터 바인딩
            // 점수가 0이면 숨기거나 다른 텍스트로 대체 가능
            if (user.score > 0) {
                itemView.findViewById<TextView>(R.id.tv_score_value).text = " ${user.score}점"
            } else {
                itemView.findViewById<TextView>(R.id.tv_score_value).text = " 궁합 ?"
            }

            // 닉네임이나 소속 정보 표시
            // (레이아웃에 닉네임 뷰가 없다면 affiliation 뷰에 합쳐서 표시하거나 레이아웃 수정 필요)
            // 여기서는 기존 affiliation 뷰에 닉네임 + 정보를 같이 보여줍니다.
            itemView.findViewById<TextView>(R.id.tv_affiliation).text = "${user.nickname}\n${user.infoText}"

            // 2. 잠금 버튼 (프로필 상세보기)
            val btnLockProfile = itemView.findViewById<ImageButton>(R.id.btn_lock)
            btnLockProfile.setOnClickListener {
                // 프로필 상세 화면으로 이동 (필요 시 Bundle로 userId 전달)
                // val bundle = Bundle().apply { putLong("USER_ID", user.userId) }
                findNavController().navigate(R.id.action_matchingFragment_to_profileDetailFragment)
            }

            // 3. 하트/궁금해요 상태 처리
            val tvLikeText = itemView.findViewById<TextView>(R.id.tv_like_text)
            val ivHeartIcon = itemView.findViewById<ImageView>(R.id.iv_heart_icon)

            if (user.isMatched) {
                // 매칭 완료 상태 (채팅 가능)
                tvLikeText.text = "매칭완료"
                ivHeartIcon.setImageResource(R.drawable.ic_heart) // 꽉 찬 하트

            } else {
                // 궁금해요 상태 (아직 매칭 안됨)
                tvLikeText.text = "궁금해요"
                ivHeartIcon.setImageResource(R.drawable.ic_unheart) // 빈 하트
            }

            container.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}