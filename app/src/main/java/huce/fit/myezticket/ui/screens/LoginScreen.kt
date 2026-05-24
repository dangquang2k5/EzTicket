package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import huce.fit.myezticket.core.common.UiState
import huce.fit.myezticket.ui.components.AuthTextField
import huce.fit.myezticket.ui.components.PrimaryButton
import huce.fit.myezticket.ui.viewmodel.LoginViewModel
import huce.fit.myezticket.data.repository.AuthRepositoryImpl
import huce.fit.myezticket.domain.usecase.LoginUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory(
            LoginUseCase(
                AuthRepositoryImpl(
                    FirebaseAuth.getInstance(),
                    FirebaseFirestore.getInstance()
                )
            )
        )
    )
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val countdown by viewModel.countdown.collectAsState()

    var isPasswordVisible by remember { mutableStateOf(false) }

    // Error Dialog State
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> onNavigateToHome()
            is UiState.Error -> {
                errorMessage = (uiState as UiState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false 
                viewModel.resetState()
            },
            title = {
                Text(text = "Đăng nhập không thành công", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(text = errorMessage, color = Color.DarkGray)
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showErrorDialog = false 
                        viewModel.resetState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Đồng ý", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9F8))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Logo (Mock text since no image resource)
        Text(text = "EZTICKET", color = Color(0xFF00B14F), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        
        Spacer(modifier = Modifier.height(40.dp))

        // Form Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(text = "Đăng nhập", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = "Chào mừng bạn đến với EZTicket", fontSize = 14.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "EMAIL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                AuthTextField(
                    value = email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "Email",
                    icon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "MẬT KHẨU", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                AuthTextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "••••••••",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    isPasswordVisible = isPasswordVisible,
                    onVisibilityChange = { isPasswordVisible = !isPasswordVisible },
                    trailingIcon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đăng ký mới",
                        color = Color(0xFF00B14F),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToRegister)
                    )
                    Text(
                        text = "Quên mật khẩu?",
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable(onClick = onNavigateToForgotPassword)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                val buttonText = if (countdown > 0) "Vui lòng thử lại sau $countdown giây" else "TIẾP TỤC"
                PrimaryButton(
                    text = buttonText,
                    onClick = { viewModel.login() },
                    isLoading = uiState is UiState.Loading,
                    enabled = countdown == 0
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Banner (Mock)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFF222222), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Giảm ngay 20% cho thành viên mới",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "HOẶC ĐĂNG NHẬP VỚI", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Social Icons (Mock circles)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            SocialCircle(Color.LightGray) // Google
            Spacer(modifier = Modifier.width(16.dp))
            SocialCircle(Color(0xFF3B5998)) // Facebook
            Spacer(modifier = Modifier.width(16.dp))
            SocialCircle(Color.Black) // Apple
        }
    }
}

@Composable
fun SocialCircle(color: Color) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(Color(0xFFE2E2E2), RoundedCornerShape(25.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(24.dp).background(color, RoundedCornerShape(12.dp)))
    }
}
