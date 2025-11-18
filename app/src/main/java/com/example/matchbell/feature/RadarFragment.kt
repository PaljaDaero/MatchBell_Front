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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.matchbell.databinding.FragmentRadarBinding
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class RadarUser(
    val id: Int,
    val score: Int,
    val name: String
)

class RadarFragment : Fragment() {

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

        // 백엔드 구축 완료 시 제거할 부분
        val dummyData = listOf(
            RadarUser(1, 95, "UserA"),
            RadarUser(2, 80, "UserB"),
            RadarUser(3, 72, "UserC"),
            RadarUser(4, 60, "UserD")
        )

        binding.radarContainer.post {
            addRadarItems(dummyData)
        }
    }
    private fun startRadarPulseAnimation() {
        if (_binding == null) return

        // 1. 느린 속도 유지
        val FADE_DURATION = 500L
        val DELAY_BETWEEN = 500L
        val PAUSE_DURATION = 1500L

        val ALPHA_RING_1 = 1f
        val ALPHA_RING_2 = 0.8f
        val ALPHA_RING_3 = 0.5f

        val ring1In = ObjectAnimator.ofFloat(binding.radarRing1, "alpha", ALPHA_RING_1).setDuration(FADE_DURATION)
        val ring2In = ObjectAnimator.ofFloat(binding.radarRing2, "alpha", ALPHA_RING_2).setDuration(FADE_DURATION)
        val ring3In = ObjectAnimator.ofFloat(binding.radarRing3, "alpha", ALPHA_RING_3).setDuration(FADE_DURATION)
        val ring1Out = ObjectAnimator.ofFloat(binding.radarRing1, "alpha", 0f).setDuration(FADE_DURATION)
        val ring2Out = ObjectAnimator.ofFloat(binding.radarRing2, "alpha", 0f).setDuration(FADE_DURATION)
        val ring3Out = ObjectAnimator.ofFloat(binding.radarRing3, "alpha", 0f).setDuration(FADE_DURATION)

        radarAnimation = AnimatorSet().apply {

            // radar ring 순차적으로 ON
            play(ring1In).after(PAUSE_DURATION)
            play(ring2In).after(ring1In).after(DELAY_BETWEEN)
            play(ring3In).after(ring2In).after(DELAY_BETWEEN)

            // radar ring 3이 OFF 되면서 1, 2도 함께 OFF
            play(ring1Out).with(ring2Out).with(ring3Out).after(ring3In).after(PAUSE_DURATION)


            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (isAdded) {
                        radarAnimation?.start()
                    }
                }
            })
        }
        radarAnimation?.start()
    }

    private fun addRadarItems(users: List<RadarUser>) {
        if (_binding == null) return

        val container = binding.radarContainer

        // 뷰가 아직 그려지지 않았으면(크기가 0이면) 함수 종료
        if (container.width == 0 || container.height == 0) return

        // --- 겹침 방지 로직 시작 ---

        // 1. 하트 아이템의 크기 (70dp)를 픽셀(px)로 변환
        val itemSizePx = (70 * resources.displayMetrics.density)

        // 2. 두 아이템이 겹치지 않기 위한 최소 거리 (아이템 크기 + 약간의 여유)
        //    (성능을 위해 제곱값으로 비교합니다)
        val minSeparationSquared = (itemSizePx * itemSizePx) * 1.2f // 1.2배 여유

        // 3. 레이더 컨테이너의 정중앙 좌표
        val centerX = container.width / 2f
        val centerY = container.height / 2f

        // 4. 아이템이 생성될 수 있는 최소/최대 반지름
        // 최소: 중앙 타겟 + 하트 절반 크기 + 여유 공간
        val minRadius = (binding.ivRadarCenter.width / 2f) + (itemSizePx / 2f) + 20f // 20px 여유
        // 최대: 화면 끝 - 하트 절반 크기
        val maxRadius = (Math.min(container.width, container.height) / 2f) - (itemSizePx / 2f)

        // 5. 이미 배치된 아이템들의 "중앙" 좌표를 저장할 리스트
        val placedItemCenters = mutableListOf<Pair<Float, Float>>()

        // 6. 겹치지 않는 자리를 찾기 위한 최대 시도 횟수
        val MAX_ATTEMPTS = 30

        // --- 겹침 방지 로직 끝 ---


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

            // 겹치지 않는 자리를 찾을 때까지 반복 (최대 30번)
            do {
                attempts++
                // 1. 랜덤 위치 계산
                val angle = Math.toRadians(Random.nextDouble(0.0, 360.0))
                val radius = Random.nextDouble(minRadius.toDouble(), maxRadius.toDouble())
                xOffset = (radius * cos(angle)).toFloat()
                yOffset = (radius * sin(angle)).toFloat()

                // 2. 새로 계산된 아이템의 중앙 좌표
                newX = centerX + xOffset
                newY = centerY + yOffset

                // 3. 기존에 배치된 아이템들과 겹치는지 확인
                isOverlapping = placedItemCenters.any { (placedX, placedY) ->
                    val dx = newX - placedX
                    val dy = newY - placedY
                    // 두 점 사이의 거리 제곱이 최소 거리 제곱보다 작으면 겹친 것
                    val distanceSquared = (dx * dx) + (dy * dy)
                    distanceSquared < minSeparationSquared
                }

            } while (isOverlapping && attempts < MAX_ATTEMPTS)

            // 4. 찾은 위치를 리스트에 추가 (다음 아이템이 참고하도록)
            placedItemCenters.add(Pair(centerX + xOffset, centerY + yOffset))

            // 5. 아이템 생성 및 뷰에 추가
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_radar_target, container, false)

            val tvScore = itemView.findViewById<TextView>(R.id.tv_score)
            val tvClick = itemView.findViewById<TextView>(R.id.tv_click)

            tvScore.text = user.score.toString()
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

            itemView.setOnClickListener {
                tvClick.clearAnimation()
                tvClick.visibility = View.GONE
            }

            container.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        radarAnimation?.cancel()
        radarAnimation = null
        _binding = null
    }
}