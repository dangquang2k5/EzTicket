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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.ui.components.EventCard
import huce.fit.myezticket.ui.components.HtmlText
import huce.fit.myezticket.utils.formatVND
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    allEvents: List<Event>,
    onBackClick: () -> Unit,
    onBuyTicketClick: (Int) -> Unit = {},
    onEventClick: (String) -> Unit = {},
    isFavorite: Boolean = false,
    onToggleFavorite: ((String) -> Unit)? = null
) {
    val now = remember { java.util.Date() }

    // Kiểm tra xem tất cả các suất diễn đã bắt đầu chưa (startDate < now)
    val allSchedulesPast = remember(event.schedules, now) {
        event.schedules.isNotEmpty() && event.schedules.all { schedule ->
            val sDate = schedule.date?.toDate()
            sDate != null && sDate.before(now)
        }
    }

    // Xác định trạng thái và text hiển thị cho Bottom Bar chính
    val bottomBarButtonText = remember(allSchedulesPast, event.schedules, now) {
        if (!allSchedulesPast) {
            "Mua vé ngay"
        } else {
            // Lấy suất diễn kết thúc muộn nhất
            val lastSchedule = event.schedules.maxByOrNull { it.endDate?.toDate()?.time ?: it.date?.toDate()?.time ?: 0L }
            val endDate = lastSchedule?.endDate?.toDate()
            
            if (endDate != null && now.before(endDate)) {
                "Đang diễn ra"
            } else {
                "Đã kết thúc"
            }
        }
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sự kiện", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Nút yêu thích
                    if (onToggleFavorite != null) {
                        IconButton(onClick = { onToggleFavorite(event.id) }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite
                                              else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Bỏ yêu thích" else "Yêu thích",
                                tint = if (isFavorite) Color(0xFFFF4081)
                                       else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    // Nút chia sẻ
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                            if (!allSchedulesPast) {
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(scheduleOffsetPx)
                                }
                            }
                        },
                        enabled = !allSchedulesPast,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Gray,
                            contentColor = Color.White,
                            disabledContentColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(bottomBarButtonText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
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
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = event.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    val dateString = event.displayDate

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = dateString, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = event.venueName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = event.address,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

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

                    // Tối ưu hóa Compose: Giữ nguyên duy nhất một node HtmlText để tái sử dụng WebView cũ
                    // Chỉ thay đổi Modifier chiều cao, tránh việc huỷ và tạo lại WebView khi click "Xem thêm"
                    HtmlText(
                        html = descContent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (needsExpand && !isDescExpanded) {
                                    Modifier.height(220.dp)
                                } else {
                                    Modifier
                                }
                            )
                    )

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
                            val timeStr = schedule.date?.toDate()?.let { sDate ->
                                val timeFmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale("vi", "VN"))
                                val start = timeFmt.format(sDate)
                                val end = schedule.endDate?.toDate()?.let { timeFmt.format(it) }
                                if (end != null) "$start - $end" else start
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

                                        val startDate = schedule.date?.toDate()
                                        val endDate = schedule.endDate?.toDate()
                                         
                                        val isStarted = startDate != null && startDate.before(now)
                                        val isEnded = endDate != null && endDate.before(now)
                                        val isPast = isStarted

                                        val buttonText = when {
                                            !isStarted -> "Mua vé ngay"
                                            !isEnded -> "Đang diễn ra"
                                            else -> "Đã kết thúc"
                                        }

                                        // Nút mua vé ngay
                                        Button(
                                            onClick = { onBuyTicketClick(index) },
                                            enabled = !isPast,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                disabledContainerColor = Color.Gray,
                                                contentColor = Color.White,
                                                disabledContentColor = Color.LightGray
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = buttonText,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
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
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            "Đang cập nhật lịch diễn.",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

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
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Đơn vị tổ chức", fontSize = 12.sp, color = Color.Gray)
                        Text(event.organizerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            // ── Sự kiện liên quan (phân trang theo batch 4) ──────────────
            val relatedEvents = allEvents.filter { it.category == event.category && it.id != event.id }
            if (relatedEvents.isNotEmpty()) {
                // Số sự kiện liên quan đang hiển thị, tăng dần 4 mỗi lần nhấn "Xem thêm"
                var relatedDisplayCount by remember { mutableStateOf(4) }
                val displayRelated = relatedEvents.take(relatedDisplayCount)
                val hasMoreRelated = relatedEvents.size > relatedDisplayCount

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

                    // Nút "Xem thêm" — chỉ hiện khi còn sự kiện chưa được hiển thị
                    if (hasMoreRelated) {
                        TextButton(
                            onClick = { relatedDisplayCount += 4 },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Xem thêm (${relatedEvents.size - relatedDisplayCount} sự kiện)",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (relatedEvents.size > 4) {
                        // Đã hiển thị hết — thông báo nhỏ
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "✓ Đã hiển thị tất cả ${relatedEvents.size} sự kiện liên quan",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}