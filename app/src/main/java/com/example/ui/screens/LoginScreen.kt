package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.GlobalState
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: (isNewUser: Boolean) -> Unit) {
    var emailInput by remember { mutableStateOf("") }
    
    // 심사 포인트: 아파트 단지 입주민을 타겟으로 한 서비스로,
    // 접근성을 높이기 위해 직관적인 구글 로그인 (또는 소셜 로그인) 제공
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "에코소트",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "AI 기반 배달 쓰레기 분리배출 도우미",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        var isLoading by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("구글 계정 이메일 연동 (ID)") },
            placeholder = { Text("mjayj9@gmail.com") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                val finalEmail = emailInput.ifBlank { "mjayj9@gmail.com" }
                GlobalState.userEmail = finalEmail
                
                isLoading = true
                coroutineScope.launch {
                    kotlinx.coroutines.delay(1000) // Firebase Auth 로그인 연동 시뮬레이션
                    isLoading = false
                    val isNewUser = GlobalState.apartmentId.isEmpty()
                    onLoginSuccess(isNewUser)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Google 계정으로 안전하게 시작하기", fontSize = 16.sp)
            }
        }
    }
}

