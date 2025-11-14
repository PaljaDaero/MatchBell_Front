package com.example.matchbell.feature.auth // 님의 정확한 패키지 경로

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R // 님의 R 파일 경로
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {

    // 💡 이 부분에 onViewCreated 함수를 통째로 추가하세요.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Coroutine(비서)에게 2초 대기 후 이동하라는 작업을 지시
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000) // 2초 (2000ms) 대기

            // 2. Navigation을 사용해 다음 화면(로그인)으로 이동
            if (isAdded) {
                // R.id.action_splashFragment_to_loginFragment는 nav_graph.xml에 추가한 이동 경로 ID입니다.
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            }
        }
    }
}