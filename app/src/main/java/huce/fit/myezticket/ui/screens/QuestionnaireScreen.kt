package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.background
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
import com.google.firebase.database.FirebaseDatabase
import huce.fit.myezticket.data.model.Event
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    event: Event,
    scheduleIndex: Int,
    selectedTicketsString: String?,
    onBackClick: () -> Unit,
    onConfirmClick: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var remainingSeconds by rememberSaveable(event.id, scheduleIndex, selectedTicketsString) {
        mutableIntStateOf(15 * 60)
    }
    val schedule = event.schedules.getOrNull(scheduleIndex)

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

    LaunchedEffect(event.id, scheduleIndex, selectedTicketsString) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bảng câu hỏi", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
            )
        },
        bottomBar = {
            Surface(color = Color(0xFF121212), modifier = Modifier.height(100.dp)) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tổng tiền", color = Color.White, fontSize = 16.sp)
                        Text("${formatVnd(totalAmount)} đ", color = Color(0xFF4CAF50), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val trimmedPhone = phoneNumber.trim()
                            val db = FirebaseDatabase.getInstance().getReference("userOrders")
                            val orderData = mapOf(
                                "eventId" to event.id,
                                "eventName" to event.name,
                                "phoneNumber" to trimmedPhone,
                                "tickets" to selectedTicketsMap,
                                "total" to totalAmount,
                                "timestamp" to System.currentTimeMillis()
                            )
                            db.push().setValue(orderData)
                            onConfirmClick(trimmedPhone)
                        },
                        enabled = phoneNumber.trim().length >= 10 && !isTimeExpired,
                        modifier = Modifier.width(160.dp).height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                    ) {
                        Text(if (isTimeExpired) "Hết giờ" else "Tiếp tục", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            // Thanh đếm ngược màu đỏ
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFD32F2F)).padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccessTime, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Hoàn tất đặt vé trong  ", color = Color.White, fontSize = 14.sp)
                Text(timerText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(event.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(" 19:30 - 19:30, 20 Tháng 11, 2026", color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(" ${event.venueName}, ${event.address}", color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(Modifier.height(24.dp))
                Text("BẢNG CÂU HỎI", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 18.sp)

                // Card nhập SĐT
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Số Điện Thoại *", color = Color.White, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White
                            )
                        )
                    }
                }

                // Card Thông tin vé màu TRẮNG
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Thông tin đặt vé", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Chọn lại vé", color = Color(0xFF4CAF50), fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Loại vé", color = Color.Black, fontWeight = FontWeight.Bold)
                            Text("Số lượng", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        selectedTicketsMap.forEach { (name, qty) ->
                            val price = schedule?.ticketTypes?.find { it.name == name }?.price ?: 0L
                            Column(Modifier.padding(vertical = 8.dp)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text(name, color = Color.Black)
                                    Text(String.format("%02d", qty), color = Color.Black)
                                }
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("${formatVnd(price)} đ", color = Color.Gray, fontSize = 12.sp)
                                    Text("${formatVnd(price * qty)} đ", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Tạm tính", color = Color.Black, fontWeight = FontWeight.Bold)
                            Text("${formatVnd(totalAmount)} đ", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Vui lòng trả lời tất cả các câu hỏi để tiếp tục", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
        }
    }
}
