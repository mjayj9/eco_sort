package com.example.util

import android.graphics.Bitmap
import android.graphics.Color

object ImageHashUtil {
    // 8x8 평균 해시 (aHash) 알고리즘 (어뷰징 방지: 완전 동일 및 매우 유사한 재사용 사진 차단)
    fun generateImageHash(bitmap: Bitmap): String {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
        val stringBuilder = StringBuilder()
        
        var totalGray = 0
        val grays = IntArray(64)
        var index = 0
        
        // 전체 평균 픽셀 밝기(그레이스케일) 계산
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val pixel = scaledBitmap.getPixel(x, y)
                val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                grays[index++] = gray
                totalGray += gray
            }
        }
        
        val avgGray = totalGray / 64
        
        // 평균보다 밝으면 1, 어두우면 0 부여하여 64비트 이진 문자열 생성
        for (gray in grays) {
            stringBuilder.append(if (gray >= avgGray) "1" else "0")
        }
        
        return stringBuilder.toString()
    }

    /**
     * 두 64비트 해시 간의 해밍 거리(Hamming Distance)를 계산합니다.
     * 거리가 0이면 동일 사진, 1~5 이내이면 각도나 조명이 약간 다른 유사 사진으로 판별됩니다.
     */
    fun hammingDistance(hash1: String, hash2: String): Int {
        if (hash1.length != hash2.length || hash1.length != 64) {
            return if (hash1 == hash2) 0 else 64
        }
        var distance = 0
        for (i in hash1.indices) {
            if (hash1[i] != hash2[i]) {
                distance++
            }
        }
        return distance
    }

    fun isSimilar(hash1: String, hash2: String, threshold: Int = 5): Boolean {
        if (hash1.isBlank() || hash2.isBlank()) return false
        return hammingDistance(hash1, hash2) <= threshold
    }
}
