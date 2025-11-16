package com.example.matchbell.feature.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R // ✅ R 클래스를 import 해야 합니다.

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 이 프래그먼트의 레이아웃을 설정합니다.
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 2초 후에 로그인 화면으로 이동하는 로직 추가
        Handler(Looper.getMainLooper()).postDelayed({
            // NavController를 사용하여 화면을 전환합니다.
            // nav_graph.xml에 정의된 전역 action의 ID를 사용합니다.
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }, 2000) // 2000 milliseconds = 2초
    }
}
