package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdBanner() {
    val context = LocalContext.current
    
    // 심사 포인트: 수익 모델이 '쿠폰 제휴'만이 아니라 '광고'로도 확장됨을 보여주는 용도.
    // 향후 Google Ads (https://ads.google.com) 연동을 지원하도록 URL 클릭 인텐트 제공.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ads.google.com"))
                context.startActivity(intent)
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("[Google 스폰서 광고] ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            Text("자연을 사랑하는 OO기업 리필 스테이션!", fontSize = 14.sp)
        }
    }
}
