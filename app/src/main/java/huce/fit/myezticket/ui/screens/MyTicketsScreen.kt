package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.model.PurchasedTicket
import huce.fit.myezticket.ui.components.HomeBottomNavigation
import java.text.SimpleDateFormat
import java.util.Locale


// ===== MODEL VÉ BÁN LẠI =====
data class ResellTicket(
    val id: String = "",
    val eventName: String = "",
    val imageUrl: String = "",
    val price: Long = 0,
    val status: String = "Đang bán"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    allEvents: List<Event> = emptyList(),
    purchasedTickets: List<PurchasedTicket> = emptyList(),
    isTicketsLoading: Boolean = false,
    onEventClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {}
) {
    // Tab lớn: 0 = Vé đã mua, 1 = Vé bán lại (BỎ Thẻ thành viên theo yêu cầu)
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedStatusFilter by remember { mutableStateOf("Tất cả") }

    val resellTickets = remember { emptyList<ResellTicket>() }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF00C853))
                    .statusBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Vé của tôi",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                // 2 tabs lớn: Vé đã mua | Vé bán lại
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MainTabChip(
                        label = "Vé đã mua",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    MainTabChip(
                        label = "Vé bán lại",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> PurchasedTicketsTab(
                    tickets = purchasedTickets,
                    allEvents = allEvents,
                    isLoading = isTicketsLoading,
                    selectedStatus = selectedStatusFilter,
                    onStatusChange = { selectedStatusFilter = it },
                    onEventClick = onEventClick,
                    onHomeClick = onHomeClick
                )
                1 -> ResellTicketsTab(resellTickets = resellTickets)
            }
        }
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
    onHomeClick: () -> Unit
) {
    var upcomingSelected by remember { mutableStateOf(true) }

    val upcomingTickets = tickets.filter { it.isUpcoming }
    val pastTickets = tickets.filter { !it.isUpcoming }
    val currentTickets = if (upcomingSelected) upcomingTickets else pastTickets

    val statusFilters = listOf("Tất cả", "Thành công", "Đang xử lý", "Đã hủy")
    val filteredByStatus = if (selectedStatus == "Tất cả") currentTickets
    else currentTickets.filter { normalizeTicketStatus(it.status) == normalizeTicketStatus(selectedStatus) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-tabs Sắp diễn ra / Đã kết thúc
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
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
                CircularProgressIndicator(color = Color(0xFF00C853))
            }
        } else {
            if (currentTickets.isNotEmpty()) {
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
                                    color = Color.White,
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
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Text("Xem thêm sự kiện", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    FilterEmptyState(message = "Không có vé ở trạng thái $selectedStatus")
                }
            } else {
                if (selectedStatus == "Thành công" || selectedStatus == "Tất cả") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF00C853))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nhận vé thành công", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredByStatus) { ticket -> PurchasedTicketItem(ticket = ticket) }
                }
            }
        }
    }
}

// ===== TAB VÉ BÁN LẠI =====
@Composable
private fun ResellTicketsTab(resellTickets: List<ResellTicket>) {
    val resellStatusTabs = listOf("Đang bán", "Chờ thanh toán", "Đã thanh toán", "Đã huỷ")
    var selectedResellStatus by remember { mutableStateOf("Đang bán") }
    val filtered = resellTickets.filter { it.status == selectedResellStatus }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().background(Color.Black).padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(resellStatusTabs) { tab ->
                ResellSubTab(tab, selectedResellStatus == tab) { selectedResellStatus = tab }
            }
        }
        if (filtered.isEmpty()) {
            EmptyTicketState("Bạn chưa có vé nào được đăng bán!", "Bán lại vé") {}
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered) { ticket -> ResellTicketItem(ticket = ticket) }
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
            color = Color.Gray,
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
                        colors = listOf(Color(0xFFE07850), Color(0xFFD4603A), Color(0xFFC8B89A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("👨‍🚀", fontSize = 72.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(message, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(buttonLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ===== ITEM VÉ ĐÃ MUA =====
@Composable
private fun PurchasedTicketItem(ticket: PurchasedTicket) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp).width(52.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(ticket.dayText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(ticket.monthText, color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
                Text(ticket.yearText, color = Color.White, fontSize = 11.sp)
            }
            Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(Color.Black))
            Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                Text(ticket.eventName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusBadge(ticket.status, when (normalizeTicketStatus(ticket.status)) {
                        "Thành công" -> Color(0xFF00C853)
                        "Đang xử lý" -> Color(0xFFFF9100)
                        "Đã hủy" -> Color(0xFFFF1744)
                        else -> Color.Gray
                    })
                    val ticketTypeLabel = if (ticket.quantity > 1) {
                        "${ticket.ticketTypeName} x${ticket.quantity}"
                    } else {
                        ticket.ticketTypeName
                    }
                    if (ticketTypeLabel.isNotBlank()) StatusBadge(ticketTypeLabel, Color(0xFF00838F))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("⊟  Order code: ${ticket.orderCode}", color = Color.Gray, fontSize = 12.sp)
                Text("🕐  ${ticket.timeText}", color = Color.Gray, fontSize = 12.sp)
                Text("📍  ${ticket.location}", color = Color.Gray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ===== ITEM VÉ BÁN LẠI =====
@Composable
private fun ResellTicketItem(ticket: ResellTicket) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ticket.imageUrl, contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ticket.eventName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${formatMyTicketPrice(ticket.price)} đ", color = Color(0xFF00C853), fontWeight = FontWeight.Bold)
            }
            StatusBadge(ticket.status, Color(0xFF00C853))
        }
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
        Text(event.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("Từ ${formatMyTicketPrice(event.minPrice)} đ", color = Color(0xFF00C853), fontWeight = FontWeight.Bold, fontSize = 11.sp)
        if (firstDate.isNotBlank()) Text(firstDate, color = Color.Gray, fontSize = 10.sp)
    }
}

// ===== UI COMPONENTS =====

@Composable
private fun MainTabChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Color.White else Color.Transparent)
            .border(1.dp, if (selected) Color.White else Color(0xAAFFFFFF), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF00C853) else Color.White,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

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
        Text(label, color = if (selected) Color.White else Color.Gray, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 15.sp)
        if (selected) {
            Spacer(modifier = Modifier.height(3.dp))
            Box(modifier = Modifier.width(40.dp).height(2.dp).background(Color(0xFF00C853), RoundedCornerShape(1.dp)))
        }
    }
}

@Composable
private fun ResellSubTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = if (selected) Color.White else Color.Gray, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
        if (selected) {
            Spacer(modifier = Modifier.height(3.dp))
            Box(modifier = Modifier.height(2.dp).width(label.length.dp * 7).background(Color(0xFF00C853), RoundedCornerShape(1.dp)))
        }
    }
}

@Composable
private fun StatusFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Color(0xFF00C853) else Color(0xFF1E1E1E))
            .border(1.dp, if (selected) Color(0xFF00C853) else Color(0xFF444444), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(label, color = if (selected) Color.White else Color.Gray, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun StatusBadge(label: String, color: Color, textColor: Color = Color.White) {
    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
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
