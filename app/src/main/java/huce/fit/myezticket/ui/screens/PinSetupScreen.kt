package huce.fit.myezticket.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import huce.fit.myezticket.ui.viewmodel.ProfileViewModel

enum class PinSetupState {
    ENTER_CURRENT, // Nhập PIN hiện tại để thay đổi/tắt
    SELECT_ACTION, // Chọn Đổi PIN hay Tắt PIN (Sau khi nhập đúng PIN cũ)
    ENTER_NEW,     // Nhập PIN mới
    CONFIRM_NEW    // Xác nhận PIN mới
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val savedPin by viewModel.pinCode.collectAsState()
    
    var currentStep by remember { mutableStateOf<PinSetupState?>(null) }
    var enteredDigits by remember { mutableStateOf("") }
    
    var tempNewPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    // Chờ DataStore trả về giá trị thật trước khi quyết định có cần xác thực PIN cũ hay không.
    LaunchedEffect(savedPin) {
        if (currentStep == null && savedPin != null) {
            if (!savedPin.isNullOrBlank()) {
                currentStep = PinSetupState.ENTER_CURRENT
            } else {
                currentStep = PinSetupState.ENTER_NEW
            }
        }
    }

    // Xử lý khi nhập đủ 4 số
    fun onPinEntered(pin: String) {
        errorMessage = ""
        when (currentStep) {
            PinSetupState.ENTER_CURRENT -> {
                if (pin == savedPin) {
                    // Đúng mã PIN hiện tại -> Cho phép chọn Đổi hoặc Tắt PIN
                    tempNewPin = ""
                    enteredDigits = ""
                    currentStep = PinSetupState.SELECT_ACTION
                } else {
                    errorMessage = "Mã PIN hiện tại không chính xác!"
                    enteredDigits = ""
                }
            }
            PinSetupState.SELECT_ACTION -> {}
            PinSetupState.ENTER_NEW -> {
                tempNewPin = pin
                enteredDigits = ""
                currentStep = PinSetupState.CONFIRM_NEW
            }
            PinSetupState.CONFIRM_NEW -> {
                if (pin == tempNewPin) {
                    // Trùng khớp -> Lưu vào DataStore
                    viewModel.savePinCode(pin)
                    successMessage = "Thiết lập mã PIN bảo mật thành công!"
                    enteredDigits = ""
                } else {
                    errorMessage = "Mã PIN xác nhận không trùng khớp, vui lòng thử lại từ đầu."
                    enteredDigits = ""
                    currentStep = PinSetupState.ENTER_NEW
                }
            }
            null -> {}
        }
    }

    // Nhấn một phím số
    fun onKeyPress(digit: String) {
        if (enteredDigits.length < 4) {
            enteredDigits += digit
            if (enteredDigits.length == 4) {
                onPinEntered(enteredDigits)
            }
        }
    }

    // Xóa phím
    fun onDeletePress() {
        if (enteredDigits.isNotEmpty()) {
            enteredDigits = enteredDigits.dropLast(1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thiết lập mã PIN", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F9F8)
    ) { paddingValues ->
        if (successMessage.isNotBlank()) {
            // Màn hình thành công
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Success",
                        tint = Color(0xFF00B14F),
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = successMessage,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tác vụ hoàn tất thành công. Vé của bạn luôn được bảo vệ an toàn.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F)),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Hoàn thành", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        } else if (currentStep == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00B14F))
            }
        } else if (currentStep == PinSetupState.SELECT_ACTION) {
            // Màn hình Chọn tác vụ sau khi xác thực thành công mã PIN cũ
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Verified",
                        tint = Color(0xFF00B14F),
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Xác thực thành công",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Vui lòng chọn tác vụ bảo mật bạn muốn thực hiện tiếp theo.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = {
                        currentStep = PinSetupState.ENTER_NEW
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F)),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Thay đổi mã PIN mới", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.clearPinCode()
                        successMessage = "Đã tắt mã PIN bảo mật thành công!"
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Tắt mã PIN bảo mật", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        } else {
            // Màn hình nhập PIN
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Tiêu đề & Bong bóng trạng thái
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "PIN Lock",
                        tint = Color(0xFF00B14F),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val titleText = when (currentStep) {
                        PinSetupState.ENTER_CURRENT -> "Nhập mã PIN hiện tại"
                        PinSetupState.SELECT_ACTION -> ""
                        PinSetupState.ENTER_NEW -> "Thiết lập mã PIN mới"
                        PinSetupState.CONFIRM_NEW -> "Xác nhận mã PIN mới"
                        null -> ""
                    }
                    val subtitleText = when (currentStep) {
                        PinSetupState.ENTER_CURRENT -> "Vui lòng nhập mã PIN bảo mật hiện tại của bạn."
                        PinSetupState.SELECT_ACTION -> ""
                        PinSetupState.ENTER_NEW -> "Mã PIN gồm 4 số dùng để bảo vệ khi truy cập xem chi tiết vé."
                        PinSetupState.CONFIRM_NEW -> "Vui lòng nhập lại mã PIN mới để xác nhận."
                        null -> ""
                    }

                    Text(
                        text = titleText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = subtitleText,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 4 ô bong bóng tròn PIN
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 4) {
                            val isFilled = i < enteredDigits.length
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) Color(0xFF00B14F) else Color.Transparent)
                                    .border(2.dp, Color(0xFF00B14F), CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Lỗi thông báo
                    if (errorMessage.isNotBlank()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }

                // Bàn phím số numeric pad
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                ) {
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("", "0", "delete")
                    )

                    keys.forEach { rowKeys ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowKeys.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.5f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (key) {
                                        "delete" -> {
                                            IconButton(
                                                onClick = { onDeletePress() },
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Backspace,
                                                    contentDescription = "Backspace",
                                                    tint = Color.DarkGray
                                                )
                                            }
                                        }
                                        "" -> {
                                            // Empty spacing
                                        }
                                        else -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .clickable { onKeyPress(key) }
                                                    .border(1.dp, Color(0xFFE2E2E2), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = key,
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
