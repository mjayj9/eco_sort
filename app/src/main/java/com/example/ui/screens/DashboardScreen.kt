package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.GlobalState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val apartmentId = GlobalState.apartmentId
    val totalAptRecycled = GlobalState.totalAppRecycled
    val myRecycled = GlobalState.currentCount
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

    // 사회적 가치 환산 상수 (환경부 및 자원순환 연구 기준)
    val costSavedPerItem = 50 // 1건당 평균 50원 폐기물 처리비용 절감
    val waterSavedPerItem = 2 // 1건당 2L 수질 오염 복구용 수자원 절약
    val co2SavedPerItem = 0.3 // 1건당 약 0.3kg 탄소 배출 저감

    val totalCostSaved = totalAptRecycled * costSavedPerItem
    val totalWaterSaved = totalAptRecycled * waterSavedPerItem
    val totalCo2Saved = String.format(Locale.US, "%.1f", totalAptRecycled * co2SavedPerItem)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "단지 대시보드 & 리더보드",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header: Current Apartment Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("나의 소속 단지", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(apartmentId, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "누적 ${numberFormat.format(totalAptRecycled)}건",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // 1. Apartment Competition Leaderboard
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFA000))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "단지 간 경쟁 리더보드",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            "아파트 입주민들의 자발적 분리배출로 친환경 1등 단지를 만듭니다.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Ranking Items
                        val rankingList = listOf(
                            Triple("1위", "자이 리사이클뷰", 15200),
                            Triple("2위", apartmentId, totalAptRecycled),
                            Triple("3위", "푸르지오 그린타운", 8900)
                        ).sortedByDescending { it.third }

                        rankingList.forEachIndexed { index, rankData ->
                            val isMyApt = rankData.second == apartmentId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isMyApt) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                border = if (isMyApt) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "${index + 1}위",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = when (index) {
                                                0 -> Color(0xFFD4AF37) // Gold
                                                1 -> Color(0xFFA0A0A0) // Silver
                                                else -> Color(0xFFCD7F32) // Bronze
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            rankData.second,
                                            fontWeight = if (isMyApt) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp,
                                            color = if (isMyApt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isMyApt) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("(우리 단지)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Text(
                                        "${numberFormat.format(rankData.third)}건",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Social Value Impact Cards
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "우리가 만든 사회적 가치",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            "정확한 오염도 판정과 세척 배출을 통해 창출된 경제적·환경적 가치",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cost saved
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("💰 폐기물 처리비 절약", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "${numberFormat.format(totalCostSaved)}원",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Water saved
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("💧 수자원 낭비 방지", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "${numberFormat.format(totalWaterSaved)} L",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0288D1)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌱 탄소(CO₂) 저감량", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${totalCo2Saved} kg CO₂e", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }

            // 3. Personal Contribution
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("👤 나의 환경 기여도", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("이번 달 인증 실천", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${myRecycled}건", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("단지 내 기여 랭킹", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("상위 12%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            }
                            Column {
                                Text("총 획득 포인트", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${GlobalState.currentPoints}P", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // 4. Recent Recycling History Records
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "나의 최근 분리배출 인증 내역",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (GlobalState.recycleHistory.isEmpty()) {
                item {
                    Text(
                        "아직 인증된 분리배출 내역이 없습니다. AI 스캐너로 첫 배출을 인증해보세요!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(GlobalState.recycleHistory) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        record.itemName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            record.material,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "오염도 ${record.pollutionPercent}% (등급 ${record.grade}) • ${record.dateString}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                "+${record.pointsEarned}P",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
