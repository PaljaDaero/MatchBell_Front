package com.example.matchbell

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // XML의 ID와 일치시켜야 하므로 R.id.nav_view를 사용합니다.
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        // 전체 화면 설정 (System Bar 영역까지 확장)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // FragmentManager를 통해 NavHostFragment를 안전하게 찾습니다.
        // (XML에서 <fragment> 태그 사용 시 안전하지만, 타이밍 이슈 방지용 null 체크 유지)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_main) as? NavHostFragment

        if (navHostFragment != null) {
            val navController = navHostFragment.navController
            navView.setupWithNavController(navController)

            // 아이콘 색상을 테마색이 아닌 원본 색상으로 표시
            navView.itemIconTintList = null

            // 특정 화면에서 하단 탭바 숨기기
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.splashFragment,
                    R.id.loginFragment,
                    R.id.signupTermsFragment,
                    R.id.signupInfoFragment,
                    R.id.chatRoomFragment,R.id.permissionFragment,R.id.profileSetupFragment -> {
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