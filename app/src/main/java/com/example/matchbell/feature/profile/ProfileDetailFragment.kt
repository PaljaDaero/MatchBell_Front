package com.example.matchbell.feature.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.matchbell.R
import com.example.matchbell.databinding.FragmentProfileDetailBinding
import com.example.matchbell.feature.CookieSpendRequest
import com.example.matchbell.feature.MatchProfileResponse
import com.example.matchbell.feature.MatchingScore
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileDetailFragment : Fragment() {

    @Inject
    lateinit var authApi: AuthApi

    private var _binding: FragmentProfileDetailBinding? = null
    private val binding get() = _binding!!

    // 점수 계산기
    private val matchingScoreCalculator = MatchingScore()

    private var initialScore: Int = 0
    private var targetUserId: Long = -1L
    private val BASE_URL = "http://3.239.45.21:8080"

    // [중요] 서버가 잠금 상태를 안 알려주므로, 앱에서 '내가 풀었다'는 걸 기억하는 변수
    private var isUnlockedLocal: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileDetailBinding.inflate(inflater, container, false)
        showLoading(false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments
        var basicName = "알 수 없음"
        var basicRegion = ""
        var basicProfileUrl: String? = null

        if (args != null) {
            val idFromChat = args.getLong("userId", -1L)
            targetUserId = if (idFromChat != -1L) idFromChat else args.getLong("targetUserId", -1L)

            basicName = args.getString("targetName") ?: args.getString("USER_NAME") ?: "알 수 없음"
            basicRegion = args.getString("targetRegion") ?: ""
            basicProfileUrl = args.getString("PROFILE_URL")
            initialScore = args.getInt("targetScore", 0)
        }

        if (targetUserId == -1L) {
            Toast.makeText(context, "유저 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        // 1. 기본 정보 즉시 표시
        binding.tvMessage.text = "${basicName}님의 상세 프로필"

        if (initialScore > 0) {
            binding.tvMatchBadge.text = "나와의 궁합 ${initialScore}점!"
            binding.tvMatchBadge.visibility = View.VISIBLE
        } else {
            binding.tvMatchBadge.text = "나와의 궁합 ?점"
            binding.tvMatchBadge.visibility = View.VISIBLE
        }

        loadProfileImage(basicProfileUrl)
        if (basicRegion.isNotEmpty()) binding.tvRegionReal.text = basicRegion

        // 2. 초기화: 일단 잠금 상태로 시작
        lockProfileUI()

        // 3. 데이터 로드
        loadProfileData()

        val lockClickListener = View.OnClickListener { handleLockClick() }
        binding.ivLockOverlay.setOnClickListener(lockClickListener)
        binding.ivLockRegion.setOnClickListener(lockClickListener)
        binding.ivLockBirth.setOnClickListener(lockClickListener)
    }

    private fun showLoading(isLoading: Boolean) {
        if (_binding == null) return
        binding.flLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun loadProfileImage(url: String?) {
        if (!url.isNullOrEmpty()) {
            val fullUrl = if (url.startsWith("http")) url else "$BASE_URL$url"
            Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.bg_profile_image)
                .error(R.drawable.bg_profile_image)
                .into(binding.ivProfileReal)
        } else {
            binding.ivProfileReal.setImageResource(R.drawable.bg_profile_image)
        }
    }

    private fun loadProfileData() {
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = authApi.getMatchProfile("Bearer $token", targetUserId)
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        updateUI(profile)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileDetail", "Network Error", e)
            } finally {
                showLoading(false)
            }
        }
    }

    // [수정] Flat한 구조의 Response를 바로 사용
    private fun updateUI(profile: MatchProfileResponse) {

        // 1. 기본 정보 바인딩 (profile에서 바로 꺼냄)
        binding.tvMessage.text = "${profile.nickname}님의 상세 프로필"
        loadProfileImage(profile.avatarUrl)
        binding.tvRegionReal.text = profile.region ?: "지역 정보 없음"

        // 2. 점수 정보 (compat 객체 사용)
        val compat = profile.compat
        if (compat != null) {
            val finalS = compat.finalScore
            val stressS = compat.stressScore
            val calculatedScore = matchingScoreCalculator.calculateCompositeScore(finalS, stressS)

            binding.tvMatchBadge.text = "나와의 궁합 ${calculatedScore}점!"
            binding.tvMatchBadge.visibility = View.VISIBLE
        } else {
            binding.tvMatchBadge.text = "나와의 궁합 ?점"
            binding.tvMatchBadge.visibility = View.VISIBLE
        }

        // 3. 텍스트 정보
        binding.textView.text = profile.intro ?: "소개가 없습니다."
        binding.tvJob.text = profile.job ?: "직업 정보 없음"

        val fullBirth = profile.birth
        if (!fullBirth.isNullOrEmpty() && fullBirth.length >= 4) {
            val year = fullBirth.substring(0, 4)
            binding.tvBirthReal.text = "${year}년생"
        } else {
            binding.tvBirthReal.text = "0000년생"
        }

        // 4. 잠금 상태 결정 (로컬 변수 확인)
        if (isUnlockedLocal) {
            unlockProfileUI()
        } else {
            lockProfileUI()
        }
    }

    private fun handleLockClick() {
        val dialog = CookieSpendDialogFragment(onConfirm = { requestProfileUnlock() })
        dialog.show(parentFragmentManager, "CookieSpendDialog")
    }

    // [수정] 쿠키 차감 요청 (Body 포함)
    private fun requestProfileUnlock() {
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return
        showLoading(true)

        // 쿠키 2개 차감 요청 객체 생성
        val requestBody = CookieSpendRequest(
            amount = 2,
            reason = "프로필 잠금 해제"
        )

        lifecycleScope.launch {
            try {
                // API 호출 시 Body 전달
                val response = authApi.unlockProfile("Bearer $token", targetUserId, requestBody)

                if (response.isSuccessful) {
                    val result = response.body()

                    // 성공 시 로컬 변수를 true로 변경 (이제 updateUI 호출 시 안 잠김)
                    isUnlockedLocal = true

                    Toast.makeText(context, "잠금 해제 완료!", Toast.LENGTH_SHORT).show()
                    loadProfileData() // 화면 갱신
                } else {
                    Toast.makeText(context, if (response.code() == 400) "쿠키 부족" else "실패", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }
            } catch (e: Exception) {
                showLoading(false)
            }
        }
    }

    // --- UI 잠금/해제 제어 ---

    private fun lockProfileUI() {
        binding.ivLockOverlay.visibility = View.VISIBLE
        binding.ivProfileReal.visibility = View.INVISIBLE

        binding.textView.visibility = View.VISIBLE
        binding.tvJob.visibility = View.VISIBLE
        binding.labelJob.visibility = View.VISIBLE

        binding.ivLockRegion.visibility = View.VISIBLE
        binding.tvRegionReal.visibility = View.GONE

        binding.ivLockBirth.visibility = View.VISIBLE
        binding.tvBirthReal.visibility = View.GONE

        updateConstraints(isLocked = true)
    }

    private fun unlockProfileUI() {
        binding.ivLockOverlay.visibility = View.GONE
        binding.ivProfileReal.visibility = View.VISIBLE

        binding.textView.visibility = View.VISIBLE
        binding.tvJob.visibility = View.VISIBLE
        binding.labelJob.visibility = View.VISIBLE

        binding.ivLockRegion.visibility = View.GONE
        binding.tvRegionReal.visibility = View.VISIBLE

        binding.ivLockBirth.visibility = View.GONE
        binding.tvBirthReal.visibility = View.VISIBLE

        updateConstraints(isLocked = false)
    }

    private fun updateConstraints(isLocked: Boolean) {
        val constraintSet = androidx.constraintlayout.widget.ConstraintSet()
        constraintSet.clone(binding.clContent)

        if (isLocked) {
            constraintSet.connect(binding.labelBirth.id, androidx.constraintlayout.widget.ConstraintSet.TOP, binding.ivLockRegion.id, androidx.constraintlayout.widget.ConstraintSet.BOTTOM)
            constraintSet.connect(binding.labelJob.id, androidx.constraintlayout.widget.ConstraintSet.TOP, binding.ivLockBirth.id, androidx.constraintlayout.widget.ConstraintSet.BOTTOM)
        } else {
            constraintSet.connect(binding.labelBirth.id, androidx.constraintlayout.widget.ConstraintSet.TOP, binding.tvRegionReal.id, androidx.constraintlayout.widget.ConstraintSet.BOTTOM)
            constraintSet.connect(binding.labelJob.id, androidx.constraintlayout.widget.ConstraintSet.TOP, binding.tvBirthReal.id, androidx.constraintlayout.widget.ConstraintSet.BOTTOM)
        }

        constraintSet.applyTo(binding.clContent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}