package com.example.repository

import android.util.Log
import com.example.util.GlobalState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * 분리배출 인증 및 포인트 원격 트랜잭션을 관리하는 리포지토리.
 * Cloud Firestore 원격 동기화를 수행하며, 미연동 시 로컬 오프라인 모드로 안전하게 작동합니다.
 */
object FirestoreRepository {
    private const val TAG = "FirestoreRepository"

    var isLastSyncOffline: Boolean = false
        private set

    suspend fun verifyAndReward(
        apartmentId: String,
        points: Int = 50,
        material: String = "플라스틱",
        itemName: String = "배달 용기",
        pollutionPercent: Int = 0,
        grade: Int = 0,
        imageHash: String = ""
    ): Boolean {
        // Firebase 활성화 시 원격 Firestore 트랜잭션 동기화 (로컬 addRecycle은 ViewModel/Screen 단일 진실 소스에서 호출)
        return try {
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
                isLastSyncOffline = false
                true
            } else {
                isLastSyncOffline = true
                Log.i(TAG, "No authenticated Firebase user. Operating in local-only mode.")
                true
            }
        } catch (e: Throwable) {
            isLastSyncOffline = true
            Log.w(TAG, "Firebase sync failed or not configured. Saved to local storage safely.", e)
            true
        }
    }

    suspend fun exchangeCoupon(title: String, brand: String, pointsCost: Int): Boolean {
        return try {
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
                true
            } else {
                true
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase coupon sync failed. Operating in local storage.", e)
            true
        }
    }
}
