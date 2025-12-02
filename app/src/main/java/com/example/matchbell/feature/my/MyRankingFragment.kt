package com.example.matchbell.feature.my

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.matchbell.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class MyRankingFragment : Fragment() {

    // private var binding: ActivityRankingBinding? = null // ViewBinding 사용 시

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // R.layout.activity_ranking 파일을 인플레이트하여 뷰를 반환
        return inflater.inflate(R.layout.fragment_my_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 랭킹 그래프를 찾고 데이터를 설정하는 로직 추가
        try {
            val chart = view.findViewById<BarChart>(R.id.chart_ranking_comparison)
            setupBarChart(chart)
        } catch (e: Exception) {
            // BarChart 관련 오류 (예: 라이브러리 로드 실패) 발생 시 로그 기록
            Log.e("RankingFragment", "Error setting up BarChart: ${e.message}")
        }
    }

    /**
     * MPAndroidChart BarChart에 더미 데이터를 설정하는 함수 (평균 비교 그래프 구현)
     */
    private fun setupBarChart(chart: BarChart) {
        // 더미 데이터 생성 (전체 이용자의 궁합 점수 분포를 시뮬레이션)
        val entries = ArrayList<BarEntry>()
        val percentageOfUser = 5 // 명지님의 궁합 점수가 5번째 구간에 있다고 가정 (예: 70~80점 구간)

        for (i in 0..9) {
            // i는 궁합 점수 구간 (0~10, 10~20, ... 90~100)을 나타낸다고 가정
            // y 값은 해당 구간의 사용자 수 (더미 값)
            val yValue = (Math.random() * 80 + 20).toFloat()
            entries.add(BarEntry(i.toFloat(), yValue))
        }

        val dataSet = BarDataSet(entries, "전체 사용자 궁합 분포")
        dataSet.color = Color.parseColor("#FFC0CB") // 연한 핑크색으로 설정
        dataSet.valueTextColor = Color.BLACK
        dataSet.setDrawValues(false) // 값(숫자) 표시 숨김

        // 명지님 자신의 위치를 표시하기 위해 해당 막대의 색상을 강조
        if (entries.size > percentageOfUser) {
            val colors = ArrayList<Int>()
            for (i in 0 until entries.size) {
                if (i == percentageOfUser) {
                    // 명지님의 위치를 강조하는 진한 핑크색
                    colors.add(Color.parseColor("#FF69B4"))
                } else {
                    // 기본 색상
                    colors.add(Color.parseColor("#FFC0CB"))
                }
            }
            dataSet.colors = colors
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.8f // 막대의 너비

        chart.data = barData

        // 차트 스타일 설정
        chart.description.isEnabled = false // 차트 설명 비활성화
        chart.setDrawGridBackground(false) // 격자 배경 비활성화
        chart.setPinchZoom(false) // 줌 기능 비활성화
        chart.setTouchEnabled(false) // 터치 상호작용 비활성화
        chart.legend.isEnabled = false // 범례 비활성화

        // X축 (궁합 점수 구간) 설정
        val xAxis = chart.xAxis
        xAxis.setDrawGridLines(false) // 격자 선 제거
        xAxis.setDrawAxisLine(false) // 축 라인 제거
        xAxis.setDrawLabels(false) // 레이블 (숫자) 제거
        xAxis.setCenterAxisLabels(true)
        xAxis.axisMinimum = -0.5f
        xAxis.axisMaximum = 9.5f

        // Y축 (왼쪽) 설정
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawAxisLine(false)
        leftAxis.setDrawLabels(false)
        leftAxis.axisMinimum = 0f

        // Y축 (오른쪽) 설정
        chart.axisRight.isEnabled = false

        chart.invalidate() // 차트 새로고침
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // binding = null // 바인딩 사용 시 해제
    }
}