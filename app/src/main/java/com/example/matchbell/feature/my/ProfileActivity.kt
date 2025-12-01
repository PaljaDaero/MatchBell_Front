package com.example.matchbell.feature.my

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.matchbell.R

class ProfileActivity : AppCompatActivity() {

    private var isUnlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_profile_detail)

        // 1. 뷰 참조 가져오기
        val cvProfileImage = findViewById<CardView>(R.id.cv_profile_image)
        val ivLockOverlay = findViewById<ImageView>(R.id.iv_lock_overlay)
        val ivProfileReal = findViewById<ImageView>(R.id.iv_profile_real)

        val ivLockRegion = findViewById<ImageView>(R.id.iv_lock_region)
        val tvRegionReal = findViewById<TextView>(R.id.tv_region_real)

        // 위치를 바꿔야 할 '생년월일' 라벨과 그 위의 요소들
        val labelBirth = findViewById<TextView>(R.id.label_birth)
        val ivLockBirth = findViewById<ImageView>(R.id.iv_lock_birth)
        val tvBirthReal = findViewById<TextView>(R.id.tv_birth_real)

        // 위치를 바꿔야 할 '직업' 라벨
        val labelJob = findViewById<TextView>(R.id.label_job)

        // 2. 클릭 리스너 설정
        cvProfileImage.setOnClickListener {
            if (!isUnlocked) {
                unlockProfile(
                    ivLockOverlay, ivProfileReal,
                    ivLockRegion, tvRegionReal,
                    ivLockBirth, tvBirthReal,
                    labelBirth, labelJob // 라벨 뷰도 함께 전달
                )
            }
        }
    }

    private fun unlockProfile(
        lockOverlay: ImageView, realImage: ImageView,
        lockRegion: ImageView, realRegion: TextView,
        lockBirth: ImageView, realBirth: TextView,
        labelBirth: TextView, labelJob: TextView
    ) {
        // 상태 변경
        isUnlocked = true

        // 1. 프로필 이미지: 자물쇠 숨기고 실제 사진 보이기
        lockOverlay.visibility = View.GONE
        realImage.visibility = View.VISIBLE

        // 2. 지역 정보: 자물쇠 숨기고 텍스트 보이기
        lockRegion.visibility = View.GONE
        realRegion.visibility = View.VISIBLE

        // 3. 생년월일 정보: 자물쇠 숨기고 텍스트 보이기
        lockBirth.visibility = View.GONE
        realBirth.visibility = View.VISIBLE

        // 4. [핵심] 라벨 위치 재설정 (ConstraintLayout 파라미터 조작)

        // (A) '생년월일' 라벨을 -> '실제 지역 텍스트(tvRegionReal)' 밑으로 붙임
        val paramsBirth = labelBirth.layoutParams as ConstraintLayout.LayoutParams
        paramsBirth.topToBottom = realRegion.id // R.id.tv_region_real
        labelBirth.layoutParams = paramsBirth

        // (B) '직업' 라벨을 -> '실제 생년월일 텍스트(tvBirthReal)' 밑으로 붙임
        val paramsJob = labelJob.layoutParams as ConstraintLayout.LayoutParams
        paramsJob.topToBottom = realBirth.id // R.id.tv_birth_real
        labelJob.layoutParams = paramsJob
    }
}