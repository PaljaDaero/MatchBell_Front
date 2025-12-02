package com.example.matchbell.feature.my

import android.os.Bundle
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

        // 1. 데이터 받기 (Bundle)
        val partnerName = arguments?.getString("partnerName") ?: "상대방"
        val score = arguments?.getInt("score") ?: 0
        // val desc = arguments?.getString("description") // 필요 시 사용

        // 2. UI 업데이트
        // 메시지: "XXX님과 XXX님의 궁합 결과" (내 이름은 로컬 저장소 등에서 가져와야 함, 일단 '나'로 표시하거나 생략)
        binding.tvMessage.text = "나와 ${partnerName}님의 궁합 결과"

        // 점수 표시
        binding.tvScoreCard.text = "${score}점!"

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