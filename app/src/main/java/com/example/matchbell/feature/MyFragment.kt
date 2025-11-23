package com.example.matchbell.feature.my // 패키지명은 본인 프로젝트에 맞게 확인해주세요 (feature.my 등)

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import com.example.matchbell.databinding.FragmentMyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hilt 사용 시 필수
class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

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
            // nav_graph.xml에 등록한 '화살표 ID' 또는 '도착지 ID'를 적어야 합니다.
            // 아래 코드는 "MyFragment에서 SettingsFragment로 가는 화살표"를 타라! 는 뜻입니다.
            findNavController().navigate(R.id.action_myFragment_to_settingsFragment)
        }

        /* 아래 버튼들은 아직 연결할 화면(Fragment)을 안 만드셨다면
           앱이 죽을 수 있으므로, 일단 주석 상태로 두거나 Toast 메시지만 띄우세요.
        */

        // 2. 오늘의 운세 버튼
        binding.btnTodayFortune.setOnClickListener {
            // findNavController().navigate(R.id.action_my_to_fortune) // 나중에 추가
        }

        // 3. 나만의 궁합 버튼
        binding.btnMyMatching.setOnClickListener {
            // findNavController().navigate(R.id.action_my_to_matching) // 나중에 추가
        }

        // 4. 궁합 랭킹 버튼
        binding.btnMyRanking.setOnClickListener {
            // findNavController().navigate(R.id.action_my_to_ranking) // 나중에 추가
        }

        // 5. 서버 데이터 연동 예시
        // binding.tvName.text = "김명지"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}