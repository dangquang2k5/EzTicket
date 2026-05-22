package huce.fit.myezticket.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
<<<<<<< HEAD
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
=======
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
<<<<<<< HEAD
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
=======
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import huce.fit.myezticket.data.model.Event
<<<<<<< HEAD
import huce.fit.myezticket.ui.components.EventCard
import huce.fit.myezticket.ui.components.HtmlText
import huce.fit.myezticket.utils.formatVND
import kotlinx.coroutines.launch


=======
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import huce.fit.myezticket.ui.components.EventCard
import huce.fit.myezticket.ui.components.HtmlText
import huce.fit.myezticket.utils.formatVND
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    allEvents: List<Event>,
<<<<<<< HEAD
    onBackClick: () -> Unit,
    onBuyTicketClick: (Int) -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    val now = java.util.Date()

    // Tìm index của suất diễn gần thời điểm hiện tại nhất (ưu tiên sắp diễn, nếu không thì lấy cuối cùng)
    val defaultExpandedIndex = remember(event.schedules) {
        val upcoming = event.schedules.indexOfFirst { it.date?.toDate()?.after(now) == true }
        if (upcoming >= 0) upcoming else (event.schedules.size - 1).coerceAtLeast(0)
    }

    // Mỗi suất diễn có trạng thái mở rộng độc lập
    // expandedIndex = -1 → không ai mở; >= 0 → index đang mở
    var expandedIndex by remember { mutableStateOf(defaultExpandedIndex) }

    // Trạng thái mở rộng phần giới thiệu
    var isDescExpanded by remember { mutableStateOf(false) }


    // ScrollState để cuộn đến phần lịch diễn
    val scrollState = rememberScrollState()
    var scheduleOffsetPx by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
=======
    onBackClick: () -> Unit
) {
    // BIẾN QUAN TRỌNG: Nhớ xem đang chọn suất diễn số mấy (mặc định là 0 - suất đầu tiên)
    var selectedScheduleIndex by remember { mutableStateOf(0) }

    // Lấy ra suất diễn đang được chọn
    val currentSchedule = event.schedules.getOrNull(selectedScheduleIndex)
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sự kiện", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
<<<<<<< HEAD
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, event.name)
                            putExtra(Intent.EXTRA_TEXT, "Xem sự kiện \"${event.name}\" trên EzTicket!")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ sự kiện"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Chia sẻ")
                    }
=======
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
<<<<<<< HEAD
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
=======
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
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
<<<<<<< HEAD
                    Column {
                        Text("Giá từ", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "${event.minPrice.formatVND()}đ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(scheduleOffsetPx)
                            }
                        },
=======
                    Column {currentSchedule
                        Text("Giá từ", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(text = "${event.minPrice.formatVND()}đ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = { /* Mở màn hình chọn ghế */ },
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
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
<<<<<<< HEAD
                .verticalScroll(scrollState)
                .background(Color(0xFFF5F5F5))
        ) {
            // Ảnh banner
            AsyncImage(
                model = event.image_url,
                contentDescription = "Poster",
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentScale = ContentScale.Crop
            )

            // ── Thông tin chung ──────────────────────────────────────────
=======
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5))
        ) {
            AsyncImage(model = event.image_url, contentDescription = "Poster", modifier = Modifier.fillMaxWidth().height(250.dp), contentScale = ContentScale.Crop)

            // Thông tin chung
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = event.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(12.dp))

<<<<<<< HEAD
                    val dateString = if (event.schedules.isNotEmpty()) {
                        val sortedDates = event.schedules.mapNotNull { it.date?.toDate() }.sorted()
                        if (sortedDates.isNotEmpty()) {
                            val fmt = java.text.SimpleDateFormat("HH:mm, dd/MM/yyyy", java.util.Locale("vi", "VN"))
                            val first = fmt.format(sortedDates[0])
                            if (sortedDates.size > 1) "$first và khác" else first
                        } else "Đang cập nhật lịch diễn..."
                    } else "Đang cập nhật lịch diễn..."

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
=======
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
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = dateString, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
<<<<<<< HEAD
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
=======

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = event.location, fontSize = 14.sp)
                    }
                }
            }

<<<<<<< HEAD
            // ── Giới thiệu ───────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Giới thiệu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val descContent = event.description.ifEmpty { "Đang cập nhật..." }
                    val needsExpand = remember(event.description) {
                        val cleanText = event.description.replace(Regex("<[^>]*>"), "")
                        cleanText.length > 400
                    }

                    // Hiển thị thực sự
                    if (needsExpand && !isDescExpanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            HtmlText(html = descContent, modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        HtmlText(html = descContent, modifier = Modifier.fillMaxWidth())
                    }

                    // Chỉ hiển thị nút mở rộng khi nội dung vượt ngưỡng
                    if (needsExpand) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDescExpanded = !isDescExpanded }
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isDescExpanded) "Thu gọn" else "Xem thêm",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (isDescExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // ── Lịch diễn ────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .onGloballyPositioned { coords ->
                        val newOffset = coords.positionInRoot().y.toInt()
                        if (scheduleOffsetPx != newOffset) {
                            scheduleOffsetPx = newOffset
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Lịch diễn",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (event.schedules.isNotEmpty()) {
                        event.schedules.forEachIndexed { index, schedule ->
                            val isExpanded = expandedIndex == index

                            // Format dữ liệu ngày giờ
                            val timeStr = schedule.date?.toDate()?.let {
                                java.text.SimpleDateFormat("HH:mm", java.util.Locale("vi", "VN")).format(it)
                            } ?: "??:??"

                            val dayOfWeek = schedule.date?.toDate()?.let {
                                val cal = java.util.Calendar.getInstance().apply { time = it }
                                when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                                    java.util.Calendar.MONDAY -> "T2"
                                    java.util.Calendar.TUESDAY -> "T3"
                                    java.util.Calendar.WEDNESDAY -> "T4"
                                    java.util.Calendar.THURSDAY -> "T5"
                                    java.util.Calendar.FRIDAY -> "T6"
                                    java.util.Calendar.SATURDAY -> "T7"
                                    java.util.Calendar.SUNDAY -> "CN"
                                    else -> ""
                                }
                            } ?: ""

                            val fullDateStr = schedule.date?.toDate()?.let {
                                java.text.SimpleDateFormat("dd 'Tháng' MM, yyyy", java.util.Locale("vi", "VN")).format(it)
                            } ?: ""

                            // Rotation animation cho icon chevron
                            val chevronRotation by animateFloatAsState(
                                targetValue = if (isExpanded) 90f else 0f,
                                label = "chevron_$index"
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isExpanded) Color(0xFFE8F5E9) else Color(0xFFF8F9FA)
                                )
                            ) {
                                Column {
                                    // ── Hàng header: chevron + thời gian + nút mua vé ──
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedIndex = if (isExpanded) -1 else index
                                            }
                                            .padding(horizontal = 12.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Mũi tên xoay khi mở rộng
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .rotate(chevronRotation)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Thông tin giờ + ngày
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "$timeStr, $dayOfWeek",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = fullDateStr,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Nút mua vé ngay
                                        Button(
                                            onClick = { onBuyTicketClick(index) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                "Mua vé ngay",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // ── Phần mở rộng: danh sách loại vé ──
                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically(),
                                        exit = shrinkVertically()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    start = 44.dp,
                                                    end = 12.dp,
                                                    bottom = 12.dp
                                                )
                                        ) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                                thickness = 0.5.dp,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )

                                            if (schedule.ticketTypes.isEmpty()) {
                                                Text(
                                                    "Chưa có thông tin vé",
                                                    fontSize = 13.sp,
                                                    color = Color.Gray
                                                )
                                            } else {
                                                schedule.ticketTypes.forEach { ticket ->
                                                    val isSoldOut = ticket.quantity <= 0

                                                    Card(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 4.dp),
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.surface
                                                        )
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(
                                                                    horizontal = 14.dp,
                                                                    vertical = 12.dp
                                                                ),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = ticket.name,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = if (isSoldOut) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                                                modifier = Modifier.weight(1f),
                                                                textDecoration = if (isSoldOut) TextDecoration.LineThrough else null
                                                            )

                                                            if (isSoldOut) {
                                                                Surface(
                                                                    color = Color(0xFFFF6B8A),
                                                                    shape = RoundedCornerShape(20.dp)
                                                                ) {
                                                                    Text(
                                                                        "Hết vé",
                                                                        modifier = Modifier.padding(
                                                                            horizontal = 10.dp,
                                                                            vertical = 4.dp
                                                                        ),
                                                                        fontSize = 11.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = Color.White,
                                                                        textDecoration = TextDecoration.LineThrough
                                                                    )
                                                                }
                                                            } else {
                                                                Text(
                                                                    "${ticket.price.formatVND()}đ",
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
=======
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
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
                                    }
                                }
                            }
                        }
                    } else {
<<<<<<< HEAD
                        Text(
                            "Đang cập nhật lịch diễn.",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
=======
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
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
                    }
                }
            }

<<<<<<< HEAD
            // ── Ban tổ chức ───────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = event.organizerLogo.ifEmpty {
                            "https://ui-avatars.com/api/?name=${event.organizerName.replace(" ", "+")}"
                        },
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
=======
            // Ban tổ chức
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = event.organizerLogo.ifEmpty { "https://ui-avatars.com/api/?name=${event.organizerName.replace(" ", "+")}" },
                        contentDescription = null, modifier = Modifier.size(60.dp).clip(CircleShape), contentScale = ContentScale.Crop
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Đơn vị tổ chức", fontSize = 12.sp, color = Color.Gray)
<<<<<<< HEAD
                        Text(event.organizerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
=======
                        Text(text = event.organizerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
                    }
                }
            }

<<<<<<< HEAD
            // ── Sự kiện liên quan ─────────────────────────────────────────
            val relatedEvents = allEvents.filter { it.category == event.category && it.id != event.id }
            if (relatedEvents.isNotEmpty()) {
                var showAllRelated by remember { mutableStateOf(false) }
                val displayRelated = relatedEvents.take(if (showAllRelated) relatedEvents.size else 4)

                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(
                        "Sự kiện liên quan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
                    )

                    // Lưới 2 cột – có thể click để sang trang sự kiện đó
                    val rows = displayRelated.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                rowItems.forEach { relEvent ->
                                    EventCard(
                                        event = relEvent,
                                        modifier = Modifier.weight(1f).padding(8.dp),
                                        onEventClick = { eventId -> onEventClick(eventId) }
                                    )
                                }
                                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Nút "Xem thêm" / "Thu gọn" ở dưới
                    if (relatedEvents.size > 4) {
                        TextButton(
                            onClick = { showAllRelated = !showAllRelated },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = if (showAllRelated) "Thu gọn" else "Xem thêm",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (showAllRelated) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
=======
            // Sự kiện liên quan
            val relatedEvents = allEvents.filter { it.category == event.category && it.id != event.id }
            if (relatedEvents.isNotEmpty()) {
                Text("Sự kiện liên quan", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(16.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(relatedEvents) { item ->
                        Box(modifier = Modifier.width(260.dp)) { EventCard(event = item, onEventClick = {}) }
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}