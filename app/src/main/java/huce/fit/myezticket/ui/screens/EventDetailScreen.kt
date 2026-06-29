package huce.fit.myezticket.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import huce.fit.myezticket.domain.model.Event
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
    onToggleFavorite: ((String) -> Unit)? = null,
    onSeeMoreRelatedClick: (String) -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val now = remember { java.util.Date() }

    // Lọc các suất diễn có ít nhất 1 loại vé hiển thị (isVisible == true)
    val visibleSchedules = remember(event.schedules) {
        event.schedules.mapIndexed { index, schedule ->
            index to schedule
        }.filter { (_, schedule) ->
            schedule.ticketTypes.any { it.isVisible }
        }
    }

    // Kiểm tra xem tất cả các suất diễn hiển thị đã bắt đầu chưa (startDate < now)
    val allSchedulesPast = remember(visibleSchedules, now) {
        visibleSchedules.isNotEmpty() && visibleSchedules.all { (_, schedule) ->
            val sDate = schedule.dateMillis?.let { java.util.Date(it) }
            sDate != null && sDate.before(now)
        }
    }

    // Xác định trạng thái và text hiển thị cho Bottom Bar chính
    val bottomBarButtonText = remember(allSchedulesPast, visibleSchedules, now, event) {
        if (event.status == "COMING_SOON") {
            "Sắp mở bán"
        } else if (event.isSoldOut) {
            "Hết vé"
        } else if (!allSchedulesPast) {
            "Mua vé ngay"
        } else {
            // Lấy suất diễn kết thúc muộn nhất trong các suất diễn hiển thị
            val lastSchedule = visibleSchedules.map { it.second }.maxByOrNull { it.endDateMillis ?: it.dateMillis ?: 0L }
            val endDate = lastSchedule?.endDateMillis?.let { java.util.Date(it) }
            
            if (endDate != null && now.before(endDate)) {
                "Đang diễn ra"
            } else {
                "Đã kết thúc"
            }
        }
    }

    // Tìm index của suất diễn gần thời điểm hiện tại nhất (ưu tiên sắp diễn, nếu không thì lấy cuối cùng)
    val defaultExpandedIndex = remember(visibleSchedules) {
        val upcoming = visibleSchedules.indexOfFirst { (_, schedule) ->
            val sDate = schedule.dateMillis?.let { java.util.Date(it) }
            sDate != null && sDate.after(now)
        }
        if (upcoming >= 0) {
            visibleSchedules[upcoming].first
        } else {
            visibleSchedules.lastOrNull()?.first ?: -1
        }
    }

    // Mỗi suất diễn có trạng thái mở rộng độc lập
    // expandedIndex = -1 → không ai mở; >= 0 → index đang mở
    var expandedIndex by remember { mutableStateOf(defaultExpandedIndex) }

    // Trạng thái mở rộng phần giới thiệu
    var isDescExpanded by remember { mutableStateOf(false) }

    // ScrollState để cuộn đến phần lịch diễn
    val scrollState = rememberScrollState()
    var scheduleOffsetPx by remember { mutableStateOf(0) }
    var scrollContainerOffsetY by remember { mutableStateOf(0) }
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
                            if (!allSchedulesPast && event.status != "COMING_SOON" && !event.isSoldOut && visibleSchedules.isNotEmpty()) {
                                expandedIndex = defaultExpandedIndex
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(scheduleOffsetPx)
                                }
                            }
                        },
                        enabled = !allSchedulesPast && event.status != "COMING_SOON" && !event.isSoldOut && visibleSchedules.isNotEmpty(),
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val currentMaxWidth = this.maxWidth
                val isLandscapeOrTablet = currentMaxWidth > 720.dp
                
                if (isLandscapeOrTablet) {
                    // BỐ CỤC 2 CỘT CHO TABLET / LANDSCAPE
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5))
                    ) {
                        // Cột trái (Cố định): Ảnh banner và card thông tin chung
                        val leftScrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .width(360.dp)
                                .fillMaxHeight()
                                .verticalScroll(leftScrollState)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            EventPosterBanner(
                                imageUrl = event.image_url,
                                status = if (event.isSoldOut) "SOLD_OUT" else event.status,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                            EventGeneralInfoCard(
                                event = event,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }

                        VerticalDivider(color = Color(0xFFE2E2E2), thickness = 1.dp)

                        // Cột phải (Cuộn): Giới thiệu, Lịch diễn, BTC, Sự kiện liên quan
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 8.dp)
                                .onGloballyPositioned { coords ->
                                    scrollContainerOffsetY = coords.positionInRoot().y.toInt()
                                }
                        ) {
                            // ── Giới thiệu ──
                            EventDescriptionCard(
                                description = event.description,
                                isDescExpanded = isDescExpanded,
                                onToggleDesc = { isDescExpanded = !isDescExpanded },
                                collapsible = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            )

                            // ── Lịch diễn ──
                            EventSchedulesCard(
                                event = event,
                                expandedIndex = expandedIndex,
                                onExpandIndex = { expandedIndex = it },
                                onBuyTicketClick = onBuyTicketClick,
                                now = now,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .onGloballyPositioned { coords ->
                                        if (scrollContainerOffsetY > 0) {
                                            val newOffset = coords.positionInRoot().y.toInt() - scrollContainerOffsetY + scrollState.value
                                            if (scheduleOffsetPx != newOffset) {
                                                scheduleOffsetPx = newOffset
                                            }
                                        }
                                    }
                            )

                            // ── Ban tổ chức ──
                            EventOrganizerCard(
                                event = event,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            // ── Sự kiện liên quan ──
                            val rightSideWidth = currentMaxWidth - 360.dp
                            val relColumns = if (rightSideWidth > 550.dp) 3 else 2
                            EventRelatedSection(
                                event = event,
                                allEvents = allEvents,
                                columns = relColumns,
                                onEventClick = onEventClick,
                                onSeeAllClick = onSeeMoreRelatedClick,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                } else {
                    // BỐ CỤC 1 CỘT (GIỮ NGUYÊN BỐ CỤC CŨ CHO ĐIỆN THOẠI ĐỨNG)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .background(Color(0xFFF5F5F5))
                            .onGloballyPositioned { coords ->
                                scrollContainerOffsetY = coords.positionInRoot().y.toInt()
                            }
                    ) {
                        // Ảnh banner
                        EventPosterBanner(
                            imageUrl = event.image_url,
                            status = if (event.isSoldOut) "SOLD_OUT" else event.status,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )

                        // ── Thông tin chung ──
                        EventGeneralInfoCard(
                            event = event,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )

                        // ── Giới thiệu ──
                        EventDescriptionCard(
                            description = event.description,
                            isDescExpanded = isDescExpanded,
                            onToggleDesc = { isDescExpanded = !isDescExpanded },
                            collapsible = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // ── Lịch diễn ──
                        EventSchedulesCard(
                            event = event,
                            expandedIndex = expandedIndex,
                            onExpandIndex = { expandedIndex = it },
                            onBuyTicketClick = onBuyTicketClick,
                            now = now,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .onGloballyPositioned { coords ->
                                    if (scrollContainerOffsetY > 0) {
                                        val newOffset = coords.positionInRoot().y.toInt() - scrollContainerOffsetY + scrollState.value
                                        if (scheduleOffsetPx != newOffset) {
                                            scheduleOffsetPx = newOffset
                                        }
                                    }
                                }
                        )

                        // ── Ban tổ chức ──
                        EventOrganizerCard(
                            event = event,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // ── Sự kiện liên quan ──
                        EventRelatedSection(
                            event = event,
                            allEvents = allEvents,
                            columns = 2,
                            onEventClick = onEventClick,
                            onSeeAllClick = onSeeMoreRelatedClick,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }
}