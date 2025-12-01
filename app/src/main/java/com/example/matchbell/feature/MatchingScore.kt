package com.example.matchbell.feature

/**
 * 궁합 점수 산정 알고리즘을 구현한 클래스입니다.
 * finalScore (S)와 stressScore (T)를 통합하여 최종 단일 호환 점수 (C)를 계산합니다.
 *
 * T_min (5.0)과 T_p95 (45.0)는 데이터가 쌓일 때까지 임시로 사용되는 고정 상수입니다.
 */
class MatchingScore {
    // 7.1.3 단일 호환 점수 (Composite Compatibility) 가중치
    private val Ws: Double = 0.7 // 궁합 자체의 강도 (S) 가중치
    private val Wh: Double = 0.3 // 갈등/불안정 리스크의 역 (H) 가중치

    // [초기 고정 상수] 데이터가 쌓일 때까지 사용할 임시 통계 기준선
    private val T_MIN_INITIAL: Double = 5.0
    private val T_P95_INITIAL: Double = 45.0

    /**
     * 최종 단일 호환 점수 (C)를 계산하는 메인 함수
     * @param finalScore 최종 점수 S (코드의 score, 범위 대략 0–100)
     * @param stressScore 스트레스 점수 T (코드의 stress)
     * @return 0-100 사이의 최종 통합 궁합 점수 C (정수)
     */
    fun calculateCompositeScore(
        finalScore: Double,
        stressScore: Double
    ): Int {
        val S = finalScore
        val T = stressScore

        // 1. 스트레스 정규화 및 조화도 (H) 계산 (7.1.2)
        val H = calculateHarmonyScore(T, T_MIN_INITIAL, T_P95_INITIAL)

        // 2. 단일 호환 점수 C 계산 (7.1.3)
        var C = (Ws * S) + (Wh * H)

        // 3. 리스크 게이트 적용 (7.1.4)
        C = applyRiskGate(S, T, C)

        // 결과는 0–100 정수점으로 변환 후 반환합니다.
        return C.coerceIn(0.0, 100.0).toInt()
    }

// -----------------------------------------------------------------------------------

    /**
     * 스트레스 점수 (T)를 조화도 (H)로 변환하는 함수 (7.1.2 정규화)
     */
    private fun calculateHarmonyScore(T: Double, tMin: Double, tP95: Double): Double {
        // Tnorm 계산: Tnorm = clamp((T - Tmin) / (Tp95 - Tmin), 0, 1)

        val Tnorm: Double = if (tP95 <= tMin) {
            // 통계적 오류 방지: 분모가 0 이하인 경우 (실제로는 발생해서는 안됨)
            if (T > tMin) 1.0 else 0.0
        } else {
            val rawTnorm = (T - tMin) / (tP95 - tMin)
            rawTnorm.coerceIn(0.0, 1.0)
        }

        // 조화도 H: H = (1 − Tnorm) × 100
        return (1.0 - Tnorm) * 100.0
    }

// -----------------------------------------------------------------------------------

    /**
     * 리스크 게이트를 적용하여 최종 점수 C의 상한을 제한하는 함수 (7.1.4)
     */
    private fun applyRiskGate(S: Double, T: Double, C: Double): Double {
        // 리스크 게이트 조건: S (finalScore) ≤ 35 and T (stressScore) ≥ 40
        if (S <= 35.0 && T >= 40.0) {
            // C = min(C, 35)로 상한 캡 적용
            return C.coerceAtMost(35.0)
        }
        return C
    }
}