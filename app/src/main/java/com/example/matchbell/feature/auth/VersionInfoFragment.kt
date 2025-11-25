package com.example.matchbell.feature.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // [나중에 주석 해제]
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VersionInfoFragment : Fragment(R.layout.fragment_version_info) {

    // --------------------------------------------------------------------------
    // [TODO: 백엔드 연결 1] 뷰모델 가져오기
    // --------------------------------------------------------------------------
    // 지금은 서버 통신이 없어서 주석 처리 해둠. 나중에 주석을 푸세요.
    // private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 뒤로가기 버튼
        view.findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            findNavController().popBackStack()
        }

        // 2. 텍스트뷰 찾기
        val tvVersion = view.findViewById<TextView>(R.id.tv_version)
        val tvVersionStatus = view.findViewById<TextView>(R.id.tv_version_status)

        // 3. 내 폰에 설치된 앱 버전 가져오기 (이건 백엔드 없어도 원래 되는 기능)
        val myAppVersion = getAppVersionName()
        tvVersion.text = "현재 버전 : v$myAppVersion"

        // --------------------------------------------------------------------------
        // [TODO: 백엔드 연결 2] "최신 버전입니다" 문구 수정하기
        // --------------------------------------------------------------------------

        // [현재 상태: 삭제 예정]
        // 무조건 최신 버전이라고 우기는 코드입니다. 나중엔 지우세요.
        tvVersionStatus.text = "최신 버전입니다."
        tvVersionStatus.setTextColor(android.graphics.Color.parseColor("#FF8A80")) // 분홍색


        // [미래 코드: 이렇게 추가하세요]
        /*
        // 1. 서버한테 "지금 스토어에 올라온 최신 버전 몇이야?" 라고 물어봄
        viewModel.checkLatestVersion()

        // 2. 서버 대답 듣기
        viewModel.latestVersion.observe(viewLifecycleOwner) { serverVersion ->
            // serverVersion 예시: "1.0.2", myAppVersion 예시: "1.0.0"

            if (myAppVersion == serverVersion) {
                // 버전이 같으면 -> 최신 버전임
                tvVersionStatus.text = "최신 버전입니다."
                tvVersionStatus.setTextColor(Color.parseColor("#FF8A80"))
            } else {
                // 버전이 다르면 -> 업데이트 필요함
                tvVersionStatus.text = "새로운 버전(v$serverVersion)이 있습니다."
                tvVersionStatus.setTextColor(Color.RED) // 빨간색으로 경고

                // (선택사항) 업데이트 버튼을 보이게 하거나 클릭 리스너 추가
                // tvVersionStatus.setOnClickListener { 스토어로 이동 }
            }
        }
        */
    }

    // 앱 버전 이름 가져오는 함수 (이건 수정할 필요 없음! 계속 씁니다.)
    private fun getAppVersionName(): String {
        return try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }
}