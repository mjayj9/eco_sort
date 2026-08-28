package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.repository.AiVisionRepository
import com.example.repository.EcoGradeCalculator
import com.example.repository.FirestoreRepository
import com.example.util.GlobalState
import com.example.util.ImageHashUtil
import kotlinx.coroutines.launch
import org.json.JSONObject

fun getBitmapFromDrawable(context: Context, resId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, resId) ?: return Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 400
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 400
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        null
    }
}

enum class ScanStep { INITIAL, ANALYZED, DONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScannerScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val apartmentId = GlobalState.apartmentId
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var verifyImage by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("AI가 오염도를 정밀 분석 중입니다...") }
    var resultJson by remember { mutableStateOf<JSONObject?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var earnedPoints by remember { mutableIntStateOf(0) }

    var scanStep by remember { mutableStateOf(ScanStep.INITIAL) }
    var firstImageHash by remember { mutableStateOf<String?>(null) }
    var lastAnalysisTime by remember { mutableStateOf(0L) }
    var showGuideDialog by remember { mutableStateOf(false) }

    // Helper to start real AI analysis
    fun startRealAiAnalysis(bitmap: Bitmap) {
        if (!GlobalState.canRecycleToday()) {
            errorMsg = "오늘 분리배출 참여 횟수(최대 15회) 또는 일일 적립 한도(1,500P)를 모두 달성하였습니다. 내일 다시 참여해주세요!"
            return
        }

        val currentHash = ImageHashUtil.generateImageHash(bitmap)
        if (GlobalState.isImageHashDuplicated(currentHash)) {
            errorMsg = "이미 등록되었거나 매우 유사한 배출 사진입니다. (어뷰징 방지: 중복 사진 감지)"
            return
        }

        capturedImage = bitmap
        isLoading = true
        loadingMessage = "Gemini AI가 쓰레기 오염도와 재질을 분석 중입니다..."
        errorMsg = null
        resultJson = null

        coroutineScope.launch {
            try {
                val resultStr = AiVisionRepository.analyzeWasteImage(bitmap)
                isLoading = false
                val cleanResult = resultStr.trim()
                val json = JSONObject(cleanResult)

                if (json.has("error")) {
                    errorMsg = json.getString("error")
                } else {
                    val isSuccess = json.optBoolean("판독_성공", false)
                    if (isSuccess) {
                        resultJson = json
                        scanStep = ScanStep.ANALYZED
                        firstImageHash = currentHash
                        lastAnalysisTime = System.currentTimeMillis()
                    } else {
                        errorMsg = json.optString(
                            "불가_사유",
                            "판독이 불가합니다. 어둡거나 흔들렸다면 밝은 곳에 두고 다시 촬영해주세요."
                        )
                        capturedImage = null
                        resultJson = null
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                errorMsg = "AI 분석 응답 처리 중 오류가 발생했습니다: ${e.localizedMessage}"
            }
        }
    }

    // Helper to verify disposal background
    fun startDisposalVerification(bitmap: Bitmap) {
        val newHash = ImageHashUtil.generateImageHash(bitmap)
        val timeDiff = System.currentTimeMillis() - lastAnalysisTime

        val isSimilarToFirst = firstImageHash?.let { ImageHashUtil.isSimilar(newHash, it, threshold = 5) } ?: false
        if (isSimilarToFirst) {
            errorMsg = "1차 쓰레기 사진과 동일/유사한 사진은 2차 배출 인증에 사용할 수 없습니다. 배출 장소(분리수거함)가 보이도록 촬영해주세요."
            return
        }
        if (timeDiff < 2000) {
            errorMsg = "분석 후 최소 2초 뒤에 배출 인증이 가능합니다 (어뷰징 방지)."
            return
        }

        verifyImage = bitmap
        isLoading = true
        loadingMessage = "배출 장소(분리수거함/쓰레기통)를 AI로 검증 중입니다..."
        errorMsg = null

        coroutineScope.launch {
            try {
                val resultStr = AiVisionRepository.verifyDisposalBackground(bitmap)
                isLoading = false
                val verifyJson = JSONObject(resultStr.trim())

                if (verifyJson.has("error")) {
                    errorMsg = verifyJson.getString("error")
                } else {
                    val isPass = verifyJson.optBoolean("통과", false)
                    if (isPass) {
                        val material = resultJson?.optString("재질", "플라스틱") ?: "플라스틱"
                        val itemName = resultJson?.optString("품목명", "배달 용기") ?: "배달 용기"
                        val pollution = resultJson?.optInt("오염도_퍼센트", 0) ?: 0
                        val grade = EcoGradeCalculator.getGradeLevel(pollution.toDouble())

                        val reward = GlobalState.addRecycle(
                            material = material,
                            isSuccess = true,
                            itemName = itemName,
                            pollutionPercent = pollution,
                            grade = grade,
                            imageHash = newHash
                        )
                        FirestoreRepository.verifyAndReward(
                            apartmentId = apartmentId,
                            points = reward,
                            material = material,
                            itemName = itemName,
                            pollutionPercent = pollution,
                            grade = grade,
                            imageHash = newHash
                        )
                        earnedPoints = reward
                        scanStep = ScanStep.DONE
                    } else {
                        errorMsg = verifyJson.optString(
                            "사유",
                            "분리수거함이나 올바른 배출장소가 인식되지 않았습니다. 배경에 분리수거함이 보이도록 다시 찍어주세요."
                        )
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                errorMsg = "배경 인식 중 오류가 발생했습니다. 다시 시도해주세요."
            }
        }
    }

    // Camera Launcher for 1st scan
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            startRealAiAnalysis(bitmap)
        }
    }

    // Gallery Launcher for 1st scan
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bitmap = getBitmapFromUri(context, uri)
            if (bitmap != null) {
                startRealAiAnalysis(bitmap)
            }
        }
    }

    // Camera Launcher for 2nd verification
    val verifyCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            startDisposalVerification(bitmap)
        }
    }

    // Gallery Launcher for 2nd verification
    val verifyGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bitmap = getBitmapFromUri(context, uri)
            if (bitmap != null) {
                startDisposalVerification(bitmap)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (scanStep == ScanStep.ANALYZED) {
                verifyCameraLauncher.launch(null)
            } else {
                cameraLauncher.launch(null)
            }
        } else {
            errorMsg = "카메라를 사용하려면 권한을 허용해주세요. 갤러리에서 사진을 선택할 수도 있습니다."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "AI 스캐너 (목표: ${GlobalState.currentCount}/${GlobalState.targetGoal})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "단지: $apartmentId",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showGuideDialog = true }) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = "분리배출 가이드",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "${GlobalState.currentPoints}P",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main image preview box
            if (capturedImage == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "카메라",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "배달 쓰레기 사진을 촬영하거나 선택하세요",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Gemini 멀티모달 AI가 오염도와 재활용 방법을 실시간 판정합니다",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Action Buttons (Camera & Gallery)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("scan_camera_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("카메라 촬영", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("scan_gallery_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("갤러리 사진", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive AI Demonstration Cards (Uses REAL AI Vision)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "실시간 AI 판독 샘플 테스트",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            "샘플을 탭하면 실제 AI가 이미지를 분석하여 실시간 판정합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val cleanResId = R.drawable.clean_bottle
                            val dirtyResId = R.drawable.dirty_plate

                            // Clean sample card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val bitmap = getBitmapFromDrawable(context, cleanResId)
                                        startRealAiAnalysis(bitmap)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = cleanResId),
                                        contentDescription = "깨끗한 페트병 샘플",
                                        modifier = Modifier
                                            .height(90.dp)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "🟢 깨끗한 페트병 (AI 분석)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Dirty sample card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val bitmap = getBitmapFromDrawable(context, dirtyResId)
                                        startRealAiAnalysis(bitmap)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = dirtyResId),
                                        contentDescription = "오염된 용기 샘플",
                                        modifier = Modifier
                                            .height(90.dp)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "🔴 양념 오염 용기 (AI 분석)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Image Display Box with Real Bounding Box Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = capturedImage!!.asImageBitmap(),
                        contentDescription = "촬영된 쓰레기 이미지",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Draw AI Bounding Box on top of Image
                    resultJson?.let {
                        val bbox = it.optJSONObject("오염부분_좌표")
                        if (bbox != null) {
                            val ymin = bbox.optDouble("ymin", 0.0).toFloat()
                            val xmin = bbox.optDouble("xmin", 0.0).toFloat()
                            val ymax = bbox.optDouble("ymax", 0.0).toFloat()
                            val xmax = bbox.optDouble("xmax", 0.0).toFloat()

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val left = xmin * canvasWidth
                                val top = ymin * canvasHeight
                                val right = xmax * canvasWidth
                                val bottom = ymax * canvasHeight

                                drawRect(
                                    color = Color.Red,
                                    topLeft = Offset(left, top),
                                    size = Size((right - left).coerceAtLeast(10f), (bottom - top).coerceAtLeast(10f)),
                                    style = Stroke(width = 6f)
                                )
                            }
                        }
                    }

                    if (isLoading) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    loadingMessage,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Error Display
            errorMsg?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "경고",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Step: DONE (Success Celebration)
            if (scanStep == ScanStep.DONE) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "성공",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("🎊 분리배출 실천 인증 완료! 🎊", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "+${earnedPoints} 에코 포인트 적립 완료!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "[$apartmentId] 단지 누적 실적 및 랭킹에 즉시 반영되었습니다.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val shareText = """
                            🌿 나는 오늘 지구를 구했어요!
                            [$apartmentId] 단지 분리배출 챌린지 참여 중!
                            내 에코 포인트: ${GlobalState.currentPoints}P (${GlobalState.currentCount}회 달성)
                            #에코픽 #EcoPick #분리배출 #ESG #AI스마트분리수거
                        """.trimIndent()

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "나의 에코 실천 자랑하기")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("캠페인 자랑하기 (SNS 공유)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        capturedImage = null
                        verifyImage = null
                        resultJson = null
                        scanStep = ScanStep.INITIAL
                        errorMsg = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("새로운 쓰레기 스캔하기")
                }
            } else {
                // Step: ANALYZED (Show AI Output)
                resultJson?.let {
                    val pollutionLevel = it.optInt("오염도_퍼센트", 0)
                    val grade = it.optInt("등급", 0)
                    val method = it.optString("배출방법", "분리배출함에 배출")
                    val material = it.optString("재질", "플라스틱")
                    val itemName = it.optString("품목명", "배달 용기")
                    val feedback = it.optString("피드백", "")
                    val stateText = it.optString("상태", material)
                    val analysisMode = it.optString("분석모드", "Gemini_AI_비전")

                    val isPass = grade <= 1
                    val isWashable = grade == 2

                    val statusColor = when {
                        isPass -> MaterialTheme.colorScheme.primary
                        isWashable -> Color(0xFFF57C00) // Orange
                        else -> MaterialTheme.colorScheme.error
                    }
                    val statusContainerColor = when {
                        isPass -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        isWashable -> Color(0xFFFFF3E0)
                        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    }
                    val classificationText = when {
                        grade == 0 -> "A등급 (즉시 배출)"
                        grade == 1 -> "B등급 (가벼운 헹굼)"
                        grade == 2 -> "C등급 (세척 후 배출)"
                        else -> "F등급 (일반쓰레기)"
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (analysisMode == "오프라인_간이추정") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text(
                                "⚡ [오프라인 간이 분석 모드] 네트워크 미연결 상태로 로컬 비전 알고리즘을 통해 추정되었습니다.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Result Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Text("🤖", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI 비전 오염도 분석 및 가이드",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        "※ 본 결과는 AI 비전 영상 추정치이며, 실제 선별장 및 지자체 세부 기준과 일부 다를 수 있습니다.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    // Key Summary Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("📦 인식 물품 및 재질", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "$material\n($itemName)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = statusContainerColor),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🏷️ 분리배출 판정", fontSize = 11.sp, color = statusColor)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    classificationText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = statusColor
                                )
                                Text(
                                    "오염도 ${pollutionLevel}% (등급 $grade)",
                                    fontSize = 11.sp,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Comprehensive Guide Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Current Status Section
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔍", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "현재 상태 (AI 비전 진단)",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                if (pollutionLevel > 0) "오염도 ${pollutionLevel}% - $stateText" else "오염도 0% - 매우 깨끗한 상태",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 22.dp, top = 4.dp, bottom = 12.dp),
                                fontWeight = FontWeight.Medium
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Washing & Preparation Guide
                            if (feedback.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🚿", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "세척 및 분리 가이드",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    feedback,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 22.dp, top = 4.dp, bottom = 12.dp)
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Final Disposal Method
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("♻️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "최종 배출 방법 안내",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                method,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 22.dp, top = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2nd Stage Verification Action Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "📸 2차 실천 인증 (배출 장소 확인)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "실제로 분리수거함이나 쓰레기통에 배출하는 모습을 인증하면 에코 포인트를 지급합니다.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        errorMsg = null
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("verify_camera_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("배출 인증 촬영", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        errorMsg = null
                                        verifyGalleryLauncher.launch("image/*")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("verify_gallery_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("인증 사진 선택", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            capturedImage = null
                            resultJson = null
                            scanStep = ScanStep.INITIAL
                            errorMsg = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("다른 사진 다시 찍기")
                    }
                }
            }
        }
    }

    if (showGuideDialog) {
        AlertDialog(
            onDismissRequest = { showGuideDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💡 환경부 분리배출 4대 원칙")
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("1. 비운다: 용기 안의 내용물을 완전히 비웁니다.", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. 헹군다: 묻어있는 양념이나 이물질을 물로 깨끗이 씻습니다.", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("3. 분리한다: 라벨, 뚜껑 등 재질이 다른 부분을 분리합니다.", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("4. 섞지 않는다: 종류별(플라스틱, 비닐, 캔, 유리 등)로 구분 배출합니다.", fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("🎯 AI 판정 등급 및 포인트 기준", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("• A등급 (0~10% 오염): 투명/깨끗함 -> 즉시 배출 (+100P)")
                    Text("• B등급 (11~30% 오염): 가벼운 얼룩 -> 가벼운 헹굼 후 배출 (+70P)")
                    Text("• C등급 (31~60% 오염): 음식물/기름때 -> 주방세제 세척 후 배출 (+40P)")
                    Text("• F등급 (61~100% 오염): 심한 착색/찌꺼기 -> 일반쓰레기(종량제) 배출 (0P)")
                }
            },
            confirmButton = {
                Button(onClick = { showGuideDialog = false }) {
                    Text("확인")
                }
            }
        )
    }
}
