package com.example.repository

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import com.example.BuildConfig
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.InlineData
import com.example.network.Part
import com.example.network.RetrofitClient
import com.example.util.GlobalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream

fun Bitmap.toBase64AndResize(): String {
    // 1. Resize to prevent Payload Too Large (max dimension 1024)
    val maxDimension = 1024
    val scale = if (width > height) {
        maxDimension.toFloat() / width
    } else {
        maxDimension.toFloat() / height
    }
    
    val resizedBitmap = if (scale < 1) {
        Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
    } else {
        this
    }

    // 2. Compress
    val outputStream = ByteArrayOutputStream()
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}

object AiVisionRepository {

    private fun extractJsonFromResponse(rawText: String): String {
        val trimmed = rawText.trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        return if (start != -1 && end != -1 && end >= start) {
            trimmed.substring(start, end + 1)
        } else {
            trimmed
        }
    }

    /**
     * Gemini 멀티모달 AI(Gemini 2.5 Flash)를 이용해 쓰레기 이미지의 오염도, 재질, 세척법, 배출법을 비전 분석합니다.
     */
    suspend fun analyzeWasteImage(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val apiKey = GlobalState.getApiKey()
        val base64Img = bitmap.toBase64AndResize()

        val prompt = """
            너는 대한민국 환경부 분리배출 지침 및 AI 비전 기반 배달 쓰레기 분석 전문가 AI야.
            전달된 이미지 속 배달 쓰레기(플라스틱 용기, 비닐, 캔, 유리, 스티로폼, 종이 등)를 시각적으로 분석하여 다음 기준에 따라 반드시 순수 JSON 포맷으로만 응답해.
            
            [오염도 및 등급 판정 기준 - EcoGradeCalculator 표준]
            - 등급 0 (A등급, 오염도 0~10%): 완전히 깨끗하거나 가벼운 물자국 수준 -> 즉시 분리배출 가능 (포인트 +100P)
            - 등급 1 (B등급, 오염도 11~30%): 옅은 오염 또는 가벼운 먼지 -> 가벼운 헹굼 후 배출 (+70P)
            - 등급 2 (C등급, 오염도 31~60%): 음식물 양념, 국물, 기름때가 묻어있어 주방세제 세척이 필요한 상태 -> 정밀 세척 후 분리배출 (+40P)
            - 등급 3 (F등급, 오염도 61~100%): 짙은 착색, 음식물 찌꺼기로 세척이 불가능한 상태 -> 일반쓰레기(종량제 봉투) 배출 (0P)
            
            만약 사진이 너무 어둡거나, 흔들렸거나, 쓰레기 객체가 전혀 보이지 않는 경우 "판독_성공": false 로 응답해.
            
            [반환 JSON 스키마 - 다른 텍스트나 설명 없이 오직 JSON만 반환]:
            {
              "판독_성공": true,
              "분석모드": "Gemini_AI_비전",
              "재질": "플라스틱", 
              "품목명": "배달 떡볶이 용기",
              "오염도_퍼센트": 45,
              "등급": 2,
              "상태": "붉은 양념 얼룩 및 기름기 오염 감지",
              "피드백": "양념과 기름때가 묻어있습니다. 따뜻한 물과 주방세제로 1~2회 헹군 뒤 배출해주세요. 붉은 착색이 지워지지 않으면 종량제 봉투에 버려야 합니다.",
              "헹굼_권장여부": true,
              "배출방법": "세척 후 플라스틱 수거함 배출 (착색 심할 시 종량제 봉투 배출)",
              "오염부분_좌표": {
                "ymin": 0.25,
                "xmin": 0.3,
                "ymax": 0.75,
                "xmax": 0.7
              },
              "불가_사유": ""
            }
            
            판독 불가 시:
            {
              "판독_성공": false,
              "분석모드": "Gemini_AI_비전",
              "불가_사유": "사진이 너무 어둡거나 쓰레기 객체를 식별할 수 없습니다. 밝은 조명 아래에서 다시 촬영해주세요."
            }
        """.trimIndent()

        val requestBody = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Img))
                    )
                )
            ),
            generationConfig = com.example.network.GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        // 1. If valid API key exists, call real Gemini API
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val response = try {
                    RetrofitClient.service.generateContent(apiKey, requestBody)
                } catch (e: retrofit2.HttpException) {
                    if (e.code() in 400..499) {
                        if (e.code() == 429) {
                            return@withContext """{"error": "Gemini API 요청 한도를 초과했습니다 (HTTP 429). 1분 후 다시 시도해주세요."}"""
                        } else if (e.code() == 400 || e.code() == 403) {
                            return@withContext """{"error": "Gemini API 키가 유효하지 않거나 권한이 없습니다 (HTTP ${e.code()}). 설정에서 API 키를 확인해주세요."}"""
                        }
                        throw e
                    } else {
                        // 5xx Server error -> try fallback lite model
                        RetrofitClient.service.generateContentFallback(apiKey, requestBody)
                    }
                } catch (e: Exception) {
                    RetrofitClient.service.generateContentFallback(apiKey, requestBody)
                }
                
                val rawText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!rawText.isNullOrBlank()) {
                    val cleanJson = extractJsonFromResponse(rawText)
                    JSONObject(cleanJson)
                    return@withContext cleanJson
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 429) {
                    return@withContext """{"error": "API 분당 최대 요청 수를 초과했습니다 (HTTP 429). 1분 후 다시 시도해주세요."}"""
                }
                return@withContext """{"error": "AI 서버 통신 오류 (HTTP ${e.code()}): ${e.message()}"}"""
            } catch (e: Exception) {
                // Network connection failure or offline
            }
        }

        // 2. Local heuristic image analysis for offline support
        return@withContext analyzeImageHeuristic(bitmap)
    }

    /**
     * 2차 인증: 배출 장소(분리수거함, 쓰레기통, 종량제 봉투) 배경 검증 AI
     */
    suspend fun verifyDisposalBackground(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val apiKey = GlobalState.getApiKey()
        val base64Img = bitmap.toBase64AndResize()

        val prompt = """
            사용자가 쓰레기를 버린 후 촬영한 2차 실천 인증 사진이야.
            사진의 배경이나 주변에 분리수거함(플라스틱/캔/유리/비닐 수거함), 공용 쓰레기통, 클린하우스, 또는 종량제 봉투 등 '올바른 폐기/배출 장소'가 식별되는지 정밀 검정해.
            
            [반환 JSON 스키마 - 순수 JSON만 반환]:
            {
              "통과": true,
              "사유": "분리수거함 및 올바른 배출 위치가 정상 확인되었습니다.",
              "배출장소_유형": "아파트 단지 분리수거장"
            }
            
            만약 일반 방(침대, 책상, 거실 바닥) 등 배출 장소가 전혀 보이지 않거나 쓰레기만 단독으로 찍힌 경우:
            {
              "통과": false,
              "사유": "분리수거함이나 쓰레기통이 배경에 보이지 않습니다. 배출 장소와 함께 다시 촬영해주세요."
            }
        """.trimIndent()

        val requestBody = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Img))
                    )
                )
            ),
            generationConfig = com.example.network.GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.1f
            )
        )

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val response = try {
                    RetrofitClient.service.generateContent(apiKey, requestBody)
                } catch (e: retrofit2.HttpException) {
                    if (e.code() in 400..499) {
                        if (e.code() == 429) {
                            return@withContext """{"error": "API 분당 최대 요청 수를 초과했습니다. 잠시 후 다시 시도해주세요."}"""
                        }
                        throw e
                    } else {
                        RetrofitClient.service.generateContentFallback(apiKey, requestBody)
                    }
                } catch (e: Exception) {
                    RetrofitClient.service.generateContentFallback(apiKey, requestBody)
                }
                
                val rawText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!rawText.isNullOrBlank()) {
                    val cleanJson = extractJsonFromResponse(rawText)
                    JSONObject(cleanJson)
                    return@withContext cleanJson
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 429) {
                    return@withContext """{"error": "API 분당 최대 요청 수를 초과했습니다. 잠시 후 다시 시도해주세요."}"""
                }
            } catch (e: Exception) {
                // fall through to fail-safe rejection
            }
        }

        // Fail-safe: Reject verification if AI is unreachable to prevent false reward abuse
        return@withContext """
            {
              "통과": false,
              "사유": "AI 인증 서버 연결이 원활하지 않습니다. 인터넷 연결 및 API 키 상태를 확인한 후 다시 시도해주세요."
            }
        """.trimIndent()
    }

    /**
     * 로컬 픽셀 컬러 및 채도 분석 기반 정밀 휴리스틱 (오프라인/API 미설정 시 안전 대비)
     */
    private fun analyzeImageHeuristic(bitmap: Bitmap): String {
        val sampleSize = 32
        val scaled = Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, false)
        var redStainCount = 0
        var darkStainCount = 0
        var totalPixels = sampleSize * sampleSize

        for (x in 0 until sampleSize) {
            for (y in 0 until sampleSize) {
                val pixel = scaled.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // 붉은 양념 / 기름때 검출
                if (r > 130 && r > g * 1.3 && r > b * 1.3) {
                    redStainCount++
                } else if (r < 60 && g < 60 && b < 60) {
                    darkStainCount++
                }
            }
        }

        val redRatio = (redStainCount.toFloat() / totalPixels) * 100f
        val isContaminated = redRatio > 3.0f

        return if (!isContaminated) {
            """
            {
              "판독_성공": true,
              "분석모드": "오프라인_간이추정",
              "재질": "플라스틱 (PET/PP)",
              "품목명": "투명 음료 페트병 / 깨끗한 용기",
              "오염도_퍼센트": 0,
              "등급": 0,
              "상태": "오염 없는 깨끗한 상태 감지 (오프라인 추정)",
              "피드백": "오프라인 분석: 내용물이 비워져 있고 육안 오염이 적습니다. 라벨을 제거하고 분리수거함에 배출하세요.",
              "헹굼_권장여부": false,
              "배출방법": "플라스틱 / 투명 페트 전용 분리수거함에 배출",
              "오염부분_좌표": {"ymin": 0.1, "xmin": 0.2, "ymax": 0.9, "xmax": 0.8},
              "불가_사유": ""
            }
            """.trimIndent()
        } else {
            val estimatedPollution = (redRatio * 4.5f).toInt().coerceIn(15, 85)
            val grade = EcoGradeCalculator.getGradeLevel(estimatedPollution.toDouble())
            """
            {
              "판독_성공": true,
              "분석모드": "오프라인_간이추정",
              "재질": "플라스틱 (배달 용기)",
              "품목명": "양념 배달음식 포장 용기",
              "오염도_퍼센트": $estimatedPollution,
              "등급": $grade,
              "상태": "붉은 양념 및 얼룩 감지 (오프라인 추정 오염도 ${estimatedPollution}%)",
              "피드백": "오프라인 분석: 용기 내부에 붉은 기름 및 양념이 감지되었습니다. 따뜻한 물과 주방세제로 세척해주세요.",
              "헹굼_권장여부": true,
              "배출방법": ${if (grade >= 3) "\"오염도 ${estimatedPollution}%로 세척이 어렵습니다. 종량제 봉투(일반쓰레기)에 배출하세요.\"" else "\"깨끗이 세척 후 플라스틱 전용 수거함에 배출하세요.\""},
              "오염부분_좌표": {"ymin": 0.25, "xmin": 0.2, "ymax": 0.8, "xmax": 0.8},
              "불가_사유": ""
            }
            """.trimIndent()
        }
    }
}
