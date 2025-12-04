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
    private val onConfirm: () -> Unit
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
        // 배경을 투명하게 (둥근 모서리 적용을 위해)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        // [중요] Fragment 속성으로 취소 방지
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 내 쿠키 잔액 조회
        loadMyCookieBalance()

        // 2. 취소 버튼 (닫기)
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // 3. 사용 버튼 (확인)
        binding.btnConfirm.setOnClickListener {
            dismiss()
            onConfirm()
        }
    }

    /**
     * [핵심 해결책] onStart에서 다이얼로그 속성을 강제 설정
     * 화면에 나타나는 시점에 물리적으로 터치를 막아버립니다.
     */
    override fun onStart() {
        super.onStart()
        dialog?.let {
            // 1. 뒤로가기 버튼 무시
            it.setCancelable(false)

            // 2. 바깥 영역 터치 무시
            it.setCanceledOnTouchOutside(false)

            // 3. 다이얼로그 크기 설정 (가로 90%)
            val params = it.window?.attributes
            params?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            params?.height = WindowManager.LayoutParams.WRAP_CONTENT
            it.window?.attributes = params
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}