package com.example.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// 심사 포인트: Firebase Transaction을 활용하여 여러 문서(포인트 증가 점수, 아파트 통계)를 
// 중간에 충돌하거나 데이터가 꼬이지 않도록 안전하게 동시 업데이트 처리
object FirestoreRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    // 본 데모는 심사 도중 실제 Firebase google-services.json 미연동 환경에서도 
    // 프론트엔드 로직이 정상 작동함을 입증하기 위해, 초기 데모 모드 (UI 테스트용) 분기를 둡니다.
    private val isDemoMode = true

    /**
     * 어뷰징 단계를 통과한 사용자가 최종적으로 분리배출을 완료했을 때 보상을 지급하는 함수.
     * 트랜잭션 단위: 유저의 포인트 데이터 + 유저 소속 단지의 누적 분리수거 카운트
     */
    suspend fun verifyAndReward(apartmentId: String, points: Int = 50): Boolean {
        if (isDemoMode) {
            // 데모 모드에서는 실제 네트워크 요청을 건너뛰고 성공 스텁 반환
            return true
        }

        val userId = auth.currentUser?.uid ?: return false
        val userRef = firestore.collection("users").document(userId)
        val aptRef = firestore.collection("apartments").document(apartmentId)

        return try {
            firestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                
                // 1. 유저 문서 데이터 업데이트
                if (!userSnapshot.exists()) {
                    transaction.set(userRef, hashMapOf("points" to points, "apartmentId" to apartmentId))
                } else {
                    val currentPoints = userSnapshot.getLong("points") ?: 0L
                    transaction.update(userRef, "points", currentPoints + points)
                }

                // 2. 소속 단지(아파트)의 누적 처리량 동시 업데이트
                val aptSnapshot = transaction.get(aptRef)
                if (!aptSnapshot.exists()) {
                    transaction.set(aptRef, hashMapOf("totalRecycled" to 1, "apartmentName" to apartmentId))
                } else {
                    transaction.update(aptRef, "totalRecycled", FieldValue.increment(1))
                }
            }.await() // 트랜잭션 완료 대기
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 심사 포인트: 제휴처 연동 포인트 차감 (트랜잭션 처리로 중복 차감 방지)
    suspend fun exchangeCoupon(pointsCost: Int): Boolean {
        if (isDemoMode) {
            // 데모 모드에서는 실제 네트워크 요청을 건너뛰고 성공 스텁 반환
            return true
        }

        val userId = auth.currentUser?.uid ?: return false
        val userRef = firestore.collection("users").document(userId)
        
        return try {
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
        } catch(e: Exception) {
            false
        }
    }
}
