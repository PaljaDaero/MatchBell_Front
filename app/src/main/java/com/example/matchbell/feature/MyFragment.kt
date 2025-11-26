package com.example.matchbell.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
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
            findNavController().navigate(R.id.action_myFragment_to_settingsFragment)
        }

        // 2. 쿠키 버튼
        binding.btnCookie.setOnClickListener {
            showCookieDialog()
        }

        // 3. 나만의 궁합 버튼
        binding.btnMyMatching.setOnClickListener {
            findNavController().navigate(R.id.action_my_matching)
        }

        // 4. 궁합 랭킹 버튼
        binding.btnMyRanking.setOnClickListener {
            findNavController().navigate(R.id.action_my_ranking)
        }

        // 5. 서버 데이터 연동 예시
        // binding.tvName.text = "김명지"
    }

    private fun showCookieDialog() {
        // 1. AlertDialog Builder 생성
        val builder = AlertDialog.Builder(requireContext())

        // 2. Custom Layout 인플레이트 (R.layout.dialog_ranking 사용)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cookie, null)
        builder.setView(dialogView)

        // 3. Dialog 생성
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 4. 다이얼로그 내 버튼 클릭 리스너 설정
        val chargeButton = dialogView.findViewById<Button>(R.id.btn_dialog_charge)
        val closeButton = dialogView.findViewById<Button>(R.id.btn_dialog_close)

        // 쿠키 충전 버튼 로직
        chargeButton.setOnClickListener {
            // findNavController().navigate(R.id.action_show_charge_screen)
        }

        // 닫기 버튼 로직
        closeButton.setOnClickListener {
            dialog.dismiss() // 다이얼로그 닫기
        }

        // 5. 다이얼로그 표시
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}