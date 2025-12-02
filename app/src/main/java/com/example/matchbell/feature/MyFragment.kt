//
//package com.example.matchbell.feature.my
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.fragment.findNavController
//import com.bumptech.glide.Glide
//import com.example.matchbell.R
//import com.example.matchbell.databinding.FragmentMyBinding
//import com.example.matchbell.feature.auth.ProfileViewModel
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.launch
//
//@AndroidEntryPoint
//class MyFragment : Fragment() {
//
//    private var _binding: FragmentMyBinding? = null
//    private val binding get() = _binding!!
//
//    // [추가됨] 백엔드와 통신할 뷰모델 연결
//    private val viewModel: ProfileViewModel by viewModels()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentMyBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // 1. 화면 켜지면 서버에서 내 정보 가져오기
//        viewModel.fetchMyProfile(requireContext())
//
//        // 2. 서버 데이터가 오면 화면에 보여주기 (ViewBinding 사용)
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.myProfile.collect { user ->
//                if (user != null) {
//                    // [주의] XML 파일(fragment_my.xml)에 이 ID들이 진짜 있는지 확인하세요!
//                    // 없으면 빨간 줄이 뜰 수 있으니, 실제 ID로 바꿔주셔야 합니다.
//
//                    binding.tvName.text = user.nickname // 닉네임 표시
//                    // binding.tvMbti.text = user.intro // (만약 MBTI나 소개 텍스트뷰가 있다면 주석 해제)
//
//                    // 프로필 사진 로딩
//                    if (!user.avatarUrl.isNullOrEmpty()) {
//                        Glide.with(this@MyFragment)
//                            .load(user.avatarUrl)
//                            .placeholder(R.drawable.ic_profile_default)
//                        // [주의] XML에 이미지뷰 ID가 ivProfile 인지 ivProfileImage 인지 확인!
//                        // .into(binding.ivProfile)
//                    }
//                }
//            }
//        }
//
//        // 3. 설정 버튼(톱니바퀴) 클릭
//        binding.ivSettings.setOnClickListener {
//            try {
//                // ⬇️ [수정됨] 바로 수정화면으로 가는 게 아니라, '설정 화면'으로 이동!
//                findNavController().navigate(R.id.action_myFragment_to_settingsFragment)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        binding.btnMyMatching.setOnClickListener {
//            // findNavController().navigate(R.id.action_my_to_matching)
//        }
//
//        binding.btnMyRanking.setOnClickListener {
//            // findNavController().navigate(R.id.action_my_to_ranking)
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}