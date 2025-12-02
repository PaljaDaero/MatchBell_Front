package com.example.matchbell

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.matchbell.databinding.DialogRadarBinding
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RadarDialogFragment : DialogFragment() {

    @Inject
    lateinit var authApi: AuthApi

    private var _binding: DialogRadarBinding? = null
    private val binding get() = _binding!!

    // [수정] Long 타입으로 변경
    private var targetUserId: Long = -1L

    companion object {
        const val ARG_USER_ID = "user_id"
        const val ARG_NAME = "name"
        const val ARG_AFFILIATION = "affiliation"
        const val ARG_SCORE = "score"

        // [수정] id 인자를 Long으로 변경
        fun newInstance(
            id: Long,
            name: String,
            affiliation: String,
            score: Int,
        ): RadarDialogFragment {
            return RadarDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USER_ID, id) // putLong 사용
                    putString(ARG_NAME, name)
                    putString(ARG_AFFILIATION, affiliation)
                    putInt(ARG_SCORE, score)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        // [수정] getLong 사용
        targetUserId = args.getLong(ARG_USER_ID, -1L)
        val name = args.getString(ARG_NAME) ?: "Unknown"
        val affiliation = args.getString(ARG_AFFILIATION) ?: ""
        val score = args.getInt(ARG_SCORE, 0)

        Log.d("RadarDialog", "Open Dialog for UserID: $targetUserId") // [로그 확인]

        binding.tvNickname.text = name
        binding.tvAffiliation.text = affiliation
        binding.tvScore.text = "궁합 ${score}점"

        // 1. 이미 보낸 요청인지 서버에서 확인
        checkIfAlreadyLiked()

        // 2. 궁금해요 버튼 클릭
        binding.btnLike.setOnClickListener {
            sendLikeRequest(name)
        }

        binding.btnDialogClose.setOnClickListener {
            dismiss()
        }

        binding.flProfile.setOnClickListener {
            dismiss()
            val bundle = Bundle().apply {
                putLong("targetUserId", targetUserId)
            }
            parentFragment?.findNavController()?.navigate(
                R.id.action_radarFragment_to_profileDetailFragment,
                bundle
            )
        }
    }

    // 내가 보낸 목록 조회
    private fun checkIfAlreadyLiked() {
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return

        lifecycleScope.launch {
            try {
                // "내가 보낸 궁금해요 목록" 가져오기
                val response = authApi.getSentCurious("Bearer $token")

                if (response.isSuccessful) {
                    val sentList = response.body() ?: emptyList()

                    // [로그] 받아온 목록 확인
                    Log.d("RadarDialog", "Sent List Size: ${sentList.size}")
                    sentList.forEach { Log.d("RadarDialog", "Sent ID: ${it.userId}") }

                    // 비교 (Long vs Long)
                    val isAlreadySent = sentList.any { it.userId == targetUserId }

                    if (isAlreadySent) {
                        Log.d("RadarDialog", "User $targetUserId is in the list! Disable button.")
                        disableLikeButton()
                    } else {
                        Log.d("RadarDialog", "User $targetUserId not found in list.")
                    }
                } else {
                    Log.e("RadarDialog", "GetSentCurious Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("RadarDialog", "Check error", e)
            }
        }
    }

    // 궁금해요 전송
    private fun sendLikeRequest(targetName: String) {
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return

        lifecycleScope.launch {
            try {
                val response = authApi.sendLike("Bearer $token", targetUserId)

                if (response.isSuccessful) {
                    Toast.makeText(context, "${targetName}님에게 궁금해요를 보냈습니다!", Toast.LENGTH_SHORT).show()
                    disableLikeButton() // 전송 성공 시 즉시 비활성화
                } else {
                    val errorMsg = response.errorBody()?.string() ?: ""
                    Log.e("RadarDialog", "SendLike Failed: ${response.code()} / $errorMsg")

                    // 이미 보낸 경우 (409 Conflict 등 서버 응답에 따라 처리)
                    if (response.code() == 409 || errorMsg.contains("이미")) {
                        Toast.makeText(context, "이미 요청을 보낸 상대입니다.", Toast.LENGTH_SHORT).show()
                        disableLikeButton()
                    } else {
                        Toast.makeText(context, "전송 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun disableLikeButton() {
        binding.btnLike.isEnabled = false
        binding.btnLike.text = "보냄"
        binding.btnLike.alpha = 0.5f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}