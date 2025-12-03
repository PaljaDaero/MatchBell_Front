package com.example.matchbell.feature.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
    private var isMatched = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. UserId 확인
        targetUserId = arguments?.getLong("targetUserId") ?: -1L

        // [로그] 전달받은 ID가 정상인지 확인
        Log.d("ProfileDetail", "Passed Target User ID: $targetUserId")

        if (targetUserId == -1L) {
            Toast.makeText(context, "유저 ID 오류", Toast.LENGTH_SHORT).show()
            return
        }

        lockProfileUI()
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
                Log.d("ProfileDetail", "Requesting profile for ID: $targetUserId...")
                val response = authApi.getMatchProfile("Bearer $token", targetUserId)

                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        Log.d("ProfileDetail", "Load Success: ${profile.nickname}")
                        updateUI(profile)
                    } else {
                        Log.e("ProfileDetail", "Response Body is NULL")
                        Toast.makeText(context, "데이터가 비어있습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // [중요] 실패 원인 로그 출력
                    val errorBody = response.errorBody()?.string()
                    Log.e("ProfileDetail", "Load Failed! Code: ${response.code()}, Body: $errorBody")

                    if (response.code() == 404) {
                        Toast.makeText(context, "존재하지 않거나 매칭되지 않은 유저입니다.", Toast.LENGTH_LONG).show()
                    } else if (response.code() == 500) {
                        Toast.makeText(context, "서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "프로필 로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileDetail", "Network Error", e)
                Toast.makeText(context, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(profile: MatchProfileResponse) {
        binding.tvMessage.text = "${profile.nickname}님의 상세 프로필"
        val score = profile.compat.finalScore.toInt()
        binding.tvMatchBadge.text = "나와의 궁합 ${score}점!"
        binding.textView.text = profile.intro ?: "자기소개가 없습니다."
        binding.tvJob.text = profile.job
        binding.tvRegionReal.text = profile.region
        binding.tvBirthReal.text = profile.birth
        if (!profile.avatarUrl.isNullOrEmpty()) {
            Glide.with(this).load(profile.avatarUrl).into(binding.ivProfileReal)
        }
    }

    private fun handleLockClick() {
        if (isUnlocked) return
        if (!isMatched) {
            Toast.makeText(context, "매칭된 상태일 때만 확인할 수 있습니다.", Toast.LENGTH_LONG).show()
            return
        }
        val dialog = CookieSpendDialogFragment(onUnlockSuccess = { unlockProfileUI() })
        dialog.show(parentFragmentManager, "CookieSpendDialog")
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