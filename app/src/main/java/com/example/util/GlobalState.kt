package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.BuildConfig
import com.example.repository.EcoGradeCalculator
import java.security.MessageDigest
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
    var userName by mutableStateOf("에코회원")
    var userEmail by mutableStateOf("")
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

    // Local user registry: email -> UserAccount
    private val registeredUsers = mutableMapOf<String, UserAccount>()

    data class UserAccount(
        val name: String,
        val email: String,
        var passwordHash: String,
        val apartment: String
    )

    fun hashPassword(password: String): String {
        val salted = "ecopick_salt_$password"
        val bytes = salted.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    val isAdmin: Boolean
        get() = BuildConfig.DEBUG && (userEmail == "admin@ecopick.kr" || userEmail == "admin@ecopick.local")

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("ecosort_prefs", Context.MODE_PRIVATE)
            loadFromPrefs()
        }
    }

    private fun loadFromPrefs() {
        prefs?.let { p ->
            isLoggedIn = p.getBoolean("is_logged_in", false)
            userName = p.getString("user_name", "에코회원") ?: "에코회원"
            userEmail = p.getString("user_email", "") ?: ""
            apartmentId = p.getString("apartment_id", "래미안 에코팰리스") ?: "래미안 에코팰리스"
            currentPoints = p.getInt("current_points", 5000)
            targetGoal = p.getInt("target_goal", 10)
            currentCount = p.getInt("current_count", 3)
            totalAppRecycled = p.getInt("total_recycled", 12450)
            customApiKey = p.getString("custom_api_key", "") ?: ""
            notifyRecycleDay = p.getBoolean("notify_recycle", true)
            notifyPoints = p.getBoolean("notify_points", true)
            notifyRanking = p.getBoolean("notify_ranking", false)

            dailyRecycleCount = p.getInt("daily_recycle_count", 0)
            dailyPointsEarned = p.getInt("daily_points_earned", 0)
            lastRecordedDay = p.getString("last_recorded_day", "") ?: ""
        }

        // Initialize default sample demo accounts with hashed passwords
        registeredUsers["demo@ecopick.kr"] = UserAccount("에코체험자", "demo@ecopick.kr", hashPassword("ecopick123"), "래미안 에코팰리스")
        registeredUsers["admin@ecopick.kr"] = UserAccount("관리사무소", "admin@ecopick.kr", hashPassword("admin123"), "래미안 에코팰리스")

        if (recycleHistory.isEmpty()) {
            recycleHistory.add(
                RecycleRecord(
                    material = "투명 페트병",
                    itemName = "생수 500ml 페트병",
                    pollutionPercent = 0,
                    grade = 0,
                    pointsEarned = 100,
                    imageHash = "1111000011110000111100001111000011110000111100001111000011110000",
                    dateString = "2026-08-27 14:20"
                )
            )
            recycleHistory.add(
                RecycleRecord(
                    material = "플라스틱",
                    itemName = "배달 샐러드 보울",
                    pollutionPercent = 4,
                    grade = 0,
                    pointsEarned = 100,
                    imageHash = "0000111100001111000011110000111100001111000011110000111100001111",
                    dateString = "2026-08-26 19:10"
                )
            )
            recycleHistory.add(
                RecycleRecord(
                    material = "알루미늄 캔",
                    itemName = "탄산음료 캔",
                    pollutionPercent = 0,
                    grade = 0,
                    pointsEarned = 100,
                    imageHash = "1010101010101010101010101010101010101010101010101010101010101010",
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

            putInt("daily_recycle_count", dailyRecycleCount)
            putInt("daily_points_earned", dailyPointsEarned)
            putString("last_recorded_day", lastRecordedDay)
            apply()
        }
    }

    fun registerUser(name: String, email: String, password: String, apartment: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank()) return false
        val hashed = hashPassword(password)
        registeredUsers[cleanEmail] = UserAccount(name.trim(), cleanEmail, hashed, apartment)
        userName = name.trim()
        userEmail = cleanEmail
        apartmentId = apartment
        isLoggedIn = true
        saveToPrefs()
        return true
    }

    fun loginUser(email: String, password: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank()) return false
        val account = registeredUsers[cleanEmail] ?: return false
        val inputHash = hashPassword(password)
        if (account.passwordHash == inputHash) {
            userName = account.name
            userEmail = account.email
            if (account.apartment.isNotEmpty()) {
                apartmentId = account.apartment
            }
            isLoggedIn = true
            saveToPrefs()
            return true
        }
        return false
    }

    fun resetPassword(email: String, newPass: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val account = registeredUsers[cleanEmail]
        return if (account != null) {
            account.passwordHash = hashPassword(newPass)
            true
        } else {
            false
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
        dailyRecycleCount = 0
        dailyPointsEarned = 0
        recycleHistory.clear()
        redeemedCoupons.clear()
        if (userEmail.isNotBlank()) {
            registeredUsers.remove(userEmail)
        }
        userEmail = ""
        userName = "에코회원"
        customApiKey = ""
        prefs?.edit()?.clear()?.apply()
    }

    // Daily quota & anti-abuse tracking
    var dailyRecycleCount by mutableIntStateOf(0)
    var dailyPointsEarned by mutableIntStateOf(0)
    private var lastRecordedDay = ""

    const val MAX_DAILY_RECYCLE_COUNT = 15
    const val MAX_DAILY_POINTS = 1500

    fun getApiKey(): String {
        return if (customApiKey.isNotBlank()) customApiKey.trim() else BuildConfig.GEMINI_API_KEY
    }

    fun checkAndResetDailyQuota() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (lastRecordedDay != today) {
            dailyRecycleCount = 0
            dailyPointsEarned = 0
            lastRecordedDay = today
            saveToPrefs()
        }
    }

    fun canRecycleToday(): Boolean {
        checkAndResetDailyQuota()
        return dailyRecycleCount < MAX_DAILY_RECYCLE_COUNT && dailyPointsEarned < MAX_DAILY_POINTS
    }

    fun isImageHashDuplicated(newHash: String): Boolean {
        if (newHash.isBlank()) return false
        return recycleHistory.any { record ->
            ImageHashUtil.isSimilar(record.imageHash, newHash, threshold = 5)
        }
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

            // EcoGradeCalculator 기준 단일 진실 소스로 포인트 산정 (A: 100P, B: 70P, C: 40P, F: 0P)
            var reward = EcoGradeCalculator.getPointsForPollution(pollutionPercent.toDouble())

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
