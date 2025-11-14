package com.example.matchbell// (본인 패키지 이름에 맞게 수정하세요!)

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import android.view.View

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 방금 수정한 껍데기(activity_main.xml)를 화면에 띄움
        setContentView(R.layout.activity_main)

        // 2. 껍데기에서 '탭 바'와 '빈 공간(지도 관리자)'을 찾아옴
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)

        // 3. 둘을 연결! (이제 탭을 누르면 화면이 바뀝니다)
        bottomNavView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // 스플래시와 로그인 화면일 때 탭 바 숨김
                R.id.splashFragment, R.id.loginFragment, R.id.signupTermsFragment, R.id.signupInfoFragment -> {
                    bottomNavView.visibility = View.GONE
                }
                else -> {
                    // 나머지 탭 화면 (레이더, 매칭 등)에서는 보임
                    bottomNavView.visibility = View.VISIBLE
                }
            }
        }
    }
}
