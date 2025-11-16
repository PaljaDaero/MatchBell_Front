package com.example.matchbell.feature.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupTermsFragment : Fragment(R.layout.fragment_signup_terms) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnNext = view.findViewById<Button>(R.id.btn_next)
        val cbAll = view.findViewById<CheckBox>(R.id.cb_all_agree)
        val cbService = view.findViewById<CheckBox>(R.id.cb_service)
        val cbPrivacy = view.findViewById<CheckBox>(R.id.cb_privacy)
        val cbMarketing = view.findViewById<CheckBox>(R.id.cb_marketing)

        // '모두 동의' 체크 시 전체 체크/해제
        cbAll.setOnCheckedChangeListener { _, isChecked ->
            cbService.isChecked = isChecked
            cbPrivacy.isChecked = isChecked
            cbMarketing.isChecked = isChecked
        }

        // '확인' 버튼 클릭 시
        btnNext.setOnClickListener {
            if (cbService.isChecked && cbPrivacy.isChecked) {
                // [수정됨] 다음 화면(SignupInfoFragment)으로 이동!
                // (nav_graph에서 화살표를 그어야 이 ID가 생깁니다!)
                findNavController().navigate(R.id.action_signupTermsFragment_to_signupInfoFragment)
            } else {
                Toast.makeText(context, "필수 약관에 동의해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}