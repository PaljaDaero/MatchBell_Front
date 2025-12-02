package com.example.matchbell

// [필수 추가] 위치 및 권한 관련 import
import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.matchbell.databinding.FragmentRadarBinding
import com.example.matchbell.feature.MatchingScore
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
    val id: Long, // userId
    val calculatedScore: Int, // MatchingScore를 통해 계산된 최종 점수
    val name: String, // nickname
    val affiliation: String, // region + age (UI 표시용으로 가공)
    var isLikeSent: Boolean = false,
    val avatarUrl: String? // 프로필 이미지 로드용
)

@AndroidEntryPoint
class RadarFragment : Fragment() {

    @Inject
    lateinit var authApi: AuthApi

    private val matchingScoreCalculator = MatchingScore()

    private var _binding: FragmentRadarBinding? = null
    private val binding get() = _binding!!

    private var radarAnimation: AnimatorSet? = null

    // [추가] 1. 위치 클라이언트 및 요청 변수 선언
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    // [추가] 2. 위치 콜백 정의
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                // 위치 획득 성공 후 업데이트 로직 호출
                stopLocationUpdates() // 위치 업데이트 중지

                val lat = location.latitude
                val lng = location.longitude

                // 좌표를 지역명으로 변환하는 함수 호출
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

    // [통합/수정] 중복된 onViewCreated 제거하고 하나로 통합
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startRadarPulseAnimation()

        // [추가] Fused Location Client 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.radarContainer.post {
            // 위치 획득 프로세스 시작
            acquireLocationAndLoadRadar()
        }
    }

    // [수정] 3. 위치 획득을 시작하는 메인 함수
    private fun acquireLocationAndLoadRadar() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {

            // 권한이 있을 경우 위치 업데이트 시작
            startLocationUpdates()
        } else {
            // 권한이 없을 경우 요청
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // [추가] 4. 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 승인됨: 위치 업데이트 시작
                startLocationUpdates()
            } else {
                // 권한 거부됨: 에러 메시지 표시
                Toast.makeText(context, "위치 권한이 거부되어 레이더 기능을 사용할 수 없습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // [추가] 5. Fused Location Provider를 사용하여 위치 요청
    private fun startLocationUpdates() {
        // Build LocationRequest
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(true)
            .setMaxUpdates(1) // 한 번만 업데이트 요청
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Toast.makeText(context, "위치 서비스에 접근할 수 없습니다. (권한 오류)", Toast.LENGTH_SHORT).show()
        }
    }

    // [추가] 6. 위치 업데이트 중지
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // [추가] 7. 좌표(Lat/Lng)를 지역명(Region)으로 변환 (Geocoder 사용)
    private fun getRegionFromLocation(lat: Double, lng: Double) {
        val geocoder = Geocoder(requireContext(), Locale.KOREA)
        try {
            // getFromLocation은 네트워크가 필요하며, API 33 미만 호환성을 위해 사용
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val region = if (!addresses.isNullOrEmpty()) {
                // Address 객체에서 필요한 지역 정보 (시/군/구) 추출
                addresses[0].locality ?: addresses[0].adminArea ?: "위치 미확인"
            } else {
                "위치 미확인"
            }

            // 최종적으로 위치 업데이트 API 호출 및 레이더 로드
            updateLocationAndLoad(lat, lng, region)

        } catch (e: Exception) {
            Log.e("RadarFragment", "Geocoder failed: ${e.message}", e)
            Toast.makeText(context, "지역 정보 변환에 실패했습니다.", Toast.LENGTH_SHORT).show()

            // 변환 실패 시 미확인 지역으로 전달하여 서버 통신은 시도
            updateLocationAndLoad(lat, lng, "위치 미확인")
        }
    }

    /**
     * 위치 정보를 서버에 업데이트하고 성공 시 레이더 데이터를 로드합니다.
     */
    private fun updateLocationAndLoad(lat: Double, lng: Double, region: String) {
        // [수정] 서버 API 요청 바디용 모델 클래스를 사용하고 변수명을 명확히 변경
        val locationRequestForServer = com.example.matchbell.data.model.LocationRequest(
            lat = lat,
            lng = lng,
            region = region
        )

        lifecycleScope.launch {
            try {
                // 서버 API 호출 시 서버 전송용 객체를 사용
                val response = authApi.updateMyLocation(locationRequestForServer)

                if (response.isSuccessful) {
                    Log.d("RadarFragment", "Location updated successfully.")
                    // 위치 업데이트 성공 후 레이더 데이터 로드
                    loadRadarData()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "위치 업데이트 실패"
                    Log.e("RadarFragment", "Location update failed: ${response.code()}, $errorMsg")
                    Toast.makeText(context, "위치 업데이트 실패: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("RadarFragment", "Network error during location update", e)
                Toast.makeText(context, "네트워크 오류: 위치 업데이트에 실패했습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 레이더 사용자 목록을 AuthApi로부터 로드하고 점수를 계산하여 아이템을 배치합니다.
     */
    private fun loadRadarData() {
        lifecycleScope.launch {
            try {
                // [변경] authApi 사용
                val response = authApi.getRadarUsers()

                if (response.isSuccessful) {
                    val usersData = response.body()?.users ?: emptyList()

                    val displayUsers = usersData.map { userData ->

                        // 1. MatchingScore를 사용하여 최종 점수 계산
                        // T_min, T_p95는 MatchingScore.kt의 내부 상수를 사용한다고 가정
                        val calculatedScore = matchingScoreCalculator.calculateCompositeScore(
                            finalScore = userData.finalScore,
                            stressScore = userData.stressScore
                        )

                        // 2. UI 표시용 데이터 가공
                        val affiliationText = "${userData.region}, ${userData.age}세"

                        // 3. Display 모델 생성
                        RadarUser(
                            id = userData.userId,
                            calculatedScore = calculatedScore,
                            name = userData.nickname,
                            affiliation = affiliationText,
                            avatarUrl = userData.avatarUrl
                        )
                    }
                    // 4. 아이템 컨테이너에 배치 시작
                    addRadarItems(displayUsers)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "알 수 없는 에러"
                    Toast.makeText(context, "레이더 로드 실패: ${response.code()}, $errorMessage", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                Log.e("RadarFragment", "Error loading radar data", e)
            }
        }
    }

    private fun addRadarItems(users: List<RadarUser>) {
        if (_binding == null) return

        val container = binding.radarContainer

        if (container.width == 0 || container.height == 0) return

        val itemSizePx = (70 * resources.displayMetrics.density)

        val minSeparationSquared = (itemSizePx * itemSizePx) * 1.2f

        val centerX = container.width / 2f
        val centerY = container.height / 2f

        val minRadius = (binding.ivRadarCenter.width / 2f) + (itemSizePx / 2f) + 20f
        val maxRadius = (Math.min(container.width, container.height) / 2f) - (itemSizePx / 2f)

        val placedItemCenters = mutableListOf<Pair<Float, Float>>()

        val MAX_ATTEMPTS = 30

        val blinkAnim = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 500
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }

        users.forEach { user ->

            var newX: Float
            var newY: Float
            var xOffset: Float = 0f
            var yOffset: Float = 0f
            var attempts = 0
            var isOverlapping: Boolean

            do {
                attempts++
                val angle = Math.toRadians(Random.nextDouble(0.0, 360.0))
                val radius = Random.nextDouble(minRadius.toDouble(), maxRadius.toDouble())
                xOffset = (radius * cos(angle)).toFloat()
                yOffset = (radius * sin(angle)).toFloat()

                newX = centerX + xOffset
                newY = centerY + yOffset

                isOverlapping = placedItemCenters.any { (placedX, placedY) ->
                    val dx = newX - placedX
                    val dy = newY - placedY
                    val distanceSquared = (dx * dx) + (dy * dy)
                    distanceSquared < minSeparationSquared
                }

            } while (isOverlapping && attempts < MAX_ATTEMPTS)

            placedItemCenters.add(Pair(centerX + xOffset, centerY + yOffset))

            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_radar_target, container, false)

            val tvScore = itemView.findViewById<TextView>(R.id.tv_score)
            val tvClick = itemView.findViewById<TextView>(R.id.tv_click)

            // [수정] 계산된 최종 점수 사용
            tvScore.text = user.calculatedScore.toString()
            tvClick.startAnimation(blinkAnim)

            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            }
            itemView.layoutParams = params

            itemView.translationX = xOffset
            itemView.translationY = yOffset

            // [수정] TODO: 아이템 클릭 시 팝업 띄우는 로직 추가
            itemView.setOnClickListener {
                tvClick.clearAnimation()
                tvClick.visibility = View.GONE

                // RadarDialogFragment 인스턴스 생성 및 표시
                val dialog = RadarDialogFragment.newInstance(
                    user.id.toInt(), // Int로 변환 (Bundle에 Int로 넣는 기존 코드 유지)
                    user.name,
                    user.affiliation,
                    user.calculatedScore, // [추가] 점수 전달
                    false // isMutual은 API에 없으므로 일단 false
                )
                // dialog.show(parentFragmentManager, "RadarDialog") // 주석 처리된 이전 코드를 대체
                dialog.show(parentFragmentManager, "RadarDialog")
            }

            container.addView(itemView)
        }
    }

    private fun startRadarPulseAnimation() {
        if (_binding == null) return

        val FADE_DURATION = 500L
        val DELAY_BETWEEN = 500L
        val PAUSE_DURATION = 200L

        // 링이 나타날 때의 최종 알파 값 (가장 진함: 1 -> 가장 연함: 3)
        val ALPHA_RING1_MAX = 0.5f
        val ALPHA_RING2_MAX = 0.5f
        val ALPHA_RING3_MAX = 0.5f

        // 링이 나타나는 애니메이션 (alpha 0 -> MAX)
        val ring1In = ObjectAnimator.ofFloat(binding.radarRing1, "alpha", 0f, ALPHA_RING1_MAX).setDuration(FADE_DURATION)
        val ring2In = ObjectAnimator.ofFloat(binding.radarRing2, "alpha", 0f, ALPHA_RING2_MAX).setDuration(FADE_DURATION)
        val ring3In = ObjectAnimator.ofFloat(binding.radarRing3, "alpha", 0f, ALPHA_RING3_MAX).setDuration(FADE_DURATION)

        // 링이 사라지는 애니메이션 (alpha MAX -> 0)
        val ring1Out = ObjectAnimator.ofFloat(binding.radarRing1, "alpha", ALPHA_RING1_MAX, 0f).setDuration(FADE_DURATION)
        val ring2Out = ObjectAnimator.ofFloat(binding.radarRing2, "alpha", ALPHA_RING2_MAX, 0f).setDuration(FADE_DURATION)
        val ring3Out = ObjectAnimator.ofFloat(binding.radarRing3, "alpha", ALPHA_RING3_MAX, 0f).setDuration(FADE_DURATION)

        radarAnimation = AnimatorSet().apply {

            play(ring1In)
            play(ring2In).after(ring1In).after(DELAY_BETWEEN)
            play(ring3In).after(ring2In).after(DELAY_BETWEEN)

            play(ring1Out).with(ring2Out).with(ring3Out).after(ring3In).after(PAUSE_DURATION)


            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (isAdded) {
                        radarAnimation?.start()
                    }
                }
            })
        }

        binding.radarRing1.alpha = 0f
        binding.radarRing2.alpha = 0f
        binding.radarRing3.alpha = 0f

        radarAnimation?.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        radarAnimation?.cancel()
        radarAnimation = null
        _binding = null
    }
}