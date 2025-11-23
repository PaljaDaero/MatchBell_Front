package com.example.matchbell

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import android.view.View
import androidx.core.view.WindowCompat

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        bottomNavView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment, R.id.loginFragment, R.id.signupTermsFragment, R.id.signupInfoFragment, R.id.permissionFragment, R.id.profileSetupFragment,R.id.findPasswordFragment,R.id.resetPasswordFragment -> {
                    bottomNavView.visibility = View.GONE
                }
                else -> {
                    bottomNavView.visibility = View.VISIBLE
                }
            }
        }
    }
}//교수님이 주신 소스코드 코틀린으로 바꿔서 넣기 (csv파일) 리소스저장한데에 넣고, 하면 됨.변경해달라할때 입력하라고 나와있는데 그걸 DB에서 가져오는 걸로 (키로 뽑아쓰는 느낌)
