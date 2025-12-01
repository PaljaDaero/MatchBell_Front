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
        const val ARG_SCORE = "score"
        const val ARG_IS_MUTUAL = "is_mutual_curiosity"

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
                    putInt(ARG_SCORE, score)
                    putBoolean(ARG_IS_MUTUAL, isMutual)
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

        val name = args.getString(ARG_NAME) ?: "Unknown"
        val affiliation = args.getString(ARG_AFFILIATION) ?: ""
        val score = args.getInt(ARG_SCORE)
        val isMutual = args.getBoolean(ARG_IS_MUTUAL)

        // 1. 데이터 바인딩
        binding.tvNickname.text = "닉네임 : $name"
        binding.tvAffiliation.text = affiliation
        binding.tvScore.text = "나와의 궁합 ${score}점!"

        // 2. 버튼 및 아이콘 리스너 설정

        // 2-1. 궁금해요 버튼
        binding.btnLike.setOnClickListener {
            Toast.makeText(context, "${name}님이 궁금해요!", Toast.LENGTH_SHORT).show()

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
            findNavController().navigate(R.id.action_radarFragment_to_profileDetailFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}