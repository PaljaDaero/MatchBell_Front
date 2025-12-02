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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.matchbell.R
import com.example.matchbell.data.model.CookieChargeRequest
import com.example.matchbell.databinding.FragmentMyBinding
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

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

        loadProfileData()
    }

    private fun loadCookieBalance(cookieCountTextView: TextView) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = authApi.getCookieBalance()
                if (response.isSuccessful) {
                    val balance = response.body()?.balance ?: 0
                    cookieCountTextView.text = balance.toString()
                } else {
                    Log.e("MyFragment", "Failed to load cookie balance: ${response.code()}")
                    cookieCountTextView.text = "Error"
                    Toast.makeText(requireContext(), "잔액 로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MyFragment", "Network error when loading cookie balance", e)
                cookieCountTextView.text = "N/W Error"
                Toast.makeText(requireContext(), "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = authApi.getMyProfile()

                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        binding.tvNickname.text = profile.nickname
                        val age = calculateKoreanAge(profile.birth)
                        binding.tvAge.text = age.toString()
                        binding.tvJob.text = profile.job
                        binding.tvComment.text = profile.intro ?: "작성된 소개가 없습니다."
                        binding.tvPersonality.text = profile.tendency
                        binding.tvName.text = profile.nickname

                        // ⬇️⬇️⬇️ [추가됨] 프로필 이미지 로딩 (서버 주소 처리) ⬇️⬇️⬇️
                        val avatarUrl = profile.avatarUrl
                        if (!avatarUrl.isNullOrEmpty()) {

                            // 1. 서버 주소가 없으면 붙여주기
                            // (주의: http://3.239.45.21:8080 부분은 실제 서버 주소와 일치해야 함)
                            val fullUrl = if (avatarUrl.startsWith("http")) {
                                avatarUrl
                            } else {
                                "http://3.239.45.21:8080${avatarUrl}"
                            }

                            // 2. Glide로 이미지 띄우기
                            Glide.with(this@MyFragment)
                                .load(fullUrl)
                                .placeholder(R.drawable.ic_profile_default) // 로딩 중 이미지
                                .error(R.drawable.ic_profile_default)       // 에러 시 이미지
                                .into(binding.ivProfileImage) // XML의 이미지뷰 ID 확인 (ivProfileImage인지 ivProfile인지)
                        }
                        // ⬆️⬆️⬆️ [추가됨] ⬆️⬆️⬆️

                    } else {
                        Toast.makeText(requireContext(), "프로필 데이터가 비어있습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("MyFragment", "Failed to load profile: ${response.code()}")
                    Toast.makeText(requireContext(), "프로필 로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    clearProfileFields()
                }
            } catch (e: Exception) {
                Log.e("MyFragment", "Network error when loading profile", e)
                Toast.makeText(requireContext(), "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                clearProfileFields()
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
            Log.e("MyFragment", "Error calculating Korean age from date: $birthDateString", e)
            -1
        }
    }

    private fun clearProfileFields() {
        binding.tvNickname.text = "로딩 실패"
        binding.tvAge.text = "--"
        binding.tvJob.text = "--"
        binding.tvComment.text = "정보를 불러올 수 없습니다."
        binding.tvPersonality.text = "--"
        binding.tvName.text = "--"
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
                    val response = authApi.chargeCookie(chargeRequest)
                    if (response.isSuccessful) {
                        val newBalance = response.body()?.balance ?: 0
                        cookieCount.text = newBalance.toString()
                        Toast.makeText(requireContext(), "$chargeAmount 쿠키가 충전되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("MyFragment", "Failed to charge cookie: ${response.code()}")
                        Toast.makeText(requireContext(), "쿠키 충전 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("MyFragment", "Network error when charging cookie", e)
                    Toast.makeText(requireContext(), "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}