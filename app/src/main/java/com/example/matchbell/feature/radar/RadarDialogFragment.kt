package com.example.matchbell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.databinding.DialogRadarBinding

// 레이더 아이템 클릭 시 표시되는 '궁금해요' 팝업 다이얼로그
class RadarDialogFragment : DialogFragment() {

    private var _binding: DialogRadarBinding? = null
    private val binding get() = _binding!!

    // Bundle Arguments Key
    companion object {
        const val ARG_USER_ID = "user_id"
        const val ARG_NAME = "name"
        const val ARG_AFFILIATION = "affiliation"
        const val ARG_SCORE = "score" // [추가] 점수 키 정의
        const val ARG_ISMUTUAL = "is_mutual" // [추가] 상호 궁금해요 여부 키 정의 (필요 시)

        // [수정] score와 isMutual 인자를 받도록 newInstance 함수 시그니처 수정
        fun newInstance(
            id: Int,
            name: String,
            affiliation: String,
            score: Int,
            isMutual: Boolean
        ): RadarDialogFragment {
            return RadarDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_USER_ID, id)
                    putString(ARG_NAME, name)
                    putString(ARG_AFFILIATION, affiliation)
                    putInt(ARG_SCORE, score) // [추가] 점수 저장
                    putBoolean(ARG_ISMUTUAL, isMutual) // [추가] 상호 여부 저장
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 다이얼로그 배경을 투명하게 설정하여 커스텀 레이아웃을 사용
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        _binding = DialogRadarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments
        if (args == null) {
            dismiss()
            return
        }

        val userId = args.getInt(ARG_USER_ID, -1)
        val name = args.getString(ARG_NAME) ?: "Unknown"
        val affiliation = args.getString(ARG_AFFILIATION) ?: ""
        val score = args.getInt(ARG_SCORE, 0) // [추가] 점수 로드

        // 1. 데이터 바인딩
        binding.tvNickname.text = name // 닉네임만 표시
        binding.tvAffiliation.text = affiliation
        binding.tvScore.text = "궁합 ${score}점" // [추가] 궁합 점수 표시

        // 2. 버튼 및 아이콘 리스너 설정

        // 2-1. 궁금해요 버튼
        binding.btnLike.setOnClickListener {
            // TODO: 궁금해요 API 호출 로직 추가
            Toast.makeText(context, "${name}님에게 궁금해요 요청!", Toast.LENGTH_SHORT).show()

            // 버튼 비활성화 시뮬레이션 (한 번 보냈으면 다시 못 보냄)
            binding.btnLike.isEnabled = false
        }

        // 2-2. 닫기 버튼
        binding.btnDialogClose.setOnClickListener {
            dismiss()
        }

        // 2-3. 잠금 아이콘/프로필 클릭 (상세 프로필 잠금 해제 시도)
        binding.flProfile.setOnClickListener {
            // 창 닫고 상세 프로필 fragment로 이동
            dismiss()

            // [수정] RadarFragment에서 ProfileDetailFragment로 이동하는 Action ID를 사용
            // 단, DialogFragment는 NavController를 직접 소유하지 않으므로,
            // parentFragment.findNavController() 대신 findNavController()를 사용하거나,
            // 안전하게는 parentFragment의 NavController를 사용해야 합니다.

            // DialogFragment는 자신을 띄운 부모 Fragment의 NavController를 사용합니다.
            parentFragment?.findNavController()?.navigate(R.id.action_radarFragment_to_profileDetailFragment)

            // 또는 Fragment에서 직접 이동하는 Action ID를 사용 (이 경우 nav_graph 구조에 따라 오류 가능성 있음)
            // findNavController().navigate(R.id.action_radarFragment_to_profileDetailFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}