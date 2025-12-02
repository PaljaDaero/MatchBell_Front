package com.example.matchbell.feature.my

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.matchbell.R
import com.example.matchbell.data.model.CookieChargeRequest
import com.example.matchbell.databinding.FragmentMyBinding
import com.example.matchbell.feature.auth.ProfileViewModel
import com.example.matchbell.feature.auth.TokenManager // [필수] 토큰 매니저
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var authApi: AuthApi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivSettings.setOnClickListener {
            findNavController().navigate(R.id.action_myFragment_to_settingsFragment)
        }

        binding.btnCookie.setOnClickListener {
            showCookieDialog()
        }

        binding.btnMyMatching.setOnClickListener {
            findNavController().navigate(R.id.action_my_matching)
        }

        binding.btnMyRanking.setOnClickListener {
            findNavController().navigate(R.id.action_my_ranking)
        }

        // 프로필 데이터 로드
        viewModel.fetchMyProfile(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myProfile.collect { user ->
                if (user != null) {
                    binding.tvNickname.text = user.nickname
                    binding.tvJob.text = user.job ?: "직업 없음"
                    binding.tvComment.text = user.intro ?: "소개가 없습니다."
                    binding.tvPersonality.text = user.tendency ?: "성향 정보 없음"
                    binding.tvName.text = user.nickname

                    if (!user.birth.isNullOrEmpty() && user.birth.length >= 4) {
                        val age = calculateKoreanAge(user.birth)
                        binding.tvAge.text = age.toString()
                    } else {
                        binding.tvAge.text = "--"
                    }

                    // 이미지 로딩 (서버 주소 보정)
                    if (!user.avatarUrl.isNullOrEmpty()) {
                        val fullUrl = if (user.avatarUrl.startsWith("http")) user.avatarUrl else "http://3.239.45.21:8080${user.avatarUrl}"
                        Glide.with(this@MyFragment).load(fullUrl).placeholder(R.drawable.ic_profile_default).error(R.drawable.ic_profile_default).into(binding.ivProfileImage)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { msg ->
                if (msg.contains("실패") || msg.contains("오류")) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    clearProfileFields()
                }
            }
        }
    }

    private fun loadCookieBalance(cookieCountTextView: TextView) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // ⬇️⬇️⬇️ [수정됨] 토큰 가져와서 헤더에 넣기 ⬇️⬇️⬇️
                val token = TokenManager.getAccessToken(requireContext())
                if (token.isNullOrEmpty()) {
                    cookieCountTextView.text = "로그인 필요"
                    return@launch
                }

                // AuthApi 호출 시 토큰 전달 (401 에러 해결)
                val response = authApi.getCookieBalance("Bearer $token")
                // ⬆️⬆️⬆️ [수정됨] ⬆️⬆️⬆️

                if (response.isSuccessful) {
                    val balance = response.body()?.balance ?: 0
                    cookieCountTextView.text = balance.toString()
                } else {
                    Log.e("MyFragment", "Failed to load cookie balance: ${response.code()}")
                    cookieCountTextView.text = "Error"
                }
            } catch (e: Exception) {
                Log.e("MyFragment", "Network error", e)
                cookieCountTextView.text = "N/W Error"
            }
        }
    }

    private fun calculateKoreanAge(birthDateString: String): Int {
        return try {
            val birthYearString = birthDateString.substring(0, 4)
            val birthYear = birthYearString.toInt()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            currentYear - birthYear + 1
        } catch (e: Exception) {
            Log.e("MyFragment", "Error calculating age", e)
            0
        }
    }

    private fun clearProfileFields() {
        binding.tvNickname.text = ""
        binding.tvAge.text = ""
        binding.tvJob.text = ""
        binding.tvComment.text = ""
        binding.tvPersonality.text = ""
        binding.tvName.text = ""
    }

    private fun showCookieDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cookie, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cookieCount = dialogView.findViewById<TextView>(R.id.tv_cookie_count)
        val chargeButton = dialogView.findViewById<Button>(R.id.btn_dialog_charge)
        val closeButton = dialogView.findViewById<Button>(R.id.btn_dialog_close)

        loadCookieBalance(cookieCount)

        chargeButton.setOnClickListener {
            val chargeAmount = 10
            val chargeRequest = CookieChargeRequest(amount = chargeAmount, reason = "Test Charge")

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // ⬇️⬇️⬇️ [수정됨] 충전 시에도 토큰 가져오기 및 헤더에 넣기 ⬇️⬇️⬇️
                    val token = TokenManager.getAccessToken(requireContext())
                    if (token.isNullOrEmpty()) {
                        Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val response = authApi.chargeCookie("Bearer $token", chargeRequest)
                    // ⬆️⬆️⬆️ [수정됨] ⬆️⬆️⬆️

                    if (response.isSuccessful) {
                        val newBalance = response.body()?.balance ?: 0
                        cookieCount.text = newBalance.toString()
                        Toast.makeText(requireContext(), "$chargeAmount 쿠키 충전됨", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "충전 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            }
        }

        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}