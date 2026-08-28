package com.example.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GlobalState {
    var userEmail by mutableStateOf("")
    var apartmentId by mutableStateOf("")
    var currentPoints by mutableIntStateOf(0)
    var targetGoal by mutableIntStateOf(10)
    var currentCount by mutableIntStateOf(0)
    var totalAppRecycled by mutableIntStateOf(12450)
    
    val isAdmin: Boolean
        get() = userEmail == "mjayj9@gmail.com" || userEmail == "2025186@snu.ms.kr"
        
    fun addRecycle(material: String, isSuccess: Boolean) {
        if (isSuccess) {
            currentCount++
            totalAppRecycled++
            
            // 기본 보상
            var reward = 50
            
            // 목표 달성 시 추가 보상 (목표치에 비례)
            if (currentCount == targetGoal) {
                reward += targetGoal * 20 // 예: 10개면 200 추가, 100개면 2000 추가
                targetGoal += 10 // 목표 상향
            }
            
            currentPoints += reward
        }
    }
}
