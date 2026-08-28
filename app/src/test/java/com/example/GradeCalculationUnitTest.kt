package com.example

import com.example.repository.EcoGradeCalculator
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 에코픽(EcoPick) 핵심 비즈니스 로직 단위 테스트:
 * 1. 오염도(%)에 따른 등급(A/B/C/F) 및 포인트 경계값 정밀 검증
 * 2. Gemini AI JSON 스키마 파싱 검증
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GradeCalculationUnitTest {

    @Test
    fun testPollutionGradeBoundaries() {
        // A등급 (0% ~ 10.0%)
        assertEquals("A", EcoGradeCalculator.calculateGrade(0.0))
        assertEquals("A", EcoGradeCalculator.calculateGrade(5.5))
        assertEquals("A", EcoGradeCalculator.calculateGrade(10.0))
        assertEquals(100, EcoGradeCalculator.getPointsForPollution(10.0))

        // B등급 (10.0% 초과 ~ 30.0%) - 10.5%와 같은 소수점 케이스 누락 방지
        assertEquals("B", EcoGradeCalculator.calculateGrade(10.1))
        assertEquals("B", EcoGradeCalculator.calculateGrade(10.5))
        assertEquals("B", EcoGradeCalculator.calculateGrade(25.0))
        assertEquals("B", EcoGradeCalculator.calculateGrade(30.0))
        assertEquals(70, EcoGradeCalculator.getPointsForPollution(30.0))

        // C등급 (30.0% 초과 ~ 60.0%) - 30.4%와 같은 소수점 케이스 검증
        assertEquals("C", EcoGradeCalculator.calculateGrade(30.1))
        assertEquals("C", EcoGradeCalculator.calculateGrade(30.4))
        assertEquals("C", EcoGradeCalculator.calculateGrade(45.0))
        assertEquals("C", EcoGradeCalculator.calculateGrade(60.0))
        assertEquals(40, EcoGradeCalculator.getPointsForPollution(60.0))

        // F등급 (60.0% 초과) - 세척 불가, 일반 종량제 배출
        assertEquals("F", EcoGradeCalculator.calculateGrade(60.1))
        assertEquals("F", EcoGradeCalculator.calculateGrade(85.5))
        assertEquals("F", EcoGradeCalculator.calculateGrade(100.0))
        assertEquals(0, EcoGradeCalculator.getPointsForPollution(85.5))
    }

    @Test
    fun testGeminiResponseJsonParsing() {
        val sampleValidJson = """
            {
              "판독_성공": true,
              "재질": "플라스틱",
              "품목명": "배달 떡볶이 용기",
              "오염도_퍼센트": 25,
              "등급": 1,
              "상태": "약간의 양념 얼룩 감지",
              "피드백": "따뜻한 물로 1회 헹군 뒤 분리배출하세요.",
              "헹굼_권장여부": true,
              "배출방법": "플라스틱 전용 수거함 배출"
            }
        """.trimIndent()

        val json = JSONObject(sampleValidJson)
        assertTrue(json.getBoolean("판독_성공"))
        assertEquals("플라스틱", json.getString("재질"))
        assertEquals(25, json.getInt("오염도_퍼센트"))
        assertEquals("B", EcoGradeCalculator.calculateGrade(json.getDouble("오염도_퍼센트")))
    }

    @Test
    fun testUnidentifiableImageHandling() {
        val unidentifiableJson = """
            {
              "판독_성공": false,
              "불가_사유": "사진이 너무 어두워 쓰레기를 식별할 수 없습니다."
            }
        """.trimIndent()

        val json = JSONObject(unidentifiableJson)
        assertFalse(json.getBoolean("판독_성공"))
        assertTrue(json.getString("불가_사유").isNotEmpty())
    }

    @Test
    fun testImageHashPerceptualSimilarity() {
        val hash1 = "1111000011110000111100001111000011110000111100001111000011110000"
        val hashIdentical = "1111000011110000111100001111000011110000111100001111000011110000"
        val hashSlightlyDiff = "1111000011110000111100001111000011110000111100001111000011110011" // 2 bit difference
        val hashCompletelyDiff = "0000111100001111000011110000111100001111000011110000111100001111" // 64 bit difference

        assertTrue(com.example.util.ImageHashUtil.isSimilar(hash1, hashIdentical, threshold = 5))
        assertTrue(com.example.util.ImageHashUtil.isSimilar(hash1, hashSlightlyDiff, threshold = 5))
        assertFalse(com.example.util.ImageHashUtil.isSimilar(hash1, hashCompletelyDiff, threshold = 5))
    }

    @Test
    fun testPasswordHashing() {
        val pass = "secret123"
        val hash1 = com.example.util.GlobalState.hashPassword(pass)
        val hash2 = com.example.util.GlobalState.hashPassword(pass)
        assertEquals(hash1, hash2)
        assertFalse(hash1 == pass)
        assertEquals(64, hash1.length) // SHA-256 hex length
    }
}
