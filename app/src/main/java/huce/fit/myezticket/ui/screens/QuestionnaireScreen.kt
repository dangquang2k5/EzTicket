package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import huce.fit.myezticket.domain.model.Event
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import huce.fit.myezticket.utils.formatVND
import huce.fit.myezticket.utils.parseSelectedTicketsArg
import huce.fit.myezticket.utils.remainingPaymentSeconds

private fun normalizePhoneNumber(input: String): String {
    val digits = input.filter { it.isDigit() }
    return if (digits.startsWith("84")) {
        "0" + digits.substring(2)
    } else {
        digits
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    event: Event,
    scheduleIndex: Int,
    selectedTicketsString: String?,
    orderCode: String,
    expiresAtMillis: Long,
    isSaving: Boolean = false,
    paymentError: String? = null,
    onBackClick: () -> Unit,
    onTimeExpired: () -> Unit,
    onConfirmClick: (String, Map<String, Any>) -> Unit,
    onReselectTicketsClick: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var remainingSeconds by rememberSaveable(event.id, scheduleIndex, selectedTicketsString, orderCode) {
        mutableIntStateOf(remainingPaymentSeconds(expiresAtMillis))
    }
    var hasHandledExpiration by rememberSaveable(event.id, scheduleIndex, selectedTicketsString, orderCode) {
        mutableStateOf(false)
    }
    val schedule = event.schedules.getOrNull(scheduleIndex)
    val isPhoneInvalid = phoneNumber.isNotEmpty() && phoneNumber.length < 10

    val selectedTicketsMap = remember(selectedTicketsString) {
        parseSelectedTicketsArg(selectedTicketsString)
    }

    val totalAmount = selectedTicketsMap.entries.sumOf { (name, qty) ->
        (schedule?.ticketTypes?.find { it.name == name }?.price ?: 0L) * qty
    }
    val timerText = remember(remainingSeconds) {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        String.format("%02d : %02d", minutes, seconds)
    }
    val isTimeExpired = remainingSeconds <= 0
    val scheduleDate = remember(schedule?.dateMillis) {
        schedule?.dateMillis?.let { java.util.Date(it) }?.let {
            SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale("vi", "VN")).format(it)
        } ?: event.displayDate
    }

    LaunchedEffect(event.id, scheduleIndex, selectedTicketsString, orderCode, expiresAtMillis) {
        while (true) {
            remainingSeconds = remainingPaymentSeconds(expiresAtMillis)
            if (remainingSeconds <= 0) break
            delay(1000)
        }
    }

    LaunchedEffect(isTimeExpired) {
        if (isTimeExpired && !hasHandledExpiration) {
            hasHandledExpiration = true
            onTimeExpired()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > 720.dp
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bảng câu hỏi", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                if (!isWide) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp,
                        modifier = Modifier.height(100.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Tổng tiền", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                                Text("${totalAmount.formatVND()} đ", color = MaterialTheme.colorScheme.primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                                Button(
                                onClick = {
                                    val trimmedPhone = phoneNumber.trim()
                                    val orderData = mapOf(
                                        "eventId" to event.id,
                                        "eventName" to event.name,
                                        "orderCode" to orderCode,
                                        "phoneNumber" to trimmedPhone,
                                        "tickets" to selectedTicketsMap,
                                        "total" to totalAmount,
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    onConfirmClick(trimmedPhone, orderData)
                                },
                                enabled = phoneNumber.length == 10 && !isTimeExpired && !isSaving,
                                modifier = Modifier.width(160.dp).height(50.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(if (isTimeExpired) "Hết giờ" else if (isSaving) "Đang lưu..." else "Tiếp tục", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            if (isWide) {
                Row(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Cột trái: Thông tin sự kiện và Form nhập liệu
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Thanh đếm ngược màu đỏ
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.error).padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccessTime, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Hoàn tất đặt vé trong  ", color = Color.White, fontSize = 14.sp)
                            Text(timerText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(event.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(" $scheduleDate", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(" ${event.venueName}, ${event.address}", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                            }

                            Spacer(Modifier.height(24.dp))
                            Text("BẢNG CÂU HỎI", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                            // Card nhập SĐT
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Số Điện Thoại *", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { input ->
                                            phoneNumber = normalizePhoneNumber(input).take(10)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        isError = isPhoneInvalid,
                                        supportingText = {
                                            if (isPhoneInvalid) {
                                                Text("Số điện thoại phải có 10 chữ số (bắt đầu bằng 0)")
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f),
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            cursorColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    VerticalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxHeight()
                    )

                    // Cột phải: Tóm tắt vé và Nút tiếp tục
                    Column(
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface)
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Thông tin đặt vé", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    text = "Chọn lại vé",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.clickable(onClick = onReselectTicketsClick)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Loại vé", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                Text("Số lượng", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                            selectedTicketsMap.forEach { (name, qty) ->
                                val price = schedule?.ticketTypes?.find { it.name == name }?.price ?: 0L
                                Column(Modifier.padding(vertical = 8.dp)) {
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                        Text(name, color = MaterialTheme.colorScheme.onSurface)
                                        Text(String.format("%02d", qty), color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                        Text("${price.formatVND()} đ", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                                        Text("${(price * qty).formatVND()} đ", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(top = 24.dp)) {
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Tạm tính", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                Text("${totalAmount.formatVND()} đ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Vui lòng trả lời tất cả các câu hỏi để tiếp tục", color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                            if (paymentError != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    paymentError,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val trimmedPhone = phoneNumber.trim()
                                    val orderData = mapOf(
                                        "eventId" to event.id,
                                        "eventName" to event.name,
                                        "orderCode" to orderCode,
                                        "phoneNumber" to trimmedPhone,
                                        "tickets" to selectedTicketsMap,
                                        "total" to totalAmount,
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    onConfirmClick(trimmedPhone, orderData)
                                },
                                enabled = phoneNumber.length == 10 && !isTimeExpired && !isSaving,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(if (isTimeExpired) "Hết giờ" else if (isSaving) "Đang lưu..." else "Tiếp tục", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
                    // Thanh đếm ngược màu đỏ
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.error).padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Hoàn tất đặt vé trong  ", color = Color.White, fontSize = 14.sp)
                        Text(timerText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(event.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(" $scheduleDate", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(" ${event.venueName}, ${event.address}", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("BẢNG CÂU HỎI", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        // Card nhập SĐT
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Số Điện Thoại *", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { input ->
                                        phoneNumber = normalizePhoneNumber(input).take(10)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    isError = isPhoneInvalid,
                                    supportingText = {
                                        if (isPhoneInvalid) {
                                            Text("Số điện thoại phải có 10 chữ số (bắt đầu bằng 0)")
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f),
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Card thông tin vé
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Thông tin đặt vé", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(
                                        text = "Chọn lại vé",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 14.sp,
                                        modifier = Modifier.clickable(onClick = onReselectTicketsClick)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Loại vé", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                    Text("Số lượng", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                }
                                selectedTicketsMap.forEach { (name, qty) ->
                                    val price = schedule?.ticketTypes?.find { it.name == name }?.price ?: 0L
                                    Column(Modifier.padding(vertical = 8.dp)) {
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                            Text(name, color = MaterialTheme.colorScheme.onSurface)
                                            Text(String.format("%02d", qty), color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                            Text("${price.formatVND()} đ", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                                            Text("${(price * qty).formatVND()} đ", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                                        }
                                    }
                                }
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("Tạm tính", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                    Text("${totalAmount.formatVND()} đ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Vui lòng trả lời tất cả các câu hỏi để tiếp tục", color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                        if (paymentError != null) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                paymentError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp).align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}
