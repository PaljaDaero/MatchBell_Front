package com.example.matchbell.feature.profile

// CookieSpendDialogFragment 패키지 확인 필요 (com.example.matchbell.feature.profile 인지 com.example.matchbell 인지)
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.matchbell.databinding.FragmentProfileDetailBinding
import com.example.matchbell.feature.MatchProfileResponse
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

    private var isUnlocked = false
    private var targetUserId: Long = -1L

    // [수정] isMatched는 더 이상 '진입 차단'용이 아니라, '잠금 해제 가능 여부' 판단용이 될 수 있음
    // 하지만 API 자체가 "매칭된 유저만" 해제 가능하다면 API가 알아서 에러를 줄 것입니다.
    private var isMatched = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 기본 정보 받기 (Radar에서 넘겨준 값)
        val args = arguments
        targetUserId = args?.getLong("targetUserId") ?: -1L
        val basicName = args?.getString("targetName") ?: "알 수 없음"
        val basicScore = args?.getInt("targetScore") ?: 0
        val basicRegion = args?.getString("targetRegion") ?: ""

        if (targetUserId == -1L) {
            findNavController().popBackStack()
            return
        }

        // 2. 기본 UI 세팅 (API 로드 전)
        binding.tvMessage.text = "${basicName}님의 상세 프로필"
        binding.tvMatchBadge.text = "나와의 궁합 ${basicScore}점!"
        binding.tvRegionReal.text = basicRegion
        binding.textView.text = "자기소개 정보가 없습니다." // 기본값
        binding.tvJob.text = "정보 없음"
        binding.tvBirthReal.text = "0000.00.00"

        // 3. 초기 잠금
        lockProfileUI()

        // 4. 상세 데이터 로드 (매칭 안 됐으면 403 뜰 수 있음 -> 그래도 화면은 유지)
        loadProfileData()

        val lockClickListener = View.OnClickListener { handleLockClick() }
        binding.ivLockOverlay.setOnClickListener(lockClickListener)
        binding.ivLockRegion.setOnClickListener(lockClickListener)
        binding.ivLockBirth.setOnClickListener(lockClickListener)
    }

    private fun loadProfileData() {
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return

        lifecycleScope.launch {
            try {
                val response = authApi.getMatchProfile("Bearer $token", targetUserId)

                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        isMatched = true // 성공 = 매칭됨
                        updateUI(profile)
                    }
                } else {
                    // 403 등 에러가 나도 '기본 정보'는 이미 뿌려져 있으므로 괜찮음
                    Log.d("ProfileDetail", "Load failed: ${response.code()} (Not matched yet?)")
                }
            } catch (e: Exception) {
                Log.e("ProfileDetail", "Network Error", e)
            }
        }
    }

    // 서버 응답 데이터 바인딩 함수 수정
    private fun updateUI(response: MatchProfileResponse) {
        val basic = response.basic
        val detail = response.detail

        // 1. 기본 정보 (Basic) - 항상 표시
        binding.tvMessage.text = "${basic.nickname}님의 상세 프로필"

        // 궁합 점수는 detail에 있을 수도 있고, 계산해야 할 수도 있음.
        // 일단 detail이 없으면 0점 처리 혹은 radar에서 가져온 값 유지
        val score = detail?.compat?.finalScore?.toInt() ?: 0 // (Radar에서 넘겨받은 값을 쓰는 게 더 나을 수 있음)
        binding.tvMatchBadge.text = "나와의 궁합 ${score}점!"

        binding.tvRegionReal.text = basic.region

        // 2. 상세 정보 (Detail) - null이면 잠금 상태
        if (detail != null) {
            binding.textView.text = detail.intro ?: basic.shortIntro ?: "소개가 없습니다."
            binding.tvJob.text = detail.job ?: "정보 없음"
            binding.tvBirthReal.text = detail.birth ?: "0000.00.00"

            // 이미 풀려있는 상태라면 UI도 해제
            if (response.hasUnlocked) {
                unlockProfileUI()
            }
        } else {
            // 상세 정보가 없으면 (잠금 상태) 기본 멘트
            binding.textView.text = basic.shortIntro ?: "잠금된 프로필입니다."
            binding.tvJob.text = "잠금 상태"
        }

        // 3. 상태값 업데이트 (서버가 알려준 값 사용)
        isMatched = response.isMatched
        // isUnlocked = response.hasUnlocked // (필요 시 로컬 변수 동기화)

        // 4. 이미지 로드
        if (!basic.avatarUrl.isNullOrEmpty()) {
            Glide.with(this).load(basic.avatarUrl).into(binding.ivProfileReal)
        }
    }

    private fun handleLockClick() {
        if (isUnlocked) return

        // 쿠키 사용 팝업 띄우기
        // [중요] 팝업의 '확인' 버튼을 누르면 -> requestProfileUnlock() 호출
        val dialog = CookieSpendDialogFragment(
            onConfirm = {
                requestProfileUnlock()
            }
        )
        dialog.show(parentFragmentManager, "CookieSpendDialog")
    }

    // [핵심] 실제 잠금 해제 API 호출
    private fun requestProfileUnlock() {
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return

        lifecycleScope.launch {
            try {
                // 새로 만든 API 호출 (/me/matches/{id}/profile/unlock)
                val response = authApi.unlockProfile("Bearer $token", targetUserId)

                if (response.isSuccessful) {
                    val result = response.body()
                    Toast.makeText(context, "잠금 해제! (남은 쿠키: ${result?.balanceAfter})", Toast.LENGTH_SHORT).show()

                    // UI 잠금 해제
                    unlockProfileUI()

                    // 해제 후엔 데이터를 다시 로드해서 확실하게 보여주는 것이 좋음
                    loadProfileData()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: ""
                    if (response.code() == 403) {
                        Toast.makeText(context, "아직 매칭 상태가 아닙니다.", Toast.LENGTH_LONG).show()
                    } else if (response.code() == 400) {
                        Toast.makeText(context, "쿠키가 부족합니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "잠금 해제 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun lockProfileUI() {
        isUnlocked = false
        binding.ivLockOverlay.visibility = View.VISIBLE
        binding.ivLockRegion.visibility = View.VISIBLE
        binding.ivLockBirth.visibility = View.VISIBLE
        binding.ivProfileReal.visibility = View.GONE
        binding.tvRegionReal.visibility = View.GONE
        binding.tvBirthReal.visibility = View.GONE
    }

    private fun unlockProfileUI() {
        isUnlocked = true
        binding.ivLockOverlay.visibility = View.GONE
        binding.ivProfileReal.visibility = View.VISIBLE
        binding.ivLockRegion.visibility = View.GONE
        binding.tvRegionReal.visibility = View.VISIBLE
        binding.ivLockBirth.visibility = View.GONE
        binding.tvBirthReal.visibility = View.VISIBLE
        updateConstraints()
    }

    private fun updateConstraints() {
        val paramsBirth = binding.labelBirth.layoutParams as ConstraintLayout.LayoutParams
        paramsBirth.topToBottom = binding.tvRegionReal.id
        binding.labelBirth.layoutParams = paramsBirth

        val paramsJob = binding.labelJob.layoutParams as ConstraintLayout.LayoutParams
        paramsJob.topToBottom = binding.tvBirthReal.id
        binding.labelJob.layoutParams = paramsJob
        binding.root.requestLayout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}