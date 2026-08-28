package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.GlobalState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (isNewUser: Boolean) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: 로그인, 1: 회원가입

    // Login Fields
    var loginEmail by remember { mutableStateOf(GlobalState.userEmail) }
    var loginPassword by remember { mutableStateOf("123456") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Signup Fields
    var signupName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupPasswordConfirm by remember { mutableStateOf("") }
    var signupPasswordVisible by remember { mutableStateOf(false) }
    var signupApartment by remember { mutableStateOf("래미안 에코팰리스") }
    var isApartmentDropdownExpanded by remember { mutableStateOf(false) }

    // Terms
    var agreeAll by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }
    var agreePrivacy by remember { mutableStateOf(false) }
    var agreeMarketing by remember { mutableStateOf(true) }

    // UI States
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
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

    LaunchedEffect(Unit) {
        GlobalState.init(context)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Logo & Header
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Eco,
                        contentDescription = "에코픽 로고",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "에코픽 (EcoPick)",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "AI 기반 우리 단지 배달 쓰레기 분리배출 도우미",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Selector: 로그인 vs 회원가입
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        errorMessage = ""
                    },
                    text = { Text("로그인", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                    modifier = Modifier.testTag("tab_login")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        errorMessage = ""
                    },
                    text = { Text("회원가입", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                    modifier = Modifier.testTag("tab_signup")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error Display
            AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // TAB 0: 로그인 (LOGIN)
            // -------------------------------------------------------------
            if (selectedTab == 0) {
                OutlinedTextField(
                    value = loginEmail,
                    onValueChange = { loginEmail = it },
                    label = { Text("이메일 아이디") },
                    placeholder = { Text("example@gmail.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = { loginPassword = it },
                    label = { Text("비밀번호") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                            Icon(
                                if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (loginPasswordVisible) "비밀번호 숨김" else "비밀번호 보기"
                            )
                        }
                    },
                    visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberMe = !rememberMe }
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it }
                        )
                        Text("로그인 상태 유지", fontSize = 13.sp)
                    }

                    TextButton(onClick = { showForgotPasswordDialog = true }) {
                        Text("비밀번호 재설정", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (loginEmail.isBlank() || !loginEmail.contains("@")) {
                            errorMessage = "올바른 이메일 주소를 입력해주세요."
                            return@Button
                        }
                        if (loginPassword.isBlank()) {
                            errorMessage = "비밀번호를 입력해주세요."
                            return@Button
                        }

                        errorMessage = ""
                        isLoading = true
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(400)
                            val success = GlobalState.loginUser(loginEmail, loginPassword)
                            isLoading = false
                            if (success) {
                                Toast.makeText(context, "${GlobalState.userName}님, 환영합니다!", Toast.LENGTH_SHORT).show()
                                val isNewUser = GlobalState.apartmentId.isBlank()
                                onLoginSuccess(isNewUser)
                            } else {
                                errorMessage = "비밀번호가 올바르지 않습니다. 다시 확인해주세요."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("로그인", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        " 또는 ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google 1-Tap Login
                OutlinedButton(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(400)
                            val googleEmail = if (loginEmail.contains("@gmail.com")) loginEmail else "mjayj9@gmail.com"
                            GlobalState.loginUser(googleEmail, "")
                            isLoading = false
                            Toast.makeText(context, "Google 계정(${GlobalState.userEmail})으로 연동되었습니다.", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(false)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google 계정으로 시작하기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Demo / Guest Login
                TextButton(
                    onClick = {
                        GlobalState.userName = "체험 사용자"
                        GlobalState.userEmail = "guest_demo@ecopick.kr"
                        GlobalState.apartmentId = "래미안 에코팰리스"
                        GlobalState.isLoggedIn = true
                        GlobalState.saveToPrefs()
                        Toast.makeText(context, "체험 모드로 입장합니다.", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(false)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("회원가입 없이 둘러보기 (체험 모드)", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }

            // -------------------------------------------------------------
            // TAB 1: 회원가입 (SIGN UP)
            // -------------------------------------------------------------
            if (selectedTab == 1) {
                OutlinedTextField(
                    value = signupName,
                    onValueChange = { signupName = it },
                    label = { Text("이름 / 닉네임") },
                    placeholder = { Text("홍길동") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = signupEmail,
                    onValueChange = { signupEmail = it },
                    label = { Text("이메일 (ID)") },
                    placeholder = { Text("user@example.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_email_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = signupPassword,
                    onValueChange = { signupPassword = it },
                    label = { Text("비밀번호 (6자 이상)") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { signupPasswordVisible = !signupPasswordVisible }) {
                            Icon(
                                if (signupPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (signupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_password_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = signupPasswordConfirm,
                    onValueChange = { signupPasswordConfirm = it },
                    label = { Text("비밀번호 확인") },
                    leadingIcon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    visualTransformation = if (signupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_password_confirm_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Apartment Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = isApartmentDropdownExpanded,
                    onExpandedChange = { isApartmentDropdownExpanded = !isApartmentDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = signupApartment,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("거주 아파트 단지") },
                        leadingIcon = {
                            Icon(Icons.Default.Apartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isApartmentDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = isApartmentDropdownExpanded,
                        onDismissRequest = { isApartmentDropdownExpanded = false }
                    ) {
                        apartmentList.forEach { apt ->
                            DropdownMenuItem(
                                text = { Text(apt) },
                                onClick = {
                                    signupApartment = apt
                                    isApartmentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Terms Checkboxes
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newVal = !agreeAll
                                    agreeAll = newVal
                                    agreeTerms = newVal
                                    agreePrivacy = newVal
                                    agreeMarketing = newVal
                                }
                        ) {
                            Checkbox(
                                checked = agreeAll,
                                onCheckedChange = {
                                    agreeAll = it
                                    agreeTerms = it
                                    agreePrivacy = it
                                    agreeMarketing = it
                                }
                            )
                            Text("모든 약관에 동의합니다", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = agreeTerms,
                                onCheckedChange = {
                                    agreeTerms = it
                                    agreeAll = agreeTerms && agreePrivacy && agreeMarketing
                                }
                            )
                            Text("[필수] 서비스 이용약관 동의", fontSize = 13.sp, modifier = Modifier.weight(1f))
                            TextButton(onClick = { showTermsDialog = true }) {
                                Text("보기", fontSize = 12.sp)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = agreePrivacy,
                                onCheckedChange = {
                                    agreePrivacy = it
                                    agreeAll = agreeTerms && agreePrivacy && agreeMarketing
                                }
                            )
                            Text("[필수] 개인정보 수집 및 분리배출 데이터 처리 동의", fontSize = 13.sp, modifier = Modifier.weight(1f))
                            TextButton(onClick = { showTermsDialog = true }) {
                                Text("보기", fontSize = 12.sp)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = agreeMarketing,
                                onCheckedChange = {
                                    agreeMarketing = it
                                    agreeAll = agreeTerms && agreePrivacy && agreeMarketing
                                }
                            )
                            Text("[선택] 분리배출 요일 및 리워드 알림 수신 동의", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (signupName.isBlank()) {
                            errorMessage = "이름(닉네임)을 입력해주세요."
                            return@Button
                        }
                        if (signupEmail.isBlank() || !signupEmail.contains("@")) {
                            errorMessage = "올바른 이메일 주소를 입력해주세요."
                            return@Button
                        }
                        if (signupPassword.length < 6) {
                            errorMessage = "비밀번호는 최소 6자 이상이어야 합니다."
                            return@Button
                        }
                        if (signupPassword != signupPasswordConfirm) {
                            errorMessage = "비밀번호 확인이 일치하지 않습니다."
                            return@Button
                        }
                        if (!agreeTerms || !agreePrivacy) {
                            errorMessage = "필수 이용약관에 동의해주세요."
                            return@Button
                        }

                        errorMessage = ""
                        isLoading = true
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(500)
                            GlobalState.registerUser(
                                name = signupName,
                                email = signupEmail,
                                password = signupPassword,
                                apartment = signupApartment
                            )
                            GlobalState.notifyRecycleDay = agreeMarketing
                            GlobalState.saveToPrefs()
                            isLoading = false
                            Toast.makeText(context, "회원가입이 완료되었습니다! 가입 축하 5,000P 지급 완료.", Toast.LENGTH_LONG).show()
                            onLoginSuccess(false)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("signup_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("회원가입 완료 및 시작하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Project Info Footer
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "💡 2026 대한민국 청소년 창업경진대회 출품작\n서울대학교사범대학부설중학교 동아리 SNUMS_psg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }

    // -------------------------------------------------------------
    // Dialog: 비밀번호 재설정 (Password Reset)
    // -------------------------------------------------------------
    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(loginEmail) }
        var newPass by remember { mutableStateOf("") }
        var resetSuccess by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("비밀번호 재설정", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (!resetSuccess) {
                        Text("가입하신 이메일 계정을 입력하고 새로 사용할 비밀번호를 설정하세요.", fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("이메일") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPass,
                            onValueChange = { newPass = it },
                            label = { Text("새 비밀번호 (6자 이상)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                    } else {
                        Text("✅ 비밀번호가 성공적으로 변경되었습니다! 새 비밀번호로 로그인해주세요.", color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                if (!resetSuccess) {
                    Button(
                        onClick = {
                            if (resetEmail.contains("@") && newPass.length >= 6) {
                                GlobalState.resetPassword(resetEmail, newPass)
                                resetSuccess = true
                            } else {
                                Toast.makeText(context, "올바른 이메일과 6자 이상의 새 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("변경 완료")
                    }
                } else {
                    Button(onClick = { showForgotPasswordDialog = false }) {
                        Text("확인")
                    }
                }
            },
            dismissButton = {
                if (!resetSuccess) {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("취소")
                    }
                }
            }
        )
    }

    // -------------------------------------------------------------
    // Dialog: 이용약관 및 개인정보 처리방침 전문 (Terms & Privacy Dialog)
    // -------------------------------------------------------------
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("에코픽 서비스 이용약관 및 정책", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("제1조 (목적)\n본 약관은 서울대학교사범대학부설중학교 창업동아리 SNUMS_psg가 제공하는 '에코픽(EcoPick)' 서비스의 이용조건 및 절차, 이용자와 당사간의 권리, 의무를 규정함을 목적으로 합니다.\n", fontSize = 12.sp)
                    Text("제2조 (AI 분리배출 데이터 및 리워드)\n1. 회원이 촬영한 쓰레기 이미지는 Google Gemini AI 비전 모델을 통해 오염도 분석 및 분리배출 등급 산정에만 활용됩니다.\n2. 분리배출 인증 성공 시 약정된 에코 포인트가 즉시 지급되며, 적립된 포인트는 포인트샵에서 모바일 상품권 및 쿠폰으로 교환할 수 있습니다.\n", fontSize = 12.sp)
                    Text("제3조 (개인정보의 보호)\n회원의 이메일 및 단지 정보는 단지별 랭킹 산정 및 분리배출 리포트 생성 용도로만 안전하게 보관 및 처리됩니다.\n", fontSize = 12.sp)
                    Text("제4조 (부정 인증 방지)\n동일 사진 반복 등록, 허위 사진 제출 등 부정한 방법으로 포인트를 취득한 경우 적립이 취소되거나 계정이 제한될 수 있습니다.", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showTermsDialog = false }) {
                    Text("닫기")
                }
            }
        )
    }
}
