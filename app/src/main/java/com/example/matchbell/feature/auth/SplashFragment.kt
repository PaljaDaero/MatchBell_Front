package com.example.matchbell.feature.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R

// [추가됨] 토큰 매니저 import
import com.example.matchbell.feature.auth.TokenManager

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({

            // ⭐⭐⭐ [수정됨] 자동 로그인 체크 로직 ⭐⭐⭐
            val context = requireContext()
            val savedToken = TokenManager.getAccessToken(context)

            if (savedToken != null) {
                // 1. 토큰이 있다? -> 이미 로그인 된 상태 -> 메인(레이더)으로 이동
                // (주의: LoginFragment에서 radarFragment로 이동할 때 썼던 ID와 동일해야 함)
                findNavController().navigate(R.id.radarFragment)

                // 만약 nav_graph에 화살표를 만들었다면 아래처럼 쓰는 게 정석입니다.
                // findNavController().navigate(R.id.action_splashFragment_to_radarFragment)
            } else {
                // 2. 토큰이 없다? -> 로그인 필요 -> 로그인 화면으로 이동
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            }

        }, 2000)
    }
}