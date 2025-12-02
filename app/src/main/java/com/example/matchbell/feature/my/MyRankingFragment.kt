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
import com.example.matchbell.network.AuthApi
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
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

        // 1. 랭킹 데이터 로드
        loadRankingData()
    }

    private fun loadRankingData() {
        lifecycleScope.launch {
            try {
                // Header 없이 요청 (명세에 따름)
                val response = authApi.getRanking(limit = 100)

                if (response.isSuccessful) {
                    val list = response.body()?.items ?: emptyList()

                    // UI 업데이트 (Top 3)
                    updateTop3UI(list)

                    // 차트 업데이트 (전체 데이터 분포)
                    setupBarChart(binding.chartRankingComparison, list)
                } else {
                    Toast.makeText(context, "랭킹 로드 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("RankingFragment", "Network Error", e)
            }
        }
    }

    private fun updateTop3UI(list: List<RankingItem>) {
        // 1위
        if (list.isNotEmpty()) {
            val item = list[0]
            binding.tvName1.text = "${item.userANickname} & ${item.userBNickname} (${item.finalScore.toInt()}점)"
            // 프로필 이미지가 있다면 Glide로 로드 (여기선 기본값 유지)
        } else {
            binding.tvName1.text = "데이터 없음"
        }

        // 2위
        if (list.size > 1) {
            val item = list[1]
            binding.tvName2.text = "${item.userANickname} & ${item.userBNickname} (${item.finalScore.toInt()}점)"
        } else {
            binding.tvName2.text = "-"
        }

        // 3위
        if (list.size > 2) {
            val item = list[2]
            binding.tvName3.text = "${item.userANickname} & ${item.userBNickname} (${item.finalScore.toInt()}점)"
        } else {
            binding.tvName3.text = "-"
        }
    }

    private fun setupBarChart(chart: BarChart, list: List<RankingItem>) {
        if (list.isEmpty()) return

        val entries = ArrayList<BarEntry>()

        // 점수대별 분포 계산 (예: 90점대, 80점대...)
        val distribution = IntArray(10) { 0 } // 0~9 인덱스 (0~10점, ... 90~100점)

        list.forEach { item ->
            val scoreIndex = (item.finalScore / 10).toInt().coerceIn(0, 9)
            distribution[scoreIndex]++
        }

        // 그래프 데이터 생성
        for (i in 0..9) {
            entries.add(BarEntry(i.toFloat(), distribution[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "궁합 점수 분포")
        dataSet.color = Color.parseColor("#FFC0CB")
        dataSet.valueTextColor = Color.BLACK
        dataSet.setDrawValues(false)

        // 강조 색상 (가장 많은 구간 등 로직 추가 가능)
        // 여기서는 예시로 가장 높은 점수대를 강조
        val colors = ArrayList<Int>()
        for (i in 0..9) {
            if (i == 9) colors.add(Color.parseColor("#FF69B4")) // 90점대 강조
            else colors.add(Color.parseColor("#FFC0CB"))
        }
        dataSet.colors = colors

        val barData = BarData(dataSet)
        barData.barWidth = 0.8f

        chart.data = barData
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setPinchZoom(false)
        chart.setTouchEnabled(false)
        chart.legend.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawLabels(false)

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f
        chart.axisRight.isEnabled = false

        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}