package com.example.matchbell.feature

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.matchbell.R

class MatchingFragment : Fragment() {

    // 1. 레이아웃 연결 (fragment_matching.xml)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 이름을 바꾸신 fragment_matching을 여기서 연결합니다.
        return inflater.inflate(R.layout.fragment_matching, container, false)
    }

    // 2. 기능 구현 (버튼 클릭 등)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fragment에서는 view.findViewById를 써야 합니다.
        val btnLock1 = view.findViewById<ImageButton>(R.id.btn_lock_1)

        btnLock1.setOnClickListener {
            // Activity로 이동 (context 대신 requireContext() 또는 activity 사용)
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}