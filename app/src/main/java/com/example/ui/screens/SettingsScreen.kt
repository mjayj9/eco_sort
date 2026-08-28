package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.util.GlobalState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit = {},
    onChangeApartment: () -> Unit = {}
) {
    val context = LocalContext.current
    var newGoal by remember { mutableStateOf(GlobalState.targetGoal.toString()) }
    var apiKeyInput by remember { mutableStateOf(GlobalState.customApiKey) }
    var goalMessage by remember { mutableStateOf("") }
    var keyMessage by remember { mutableStateOf("") }

    // Dialog states
    var showResetDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showChangeAptDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val apartmentList = listOf(
        "래미안 에코팰리스",
        "자이 더 그린",
        "힐스테이트 클린시티",
        "e편한세상 스마트에코",
        "푸르지오 리사이클가든",
        "아이파크 네이처뷰",
        "LH 그린빌리지",
        "기타 일반 주택 및 빌라"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정 및 마이페이지", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. User Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = GlobalState.userName.take(1).ifBlank { "에" },
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = GlobalState.userName.ifBlank { "에코 회원" },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = GlobalState.userEmail.ifBlank { "guest@ecopick.kr" },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        AssistChip(
                            onClick = { showChangeAptDialog = true },
                            label = { Text("단지 변경") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Apartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("소속 단지: ", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(GlobalState.apartmentId, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            "보유: ${GlobalState.currentPoints}P",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 2. Notification Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("알림 설정", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("분리배출 요일 알림", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("단지별 배출 요일(매주 화/목/일) 전날 저녁 안내", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = GlobalState.notifyRecycleDay,
                            onCheckedChange = {
                                GlobalState.notifyRecycleDay = it
                                GlobalState.saveToPrefs()
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("에코 포인트 및 보상 알림", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("미션 달성 및 포인트 적립 시 즉시 푸시", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = GlobalState.notifyPoints,
                            onCheckedChange = {
                                GlobalState.notifyPoints = it
                                GlobalState.saveToPrefs()
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("단지 랭킹 순위 변동 알림", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("우리 아파트의 실시간 순위 변동 알림", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = GlobalState.notifyRanking,
                            onCheckedChange = {
                                GlobalState.notifyRanking = it
                                GlobalState.saveToPrefs()
                            }
                        )
                    }
                }
            }

            // 3. Goal Settings & Tiered Rewards Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrackChanges, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("목표 설정 (보상 차등 지급)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "분리배출 목표 개수를 높게 설정할수록, 달성 시 더 많은 보너스 에코 포인트가 지급됩니다.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newGoal,
                            onValueChange = { newGoal = it.filter { c -> c.isDigit() } },
                            label = { Text("목표 개수 (건)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val goal = newGoal.toIntOrNull()
                                if (goal != null && goal > 0) {
                                    GlobalState.targetGoal = goal
                                    GlobalState.saveToPrefs()
                                    goalMessage = "목표가 ${goal}건으로 저장되었습니다! (달성 보너스: +${goal * 20}P)"
                                } else {
                                    goalMessage = "유효한 목표 개수를 입력해주세요."
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("저장", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (goalMessage.isNotEmpty()) {
                        Text(
                            goalMessage,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "현재 실천 진행률: ${GlobalState.currentCount} / ${GlobalState.targetGoal} 건",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (GlobalState.targetGoal > 0) {
                                (GlobalState.currentCount.toFloat() / GlobalState.targetGoal).coerceIn(0f, 1f)
                            } else 0f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 4. Gemini AI Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini AI API 설정", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "기본적으로 AI Studio 환경 변수(GEMINI_API_KEY)가 주입되어 동작합니다. 개별 API Key로 테스트를 원하시면 아래에 입력하세요.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("사용자 커스텀 Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (apiKeyInput.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    apiKeyInput = ""
                                    GlobalState.customApiKey = ""
                                    GlobalState.saveToPrefs()
                                    keyMessage = "기본 주입 API Key로 재설정되었습니다."
                                }
                            ) {
                                Text("기본값 초기화")
                            }
                        }
                        Button(
                            onClick = {
                                GlobalState.customApiKey = apiKeyInput.trim()
                                GlobalState.saveToPrefs()
                                keyMessage = if (apiKeyInput.isNotBlank()) "커스텀 API Key가 저장되었습니다." else "기본 API Key가 적용됩니다."
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("적용", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (keyMessage.isNotEmpty()) {
                        Text(
                            keyMessage,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // 5. Terms & Policy Button
            OutlinedCard(
                onClick = { showTermsDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("서비스 이용약관 및 개인정보 처리방침", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 6. Project & Team Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "창업 프로젝트 소개 (사업계획서 요약)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• 대회명: 2026 대한민국 청소년 창업경진대회", fontSize = 12.sp)
                    Text("• 창업아이템명: 에코픽(EcoPick) - 우리 단지 분리배출 도우미", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("• 소속: 국립대학법인 서울대학교사범대학부설중학교 (SNUMS_psg)", fontSize = 12.sp)
                    Text("• 팀 구성: 정민재(대표/코딩), 채희범(개발/점검), 김관호(기획/문서), 정서준(개발보조)", fontSize = 12.sp)
                    Text("• 핵심 AI 기술: Google Gemini 멀티모달 비전(Gemini 3.5 Flash) 기반 오염도 수치화 및 실시간 분리배출 세척 가이드", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7. Account Management Actions (Logout / Reset / Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("로그아웃")
                }

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("데이터 리셋")
                }
            }

            TextButton(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("회원 탈퇴 (개인정보 및 활동 데이터 영구 삭제)", color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }

    // Dialog: Change Apartment
    if (showChangeAptDialog) {
        var selectedApt by remember { mutableStateOf(GlobalState.apartmentId) }

        AlertDialog(
            onDismissRequest = { showChangeAptDialog = false },
            title = { Text("거주 단지 변경", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    apartmentList.forEach { apt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedApt == apt,
                                onClick = { selectedApt = apt }
                            )
                            Text(apt, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        GlobalState.apartmentId = selectedApt
                        GlobalState.saveToPrefs()
                        showChangeAptDialog = false
                        Toast.makeText(context, "소속 단지가 '${selectedApt}'(으)로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("변경 완료")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeAptDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // Dialog: Logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("로그아웃") },
            text = { Text("로그아웃 하시겠습니까? 다음 로그인 시 다시 인증이 필요합니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        GlobalState.logout()
                        Toast.makeText(context, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
                        onLogout()
                    }
                ) {
                    Text("로그아웃")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // Dialog: Delete Account
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("회원 탈퇴") },
            text = { Text("탈퇴 시 현재 보유 포인트, 분리배출 실천 기록 및 발급된 쿠폰이 영구 삭제되며 복구할 수 없습니다. 계속하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        GlobalState.deleteAccount()
                        Toast.makeText(context, "회원 탈퇴 및 데이터 삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("탈퇴 확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // Dialog: Reset Demo Data
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("데이터 초기화") },
            text = { Text("포인트, 분리배출 인증 기록, 발급된 쿠폰 데이터가 기본 데모 상태로 초기화됩니다. 계속하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        GlobalState.currentPoints = 5000
                        GlobalState.currentCount = 0
                        GlobalState.recycleHistory.clear()
                        GlobalState.redeemedCoupons.clear()
                        GlobalState.saveToPrefs()
                        showResetDialog = false
                        Toast.makeText(context, "데이터가 초기화되었습니다.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("초기화")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // Dialog: Terms & Privacy
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("에코픽 이용약관 및 개인정보 처리방침", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .height(280.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("1. 수집하는 개인정보 항목: 이메일, 닉네임, 거주 단지명, 분리배출 인증 사진 및 AI 분석 결과", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("2. 개인정보의 수집 및 이용목적: 우리 단지 분리배출 랭킹 산정, 리워드 포인트 지급 및 상품권 교환 처리", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("3. AI 모델 활용: 사용자가 촬영한 이미지는 Google Gemini 비전 API를 통해 재질 식별 및 오염도(0~100%) 판정에 활용되며, 개인 식별 정보는 포함되지 않습니다.", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("4. 정보의 보유 및 파기: 회원 탈퇴 시 모든 활동 내역과 개인정보는 지체 없이 영구 파기됩니다.", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showTermsDialog = false }) {
                    Text("확인")
                }
            }
        )
    }
}
