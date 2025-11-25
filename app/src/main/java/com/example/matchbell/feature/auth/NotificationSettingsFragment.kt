package com.example.matchbell.feature.settings

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // [나중에 추가] 뷰모델 사용을 위해 필요
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationSettingsFragment : Fragment(R.layout.fragment_notification_settings) {

    // [TODO: 백엔드 연결 1] 뷰모델 가져오기 (지금은 없어서 주석 처리, 나중에 주석 해제)
    // private val viewModel: SettingsViewModel by viewModels()

    // 색상 상수
    private val COLOR_ON = Color.parseColor("#FF0000")
    private val COLOR_OFF = Color.parseColor("#FFB6C1")

    // --------------------------------------------------------------------------
    // [TODO: 백엔드 연결 2] 이 변수들은 모두 삭제 대상입니다!
    // 이유: 나중에는 서버에서 받아온 값을 쓸 것이기 때문에, 임시로 만든 이 변수들은 필요 없어집니다.
    // --------------------------------------------------------------------------
    private var isMatchOn = true  // [삭제 예정]
    private var isChatOn = true   // [삭제 예정]
    private var isEventOn = false // [삭제 예정]
    private var isDndOn = false   // [삭제 예정]

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 뒤로가기
        view.findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            findNavController().popBackStack()
        }

        // --------------------------------------------------------------------------
        // [TODO: 백엔드 연결 3] 화면 켜지자마자 서버 설정값 가져오기
        // --------------------------------------------------------------------------
        /*
        [수정 방법]
        1. viewModel.getNotificationSettings() // 서버에 요청
        2. viewModel.settings.observe(viewLifecycleOwner) { settings ->
             // 서버에서 받아온 값으로 화면 갱신 (빨강/분홍 색칠하기)
             updateColor(btnMatchOn, btnMatchOff, settings.matchOn)
             updateColor(btnChatOn, btnChatOff, settings.chatOn)
             ...
        }
        */

        // 2. 각 버튼 세트 찾기 & 초기화
        setupToggle(view, R.id.btn_match_on, R.id.btn_match_off, isMatchOn) { isOn ->
            // [TODO: 백엔드 연결 4] 로컬 변수 저장은 삭제하고, 서버 전송 코드로 교체
            isMatchOn = isOn // [삭제 예정: 로컬 변수에 저장하는 코드]

            // [추가할 코드]
            // viewModel.updateNotification("MATCH", isOn)
        }

        setupToggle(view, R.id.btn_chat_on, R.id.btn_chat_off, isChatOn) { isOn ->
            // [TODO: 백엔드 연결 4]
            isChatOn = isOn // [삭제 예정]

            // [추가할 코드]
            // viewModel.updateNotification("CHAT", isOn)
        }

        setupToggle(view, R.id.btn_event_on, R.id.btn_event_off, isEventOn) { isOn ->
            // [TODO: 백엔드 연결 4]
            isEventOn = isOn // [삭제 예정]

            // [추가할 코드]
            // viewModel.updateNotification("EVENT", isOn)
        }

        setupToggle(view, R.id.btn_dnd_on, R.id.btn_dnd_off, isDndOn) { isOn ->
            // [TODO: 백엔드 연결 4]
            isDndOn = isOn // [삭제 예정]

            // [추가할 코드]
            // viewModel.updateNotification("DND", isOn)
        }
    }

    private fun setupToggle(
        rootView: View,
        onBtnId: Int,
        offBtnId: Int,
        initialState: Boolean,
        onChanged: (Boolean) -> Unit
    ) {
        val btnOn = rootView.findViewById<TextView>(onBtnId)
        val btnOff = rootView.findViewById<TextView>(offBtnId)

        // 초기 화면 그리기
        updateColor(btnOn, btnOff, initialState)

        // ON 버튼 눌렀을 때
        btnOn.setOnClickListener {
            updateColor(btnOn, btnOff, true)
            onChanged(true)
        }

        // OFF 버튼 눌렀을 때
        btnOff.setOnClickListener {
            updateColor(btnOn, btnOff, false)
            onChanged(false)
        }
    }

    private fun updateColor(btnOn: TextView, btnOff: TextView, isOn: Boolean) {
        if (isOn) {
            btnOn.setBackgroundColor(COLOR_ON)
            btnOff.setBackgroundColor(COLOR_OFF)
        } else {
            btnOn.setBackgroundColor(COLOR_OFF)
            btnOff.setBackgroundColor(COLOR_ON)
        }
    }
}