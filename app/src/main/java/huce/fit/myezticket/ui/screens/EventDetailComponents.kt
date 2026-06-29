package huce.fit.myezticket.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.ui.components.EventCard
import huce.fit.myezticket.ui.components.HtmlText
import huce.fit.myezticket.utils.formatVND

@Composable
fun EventPosterBanner(
    imageUrl: String,
    status: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Poster",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (status == "COMING_SOON") {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFFF6F00).copy(alpha = 0.85f)
            ) {
                Text(
                    "Sắp mở bán 🔥",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (status == "SOLD_OUT") {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.85f)
            ) {
                Text(
                    "Hết vé",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EventGeneralInfoCard(
    event: Event,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(12.dp))

            val dateString = event.displayDate

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
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
}

@Composable
fun EventDescriptionCard(
    description: String,
    isDescExpanded: Boolean,
    onToggleDesc: () -> Unit,
    modifier: Modifier = Modifier,
    collapsible: Boolean = true
) {
    Card(
        modifier = modifier,
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

            val descContent = description.ifEmpty { "Đang cập nhật..." }
            val needsExpand = remember(description, collapsible) {
                if (!collapsible) false else {
                    val cleanText = description.replace(Regex("<[^>]*>"), "")
                    cleanText.length > 400
                }
            }

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

            if (needsExpand) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleDesc() }
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
}

@Composable
fun EventOrganizerCard(
    event: Event,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                // Hiển thị chữ cái đầu tiên làm background placeholder cục bộ (không cần mạng)
                val firstChar = remember(event.organizerName) {
                    event.organizerName.trim().firstOrNull()?.toString()?.uppercase() ?: "B"
                }
                Text(
                    text = firstChar,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                // AsyncImage nằm đè lên. Nếu ảnh load thành công, nó sẽ hiển thị đè lên chữ.
                // Nếu load lỗi hoặc trống, nó sẽ trong suốt và để lộ chữ cái bên dưới.
                AsyncImage(
                    model = event.organizerLogo,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Đơn vị tổ chức", fontSize = 12.sp, color = Color.Gray)
                Text(event.organizerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun EventSchedulesCard(
    event: Event,
    expandedIndex: Int,
    onExpandIndex: (Int) -> Unit,
    onBuyTicketClick: (Int) -> Unit,
    now: java.util.Date,
    modifier: Modifier = Modifier
) {
    val visibleSchedules = remember(event.schedules) {
        event.schedules.mapIndexed { index, schedule ->
            index to schedule
        }.filter { (_, schedule) ->
            schedule.ticketTypes.any { it.isVisible }
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Lịch diễn",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (visibleSchedules.isNotEmpty()) {
                visibleSchedules.forEach { (index, schedule) ->
                    val isExpanded = expandedIndex == index

                    val timeStr = schedule.dateMillis?.let { java.util.Date(it) }?.let { sDate ->
                        val timeFmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale("vi", "VN"))
                        val start = timeFmt.format(sDate)
                        val end = schedule.endDateMillis?.let { timeFmt.format(java.util.Date(it)) }
                        if (end != null) "$start - $end" else start
                    } ?: "??:??"

                    val dayOfWeek = schedule.dateMillis?.let { java.util.Date(it) }?.let {
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

                    val fullDateStr = schedule.dateMillis?.let { java.util.Date(it) }?.let {
                        java.text.SimpleDateFormat("dd 'Tháng' MM, yyyy", java.util.Locale("vi", "VN")).format(it)
                    } ?: ""

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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onExpandIndex(if (isExpanded) -1 else index)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .rotate(chevronRotation)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

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

                                val startDate = schedule.dateMillis?.let { java.util.Date(it) }
                                val endDate = schedule.endDateMillis?.let { java.util.Date(it) }
                                 
                                val isStarted = startDate != null && startDate.before(now)
                                val isEnded = endDate != null && endDate.before(now)
                                val isPast = isStarted
                                val visibleTickets = schedule.ticketTypes.filter { it.isVisible }
                                val isSoldOut = visibleTickets.isNotEmpty() && visibleTickets.all { it.quantity <= 0 }

                                val buttonText = when {
                                    isSoldOut -> "Hết vé"
                                    event.status == "COMING_SOON" -> "Sắp mở bán"
                                    !isStarted -> "Mua vé ngay"
                                    !isEnded -> "Đang diễn ra"
                                    else -> "Đã kết thúc"
                                }

                                Button(
                                    onClick = { onBuyTicketClick(index) },
                                    enabled = !isPast && event.status != "COMING_SOON" && !isSoldOut,
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

                                    val visibleTickets = schedule.ticketTypes.filter { it.isVisible }
                                    if (visibleTickets.isEmpty()) {
                                        Text(
                                            text = "Chưa có thông tin vé",
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                    } else {
                                        visibleTickets.forEach { ticket ->
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
                                                                text = "Hết vé",
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
                                                            text = "${ticket.price.formatVND()}đ",
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
                    text = "Đang cập nhật lịch diễn.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EventRelatedSection(
    event: Event,
    allEvents: List<Event>,
    columns: Int,
    onEventClick: (String) -> Unit,
    onSeeAllClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val relatedEvents = remember(allEvents, event) {
        allEvents.filter { it.category == event.category && it.id != event.id }
    }
    
    if (relatedEvents.isNotEmpty()) {
        val displayRelated = relatedEvents.take(6)

        Column(modifier = modifier) {
            Text(
                text = "Sự kiện liên quan",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
            )

            val rows = displayRelated.chunked(columns)
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
                        val emptySlots = columns - rowItems.size
                        if (emptySlots > 0) {
                            repeat(emptySlots) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (relatedEvents.size > 6) {
                TextButton(
                    onClick = { onSeeAllClick(event.category) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Xem thêm (${relatedEvents.size - 6} sự kiện)",
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
            }
        }
    }
}
