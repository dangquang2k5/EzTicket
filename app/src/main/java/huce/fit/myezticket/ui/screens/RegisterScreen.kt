package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import huce.fit.myezticket.core.common.UiState
import huce.fit.myezticket.ui.components.AuthTextField
import huce.fit.myezticket.ui.components.PrimaryButton
import huce.fit.myezticket.ui.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val phone by viewModel.phone.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Dialog States
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                dialogTitle = "Thành công"
                dialogMessage = "Đăng ký tài khoản thành công!"
                isSuccessDialog = true
                showDialog = true
            }
            is UiState.Error -> {
                dialogTitle = "Đăng ký không thành công"
                dialogMessage = (uiState as UiState.Error).message
                isSuccessDialog = false
                showDialog = true
            }
            else -> {}
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false 
                viewModel.resetState()
            },
            title = {
                Text(text = dialogTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(text = dialogMessage, color = Color.DarkGray)
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showDialog = false 
                        viewModel.resetState()
                        if (isSuccessDialog) {
                            onNavigateToLogin()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSuccessDialog) "Đến màn hình đăng nhập" else "Đồng ý", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8F5E9), // Soft green top
            Color(0xFFF7F9F8)  // Gray bottom
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Brand App Name Text Logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ez",
                color = Color(0xFF00B14F),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Ticket",
                color = Color(0xFF1B1B1B),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black
            )
        }
        
        Spacer(modifier = Modifier.height(30.dp))

        // Form Card with Premium Elevation and Layout
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Đăng ký tài khoản",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trở thành thành viên của EzTicket",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "SỐ ĐIỆN THOẠI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00B14F),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthTextField(
                    value = phone,
                    onValueChange = viewModel::onPhoneChange,
                    placeholder = "Nhập số điện thoại (10 số)",
                    icon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "EMAIL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00B14F),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthTextField(
                    value = email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "Nhập email của bạn",
                    icon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MẬT KHẨU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00B14F),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthTextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "Tối thiểu 6 ký tự",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    isPasswordVisible = isPasswordVisible,
                    onVisibilityChange = { isPasswordVisible = !isPasswordVisible },
                    trailingIcon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "XÁC NHẬN MẬT KHẨU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00B14F),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthTextField(
                    value = confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    placeholder = "Nhập lại mật khẩu",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    isPasswordVisible = isConfirmPasswordVisible,
                    onVisibilityChange = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                    trailingIcon = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "ĐĂNG KÝ",
                    onClick = { viewModel.register() },
                    isLoading = uiState is UiState.Loading
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đã có tài khoản? ",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Đăng nhập",
                        color = Color(0xFF00B14F),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToLogin)
                    )
                }
            }
        }
    }
}
