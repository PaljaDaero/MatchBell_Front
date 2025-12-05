package com.example.matchbell.feature.my

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.matchbell.databinding.FragmentMyRankingBinding
import com.example.matchbell.feature.RankingItem
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.AuthApi
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyRankingFragment : Fragment() {

    @Inject
    lateinit var authApi: AuthApi

    private var _binding: FragmentMyRankingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRankingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadRankingData()
    }

    private fun loadRankingData() {
        val context = context ?: return
        val token = TokenManager.getAccessToken(context)

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = authApi.getRanking("Bearer $token", limit = 100)

                if (response.isSuccessful) {
                    val body = response.body()
                    val list = body?.items ?: emptyList()

                    val myScore = body?.myBestCompositeScore ?: 0
                    val myPercent = body?.myPercentile

                    // 1. Top 3 목록 갱신
                    updateTop3UI(list)

                    // 2. 그래프 갱신
                    setupBarChart(binding.chartRankingComparison, list, myScore)

                    // 3. [추가] 리액션 텍스트 갱신
                    updatePercentileUI(myPercent)

                } else {
                    Log.e("RankingFragment", "Load Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("RankingFragment", "Network Error", e)
            }
        }
    }

    // [추가] 퍼센트에 따른 리액션 표시 함수
    private fun updatePercentileUI(percentile: Double?) {
        if (percentile == null || percentile == 0.0) {
            // 데이터가 없거나 랭킹 산정 불가 시
            binding.tvPercentileReaction.text = "아직 데이터가 나타나지 않았어요"
            return
        }

        val reaction = when {
            percentile <= 10.0 -> "대단해요! 최고의 궁합입니다"
            percentile <= 30.0 -> "훌륭해요! 아주 좋은 관계네요"
            percentile <= 60.0 -> "적당하네요! 좋은 친구가 될 수 있어요"
            else -> "보통이에요. 서로를 더 알아가 보세요"
        }

        // 예: "상위 12.5% - 훌륭해요! ..."
        binding.tvPercentileReaction.text = "상위 ${String.format("%.1f", percentile)}% - $reaction"
    }

    private fun updateTop3UI(list: List<RankingItem>) {
        if (list.isNotEmpty()) {
            val item = list[0]
            binding.tvName1.text = "${item.userANickname} & ${item.userBNickname} (${item.compositeScore}점)"
        } else {
            binding.tvName1.text = "데이터 없음"
        }

        if (list.size > 1) {
            val item = list[1]
            binding.tvName2.text = "${item.userANickname} & ${item.userBNickname} (${item.compositeScore}점)"
        } else {
            binding.tvName2.text = "-"
        }

        if (list.size > 2) {
            val item = list[2]
            binding.tvName3.text = "${item.userANickname} & ${item.userBNickname} (${item.compositeScore}점)"
        } else {
            binding.tvName3.text = "-"
        }
    }

    private fun setupBarChart(chart: BarChart, list: List<RankingItem>, myScore: Int) {
        if (list.isEmpty()) return

        val entries = ArrayList<BarEntry>()
        val distribution = IntArray(10) { 0 }

        list.forEach { item ->
            val scoreIndex = (item.compositeScore / 10).coerceIn(0, 9)
            distribution[scoreIndex]++
        }

        val myScoreIndex = (myScore / 10).coerceIn(0, 9)

        for (i in 0..9) {
            entries.add(BarEntry(i.toFloat(), distribution[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "점수 분포")

        val colors = ArrayList<Int>()
        for (i in 0..9) {
            if (i == myScoreIndex && myScore > 0) {
                colors.add(Color.parseColor("#EF4444")) // 내 점수 구간 (진하게)
            } else {
                colors.add(Color.parseColor("#E0E0E0")) // 나머지 (연하게)
            }
        }
        dataSet.colors = colors

        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                // 내 점수 구간이 아니면 숫자 숨김 (깔끔하게)
                // (MPAndroidChart는 값을 순회하며 이 함수를 호출하는데,
                // 정확한 인덱스를 알기 어려우므로 여기선 전체 숨김 처리하거나
                // 필요하다면 모든 값을 보여줄 수도 있습니다. 깔끔함을 위해 숨김 추천)
                return ""
            }
        })
        dataSet.setDrawValues(false)

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        chart.data = barData
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setPinchZoom(false)
        chart.setTouchEnabled(false)
        chart.legend.isEnabled = false

        chart.animateY(1000)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.textColor = Color.GRAY
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in 0..9) "${index * 10}" else ""
            }
        }

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawAxisLine(false)
        leftAxis.setDrawLabels(false)
        leftAxis.axisMinimum = 0f

        chart.axisRight.isEnabled = false

        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}