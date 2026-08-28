package com.example.repository

/**
 * EcoPick 표준 오염도 판정 및 리워드 포인트 산정 엔진 (단일 진실 소스)
 *
 * 환경부 분리배출 가이드라인 기반:
 * - A등급 (0 ~ 10% 오염): 투명/깨끗함 -> 즉시 배출 가능 (+100P)
 * - B등급 (11 ~ 30% 오염): 가벼운 물자국/먼지 -> 가벼운 헹굼 후 배출 (+70P)
 * - C등급 (31 ~ 60% 오염): 양념/기름기 잔여 -> 주방세제 정밀 세척 후 배출 (+40P)
 * - F등급 (61 ~ 100% 오염): 심한 착색/음식물 고착 -> 재활용 불가, 종량제 봉투 배출 (0P)
 */
object EcoGradeCalculator {

    fun calculateGrade(pollutionPercent: Double): String {
        return when {
            pollutionPercent <= 10.0 -> "A"
            pollutionPercent <= 30.0 -> "B"
            pollutionPercent <= 60.0 -> "C"
            else -> "F"
        }
    }

    fun getGradeLevel(pollutionPercent: Double): Int {
        return when {
            pollutionPercent <= 10.0 -> 0 // A등급
            pollutionPercent <= 30.0 -> 1 // B등급
            pollutionPercent <= 60.0 -> 2 // C등급
            else -> 3                     // F등급
        }
    }

    fun getPointsForPollution(pollutionPercent: Double): Int {
        return when {
            pollutionPercent <= 10.0 -> 100
            pollutionPercent <= 30.0 -> 70
            pollutionPercent <= 60.0 -> 40
            else -> 0
        }
    }

    fun getGradeName(grade: Int): String {
        return when (grade) {
            0 -> "A등급 (즉시 배출 가능)"
            1 -> "B등급 (가벼운 헹굼 필요)"
            2 -> "C등급 (정밀 세척 필요)"
            else -> "F등급 (종량제 봉투 배출)"
        }
    }

    fun getDisposalRecommendation(grade: Int, material: String): String {
        return when (grade) {
            0 -> "$material 전용 분리수거함에 즉시 배출하세요."
            1 -> "가볍게 물로 헹군 후 $material 전용 수거함에 배출하세요."
            2 -> "주방세제와 따뜻한 물로 세척 후 $material 전용 수거함에 배출하세요. 붉은 착색이 지워지지 않으면 종량제 봉투에 버려야 합니다."
            else -> "오염 및 착색이 심해 재활용이 어렵습니다. 종량제 봉투(일반쓰레기)에 배출하세요."
        }
    }
}
