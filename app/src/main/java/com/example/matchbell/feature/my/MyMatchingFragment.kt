package com.example.matchbell.feature.my

import android.app.DatePickerDialog
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
import com.example.matchbell.feature.MatchingScore // [필수 import] 점수 계산기
import com.example.matchbell.feature.MyCompatRequest
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class MyMatchingFragment : Fragment() {

    @Inject
    lateinit var authApi: AuthApi

    // [추가] 궁합 점수 계산기 인스턴스 생성
    private val matchingScoreCalculator = MatchingScore()

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
        setupGenderCheckBoxes()
        setupDatePicker()

        binding.btnResult.setOnClickListener {
            requestMyMatching()
        }
    }
    private fun setupGenderCheckBoxes() {
        binding.cbMale.setOnClickListener {
            if (binding.cbMale.isChecked) {
                binding.cbFemale.isChecked = false
            } else {
                binding.cbMale.isChecked = true
            }
        }

        binding.cbFemale.setOnClickListener {
            if (binding.cbFemale.isChecked) {
                binding.cbMale.isChecked = false
            } else {
                binding.cbFemale.isChecked = true
            }
        }
    }

    private fun setupDatePicker() {
        binding.etDob.isFocusable = false // 키보드 안 뜨게 설정
        binding.etDob.isClickable = true
        binding.etDob.setOnClickListener {
            val calendar = Calendar.getInstance()

            // 기본값 설정
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // 선택된 날짜 처리
                val formattedMonth = (selectedMonth + 1).toString().padStart(2, '0')
                val formattedDay = selectedDay.toString().padStart(2, '0')
                binding.etDob.setText("$selectedYear-$formattedMonth-$formattedDay")
            }, year, month, day)

            // --- 날짜 제한 설정 (2000년 ~ 2021년) ---

            // 최소 날짜: 2000년 1월 1일
            val minDate = Calendar.getInstance()
            minDate.set(2000, 0, 1) // Month는 0부터 시작 (0 = 1월)
            datePickerDialog.datePicker.minDate = minDate.timeInMillis

            // 최대 날짜: 2021년 12월 31일
            val maxDate = Calendar.getInstance()
            maxDate.set(2021, 11, 31) // 11 = 12월
            datePickerDialog.datePicker.maxDate = maxDate.timeInMillis

            datePickerDialog.show()
        }
    }

    private fun requestMyMatching() {
        val name = binding.etName.text.toString().trim()
        val birthRaw = binding.etDob.text.toString().trim()

        if (name.isEmpty() || birthRaw.isEmpty()) {
            Toast.makeText(context, "정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 날짜 파싱 (숫자만 추출해서 YYYY-MM-DD)
        val dateParts = birthRaw.split(Regex("[^0-9]")).filter { it.isNotEmpty() }
        if (dateParts.size < 3) return
        val formattedBirth = "${dateParts[0]}-${dateParts[1].padStart(2, '0')}-${dateParts[2].padStart(2, '0')}"

        val gender = if (binding.cbMale.isChecked) "MALE" else "FEMALE"
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return

        lifecycleScope.launch {
            try {
                val request = MyCompatRequest(name, gender, formattedBirth)

                // API 호출
                val response = authApi.postMyCompat("Bearer $token", request)

                if (response.isSuccessful) {
                    val result = response.body()

                    if (result != null) {
                        // API 명세에 따르면 점수와 성향 데이터는 'compat' 객체 안에 있습니다.
                        val compatData = result.compat // MyCompatResponse의 compat 필드

                        // [수정 1] 점수 계산: compat 객체 내의 finalScore, stressScore 사용
                        val realScore = matchingScoreCalculator.calculateCompositeScore(
                            finalScore = compatData.finalScore,   // compatData.finalScore로 수정
                            stressScore = compatData.stressScore  // compatData.stressScore로 수정
                        )

                        // [수정 2] 성향 데이터 처리: tendency0, tendency1 사용 및 문자열로 변환
                        // tendency0/1이 List<String> 형태이고 null일 수 있으므로 안전하게 처리합니다.
                        val myTendencyText = compatData.tendency0
                            ?.joinToString(", ") // List를 ", " 구분자로 문자열로 변환
                            ?: "분석된 성향이 없습니다" // 데이터가 null이거나 빈 리스트일 경우 기본값

                        val partnerTendencyText = compatData.tendency1
                            ?.joinToString(", ") // List를 ", " 구분자로 문자열로 변환
                            ?: "분석된 성향이 없습니다" // 데이터가 null이거나 빈 리스트일 경우 기본값

                        Log.d("MyMatching", "Calculated Score: $realScore (S:${compatData.finalScore}, T:${compatData.stressScore})")

                        // 결과 화면으로 이동
                        val bundle = Bundle().apply {
                            putString("partnerName", name)
                            putInt("score", realScore) // 계산된 점수 전달 (이제 올바른 값을 참조)
                            putString("myTendency", myTendencyText) // 올바르게 처리된 성향 문자열
                            putString("partnerTendency", partnerTendencyText) // 올바르게 처리된 성향 문자열
                        }
                        findNavController().navigate(R.id.action_my_matching_result, bundle)
                    }
                } else {
                    // [핵심 수정] 401 응답 코드를 확인하여 토큰 만료 처리
                    if (response.code() == 401) {
                        context?.let { ctx ->
                            TokenManager.handleTokenExpired(ctx, findNavController())
                        }
                    } else {
                        Toast.makeText(context, "분석 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MyMatching", "Error", e)
                Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}