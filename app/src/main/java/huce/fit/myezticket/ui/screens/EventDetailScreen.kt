package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import huce.fit.myezticket.data.model.Event
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import huce.fit.myezticket.ui.components.EventCard
import huce.fit.myezticket.ui.components.HtmlText
import huce.fit.myezticket.utils.formatVND

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    allEvents: List<Event>,
    onBackClick: () -> Unit
) {
    // BIẾN QUAN TRỌNG: Nhớ xem đang chọn suất diễn số mấy (mặc định là 0 - suất đầu tiên)
    var selectedScheduleIndex by remember { mutableStateOf(0) }

    // Lấy ra suất diễn đang được chọn
    val currentSchedule = event.schedules.getOrNull(selectedScheduleIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sự kiện", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {currentSchedule
                        Text("Giá từ", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(text = "${event.minPrice.formatVND()}đ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = { /* Mở màn hình chọn ghế */ },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Mua vé ngay", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5))
        ) {
            AsyncImage(model = event.image_url, contentDescription = "Poster", modifier = Modifier.fillMaxWidth().height(250.dp), contentScale = ContentScale.Crop)

            // Thông tin chung
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = event.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // ==========================================
                    // LOGIC HIỂN THỊ: 1 NGÀY HOẶC NGÀY ĐẦU + "KHÁC"
                    // ==========================================
                    val dateString = if (event.schedules.isNotEmpty()) {
                        val sortedDates = event.schedules.mapNotNull { it.date?.toDate() }.sorted()

                        if (sortedDates.isNotEmpty()) {
                            val formatter = java.text.SimpleDateFormat("HH:mm, dd/MM/yyyy", java.util.Locale("vi", "VN"))
                            val firstDate = formatter.format(sortedDates[0])

                            // Nếu có nhiều hơn 1 ngày diễn, thêm chữ "và khác"
                            if (sortedDates.size > 1) {
                                "$firstDate và khác"
                            } else {
                                firstDate
                            }
                        } else {
                            "Đang cập nhật lịch diễn..."
                        }
                    } else {
                        "Đang cập nhật lịch diễn..."
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = dateString, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = event.location, fontSize = 14.sp)
                    }
                }
            }

            // Khối Giới thiệu có HTML
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Giới thiệu", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    HtmlText(html = event.description.ifEmpty { "Đang cập nhật..." }, modifier = Modifier.fillMaxWidth())
                }
            }

            // KHỐI LỊCH DIỄN VÀ VÉ
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Chọn lịch diễn", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. THANH CHỌN NGÀY
                    if (event.schedules.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(event.schedules.size) { index ->
                                val schedule = event.schedules[index]
                                val isSelected = selectedScheduleIndex == index
                                val timeStr = schedule.date?.toDate()?.let { java.text.SimpleDateFormat("HH:mm", java.util.Locale("vi", "VN")).format(it) } ?: ""
                                val dateStr = schedule.date?.toDate()?.let { java.text.SimpleDateFormat("dd/MM", java.util.Locale("vi", "VN")).format(it) } ?: ""

                                Card(
                                    modifier = Modifier.clickable { selectedScheduleIndex = index }, // Đổi suất diễn khi click
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF0F0F0)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = timeStr, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Black)
                                        Text(text = dateStr, fontSize = 12.sp, color = if (isSelected) Color.White else Color.Gray)
                                    }
                                }
                            }
                        }
                    } else {
                        Text("Đang cập nhật lịch diễn.", fontSize = 14.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Thông tin vé", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    // 2. HIỂN THỊ VÉ CỦA SUẤT DIỄN ĐANG CHỌN
                    currentSchedule?.ticketTypes?.forEach { ticket ->
                        val isSoldOut = ticket.quantity <= 0
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ticket.name,
                                fontSize = 14.sp,
                                color = if (isSoldOut) Color.LightGray else Color.Black,
                                textDecoration = if (isSoldOut) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            )
                            if (isSoldOut) {
                                Text("Hết vé", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("${ticket.price.formatVND()}đ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Ban tổ chức
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = event.organizerLogo.ifEmpty { "https://ui-avatars.com/api/?name=${event.organizerName.replace(" ", "+")}" },
                        contentDescription = null, modifier = Modifier.size(60.dp).clip(CircleShape), contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Đơn vị tổ chức", fontSize = 12.sp, color = Color.Gray)
                        Text(text = event.organizerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            // Sự kiện liên quan
            val relatedEvents = allEvents.filter { it.category == event.category && it.id != event.id }
            if (relatedEvents.isNotEmpty()) {
                Text("Sự kiện liên quan", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(16.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(relatedEvents) { item ->
                        Box(modifier = Modifier.width(260.dp)) { EventCard(event = item, onEventClick = {}) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}