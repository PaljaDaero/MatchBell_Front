package com.example.matchbell

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.matchbell.databinding.FragmentRadarBinding
import com.example.matchbell.feature.MatchingScore
import com.example.matchbell.network.AuthApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class RadarUser(
    val id: Long, // userId
    val calculatedScore: Int, // MatchingScore를 통해 계산된 최종 점수
    val name: String, // nickname
    val affiliation: String, // region + age (UI 표시용으로 가공)
    var isLikeSent: Boolean = false,
    val avatarUrl: String? // 프로필 이미지 로드용
)

@AndroidEntryPoint // Hilt 사용
class RadarFragment : Fragment() {

    @Inject
    lateinit var authApi: AuthApi // ChatApi 주입

    private val matchingScoreCalculator = MatchingScore() // 궁합 점수 계산기 인스턴스

    private var _binding: FragmentRadarBinding? = null
    private val binding get() = _binding!!

    private var radarAnimation: AnimatorSet? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRadarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startRadarPulseAnimation()

        // [추가] 뷰가 완전히 그려진 후 데이터 로드 시작
        binding.radarContainer.post {
            loadRadarData()
        }
    }

    // [추가] 레이더 데이터 로드 및 점수 계산 로직
    private fun loadRadarData() {
        lifecycleScope.launch {
            try {
                val response = authApi.getRadarUsers()

                if (response.isSuccessful) {
                    val usersData = response.body()?.users ?: emptyList()
                    val displayUsers = usersData.map { userData ->

                        // 1. MatchingScore를 사용하여 최종 점수 계산
                        val finalScore = userData.finalScore
                        val stressScore = userData.stressScore

                        // T_min, T_p95는 임시 상수를 사용 (MatchingScore.kt에 하드코딩되어 있다고 가정)
                        val calculatedScore = matchingScoreCalculator.calculateCompositeScore(
                            finalScore = finalScore,
                            stressScore = stressScore)

                        // 2. UI 표시용 데이터 가공 (소속/나이/지역)
                        val affiliationText = "${userData.region}, ${userData.age}세"

                        // 3. Display 모델 생성
                        RadarUser(
                            id = userData.userId,
                            calculatedScore = calculatedScore,
                            name = userData.nickname,
                            affiliation = affiliationText,
                            avatarUrl = userData.avatarUrl
                        )
                    }
                    // 4. 아이템 컨테이너에 배치 시작
                    addRadarItems(displayUsers)
                } else {
                    // 서버 에러 (400 Bad Request 포함) 처리
                    val errorMessage = response.errorBody()?.string() ?: "알 수 없는 에러"
                    Toast.makeText(context, "레이더 로드 실패: $errorMessage", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun addRadarItems(users: List<RadarUser>) {
        if (_binding == null) return

        val container = binding.radarContainer

        if (container.width == 0 || container.height == 0) return

        val itemSizePx = (70 * resources.displayMetrics.density)

        val minSeparationSquared = (itemSizePx * itemSizePx) * 1.2f

        val centerX = container.width / 2f
        val centerY = container.height / 2f

        val minRadius = (binding.ivRadarCenter.width / 2f) + (itemSizePx / 2f) + 20f
        val maxRadius = (Math.min(container.width, container.height) / 2f) - (itemSizePx / 2f)

        val placedItemCenters = mutableListOf<Pair<Float, Float>>()

        val MAX_ATTEMPTS = 30

        val blinkAnim = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 500
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }

        users.forEach { user ->

            var newX: Float
            var newY: Float
            var xOffset: Float = 0f
            var yOffset: Float = 0f
            var attempts = 0
            var isOverlapping: Boolean

            do {
                attempts++
                val angle = Math.toRadians(Random.nextDouble(0.0, 360.0))
                val radius = Random.nextDouble(minRadius.toDouble(), maxRadius.toDouble())
                xOffset = (radius * cos(angle)).toFloat()
                yOffset = (radius * sin(angle)).toFloat()

                newX = centerX + xOffset
                newY = centerY + yOffset

                isOverlapping = placedItemCenters.any { (placedX, placedY) ->
                    val dx = newX - placedX
                    val dy = newY - placedY
                    val distanceSquared = (dx * dx) + (dy * dy)
                    distanceSquared < minSeparationSquared
                }

            } while (isOverlapping && attempts < MAX_ATTEMPTS)

            placedItemCenters.add(Pair(centerX + xOffset, centerY + yOffset))

            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_radar_target, container, false)

            val tvScore = itemView.findViewById<TextView>(R.id.tv_score)
            val tvClick = itemView.findViewById<TextView>(R.id.tv_click)

            // [수정] 계산된 최종 점수 사용
            tvScore.text = user.calculatedScore.toString()
            tvClick.startAnimation(blinkAnim)

            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            }
            itemView.layoutParams = params

            itemView.translationX = xOffset
            itemView.translationY = yOffset

            // [수정] TODO: 아이템 클릭 시 팝업 띄우는 로직 추가
            itemView.setOnClickListener {
                tvClick.clearAnimation()
                tvClick.visibility = View.GONE

                // RadarDialogFragment 인스턴스 생성 및 표시
                val dialog = RadarDialogFragment.newInstance(
                    user.id.toInt(), // Int로 변환 (Bundle에 Int로 넣는 기존 코드 유지)
                    user.name,
                    user.affiliation,
                    user.calculatedScore, // [추가] 점수 전달
                    false // isMutual은 API에 없으므로 일단 false
                )
                // dialog.show(parentFragmentManager, "RadarDialog") // 주석 처리된 이전 코드를 대체
                dialog.show(parentFragmentManager, "RadarDialog")
            }

            container.addView(itemView)
        }
    }

    private fun startRadarPulseAnimation() {
        if (_binding == null) return

        val FADE_DURATION = 500L
        val DELAY_BETWEEN = 500L
        val PAUSE_DURATION = 200L

        // 링이 나타날 때의 최종 알파 값 (가장 진함: 1 -> 가장 연함: 3)
        val ALPHA_RING1_MAX = 0.5f
        val ALPHA_RING2_MAX = 0.5f
        val ALPHA_RING3_MAX = 0.5f

        // 링이 나타나는 애니메이션 (alpha 0 -> MAX)
        val ring1In = ObjectAnimator.ofFloat(binding.radarRing1, "alpha", 0f, ALPHA_RING1_MAX).setDuration(FADE_DURATION)
        val ring2In = ObjectAnimator.ofFloat(binding.radarRing2, "alpha", 0f, ALPHA_RING2_MAX).setDuration(FADE_DURATION)
        val ring3In = ObjectAnimator.ofFloat(binding.radarRing3, "alpha", 0f, ALPHA_RING3_MAX).setDuration(FADE_DURATION)

        // 링이 사라지는 애니메이션 (alpha MAX -> 0)
        val ring1Out = ObjectAnimator.ofFloat(binding.radarRing1, "alpha", ALPHA_RING1_MAX, 0f).setDuration(FADE_DURATION)
        val ring2Out = ObjectAnimator.ofFloat(binding.radarRing2, "alpha", ALPHA_RING2_MAX, 0f).setDuration(FADE_DURATION)
        val ring3Out = ObjectAnimator.ofFloat(binding.radarRing3, "alpha", ALPHA_RING3_MAX, 0f).setDuration(FADE_DURATION)

        radarAnimation = AnimatorSet().apply {

            play(ring1In)
            play(ring2In).after(ring1In).after(DELAY_BETWEEN)
            play(ring3In).after(ring2In).after(DELAY_BETWEEN)

            play(ring1Out).with(ring2Out).with(ring3Out).after(ring3In).after(PAUSE_DURATION)


            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (isAdded) {
                        radarAnimation?.start()
                    }
                }
            })
        }

        binding.radarRing1.alpha = 0f
        binding.radarRing2.alpha = 0f
        binding.radarRing3.alpha = 0f

        radarAnimation?.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        radarAnimation?.cancel()
        radarAnimation = null
        _binding = null
    }
}