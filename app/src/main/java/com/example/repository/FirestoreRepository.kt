package com.example.repository

import com.example.util.GlobalState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * 분리배출 인증 및 포인트 트랜잭션을 관리하는 리포지토리.
 * Cloud Firestore 연동과 로컬 상태 저장을 유기적으로 처리합니다.
 */
object FirestoreRepository {

    suspend fun verifyAndReward(
        apartmentId: String,
        points: Int = 50,
        material: String = "플라스틱",
        itemName: String = "배달 용기",
        pollutionPercent: Int = 0,
        grade: Int = 0,
        imageHash: String = ""
    ): Boolean {
        // 1. 로컬 상태 즉시 반영 및 저장
        GlobalState.addRecycle(
            material = material,
            isSuccess = true,
            itemName = itemName,
            pollutionPercent = pollutionPercent,
            grade = grade,
            imageHash = imageHash
        )

        // 2. Firebase 활성화 시 원격 트랜잭션 동기화
        try {
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()
                val userRef = firestore.collection("users").document(userId)
                val aptRef = firestore.collection("apartments").document(apartmentId)

                firestore.runTransaction { transaction ->
                    val userSnapshot = transaction.get(userRef)
                    if (!userSnapshot.exists()) {
                        transaction.set(userRef, hashMapOf("points" to points, "apartmentId" to apartmentId))
                    } else {
                        val currentPoints = userSnapshot.getLong("points") ?: 0L
                        transaction.update(userRef, "points", currentPoints + points)
                    }

                    val aptSnapshot = transaction.get(aptRef)
                    if (!aptSnapshot.exists()) {
                        transaction.set(aptRef, hashMapOf("totalRecycled" to 1, "apartmentName" to apartmentId))
                    } else {
                        transaction.update(aptRef, "totalRecycled", FieldValue.increment(1))
                    }
                }.await()
            }
        } catch (e: Throwable) {
            // Firebase가 설정되지 않은 환경에서도 로컬 상태로 원활히 지속 작동
        }
        return true
    }

    suspend fun exchangeCoupon(title: String, brand: String, pointsCost: Int): Boolean {
        val coupon = GlobalState.redeemCoupon(title, brand, pointsCost) ?: return false

        try {
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()
                val userRef = firestore.collection("users").document(userId)

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val currentPoints = snapshot.getLong("points") ?: 0L
                    if (currentPoints >= pointsCost) {
                        transaction.update(userRef, "points", currentPoints - pointsCost)
                    } else {
                        throw Exception("Not enough points")
                    }
                }.await()
            }
        } catch (e: Throwable) {
            // Firebase 미연동 시에도 로컬 포인트 차감 및 쿠폰 발급 유지
        }
        return true
    }
}
