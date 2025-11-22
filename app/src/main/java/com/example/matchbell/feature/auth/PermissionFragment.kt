package com.example.matchbell.feature.auth

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.matchbell.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionFragment : Fragment(R.layout.fragment_permission) {

    // 1. 권한 요청 결과를 받아오는 '런처'를 만듭니다.
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val gpsGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

        if (gpsGranted) {
            Toast.makeText(context, "권한 설정 완료! 다시 로그인해주세요.", Toast.LENGTH_LONG).show()
        }

        // ⭐⭐⭐ [핵심] 로그인 화면으로 이동하기 ⭐⭐⭐
        // nav_graph.xml에서 permissionFragment -> loginFragment 화살표를 만들어야 합니다!
        // ID가 action_permissionFragment_to_loginFragment 라고 가정합니다.
        findNavController().navigate(R.id.action_permissionFragment_to_loginFragment)

        // (만약 화살표 만들기 귀찮으면, 아래 코드로 백스택을 다 지우고 처음으로 갈 수도 있습니다)
        // findNavController().popBackStack(R.id.loginFragment, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCheck = view.findViewById<Button>(R.id.btn_check_permission)

        btnCheck.setOnClickListener {
            // 2. 필요한 권한 목록을 작성합니다.
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            // 알림 권한은 안드로이드 13(Tiramisu) 이상부터만 필요합니다.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            // 3. 권한 요청 팝업 띄우기!
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}