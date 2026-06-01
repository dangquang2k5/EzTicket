package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.model.PurchasedTicket
import huce.fit.myezticket.ui.components.MainTabHeader
import huce.fit.myezticket.ui.components.QrCode
import huce.fit.myezticket.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    allEvents: List<Event> = emptyList(),
    purchasedTickets: List<PurchasedTicket> = emptyList(),
    isTicketsLoading: Boolean = false,
    onEventClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onPendingPaymentClick: (PurchasedTicket) -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var selectedStatusFilter by remember { mutableStateOf("Tất cả") }

    val savedPin by profileViewModel.pinCode.collectAsState()
    var clickedTicket by remember { mutableStateOf<PurchasedTicket?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MainTabHeader(title = "Vé của tôi")
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            PurchasedTicketsTab(
                tickets = purchasedTickets,
                allEvents = allEvents,
                isLoading = isTicketsLoading,
                selectedStatus = selectedStatusFilter,
                onStatusChange = { selectedStatusFilter = it },
                onEventClick = onEventClick,
                onHomeClick = onHomeClick,
                onTicketClick = { ticket ->
                    if (ticket.displayStatus == PurchasedTicket.STATUS_PENDING) {
                        onPendingPaymentClick(ticket)
                    } else {
                        val loadedPin = savedPin
                        if (loadedPin != null) {
                            clickedTicket = ticket
                            if (loadedPin.isNotBlank()) {
                                showPinDialog = true
                            } else {
                                showDetailDialog = true
                            }
                        }
                    }
                }
            )
        }
    }

    if (showPinDialog && clickedTicket != null) {
        PinVerificationDialog(
            savedPin = savedPin.orEmpty(),
            onDismiss = {
                showPinDialog = false
                clickedTicket = null
            },
            onSuccess = {
                showPinDialog = false
                showDetailDialog = true
            }
        )
    }

    if (showDetailDialog && clickedTicket != null) {
        TicketDetailDialog(
            ticket = clickedTicket!!,
            onDismiss = {
                showDetailDialog = false
                clickedTicket = null
            }
        )
    }
}

// ===== TAB VÉ ĐÃ MUA =====
@Composable
private fun PurchasedTicketsTab(
    tickets: List<PurchasedTicket>,
    allEvents: List<Event>,
    isLoading: Boolean,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    onEventClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onTicketClick: (PurchasedTicket) -> Unit
) {
    var upcomingSelected by remember { mutableStateOf(true) }
    val nowMillis by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            delay(1000)
            value = System.currentTimeMillis()
        }
    }

    val upcomingTickets = tickets.filter { it.isUpcomingAt(nowMillis) }
    val pastTickets = tickets.filter { !it.isUpcomingAt(nowMillis) }
    val currentTickets = if (upcomingSelected) upcomingTickets else pastTickets

    val statusFilters = listOf("Tất cả", PurchasedTicket.STATUS_SUCCESS, PurchasedTicket.STATUS_PENDING, PurchasedTicket.STATUS_CANCELLED)
    val filteredByStatus = if (!upcomingSelected || selectedStatus == "Tất cả") currentTickets
    else currentTickets.filter { normalizeTicketStatus(it.displayStatus) == normalizeTicketStatus(selectedStatus) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-tabs Sắp diễn ra / Đã kết thúc
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubTabText(
                label = "Sắp diễn ra",
                selected = upcomingSelected,
                modifier = Modifier.weight(1f)
            ) { upcomingSelected = true }
            SubTabText(
                label = "Đã kết thúc",
                selected = !upcomingSelected,
                modifier = Modifier.weight(1f)
            ) { upcomingSelected = false }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            if (upcomingSelected && currentTickets.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(statusFilters) { status ->
                        StatusFilterChip(
                            label = status,
                            selected = selectedStatus == status,
                            onClick = { onStatusChange(status) }
                        )
                    }
                }
            }

            if (filteredByStatus.isEmpty()) {
                if (currentTickets.isEmpty()) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            EmptyTicketState(
                                message = "Bạn chưa có vé nào cả!",
                                buttonLabel = "Mua vé ngay",
                                onButtonClick = onHomeClick
                            )
                        }
                        if (allEvents.isNotEmpty()) {
                            item {
                                Text(
                                    "Có thể bạn cũng thích",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                            item {
                                val eventRows = allEvents.take(4).chunked(2)
                                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                                    eventRows.forEach { rowEvents ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            rowEvents.forEach { event ->
                                                Box(modifier = Modifier.weight(1f)) {
                                                    SuggestedEventCard(event = event, onClick = { onEventClick(event.id) })
                                                }
                                            }
                                            if (rowEvents.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    OutlinedButton(
                                        onClick = onHomeClick,
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Xem thêm sự kiện", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    FilterEmptyState(message = "Không có vé ở trạng thái $selectedStatus")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredByStatus) { ticket -> 
                        PurchasedTicketItem(ticket = ticket, onClick = { onTicketClick(ticket) }) 
                    }
                }
            }
        }
    }
}

// ===== EMPTY STATE =====
@Composable
private fun FilterEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyTicketState(message: String, buttonLabel: String, onButtonClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(84.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(message, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(buttonLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ===== ITEM VÉ ĐÃ MUA =====
@Composable
private fun PurchasedTicketItem(ticket: PurchasedTicket, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .width(58.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8F5E9))
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(ticket.dayText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(ticket.monthText, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
                Text(ticket.yearText, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.width(1.dp).height(86.dp).background(Color(0xFFE0E0E0)))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ticket.eventName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(ticket.displayStatus, when (normalizeTicketStatus(ticket.displayStatus)) {
                        "Thành công" -> MaterialTheme.colorScheme.primary
                        "Đang chờ thanh toán" -> Color(0xFFFF9100)
                        "Đang xử lý" -> Color(0xFFFF9100)
                        "Đã hủy" -> Color(0xFFFF1744)
                        else -> MaterialTheme.colorScheme.secondary
                    })
                    val ticketTypeLabel = if (ticket.quantity > 1) {
                        "${ticket.ticketTypeName} x${ticket.quantity}"
                    } else {
                        ticket.ticketTypeName
                    }
                    if (ticketTypeLabel.isNotBlank()) StatusBadge(ticketTypeLabel, Color(0xFF00838F))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("⊟  Order code: ${ticket.orderCode}", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                Text("🕐  ${ticket.timeText}", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                Text("📍  ${ticket.location}", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ===== DIALOGS =====
@Composable
fun PinVerificationDialog(
    savedPin: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var enteredDigits by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color.Gray
                        )
                    }
                }

                // Lock Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bảo mật",
                        tint = Color(0xFF00B14F),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Nhập mã PIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Vui lòng nhập mã PIN bảo mật để xem chi tiết vé.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredDigits.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) Color(0xFF00B14F) else Color.Transparent)
                                .border(2.dp, Color(0xFF00B14F), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Numeric pad
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("Hủy", "0", "delete")
                )

                keys.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowKeys.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when (key) {
                                    "delete" -> {
                                        IconButton(
                                            onClick = {
                                                if (enteredDigits.isNotEmpty()) {
                                                    enteredDigits = enteredDigits.dropLast(1)
                                                    errorMessage = ""
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Backspace",
                                                tint = Color.DarkGray
                                            )
                                        }
                                    }
                                    "Hủy" -> {
                                        TextButton(onClick = onDismiss) {
                                            Text("Hủy", color = Color.Gray, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(Color(0xFFF5F5F5))
                                                .clickable {
                                                    if (enteredDigits.length < 4) {
                                                        enteredDigits += key
                                                        errorMessage = ""
                                                        if (enteredDigits.length == 4) {
                                                            if (enteredDigits == savedPin) {
                                                                onSuccess()
                                                            } else {
                                                                errorMessage = "Mã PIN không đúng, vui lòng thử lại."
                                                                enteredDigits = ""
                                                            }
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TicketDetailDialog(
    ticket: PurchasedTicket,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chi tiết vé",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Event name (Large, bold)
                Text(
                    text = ticket.eventName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status & Type Badge Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusColor = when (normalizeTicketStatus(ticket.displayStatus)) {
                        "Thành công" -> Color(0xFF00B14F)
                        "Đang chờ thanh toán" -> Color(0xFFFF9100)
                        "Đang xử lý" -> Color(0xFFFF9100)
                        "Đã hủy" -> Color(0xFFFF1744)
                        else -> Color.Gray
                    }
                    StatusBadge(ticket.displayStatus, statusColor)
                    
                    val ticketTypeLabel = if (ticket.quantity > 1) {
                        "${ticket.ticketTypeName} x${ticket.quantity}"
                    } else {
                        ticket.ticketTypeName
                    }
                    if (ticketTypeLabel.isNotBlank()) {
                        StatusBadge(ticketTypeLabel, Color(0xFF00838F))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Details list
                DetailRow(label = "Mã đơn hàng (Order Code):", value = ticket.orderCode, isBoldValue = true)
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = "Mã vé:", value = ticket.qrPayload, isBoldValue = true)
                if (ticket.qrPayload.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    QrCode(
                        payload = ticket.qrPayload,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(168.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Mã QR vé",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = "Thời gian:", value = "${ticket.timeText}, ngày ${ticket.dayText}/${ticket.monthText}/${ticket.yearText}")
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = "Địa điểm:", value = ticket.location)
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = "Số lượng vé:", value = "${ticket.quantity} vé")

                Spacer(modifier = Modifier.height(24.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Đóng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isBoldValue: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            color = Color.Black,
            fontWeight = if (isBoldValue) FontWeight.Bold else FontWeight.Normal,
            lineHeight = 20.sp
        )
    }
}


// ===== SUGGESTED EVENT CARD =====
@Composable
private fun SuggestedEventCard(event: Event, onClick: () -> Unit) {
    val dateFormatter = remember { SimpleDateFormat("dd 'Tháng' MM, yyyy", Locale("vi", "VN")) }
    val firstDate = remember(event) {
        event.schedules.mapNotNull { it.date?.toDate() }.minOrNull()?.let { dateFormatter.format(it) } ?: ""
    }
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        AsyncImage(
            model = event.image_url, contentDescription = event.name,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(event.name, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("Từ ${formatMyTicketPrice(event.minPrice)} đ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        if (firstDate.isNotBlank()) Text(firstDate, color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
    }
}

// ===== UI COMPONENTS =====

@Composable
private fun SubTabText(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        if (selected) {
            Spacer(modifier = Modifier.height(3.dp))
            Box(modifier = Modifier.width(40.dp).height(2.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp)))
        }
    }
}

@Composable
private fun StatusFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0), RoundedCornerShape(50))
            .clickable { onClick() }
            .defaultMinSize(minWidth = 74.dp, minHeight = 32.dp)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.secondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatusBadge(label: String, color: Color, textColor: Color = Color.White) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .defaultMinSize(minHeight = 22.dp)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

private fun normalizeTicketStatus(status: String): String {
    return when (status.trim()) {
        "Đã huỷ" -> "Đã hủy"
        "Đang xử lí" -> "Đang xử lý"
        else -> status.trim()
    }
}

private fun formatMyTicketPrice(price: Long): String = String.format("%,d", price).replace(",", ".")
