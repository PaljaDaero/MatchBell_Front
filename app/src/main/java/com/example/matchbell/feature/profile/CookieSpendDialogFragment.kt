package com.example.matchbell.feature.profile

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.matchbell.databinding.DialogCookieSpendBinding
import com.example.matchbell.feature.CookieSpendRequest
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CookieSpendDialogFragment(
    private val onUnlockSuccess: () -> Unit
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

        // 다이얼로그 모서리를 둥글게 하기 위해 배경 투명 처리 (XML 배경색 적용을 위함)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 다이얼로그 켜지자마자 현재 내 쿠키 잔액 조회
        loadMyCookieBalance()

        // 2. 닫기 버튼 (흰색)
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // 3. 쿠키 사용 버튼 (핑크색)
        binding.btnConfirm.setOnClickListener {
            spendCookie()
        }
    }

    // [API] 내 쿠키 잔액 조회
    @SuppressLint("SetTextI18n")
    private fun loadMyCookieBalance() {
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return

        lifecycleScope.launch {
            try {
                // AuthApi에 getCookieBalance가 정의되어 있어야 함
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

    // [API] 쿠키 사용 요청
    private fun spendCookie() {
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return

        lifecycleScope.launch {
            try {
                // 쿠키 10개 차감
                val request = CookieSpendRequest(amount = 10, reason = "상세 프로필 조회")
                val response = authApi.spendCookie("Bearer $token", request)

                if (response.isSuccessful) {
                    val newBalance = response.body()?.balance
                    Toast.makeText(context, "잠금 해제 완료! (남은 쿠키: $newBalance)", Toast.LENGTH_SHORT).show()

                    // 성공 콜백 실행 -> ProfileDetailFragment에서 화면 잠금 해제됨
                    onUnlockSuccess()
                    dismiss()
                } else {
                    // 400 Bad Request 등 (잔액 부족)
                    Toast.makeText(context, "쿠키가 부족하거나 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("CookieDialog", "Error", e)
                Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 다이얼로그 크기 조절 (가로 폭을 화면의 90%로 설정)
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