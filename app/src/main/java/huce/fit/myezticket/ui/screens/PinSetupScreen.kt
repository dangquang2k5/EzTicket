package huce.fit.myezticket.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import huce.fit.myezticket.core.common.UiState
import huce.fit.myezticket.ui.viewmodel.ProfileViewModel

enum class PinSetupState {
    ENTER_CURRENT, // Nhập PIN hiện tại để thay đổi/tắt
    SELECT_ACTION, // Chọn Đổi PIN hay Tắt PIN (Sau khi nhập đúng PIN cũ)
    ENTER_NEW,     // Nhập PIN mới
    CONFIRM_NEW    // Xác nhận PIN mới
}

sealed interface ActivePinScreen {
    object Loading : ActivePinScreen
    object SelectAction : ActivePinScreen
    data class InputPin(val step: PinSetupState) : ActivePinScreen
    data class Success(val message: String) : ActivePinScreen
}

private fun getScreenWeight(state: ActivePinScreen): Int {
    return when (state) {
        is ActivePinScreen.Loading -> 0
        is ActivePinScreen.InputPin -> {
            when (state.step) {
                PinSetupState.ENTER_CURRENT -> 1
                PinSetupState.ENTER_NEW -> 3
                PinSetupState.CONFIRM_NEW -> 4
                PinSetupState.SELECT_ACTION -> 2
            }
        }
        is ActivePinScreen.SelectAction -> 2
        is ActivePinScreen.Success -> 5
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    onBackClick: () -> Unit,
    onForgotPinClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val savedPin by viewModel.pinCode.collectAsState()
    val lockoutTime by viewModel.lockoutTime.collectAsState()
    val failedAttempts by viewModel.failedAttempts.collectAsState()

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val titleFontSize = if (isTablet) 24.sp else 20.sp
    val subtitleFontSize = if (isTablet) 16.sp else 14.sp
    val pinDotSize = if (isTablet) 24.dp else 20.dp
    val keySize = if (isTablet) 68.dp else 56.dp
    val keyFontSize = if (isTablet) 26.sp else 22.sp

    var currentStep by remember { mutableStateOf<PinSetupState?>(null) }
    var enteredDigits by remember { mutableStateOf("") }
    
    var tempNewPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    var timeRemaining by remember { mutableStateOf(0L) }
    LaunchedEffect(lockoutTime) {
        while (true) {
            val now = System.currentTimeMillis()
            if (lockoutTime > now) {
                timeRemaining = lockoutTime - now
            } else {
                timeRemaining = 0L
            }
            kotlinx.coroutines.delay(1000)
        }
    }
    val isLocked = currentStep == PinSetupState.ENTER_CURRENT && timeRemaining > 0L

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
                if (huce.fit.myezticket.utils.SecurityUtils.hashSHA256(pin) == savedPin) {
                    // Đúng mã PIN hiện tại -> Cho phép chọn Đổi hoặc Tắt PIN
                    viewModel.handlePinSuccess()
                    tempNewPin = ""
                    enteredDigits = ""
                    currentStep = PinSetupState.SELECT_ACTION
                } else {
                    viewModel.handlePinFailed()
                    enteredDigits = ""
                    // Không tự gán errorMessage ở đây, UI sẽ tự bind dựa theo trạng thái failedAttempts/lockoutTime bên dưới
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
        if (isLocked) return
        if (enteredDigits.length < 4) {
            enteredDigits += digit
            if (enteredDigits.length == 4) {
                onPinEntered(enteredDigits)
            }
        }
    }

    // Xóa phím
    fun onDeletePress() {
        if (isLocked) return
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
        val scrollState = rememberScrollState()

        val currentScreenState = remember(currentStep, successMessage) {
            when {
                successMessage.isNotBlank() -> ActivePinScreen.Success(successMessage)
                currentStep == null -> ActivePinScreen.Loading
                currentStep == PinSetupState.SELECT_ACTION -> ActivePinScreen.SelectAction
                else -> ActivePinScreen.InputPin(currentStep!!)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentScreenState,
                transitionSpec = {
                    val isForward = getScreenWeight(targetState) >= getScreenWeight(initialState)
                    if (isForward) {
                        (fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it }))
                            .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { -it }))
                    } else {
                        (fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300), initialOffsetX = { -it }))
                            .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it }))
                    }
                },
                label = "PinSetupScreenTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetState ->
                when (targetState) {
                    is ActivePinScreen.Success -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 450.dp)
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isTablet) 100.dp else 80.dp)
                                        .background(Color(0xFFE8F5E9), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Success",
                                        tint = Color(0xFF00B14F),
                                        modifier = Modifier.size(if (isTablet) 50.dp else 40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = targetState.message,
                                    fontSize = if (isTablet) 24.sp else 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Tác vụ hoàn tất thành công. Vé của bạn luôn được bảo vệ an toàn.",
                                    fontSize = if (isTablet) 16.sp else 14.sp,
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
                                    Text("Hoàn thành", color = Color.White, fontWeight = FontWeight.Bold, fontSize = if (isTablet) 18.sp else 16.sp)
                                }
                            }
                        }
                    }
                    is ActivePinScreen.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF00B14F))
                        }
                    }
                    is ActivePinScreen.SelectAction -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 450.dp)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isTablet) 100.dp else 80.dp)
                                        .background(Color(0xFFE8F5E9), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF00B14F),
                                        modifier = Modifier.size(if (isTablet) 50.dp else 40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Xác thực thành công",
                                    fontSize = if (isTablet) 24.sp else 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Vui lòng chọn tác vụ bảo mật bạn muốn thực hiện tiếp theo.",
                                    fontSize = if (isTablet) 16.sp else 14.sp,
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
                                    Text("Thay đổi mã PIN mới", color = Color.White, fontWeight = FontWeight.Bold, fontSize = if (isTablet) 18.sp else 16.sp)
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
                                    Text("Tắt mã PIN bảo mật", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = if (isTablet) 18.sp else 16.sp)
                                }
                            }
                        }
                    }
                    is ActivePinScreen.InputPin -> {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val isLandscape = maxWidth > maxHeight
                            if (isLandscape) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .widthIn(max = 800.dp)
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .verticalScroll(rememberScrollState())
                                                .padding(vertical = 12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "PIN Lock",
                                                tint = Color(0xFF00B14F),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            
                                            val titleText = when (targetState.step) {
                                                PinSetupState.ENTER_CURRENT -> "Nhập mã PIN hiện tại"
                                                PinSetupState.SELECT_ACTION -> ""
                                                PinSetupState.ENTER_NEW -> "Thiết lập mã PIN mới"
                                                PinSetupState.CONFIRM_NEW -> "Xác nhận mã PIN mới"
                                            }
                                            val subtitleText = when (targetState.step) {
                                                PinSetupState.ENTER_CURRENT -> "Vui lòng nhập mã PIN bảo mật hiện tại."
                                                PinSetupState.SELECT_ACTION -> ""
                                                PinSetupState.ENTER_NEW -> "Mã PIN gồm 4 số dùng để bảo vệ xem vé."
                                                PinSetupState.CONFIRM_NEW -> "Vui lòng nhập lại mã PIN mới để xác nhận."
                                            }

                                            Text(
                                                text = titleText,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = subtitleText,
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                for (i in 0 until 4) {
                                                    val isFilled = i < enteredDigits.length
                                                    val dotColor by animateColorAsState(
                                                        targetValue = if (isFilled) Color(0xFF00B14F) else Color.Transparent,
                                                        animationSpec = tween(durationMillis = 150),
                                                        label = "dotColor"
                                                    )
                                                    val dotScale by animateFloatAsState(
                                                        targetValue = if (isFilled) 1.2f else 1.0f,
                                                        animationSpec = tween(durationMillis = 150),
                                                        label = "dotScale"
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .graphicsLayer(
                                                                scaleX = dotScale,
                                                                scaleY = dotScale
                                                            )
                                                            .clip(CircleShape)
                                                            .background(dotColor)
                                                            .border(2.dp, Color(0xFF00B14F), CircleShape)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            if (isLocked) {
                                                val minutes = timeRemaining / 60000
                                                val seconds = (timeRemaining % 60000) / 1000
                                                val timeStr = String.format("%02d:%02d", minutes, seconds)
                                                Text(
                                                    text = "Nhập sai $failedAttempts lần. Thử lại sau $timeStr",
                                                    color = Color.Red,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center
                                                )
                                            } else if (targetState.step == PinSetupState.ENTER_CURRENT && failedAttempts in 1..4) {
                                                Text(
                                                    text = "Mã PIN sai! Còn ${5 - failedAttempts} lần thử.",
                                                    color = Color.Red,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center
                                                )
                                            } else if (errorMessage.isNotBlank() && targetState.step != PinSetupState.ENTER_CURRENT) {
                                                Text(
                                                    text = errorMessage,
                                                    color = Color.Red,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center
                                                )
                                            }

                                            if (targetState.step == PinSetupState.ENTER_CURRENT) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                TextButton(
                                                    onClick = onForgotPinClick
                                                ) {
                                                    Text(
                                                        text = "Quên mã PIN?",
                                                        color = Color(0xFF00B14F),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.Center
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
                                                                .aspectRatio(2f),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            when (key) {
                                                                "delete" -> {
                                                                    IconButton(
                                                                        onClick = { onDeletePress() },
                                                                        modifier = Modifier
                                                                            .size(44.dp)
                                                                            .clip(CircleShape)
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Backspace,
                                                                            contentDescription = "Backspace",
                                                                            tint = Color.DarkGray
                                                                        )
                                                                    }
                                                                }
                                                                "" -> {}
                                                                else -> {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(44.dp)
                                                                            .clip(CircleShape)
                                                                            .background(Color.White)
                                                                            .clickable { onKeyPress(key) }
                                                                            .border(1.dp, Color(0xFFE2E2E2), CircleShape),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Text(
                                                                            text = key,
                                                                            fontSize = 18.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color.Black
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .widthIn(max = 450.dp)
                                            .fillMaxSize()
                                            .padding(bottom = 32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(top = 24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "PIN Lock",
                                                tint = Color(0xFF00B14F),
                                                modifier = Modifier.size(if (isTablet) 72.dp else 56.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            
                                            val titleText = when (targetState.step) {
                                                PinSetupState.ENTER_CURRENT -> "Nhập mã PIN hiện tại"
                                                PinSetupState.SELECT_ACTION -> ""
                                                PinSetupState.ENTER_NEW -> "Thiết lập mã PIN mới"
                                                PinSetupState.CONFIRM_NEW -> "Xác nhận mã PIN mới"
                                            }
                                            val subtitleText = when (targetState.step) {
                                                PinSetupState.ENTER_CURRENT -> "Vui lòng nhập mã PIN bảo mật hiện tại của bạn."
                                                PinSetupState.SELECT_ACTION -> ""
                                                PinSetupState.ENTER_NEW -> "Mã PIN gồm 4 số dùng để bảo vệ khi truy cập xem chi tiết vé."
                                                PinSetupState.CONFIRM_NEW -> "Vui lòng nhập lại mã PIN mới để xác nhận."
                                            }

                                            Text(
                                                text = titleText,
                                                fontSize = titleFontSize,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = subtitleText,
                                                fontSize = subtitleFontSize,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 32.dp)
                                            )

                                            Spacer(modifier = Modifier.height(if (isTablet) 40.dp else 32.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                for (i in 0 until 4) {
                                                    val isFilled = i < enteredDigits.length
                                                    val dotColor by animateColorAsState(
                                                        targetValue = if (isFilled) Color(0xFF00B14F) else Color.Transparent,
                                                        animationSpec = tween(durationMillis = 150),
                                                        label = "dotColor"
                                                    )
                                                    val dotScale by animateFloatAsState(
                                                        targetValue = if (isFilled) 1.2f else 1.0f,
                                                        animationSpec = tween(durationMillis = 150),
                                                        label = "dotScale"
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .size(pinDotSize)
                                                            .graphicsLayer(
                                                                scaleX = dotScale,
                                                                scaleY = dotScale
                                                            )
                                                            .clip(CircleShape)
                                                            .background(dotColor)
                                                            .border(2.dp, Color(0xFF00B14F), CircleShape)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(24.dp))

                                            if (isLocked) {
                                                val minutes = timeRemaining / 60000
                                                val seconds = (timeRemaining % 60000) / 1000
                                                val timeStr = String.format("%02d:%02d", minutes, seconds)
                                                Text(
                                                    text = "Bạn đã nhập sai $failedAttempts lần. Vui lòng thử lại sau $timeStr",
                                                    color = Color.Red,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(horizontal = 24.dp)
                                                )
                                            } else if (targetState.step == PinSetupState.ENTER_CURRENT && failedAttempts in 1..4) {
                                                Text(
                                                    text = "Mã PIN không đúng! Bạn còn ${5 - failedAttempts} lần thử.",
                                                    color = Color.Red,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(horizontal = 24.dp)
                                                )
                                            } else if (errorMessage.isNotBlank() && targetState.step != PinSetupState.ENTER_CURRENT) {
                                                Text(
                                                    text = errorMessage,
                                                    color = Color.Red,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(horizontal = 24.dp)
                                                )
                                            }

                                            if (targetState.step == PinSetupState.ENTER_CURRENT) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                TextButton(
                                                    onClick = onForgotPinClick,
                                                    modifier = Modifier.padding(horizontal = 16.dp)
                                                ) {
                                                    Text(
                                                        text = "Quên mã PIN?",
                                                        color = Color(0xFF00B14F),
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = if (isTablet) 48.dp else 40.dp)
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
                                                                            .size(keySize)
                                                                            .clip(CircleShape)
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Backspace,
                                                                            contentDescription = "Backspace",
                                                                            tint = Color.DarkGray
                                                                        )
                                                                    }
                                                                }
                                                                "" -> {}
                                                                else -> {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(keySize)
                                                                            .clip(CircleShape)
                                                                            .background(Color.White)
                                                                            .clickable { onKeyPress(key) }
                                                                            .border(1.dp, Color(0xFFE2E2E2), CircleShape),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Text(
                                                                            text = key,
                                                                            fontSize = keyFontSize,
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
                    }
                }
            }
        }
    }
}
