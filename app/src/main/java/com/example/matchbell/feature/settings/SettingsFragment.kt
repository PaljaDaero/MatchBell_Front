package com.example.matchbell.feature.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import com.example.matchbell.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels()
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        // 1. 로그아웃 버튼 클릭
        binding.btnLogout.setOnClickListener {
            showDialog("로그아웃", "정말 로그아웃 하시겠습니까?") {
                viewModel.logout()
            }
        }

        // 2. 탈퇴 버튼 클릭
        binding.btnWithdraw.setOnClickListener {
            showDialog("회원 탈퇴", "탈퇴 시 모든 정보가 삭제됩니다.\n정말 탈퇴하시겠습니까?") {
                viewModel.withdraw()
            }
        }

        // 3. 결과 감지 (성공하면 로그인 화면으로 이동)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.collect { event ->
                when (event) {
                    "LOGOUT_SUCCESS" -> {
                        Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    }
                    "WITHDRAW_SUCCESS" -> {
                        Toast.makeText(context, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    }
                    else -> {
                        Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인") { _, _ -> onConfirm() }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun navigateToLogin() {
        // 로그인 화면으로 이동하면서, 뒤로가기 버튼 눌러도 다시 설정 화면으로 못 오게 처리
        // R.id.loginFragment ID가 맞는지 확인하세요!
        findNavController().navigate(R.id.loginFragment, null, androidx.navigation.NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}