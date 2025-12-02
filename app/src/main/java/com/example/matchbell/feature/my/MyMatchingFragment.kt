package com.example.matchbell.feature.my

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import com.example.matchbell.databinding.FragmentMyMatchingBinding
import com.example.matchbell.feature.MyCompatRequest
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyMatchingFragment : Fragment() {

    @Inject
    lateinit var authApi: AuthApi

    private var _binding: FragmentMyMatchingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyMatchingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 성별 체크박스 중복 선택 방지 (하나만 선택되게)
        setupGenderCheckBoxes()

        // 2. 결과 보기 버튼 클릭 (API 호출)
        binding.btnResult.setOnClickListener {
            requestMyMatching()
        }
    }

    private fun setupGenderCheckBoxes() {
        // 남성 체크 시 -> 여성 체크 해제
        binding.cbMale.setOnClickListener {
            if (binding.cbMale.isChecked) {
                binding.cbFemale.isChecked = false
            } else {
                // 둘 다 꺼지는 거 방지 (최소 하나는 선택) - 필요 시 제거 가능
                binding.cbMale.isChecked = true
            }
        }

        // 여성 체크 시 -> 남성 체크 해제
        binding.cbFemale.setOnClickListener {
            if (binding.cbFemale.isChecked) {
                binding.cbMale.isChecked = false
            } else {
                binding.cbFemale.isChecked = true
            }
        }
    }

    private fun requestMyMatching() {
        val name = binding.etName.text.toString().trim()
        val birth = binding.etDob.text.toString().trim()

        // 유효성 검사
        if (name.isEmpty() || birth.isEmpty()) {
            Toast.makeText(context, "이름과 생년월일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 성별 결정
        val gender = if (binding.cbMale.isChecked) "MALE" else "FEMALE"

        // 날짜 형식 간단 체크 (YYYY-MM-DD 형식 권장)
        // 실제로는 정규식 등으로 엄격하게 체크하거나 DatePicker를 쓰는 게 좋습니다.
        if (!birth.contains("-") || birth.length < 8) {
            Toast.makeText(context, "생년월일은 YYYY-MM-DD 형식으로 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val token = context?.let { TokenManager.getAccessToken(it) } ?: return

        lifecycleScope.launch {
            try {
                val request = MyCompatRequest(name, gender, birth)
                val response = authApi.postMyCompat("Bearer $token", request)

                if (response.isSuccessful) {
                    val result = response.body()
                    val score = result?.score ?: 0

                    // 결과 화면으로 데이터 전달하며 이동
                    val bundle = Bundle().apply {
                        putString("partnerName", name)
                        putInt("score", score)
                        // 설명이 있다면 전달
                        putString("description", result?.description)
                    }
                    findNavController().navigate(R.id.action_my_matching_result, bundle)

                } else {
                    Toast.makeText(context, "분석 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MyMatching", "Network Error", e)
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}