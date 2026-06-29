package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
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
import androidx.hilt.navigation.compose.hiltViewModel
import huce.fit.myezticket.core.common.UiState
import huce.fit.myezticket.ui.components.AuthTextField
import huce.fit.myezticket.ui.components.PrimaryButton
import huce.fit.myezticket.ui.viewmodel.ProfileViewModel

@Composable
fun ForgotPinScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var password by remember { mutableStateOf("") }
    val verifyState by viewModel.verifyPasswordState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(verifyState) {
        if (verifyState is UiState.Error) {
            errorMessage = (verifyState as UiState.Error).message
            showDialog = true
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                viewModel.resetVerifyPasswordState()
            },
            title = {
                Text(text = "Thông báo", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(text = errorMessage, color = Color.DarkGray)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        viewModel.resetVerifyPasswordState()
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

    if (verifyState is UiState.Success) {
        // Màn hình Thông báo thành công
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9F8))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFE8F8EF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success Icon",
                    tint = Color(0xFF00B14F),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "✓ Xác thực thành công",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Mã PIN cũ đã được xóa. Vui lòng quay lại để thiết lập mã PIN mới.",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            PrimaryButton(
                text = "Tiếp tục",
                onClick = {
                    viewModel.resetVerifyPasswordState()
                    onNavigateBack()
                }
            )
        }
    } else {
        // Màn hình Form Quên mã PIN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9F8))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "EZTICKET",
                color = Color(0xFF00B14F),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Quên mã PIN",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Xác thực mật khẩu đăng nhập để lấy lại mã PIN.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "MẬT KHẨU ĐĂNG NHẬP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Nhập mật khẩu của bạn",
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    PrimaryButton(
                        text = "Xác nhận",
                        onClick = { viewModel.verifyPasswordAndResetPin(password) },
                        isLoading = verifyState is UiState.Loading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quay lại ",
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Thiết lập PIN",
                            color = Color(0xFF00B14F),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onNavigateBack)
                        )
                    }
                }
            }
        }
    }
}
