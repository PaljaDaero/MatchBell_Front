package com.example.matchbell.feature.my

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import com.example.matchbell.databinding.FragmentMyMatchingResultBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyMatchingResultFragment : Fragment() {
    private var _binding: FragmentMyMatchingResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyMatchingResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 데이터 받기 (MyMatchingFragment에서 보낸 키값과 똑같아야 함)
        val partnerName = arguments?.getString("partnerName") ?: "상대방"
        val score = arguments?.getInt("score") ?: 0

        // [수정] 성향 정보 받기
        val myTendency = arguments?.getString("myTendency") ?: "데이터 없음"
        val partnerTendency = arguments?.getString("partnerTendency") ?: "데이터 없음"

        Log.d("MatchingResult", "Received: $score / $myTendency / $partnerTendency")

        // 2. UI 업데이트
        binding.tvMessage.text = "나와 ${partnerName}님의 궁합 결과"
        binding.tvScoreCard.text = "${score}점!"

        // [수정] XML ID에 맞춰 텍스트 설정
        // 나의 성향
        binding.tvLabelMyTendency.text = "나의 성향:"
        binding.tvContentMyTendency.text = myTendency

        // 상대의 성향
        binding.tvLabelPartnerTendency.text = "${partnerName}님의 성향:"
        binding.tvContentPartnerTendency.text = partnerTendency

        // 3. 랭킹 버튼 리스너
        binding.btnRanking.setOnClickListener {
            findNavController().navigate(R.id.action_my_matching_result_to_ranking)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}