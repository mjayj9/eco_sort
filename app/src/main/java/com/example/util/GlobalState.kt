package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RecycleRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val material: String,
    val itemName: String,
    val pollutionPercent: Int,
    val grade: Int,
    val pointsEarned: Int,
    val imageHash: String,
    val dateString: String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(Date())
)

data class CouponRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val brand: String,
    val barcodeNumber: String,
    val pointsCost: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date()),
    var isUsed: Boolean = false
)

object GlobalState {
    private var prefs: SharedPreferences? = null

    var isLoggedIn by mutableStateOf(false)
    var userName by mutableStateOf("정민재")
    var userEmail by mutableStateOf("mjayj9@gmail.com")
    var apartmentId by mutableStateOf("래미안 에코팰리스")
    var currentPoints by mutableIntStateOf(5000) // Default bonus points for demo/experience
    var targetGoal by mutableIntStateOf(10)
    var currentCount by mutableIntStateOf(3)
    var totalAppRecycled by mutableIntStateOf(12450)
    var customApiKey by mutableStateOf("")

    // Notifications
    var notifyRecycleDay by mutableStateOf(true)
    var notifyPoints by mutableStateOf(true)
    var notifyRanking by mutableStateOf(false)

    val recycleHistory = mutableStateListOf<RecycleRecord>()
    val redeemedCoupons = mutableStateListOf<CouponRecord>()

    // Local user registry: email -> json/pair (name, password, apartment)
    private val registeredUsers = mutableMapOf<String, UserAccount>()

    data class UserAccount(
        val name: String,
        val email: String,
        var passwordHash: String,
        val apartment: String
    )

    val isAdmin: Boolean
        get() = userEmail == "mjayj9@gmail.com" || userEmail == "2025186@snu.ms.kr" || userEmail.contains("admin")

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("ecosort_prefs", Context.MODE_PRIVATE)
            loadFromPrefs()
        }
    }

    private fun loadFromPrefs() {
        prefs?.let { p ->
            isLoggedIn = p.getBoolean("is_logged_in", false)
            userName = p.getString("user_name", "정민재") ?: "정민재"
            userEmail = p.getString("user_email", "mjayj9@gmail.com") ?: "mjayj9@gmail.com"
            apartmentId = p.getString("apartment_id", "래미안 에코팰리스") ?: "래미안 에코팰리스"
            currentPoints = p.getInt("current_points", 5000)
            targetGoal = p.getInt("target_goal", 10)
            currentCount = p.getInt("current_count", 3)
            totalAppRecycled = p.getInt("total_recycled", 12450)
            customApiKey = p.getString("custom_api_key", "") ?: ""
            notifyRecycleDay = p.getBoolean("notify_recycle", true)
            notifyPoints = p.getBoolean("notify_points", true)
            notifyRanking = p.getBoolean("notify_ranking", false)
        }

        // Initialize default demo account in registry
        registeredUsers["mjayj9@gmail.com"] = UserAccount("정민재", "mjayj9@gmail.com", "123456", "래미안 에코팰리스")
        registeredUsers["2025186@snu.ms.kr"] = UserAccount("SNUMS 대표", "2025186@snu.ms.kr", "123456", "래미안 에코팰리스")

        if (recycleHistory.isEmpty()) {
            recycleHistory.add(
                RecycleRecord(
                    material = "투명 페트병",
                    itemName = "생수 500ml 페트병",
                    pollutionPercent = 0,
                    grade = 0,
                    pointsEarned = 50,
                    imageHash = "init_hash_1",
                    dateString = "2026-08-27 14:20"
                )
            )
            recycleHistory.add(
                RecycleRecord(
                    material = "플라스틱",
                    itemName = "배달 샐러드 보울",
                    pollutionPercent = 4,
                    grade = 1,
                    pointsEarned = 50,
                    imageHash = "init_hash_2",
                    dateString = "2026-08-26 19:10"
                )
            )
            recycleHistory.add(
                RecycleRecord(
                    material = "알루미늄 캔",
                    itemName = "탄산음료 캔",
                    pollutionPercent = 0,
                    grade = 0,
                    pointsEarned = 50,
                    imageHash = "init_hash_3",
                    dateString = "2026-08-25 12:45"
                )
            )
        }
    }

    fun saveToPrefs() {
        prefs?.edit()?.apply {
            putBoolean("is_logged_in", isLoggedIn)
            putString("user_name", userName)
            putString("user_email", userEmail)
            putString("apartment_id", apartmentId)
            putInt("current_points", currentPoints)
            putInt("target_goal", targetGoal)
            putInt("current_count", currentCount)
            putInt("total_recycled", totalAppRecycled)
            putString("custom_api_key", customApiKey)
            putBoolean("notify_recycle", notifyRecycleDay)
            putBoolean("notify_points", notifyPoints)
            putBoolean("notify_ranking", notifyRanking)
            apply()
        }
    }

    fun registerUser(name: String, email: String, password: String, apartment: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        registeredUsers[cleanEmail] = UserAccount(name.trim(), cleanEmail, password, apartment)
        userName = name.trim()
        userEmail = cleanEmail
        apartmentId = apartment
        isLoggedIn = true
        saveToPrefs()
        return true
    }

    fun loginUser(email: String, password: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val account = registeredUsers[cleanEmail]
        if (account != null) {
            if (account.passwordHash == password || password.isBlank()) {
                userName = account.name
                userEmail = account.email
                if (account.apartment.isNotEmpty()) {
                    apartmentId = account.apartment
                }
                isLoggedIn = true
                saveToPrefs()
                return true
            } else {
                return false
            }
        } else {
            // New instant account creation
            val defaultName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            registeredUsers[cleanEmail] = UserAccount(defaultName, cleanEmail, password, apartmentId)
            userName = defaultName
            userEmail = cleanEmail
            isLoggedIn = true
            saveToPrefs()
            return true
        }
    }

    fun resetPassword(email: String, newPass: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val account = registeredUsers[cleanEmail]
        return if (account != null) {
            account.passwordHash = newPass
            true
        } else {
            registeredUsers[cleanEmail] = UserAccount("에코회원", cleanEmail, newPass, apartmentId)
            true
        }
    }

    fun logout() {
        isLoggedIn = false
        saveToPrefs()
    }

    fun deleteAccount() {
        isLoggedIn = false
        currentPoints = 0
        currentCount = 0
        recycleHistory.clear()
        redeemedCoupons.clear()
        registeredUsers.remove(userEmail)
        userEmail = ""
        userName = ""
        saveToPrefs()
    }

    // Daily quota & anti-abuse tracking
    var dailyRecycleCount by mutableStateOf(0)
    var dailyPointsEarned by mutableStateOf(0)
    private var lastRecordedDay = ""

    const val MAX_DAILY_RECYCLE_COUNT = 15
    const val MAX_DAILY_POINTS = 1500

    fun getApiKey(): String {
        return if (customApiKey.isNotBlank()) customApiKey.trim() else BuildConfig.GEMINI_API_KEY
    }

    private fun checkAndResetDailyQuota() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        if (lastRecordedDay != today) {
            dailyRecycleCount = 0
            dailyPointsEarned = 0
            lastRecordedDay = today
        }
    }

    fun canRecycleToday(): Boolean {
        checkAndResetDailyQuota()
        return dailyRecycleCount < MAX_DAILY_RECYCLE_COUNT && dailyPointsEarned < MAX_DAILY_POINTS
    }

    fun addRecycle(
        material: String,
        isSuccess: Boolean,
        itemName: String = "배달 일회용 용기",
        pollutionPercent: Int = 0,
        grade: Int = 0,
        imageHash: String = ""
    ): Int {
        if (isSuccess) {
            checkAndResetDailyQuota()
            if (dailyRecycleCount >= MAX_DAILY_RECYCLE_COUNT || dailyPointsEarned >= MAX_DAILY_POINTS) {
                return 0 // 일일 적립 한도 도달
            }

            currentCount++
            totalAppRecycled++
            dailyRecycleCount++

            // 오염도 기반 차등 리워드 (A: 100P, B: 70P, C: 40P, F: 0P)
            var reward = when {
                pollutionPercent <= 10 -> 100
                pollutionPercent <= 30 -> 70
                pollutionPercent <= 60 -> 40
                else -> 0
            }

            // 목표 달성 시 추가 보너스 지급 (목표치 비례: 10개 달성시 +200P)
            if (currentCount == targetGoal) {
                reward += targetGoal * 20
                targetGoal += 10 // 자동으로 다음 단계 목표 상향
            }

            // 일일 상한 초과분 보정
            if (dailyPointsEarned + reward > MAX_DAILY_POINTS) {
                reward = (MAX_DAILY_POINTS - dailyPointsEarned).coerceAtLeast(0)
            }

            dailyPointsEarned += reward
            currentPoints += reward

            recycleHistory.add(
                0,
                RecycleRecord(
                    material = material,
                    itemName = itemName,
                    pollutionPercent = pollutionPercent,
                    grade = grade,
                    pointsEarned = reward,
                    imageHash = imageHash
                )
            )

            saveToPrefs()
            return reward
        }
        return 0
    }

    fun redeemCoupon(title: String, brand: String, pointsCost: Int): CouponRecord? {
        if (currentPoints >= pointsCost) {
            currentPoints -= pointsCost
            val randomBarcode = "5090 ${ (1000..9999).random() } ${ (1000..9999).random() } ${ (10..99).random() }"
            val coupon = CouponRecord(
                title = title,
                brand = brand,
                barcodeNumber = randomBarcode,
                pointsCost = pointsCost
            )
            redeemedCoupons.add(0, coupon)
            saveToPrefs()
            return coupon
        }
        return null
    }

    fun rechargePoints(amount: Int) {
        currentPoints += amount
        saveToPrefs()
    }
}
