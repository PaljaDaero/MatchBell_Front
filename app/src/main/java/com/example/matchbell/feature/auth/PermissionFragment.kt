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
        // 권한 허용 여부와 상관없이 일단 다음 화면(메인)으로 넘어가게 처리하거나,
        // 필수로 받아야 한다면 거절 시 토스트를 띄울 수도 있습니다.

        val gpsGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val notiGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false

        if (gpsGranted && notiGranted) {
            Toast.makeText(context, "모든 권한이 허용되었습니다!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "일부 권한이 거부되었습니다. 설정에서 변경 가능합니다.", Toast.LENGTH_LONG).show()
        }

        // 권한 설정이 끝나면 진짜 앱 메인 화면으로 이동!
        // (메인 화면으로 가는 액션 ID를 적어야 합니다. 일단 임시로 적어둡니다.)
        // findNavController().navigate(R.id.action_permissionFragment_to_homeFragment)
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