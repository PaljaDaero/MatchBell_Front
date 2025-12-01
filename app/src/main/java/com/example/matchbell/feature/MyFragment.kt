package com.example.matchbell.feature

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
import com.example.matchbell.R
import com.example.matchbell.data.model.CookieChargeRequest
import com.example.matchbell.databinding.FragmentMyBinding
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint // Hilt 사용 시 필수
class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    // [수정] AuthApi를 주입받기 위한 Inject
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

        // 1. 설정 버튼 클릭 (주석 해제 및 연결)
        binding.ivSettings.setOnClickListener {
            findNavController().navigate(R.id.action_myFragment_to_settingsFragment)
        }

        // 2. 쿠키 버튼
        binding.btnCookie.setOnClickListener {
            showCookieDialog()
        }

        // 3. 나만의 궁합 버튼
        binding.btnMyMatching.setOnClickListener {
            findNavController().navigate(R.id.action_my_matching)
        }

        // 4. 궁합 랭킹 버튼
        binding.btnMyRanking.setOnClickListener {
            findNavController().navigate(R.id.action_my_ranking)
        }

        // 5. 서버 데이터 연동 함수
        loadProfileData()
    }

    private fun loadCookieBalance(cookieCountTextView: TextView) {
        // [수정] viewModelScope 대신 Fragment에서 안전하게 사용할 수 있는 lifecycleScope 사용
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = authApi.getCookieBalance()
                if (response.isSuccessful) {
                    val balance = response.body()?.balance ?: 0
                    cookieCountTextView.text = balance.toString()
                } else {
                    // 에러 처리 (예: 401 Unauthorized, 500 Server Error 등)
                    Log.e("MyFragment", "Failed to load cookie balance: ${response.code()}")
                    cookieCountTextView.text = "Error"
                    Toast.makeText(requireContext(), "잔액 로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // 네트워크 에러 처리
                Log.e("MyFragment", "Network error when loading cookie balance", e)
                cookieCountTextView.text = "N/W Error"
                Toast.makeText(requireContext(), "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // [추가] 프로필 정보를 불러와 UI에 바인딩하는 함수
    private fun loadProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = authApi.getMyProfile()

                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        // 닉네임 설정
                        binding.tvNickname.text = profile.nickname

                        // 나이 계산 및 설정
                        val age = calculateKoreanAge(profile.birth) // 새 함수 호출
                        binding.tvAge.text = age.toString()

                        // 직업 설정
                        binding.tvJob.text = profile.job

                        // 자기소개(Comment) 설정
                        binding.tvComment.text = profile.intro ?: "작성된 소개가 없습니다."

                        // Personality(Tendency) 설정
                        binding.tvPersonality.text = profile.tendency

                        // 이름(tvName)은 API에 없으므로 (가정: 닉네임을 대신 사용하거나, 별도 API 필요)
                        binding.tvName.text = profile.nickname

                    } else {
                        Toast.makeText(requireContext(), "프로필 데이터가 비어있습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("MyFragment", "Failed to load profile: ${response.code()}")
                    Toast.makeText(requireContext(), "프로필 로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    // UI 초기화 또는 에러 상태 표시
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
            // "yyyy-MM-dd"에서 연도(yyyy)만 추출
            val birthYearString = birthDateString.substring(0, 4)
            val birthYear = birthYearString.toInt()

            // 현재 연도 가져오기 (Calendar 또는 LocalDate 사용 가능)
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            // 한국식 나이 = 현재 연도 - 출생 연도 + 1
            currentYear - birthYear + 1
        } catch (e: Exception) {
            Log.e("MyFragment", "Error calculating Korean age from date: $birthDateString", e)
            -1 // 에러 시 -1 반환
        }
    }

    // [추가] 필드 초기화 함수
    private fun clearProfileFields() {
        binding.tvNickname.text = "로딩 실패"
        binding.tvAge.text = "--"
        binding.tvJob.text = "--"
        binding.tvComment.text = "정보를 불러올 수 없습니다."
        binding.tvPersonality.text = "--"
        binding.tvName.text = "--"
    }

    // [수정] showCookieDialog 함수 로직 수정
    private fun showCookieDialog() {
        // 1. AlertDialog Builder 생성
        val builder = AlertDialog.Builder(requireContext())

        // 2. Custom Layout 인플레이트 (R.layout.dialog_ranking 사용)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cookie, null)
        builder.setView(dialogView)

        // 3. Dialog 생성
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 4. 다이얼로그 내 잔액 및 버튼 클릭 리스너 설정
        val cookieCount = dialogView.findViewById<TextView>(R.id.tv_cookie_count)
        val chargeButton = dialogView.findViewById<Button>(R.id.btn_dialog_charge)
        val closeButton = dialogView.findViewById<Button>(R.id.btn_dialog_close)

        // [수정] 초기 잔액 불러오기
        loadCookieBalance(cookieCount)

        // [수정] 쿠키 충전 버튼 로직
        chargeButton.setOnClickListener {
            // 충전 API 호출 (예시로 10 쿠키 충전 요청)
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

        // 닫기 버튼 로직
        closeButton.setOnClickListener {
            dialog.dismiss() // 다이얼로그 닫기
        }

        // 5. 다이얼로그 표시
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}