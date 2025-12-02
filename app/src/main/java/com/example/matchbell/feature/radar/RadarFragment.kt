@file:Suppress("DEPRECATION")

package com.example.matchbell.feature.radar

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.matchbell.R
import com.example.matchbell.RadarDialogFragment
import com.example.matchbell.databinding.FragmentRadarBinding
import com.example.matchbell.feature.MatchingScore
import com.example.matchbell.feature.auth.TokenManager
import com.example.matchbell.network.AuthApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class RadarUser(
    val id: Long,
    val calculatedScore: Int,
    val name: String,
    val affiliation: String,
)

@AndroidEntryPoint
class RadarFragment : Fragment() {

    @Inject
    lateinit var authApi: AuthApi

    private val matchingScoreCalculator = MatchingScore()

    private var _binding: FragmentRadarBinding? = null
    private val binding get() = _binding!!

    // 애니메이션 객체들을 보관할 리스트
    private val animators = mutableListOf<ObjectAnimator>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                stopLocationUpdates()
                val lat = location.latitude
                val lng = location.longitude
                getRegionFromLocation(lat, lng)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRadarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [확인사살] 뷰가 XML에서 alpha=0이었더라도 강제로 보이게 설정
        binding.radarRing1.alpha = 1f
        binding.radarRing2.alpha = 1f
        binding.radarRing3.alpha = 1f
        binding.radarRing1.visibility = View.VISIBLE
        binding.radarRing2.visibility = View.VISIBLE
        binding.radarRing3.visibility = View.VISIBLE

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.radarContainer.post {
            acquireLocationAndLoadRadar()
        }
    }

    override fun onResume() {
        super.onResume()
        startPulseAnimation()
    }

    override fun onPause() {
        super.onPause()
        stopPulseAnimation()
    }

    private fun startPulseAnimation() {
        stopPulseAnimation() // 기존 애니메이션 정리

        if (_binding == null) return

        // 속도 설정 (숫자를 작게 할수록 빨라짐)
        val animDuration = 900L  // 1.5초 -> 0.9초로 단축
        val interval = 100L      // 링 사이 간격도 500ms -> 300ms로 단축

        // 헬퍼 함수
        fun createAnim(target: View, delay: Long): ObjectAnimator {
            // alpha: 0.1(흐릿) <-> 0.7(진함) 반복
            return ObjectAnimator.ofFloat(target, "alpha", 0.1f, 0.7f).apply {
                duration = animDuration
                startDelay = delay
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE // 켜졌다 꺼졌다 반복
                interpolator = AccelerateDecelerateInterpolator() // 부드러운 가속도
            }
        }

        // 3개의 링 생성 (0초, 0.3초, 0.6초 뒤 출발)
        val ani1 = createAnim(binding.radarRing1, 0)
        val ani2 = createAnim(binding.radarRing2, interval)
        val ani3 = createAnim(binding.radarRing3, interval * 2)

        // 리스트에 추가 및 시작
        animators.add(ani1)
        animators.add(ani2)
        animators.add(ani3)

        animators.forEach { it.start() }
    }

    private fun stopPulseAnimation() {
        animators.forEach { it.cancel() }
        animators.clear()
    }

    // --- (아래는 기존 위치/서버 로직 그대로 유지) ---

    private fun acquireLocationAndLoadRadar() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(/* requestCode = */ requestCode, /* permissions = */
            permissions, /* grantResults = */
            grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(context, "위치 권한 필요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(true)
            .setMaxUpdates(1)
            .build()
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (_: SecurityException) { }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun getRegionFromLocation(lat: Double, lng: Double) {
        val geocoder = Geocoder(requireContext(), Locale.KOREA)
        try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val region = if (!addresses.isNullOrEmpty()) {
                addresses[0].locality ?: addresses[0].adminArea
            } else {
                "서울"
            }
            val finalRegion = when(region) {
                "Mountain View" -> "마운틴 뷰"
                "Seoul" -> "서울"
                else -> region ?: "서울"
            }
            updateLocationAndLoad(lat, lng, finalRegion)
        } catch (_: Exception) {
            updateLocationAndLoad(lat, lng, "서울")
        }
    }

    private fun updateLocationAndLoad(lat: Double, lng: Double, region: String) {
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return
        val req = com.example.matchbell.data.model.LocationRequest(lat, lng, region)

        lifecycleScope.launch {
            try {
                val res = authApi.updateMyLocation("Bearer $token", req)
                if (res.isSuccessful) loadRadarData()
            } catch (e: Exception) { Log.e("Radar", "LocUpdate Error", e) }
        }
    }

    private fun loadRadarData() {
        val token = context?.let { TokenManager.getAccessToken(it) } ?: return
        lifecycleScope.launch {
            try {
                val res = authApi.getRadarUsers("Bearer $token")
                if (res.isSuccessful) {
                    val list = res.body()?.users ?: emptyList()
                    val displayList = list.map { u ->
                        RadarUser(u.userId, matchingScoreCalculator.calculateCompositeScore(u.finalScore, u.stressScore), u.nickname, "${u.region}, ${u.age}세")
                    }
                    addRadarItems(displayList)
                }
            } catch (e: Exception) { Log.e("Radar", "LoadData Error", e) }
        }
    }

    @SuppressLint("Recycle")
    private fun addRadarItems(users: List<RadarUser>) {
        if (_binding == null) return
        val container = binding.radarContainer
        if (container.width == 0 || container.height == 0) return

        val itemSize = (70 * resources.displayMetrics.density)
        val minSepSq = (itemSize * itemSize) * 1.2f
        val cx = container.width / 2f
        val cy = container.height / 2f
        val minR = (binding.ivRadarCenter.width / 2f) + (itemSize/2f) + 20f
        val maxR = (Math.min(container.width, container.height) / 2f) - (itemSize/2f)
        val placed = mutableListOf<Pair<Float, Float>>()

        val blink = ObjectAnimator.ofFloat(null, "alpha", 0.2f, 1f).apply {
            duration = 1000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }

        users.forEach { user ->
            var nx: Float; var ny: Float; var tries = 0; var overlap: Boolean
            do {
                tries++
                val angle = Math.toRadians(Random.nextDouble(0.0, 360.0))
                val r = Random.nextDouble(minR.toDouble(), maxR.toDouble())
                nx = cx + (r * cos(angle)).toFloat()
                ny = cy + (r * sin(angle)).toFloat()
                overlap = placed.any { (px, py) ->
                    ((nx-px)*(nx-px) + (ny-py)*(ny-py)) < minSepSq
                }
            } while (overlap && tries < 30)
            placed.add(Pair(nx, ny))

            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_radar_target, container, false)
            val tvScore = itemView.findViewById<TextView>(R.id.tv_score)
            val tvClick = itemView.findViewById<TextView>(R.id.tv_click) // 반짝이는 배경

            tvScore.text = user.calculatedScore.toString()

            // 아이템 반짝임 효과
            val itemBlink = blink.clone()
            itemBlink.target = tvClick
            itemBlink.start()

            itemView.translationX = nx - cx // ConstraintLayout 중심 기준이면 좌표 조정 필요할 수 있음.
            // 현재 XML 구조상 Container가 꽉 차있다면 translationX/Y는 Left/Top 기준임.
            // 중심 기준으로 배치하려면 아래처럼 수정:
            itemView.x = nx - (itemSize/2) // View의 왼쪽 상단 좌표
            itemView.y = ny - (itemSize/2)

            itemView.setOnClickListener {
                RadarDialogFragment.newInstance(user.id, user.name, user.affiliation, user.calculatedScore)
                    .show(parentFragmentManager, "RadarDialog")
            }
            container.addView(itemView)
        }
    }

    override fun onDestroyView() {
        stopPulseAnimation()
        super.onDestroyView()
        _binding = null
    }
}