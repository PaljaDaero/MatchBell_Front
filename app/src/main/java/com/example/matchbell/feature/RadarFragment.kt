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