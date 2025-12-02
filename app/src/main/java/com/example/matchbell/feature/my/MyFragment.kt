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

import androidx.fragment.app.viewModels // [필수] 뷰모델

import androidx.lifecycle.lifecycleScope

import androidx.navigation.fragment.findNavController

import com.bumptech.glide.Glide

import com.example.matchbell.R

import com.example.matchbell.data.model.CookieChargeRequest

import com.example.matchbell.databinding.FragmentMyBinding

import com.example.matchbell.feature.auth.ProfileViewModel // [필수] 프로필 뷰모델

import com.example.matchbell.network.AuthApi

import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.launch

import java.util.Calendar

import javax.inject.Inject



@AndroidEntryPoint

class MyFragment : Fragment() {



    private var _binding: FragmentMyBinding? = null

    private val binding get() = _binding!!



// [추가] 프로필 데이터를 관리할 뷰모델 연결

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



// 1. 버튼 리스너들 (기존 코드 유지)

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



// 2. 데이터 로드 시작

        loadProfileData() // 아래 수정된 함수가 호출됨

    }



// [수정됨] 프로필 정보를 불러와 UI에 바인딩하는 함수 (ViewModel 사용)

    private fun loadProfileData() {



// 1. 서버에 데이터 요청

        viewModel.fetchMyProfile(requireContext())



// 2. 데이터가 도착하면 화면 갱신 (Observe)

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.myProfile.collect { user ->

                if (user != null) {

// 텍스트 설정

                    binding.tvNickname.text = user.nickname

                    binding.tvJob.text = user.job ?: "직업 없음"

                    binding.tvComment.text = user.intro ?: "소개가 없습니다."

                    binding.tvPersonality.text = user.tendency ?: "성향 정보 없음"

                    binding.tvName.text = user.nickname



// 나이 계산

                    if (!user.birth.isNullOrEmpty() && user.birth.length >= 4) {

                        val age = calculateKoreanAge(user.birth)

                        binding.tvAge.text = age.toString()

                    } else {

                        binding.tvAge.text = "--"

                    }



// ⬇️⬇️⬇️ [핵심 추가] 이미지 로딩 (서버 주소 보정) ⬇️⬇️⬇️

                    if (!user.avatarUrl.isNullOrEmpty()) {

                        val fullUrl = if (user.avatarUrl.startsWith("http")) {

                            user.avatarUrl

                        } else {

// [주의] 실제 서버 주소와 포트가 맞는지 확인하세요!

                            "http://3.239.45.21:8080${user.avatarUrl}"

                        }



                        Log.d("MyFragment", "이미지 로딩 시도: $fullUrl")



                        Glide.with(this@MyFragment)

                            .load(fullUrl)

                            .placeholder(R.drawable.ic_profile_default)

                            .error(R.drawable.ic_profile_default)

// [주의] XML 파일의 이미지뷰 ID가 ivProfileImage 인지 ivProfile 인지 꼭 확인하세요!

                            .into(binding.ivProfileImage)

                    }

// ⬆️⬆️⬆️ [핵심 추가] ⬆️⬆️⬆️



                } else {

// 데이터가 없을 때 (로딩 중이거나 에러)

// clearProfileFields() // 필요하면 호출

                }

            }

        }



// 3. 에러 메시지 처리

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

// (기존 코드 그대로 유지)

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                val response = authApi.getCookieBalance()

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

// (기존 코드 그대로 유지)

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

// (기존 코드 그대로 유지)

        binding.tvNickname.text = "로딩 실패"

        binding.tvAge.text = "--"

        binding.tvJob.text = "--"

        binding.tvComment.text = "정보를 불러올 수 없습니다."

        binding.tvPersonality.text = "--"

        binding.tvName.text = "--"

    }



    private fun showCookieDialog() {

// (기존 코드 그대로 유지 - 내용 생략)

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

                        Toast.makeText(requireContext(), "$chargeAmount 쿠키 충전됨", Toast.LENGTH_SHORT).show()

                    } else {

                        Toast.makeText(requireContext(), "충전 실패", Toast.LENGTH_SHORT).show()

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