package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import huce.fit.myezticket.data.model.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event, // Tạm thời nhận nguyên một đối tượng Event để vẽ UI
    onBackClick: () -> Unit // Hàm xử lý khi bấm nút Quay lại
) {
    Scaffold(
        // Thanh điều hướng trên cùng
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sự kiện", fontSize = 18.sp,fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        // Thanh chốt đơn dính chặt ở dưới đáy (Sticky Bottom Bar)
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Chỉ từ", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "${event.price} đ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.error
                        )
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
        // Nội dung chính có thể cuộn được
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5)) // Màu nền hơi xám nhẹ để nổi bật các Card trắng
        ) {
            // 1. Ảnh Poster to đùng trên cùng
            AsyncImage(
                model = event.image_url,
                contentDescription = "Poster",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            // 2. Khối thông tin Tên, Thời gian, Địa điểm
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = event.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // DỊCH NGÀY GIỜ TỪ FIREBASE SANG CHỮ
                    val dateString = event.date?.toDate()?.let { date ->
                        val formatter = java.text.SimpleDateFormat("HH:mm, dd 'Tháng' MM, yyyy", java.util.Locale.forLanguageTag("vi-VN"))
                        formatter.format(date)
                    } ?: "Đang cập nhật thời gian..."

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = "Time", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        // Gọi biến dateString thay vì dòng chữ gõ cứng
                        Text(text = dateString, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = event.location, fontSize = 14.sp)
                    }
                }
            }

            // 3. Khối Giới thiệu sự kiện
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Giới thiệu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.description.ifEmpty { "Đang cập nhật thông tin giới thiệu cho sự kiện này..." },
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            //// Khối Lịch diễn và Thông tin vé
// 2.5 Khối Lịch diễn và Thông tin vé
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Lịch diễn",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // DỊCH NGÀY GIỜ TỪ FIREBASE SANG CHỮ
                    val dateString = event.date?.toDate()?.let { date ->
                        val formatter = java.text.SimpleDateFormat("HH:mm, dd 'Tháng' MM, yyyy", java.util.Locale.forLanguageTag("vi-VN"))
                        formatter.format(date)
                    } ?: "Đang cập nhật thời gian..."

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = dateString, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Thông tin vé",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // ==========================================
                    // DANH SÁCH VÉ: LOGIC TỰ ĐỘNG THEO SỐ LƯỢNG
                    // ==========================================
                    event.ticketTypes.forEach { ticket ->

                        // App tự làm toán: Nếu số lượng <= 0 thì coi như đã hết vé
                        val isSoldOut = ticket.quantity <= 0

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tên vé (Sẽ tự động gạch ngang nếu isSoldOut = true)
                            Text(
                                text = ticket.name,
                                fontSize = 14.sp,
                                color = if (isSoldOut) Color.LightGray else Color.Gray,
                                textDecoration = if (isSoldOut) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            )

                            // Giá tiền (Sẽ đổi thành chữ "Hết vé" màu đỏ nếu isSoldOut = true)
                            if (isSoldOut) {
                                Text(
                                    text = "Hết vé",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = "${ticket.price} đ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            // Khoảng trống dưới cùng để không bị thanh Bottom Bar che mất chữ
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}