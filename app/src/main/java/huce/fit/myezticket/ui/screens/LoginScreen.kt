package huce.fit.myezticket.ui.screens

import android.content.Intent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import huce.fit.myezticket.core.common.UiState
import huce.fit.myezticket.ui.components.AuthTextField
import huce.fit.myezticket.ui.components.PrimaryButton
import huce.fit.myezticket.ui.viewmodel.LoginViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.SolidColor
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

val GoogleG: ImageVector
    get() = ImageVector.Builder(
        name = "GoogleG",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFF4285F4))) {
            moveTo(22.56f, 12.25f)
            curveTo(22.56f, 11.52f, 22.49f, 10.82f, 22.37f, 10.14f)
            lineTo(12f, 10.14f)
            lineTo(12f, 14.14f)
            lineTo(17.92f, 14.14f)
            curveTo(17.66f, 15.48f, 16.9f, 16.62f, 15.77f, 17.38f)
            lineTo(15.77f, 20.0f)
            lineTo(19.24f, 20.0f)
            curveTo(21.27f, 18.13f, 22.56f, 15.44f, 22.56f, 12.25f)
        }
        path(fill = SolidColor(Color(0xFF34A853))) {
            moveTo(12f, 23.0f)
            curveTo(14.97f, 23.0f, 17.46f, 22.02f, 19.24f, 20.0f)
            lineTo(15.77f, 17.38f)
            curveTo(14.8f, 18.04f, 13.51f, 18.43f, 12f, 18.43f)
            curveTo(9.14f, 18.43f, 6.72f, 16.5f, 5.85f, 13.91f)
            lineTo(2.28f, 16.67f)
            curveTo(4.1f, 20.29f, 7.85f, 23.0f, 12f, 23.0f)
        }
        path(fill = SolidColor(Color(0xFFFBBC05))) {
            moveTo(5.85f, 13.91f)
            curveTo(5.62f, 13.24f, 5.49f, 12.52f, 5.49f, 11.77f)
            curveTo(5.49f, 11.02f, 5.62f, 10.3f, 5.85f, 9.63f)
            lineTo(2.28f, 6.87f)
            curveTo(1.52f, 8.39f, 1.09f, 10.1f, 1.09f, 11.77f)
            curveTo(1.09f, 13.44f, 1.52f, 15.15f, 2.28f, 16.67f)
            lineTo(5.85f, 13.91f)
        }
        path(fill = SolidColor(Color(0xFFEA4335))) {
            moveTo(12f, 5.11f)
            curveTo(13.62f, 5.11f, 15.07f, 5.67f, 16.22f, 6.76f)
            lineTo(19.32f, 3.66f)
            curveTo(17.45f, 1.91f, 14.96f, 0.83f, 12f, 0.83f)
            curveTo(7.85f, 0.83f, 4.1f, 3.54f, 2.28f, 7.17f)
            lineTo(5.85f, 9.93f)
            curveTo(6.72f, 7.34f, 9.14f, 5.11f, 12f, 5.11f)
        }
    }.build()

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val countdown by viewModel.countdown.collectAsState()

    var isPasswordVisible by remember { mutableStateOf(false) }

    // Google Sign-In launcher setup
    val defaultWebClientIdId = remember {
        context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    }
    val webClientId = remember {
        if (defaultWebClientIdId != 0) context.getString(defaultWebClientIdId) else ""
    }

    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId.ifBlank { "728270498326-placeholder.apps.googleusercontent.com" })
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                viewModel.loginWithGoogle(idToken)
            } else {
                viewModel.setError("Không lấy được mã xác thực Google.")
            }
        } catch (e: ApiException) {
            viewModel.setError("Lỗi đăng nhập Google: ${e.statusCode}")
        }
    }

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
        Spacer(modifier = Modifier.height(60.dp))
        
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
        
        Spacer(modifier = Modifier.height(48.dp))

        // Form Card with Premium Elevation and Layout
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Đăng nhập",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Chào mừng bạn quay trở lại với EzTicket",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(28.dp))

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

                Spacer(modifier = Modifier.height(20.dp))

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
                    placeholder = "Nhập mật khẩu",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    isPasswordVisible = isPasswordVisible,
                    onVisibilityChange = { isPasswordVisible = !isPasswordVisible },
                    trailingIcon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClick = onNavigateToForgotPassword)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                val buttonText = if (countdown > 0) "Thử lại sau $countdown giây" else "ĐĂNG NHẬP"
                PrimaryButton(
                    text = buttonText,
                    onClick = { viewModel.login() },
                    isLoading = uiState is UiState.Loading,
                    enabled = countdown == 0
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Divider and Google Sign-In Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    Text(
                        text = " Hoặc ",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        googleSignInClient.signOut().addOnCompleteListener {
                            val signInIntent = googleSignInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = GoogleG,
                            contentDescription = "Google Logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Đăng nhập bằng Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1B1B)
                        )
                    }
                }
            }
        }
    }
}
