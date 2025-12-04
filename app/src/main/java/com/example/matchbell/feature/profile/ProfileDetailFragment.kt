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

    // [추가] 점수 계산기
    private val matchingScoreCalculator = MatchingScore()

    // [추가] 이전 화면에서 넘겨준 점수 저장용 (API 데이터 없을 때 백업용)
    private var initialScore: Int = 0

    private var targetUserId: Long = -1L

    private val BASE_URL = "http://3.239.45.21:8080"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileDetailBinding.inflate(inflater, container, false)
        // 로딩 레이아웃 초기화 (숨김)
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

            // [중요] 리스트에서 이미 계산해서 넘겨준 점수를 저장
            initialScore = args.getInt("targetScore", 0)
        }

        if (targetUserId == -1L) {
            Toast.makeText(context, "유저 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        // 1. 기본 정보 즉시 표시
        binding.tvMessage.text = "${basicName}님의 상세 프로필"

        // [점수 표시] 초기 점수 우선 표시
        if (initialScore > 0) {
            binding.tvMatchBadge.text = "나와의 궁합 ${initialScore}점!"
            binding.tvMatchBadge.visibility = View.VISIBLE
        } else {
            // 점수가 없어도 배지는 보이게 (혹은 "궁합 ?점")
            binding.tvMatchBadge.text = "나와의 궁합 ?점"
            binding.tvMatchBadge.visibility = View.VISIBLE
        }

        loadProfileImage(basicProfileUrl)
        if (basicRegion.isNotEmpty()) binding.tvRegionReal.text = basicRegion

        // 2. 초기화: 잠금 상태 적용
        lockProfileUI()

        // 3. 데이터 로드
        loadProfileData()

        val lockClickListener = View.OnClickListener { handleLockClick() }
        binding.ivLockOverlay.setOnClickListener(lockClickListener)
        binding.ivLockRegion.setOnClickListener(lockClickListener)
        binding.ivLockBirth.setOnClickListener(lockClickListener)
    }

    // 로딩창 제어
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

    // [수정됨] 변경된 데이터 모델에 맞춰 UI 업데이트 로직 수정
    private fun updateUI(response: MatchProfileResponse) {
        val basic = response.basic
        // val detail = response.detail // [삭제] 더 이상 존재하지 않음

        // 1. 기본 정보 바인딩
        binding.tvMessage.text = "${basic.nickname}님의 상세 프로필"
        loadProfileImage(basic.avatarUrl)
        binding.tvRegionReal.text = basic.region ?: "지역 정보 없음"

        // ----------------------------------------------------------------
        // [점수 계산] 이제 basic 안에 있는 compat을 사용합니다.
        // ----------------------------------------------------------------
        val compat = basic.compat // [수정] detail.compat -> basic.compat

        val finalS = compat?.finalScore ?: 0.0
        val stressS = compat?.stressScore ?: 0.0

        // 점수 계산 (공식 적용)
        val calculatedScore = matchingScoreCalculator.calculateCompositeScore(finalS, stressS)

        // 점수 표시 로직 (계산된 점수 > 초기 점수 > 0)
        val finalDisplayScore = if (calculatedScore > 0) {
            calculatedScore
        } else if (initialScore > 0) {
            initialScore
        } else {
            0
        }

        if (finalDisplayScore > 0) {
            binding.tvMatchBadge.text = "나와의 궁합 ${finalDisplayScore}점!"
        } else {
            binding.tvMatchBadge.text = "나와의 궁합 ?점"
        }
        binding.tvMatchBadge.visibility = View.VISIBLE

        // ----------------------------------------------------------------
        // [직업 & 자기소개 & 생년월일] 이제 basic에서 꺼냅니다.
        // ----------------------------------------------------------------

        // 자기소개 (intro가 없으면 shortIntro 사용)
        val introText = basic.intro ?: basic.shortIntro ?: "소개가 없습니다."
        binding.textView.text = introText

        // 직업
        val jobText = basic.job ?: "직업 정보 없음"
        binding.tvJob.text = jobText

        // 생년월일 (년도만 자르기)
        val fullBirth = basic.birth
        if (!fullBirth.isNullOrEmpty() && fullBirth.length >= 4) {
            val year = fullBirth.substring(0, 4)
            binding.tvBirthReal.text = "${year}년생"
        } else {
            binding.tvBirthReal.text = "0000년생"
        }

        // 잠금 상태 결정 (hasUnlocked 값에 따라 UI 변경)
        if (response.hasUnlocked) {
            unlockProfileUI()
        } else {
            lockProfileUI()
        }
    }

    private fun handleLockClick() {
        val dialog = CookieSpendDialogFragment(onConfirm = { requestProfileUnlock() })
        dialog.show(parentFragmentManager, "CookieSpendDialog")
    }

    private fun requestProfileUnlock() {
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = authApi.unlockProfile("Bearer $token", targetUserId)
                if (response.isSuccessful) {
                    val result = response.body()
                    Toast.makeText(context, "잠금 해제 완료!", Toast.LENGTH_SHORT).show()
                    loadProfileData()
                } else {
                    Toast.makeText(context, if (response.code() == 400) "쿠키 부족" else "실패", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }
            } catch (e: Exception) {
                showLoading(false)
            }
        }
    }

    // --- ConstraintSet을 이용한 안전한 잠금 UI ---

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

        // [핵심 수정] binding.root가 아니라 내부의 clContent를 복제해야 합니다!
        constraintSet.clone(binding.clContent)

        if (isLocked) {
            // [잠금 상태]
            // 생년월일 라벨 -> 지역 자물쇠(ivLockRegion) 밑으로
            constraintSet.connect(binding.labelBirth.id, androidx.constraintlayout.widget.ConstraintSet.TOP, binding.ivLockRegion.id, androidx.constraintlayout.widget.ConstraintSet.BOTTOM)
            // 직업 라벨 -> 생년월일 자물쇠(ivLockBirth) 밑으로
            constraintSet.connect(binding.labelJob.id, androidx.constraintlayout.widget.ConstraintSet.TOP, binding.ivLockBirth.id, androidx.constraintlayout.widget.ConstraintSet.BOTTOM)
        } else {
            // [해제 상태]
            // 생년월일 라벨 -> 지역 텍스트(tvRegionReal) 밑으로
            constraintSet.connect(binding.labelBirth.id, androidx.constraintlayout.widget.ConstraintSet.TOP, binding.tvRegionReal.id, androidx.constraintlayout.widget.ConstraintSet.BOTTOM)
            // 직업 라벨 -> 생년월일 텍스트(tvBirthReal) 밑으로
            constraintSet.connect(binding.labelJob.id, androidx.constraintlayout.widget.ConstraintSet.TOP, binding.tvBirthReal.id, androidx.constraintlayout.widget.ConstraintSet.BOTTOM)
        }

        // [핵심 수정] 변경 사항을 clContent에 적용
        constraintSet.applyTo(binding.clContent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}