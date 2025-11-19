package com.example.matchbell.feature.settings

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 프로필 수정 버튼 클릭 -> 프로필 수정 화면으로 이동
        view.findViewById<TextView>(R.id.btn_profile_edit).setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_profileEditFragment)
        }

        // 2. 비밀번호 변경 버튼 클릭
        view.findViewById<TextView>(R.id.btn_pw_change).setOnClickListener {
            // TODO: 비밀번호 변경 화면 연결
            Toast.makeText(context, "비밀번호 변경 화면으로 이동", Toast.LENGTH_SHORT).show()
        }

        // 3. 로그아웃 버튼 클릭
        view.findViewById<TextView>(R.id.btn_logout).setOnClickListener {
            // TODO: 로그아웃 로직 (토큰 삭제 등)
            Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            // 로그인 화면으로 이동하거나 앱 재시작
        }
    }
}