package com.example.matchbell.feature.profile

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.matchbell.databinding.DialogCookieSpendBinding
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CookieSpendDialogFragment(
    private val onConfirm: () -> Unit // [수정] 확인 버튼 눌렀을 때 실행할 함수
) : DialogFragment() {

    @Inject
    lateinit var authApi: AuthApi

    private var _binding: DialogCookieSpendBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCookieSpendBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 내 쿠키 잔액 조회 (단순 표시용)
        loadMyCookieBalance()

        // 2. 닫기
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // 3. 확인 (쿠키 사용)
        binding.btnConfirm.setOnClickListener {
            // [수정] 여기서 API 호출 안 함! 부모에게 위임
            onConfirm()
            dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadMyCookieBalance() {
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return
        lifecycleScope.launch {
            try {
                val response = authApi.getCookieBalance("Bearer $token")
                if (response.isSuccessful) {
                    val balance = response.body()?.balance ?: 0
                    binding.tvCookieCount.text = "${balance}개"
                } else {
                    binding.tvCookieCount.text = "조회 실패"
                }
            } catch (_: Exception) {
                binding.tvCookieCount.text = "-"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val params: WindowManager.LayoutParams? = dialog?.window?.attributes
        params?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = params
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}