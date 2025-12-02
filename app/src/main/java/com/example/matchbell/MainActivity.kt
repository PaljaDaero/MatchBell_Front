package com.example.matchbell

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 바텀 네비게이션 찾기
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        // 2. 전체 화면 설정
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // 3. NavController 연결
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_main) as? NavHostFragment

        if (navHostFragment != null) {
            val navController = navHostFragment.navController

            // (1) 기본 연결 (이걸 먼저 해야 기본 동작이 됩니다)
            navView.setupWithNavController(navController)

            // (2) 아이콘 색상 원본 유지
            navView.itemIconTintList = null

            // (3) ⭐⭐⭐ [추가됨] 탭 클릭 시 화면 초기화 로직 ⭐⭐⭐
            navView.setOnItemSelectedListener { item ->
                // 현재 탭과 다른 탭을 눌렀을 때만 작동
                if (item.itemId != navView.selectedItemId) {

                    // 네비게이션 옵션 설정: 상태 저장(Restore State) 끄기 -> 항상 새 화면!
                    val options = NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setRestoreState(false) // 👈 여기가 핵심! (이전 상태 복구 안 함)
                        .setPopUpTo(navController.graph.startDestinationId, false)
                        .build()

                    try {
                        // 해당 탭으로 이동 (초기화된 상태로)
                        navController.navigate(item.itemId, null, options)
                    } catch (e: Exception) {
                        // 에러 나면 기본 로직으로 처리
                        return@setOnItemSelectedListener NavigationUI.onNavDestinationSelected(item, navController)
                    }
                }
                true
            }

            // (4) ⭐⭐⭐ [추가됨] 이미 선택된 탭 다시 누를 때 (Re-select) 초기화 ⭐⭐⭐
            navView.setOnItemReselectedListener { item ->
                // 백스택을 비워서 첫 화면으로 돌아가게 함
                navController.popBackStack(item.itemId, false)
            }

            // (5) 특정 화면에서 하단 탭바 숨기기
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.splashFragment,
                    R.id.loginFragment,
                    R.id.signupTermsFragment,
                    R.id.signupInfoFragment,
                    R.id.chatRoomFragment,
                    R.id.permissionFragment,
                    R.id.profileSetupFragment,
                    R.id.settingsFragment,
                    R.id.profileEditFragment -> { // (설정-프로필수정도 추가하면 좋음)
                        navView.visibility = View.GONE
                    }
                    else -> {
                        navView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}