package com.example.matchbell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.databinding.FragmentMyBinding

class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        // 1. 설정 버튼 클릭
//        binding.ivSettings.setOnClickListener {
//            // R.id.fragment_my_settings로 이동 (nav_graph.xml에 정의된 ID 사용)
//            findNavController().navigate(R.id.fragment_my_settings)
//        }
//
//        // 2. 오늘의 운세 버튼 클릭
//        binding.btnTodayFortune.setOnClickListener {
//            findNavController().navigate(R.id.fragment_my_today_fortune)
//        }
//
//        // 3. 나만의 궁합 버튼 클릭
//        binding.btnMyMatching.setOnClickListener {
//            findNavController().navigate(R.id.fragment_my_matching)
//        }
//
//        // 4. 궁합 랭킹 버튼 클릭
//        binding.btnMyRanking.setOnClickListener {
//            findNavController().navigate(R.id.fragment_my_ranking)
//        }

        // 5. 한줄 소개 텍스트는 서버에서 받아와서 설정 (예시)
        // binding.tvComment.text = "서버에서 받은 한줄소개"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}