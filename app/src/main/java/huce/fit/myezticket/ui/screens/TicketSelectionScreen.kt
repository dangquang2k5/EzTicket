package huce.fit.myezticket.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import huce.fit.myezticket.domain.model.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketSelectionScreen(
    event: Event,
    scheduleIndex: Int,
    isCreatingPendingTicket: Boolean = false,
    onBackClick: () -> Unit,
    onContinueClick: (Map<String, Int>) -> Unit
) {
    val schedule = event.schedules.getOrNull(scheduleIndex)
    val selectedQuantities = remember {
        mutableStateMapOf<String, Int>().apply {
            schedule?.ticketTypes?.filter { it.isVisible }?.forEach { put(it.name, 0) }
        }
    }

    // Trạng thái mở rộng phần chi tiết vé ở dưới
    var isExpanded by remember { mutableStateOf(false) }

    val totalTickets = selectedQuantities.values.sum()
    val totalPrice = schedule?.ticketTypes?.sumOf {
        (selectedQuantities[it.name] ?: 0) * it.price
    } ?: 0L

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > 720.dp
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("Chọn vé", fontWeight = FontWeight.Bold) },
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
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .navigationBarsPadding()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(event.displayDate, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                            }
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                selectedQuantities.filter { it.value > 0 }.forEach { (name, qty) ->
                                    val ticket = schedule?.ticketTypes?.find { it.name == name }
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(name, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                        Text("${String.format("%,d", (ticket?.price ?: 0L) * qty).replace(",", ".")} đ", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ConfirmationNumber, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("x $totalTickets", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Button(
                            onClick = { onContinueClick(selectedQuantities.filter { it.value > 0 }.toMap()) },
                            enabled = totalTickets > 0 && !isCreatingPendingTicket,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        ) {
                            Text(
                                text = if (isCreatingPendingTicket) {
                                    "Đang giữ vé..."
                                } else {
                                    "Tiếp tục - ${String.format("%,d", totalPrice).replace(",", ".")} đ"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        ) { padding ->
            if (isWide) {
                Row(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Cột trái: Danh sách vé
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))
                        schedule?.ticketTypes?.filter { it.isVisible }?.forEach { ticket ->
                            TicketItemRow(
                                name = ticket.name,
                                price = ticket.price,
                                quantity = selectedQuantities[ticket.name] ?: 0,
                                onQuantityChange = { selectedQuantities[ticket.name] = it },
                                maxQuantity = ticket.quantity
                            )
                            DashedDivider()
                        }
                    }

                    VerticalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxHeight()
                    )

                    // Cột phải: Tóm tắt vé và Nút
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
                            Text(
                                text = "Thông tin đặt vé",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(16.dp))

                            Text(event.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                            Spacer(Modifier.height(4.dp))
                            Text(event.displayDate, color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)

                            Spacer(Modifier.height(20.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(Modifier.height(16.dp))

                            selectedQuantities.filter { it.value > 0 }.forEach { (name, qty) ->
                                val ticket = schedule?.ticketTypes?.find { it.name == name }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(name, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text("x$qty", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                                    }
                                    Text(
                                        text = "${String.format("%,d", (ticket?.price ?: 0L) * qty).replace(",", ".")} đ",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(top = 24.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tạm tính", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                                Text(
                                    text = "${String.format("%,d", totalPrice).replace(",", ".")} đ",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = { onContinueClick(selectedQuantities.filter { it.value > 0 }.toMap()) },
                                enabled = totalTickets > 0 && !isCreatingPendingTicket,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            ) {
                                Text(
                                    text = if (isCreatingPendingTicket) {
                                        "Đang giữ vé..."
                                    } else {
                                        "Tiếp tục"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(8.dp))
                    schedule?.ticketTypes?.filter { it.isVisible }?.forEach { ticket ->
                        TicketItemRow(
                            name = ticket.name,
                            price = ticket.price,
                            quantity = selectedQuantities[ticket.name] ?: 0,
                            onQuantityChange = { selectedQuantities[ticket.name] = it },
                            maxQuantity = ticket.quantity
                        )
                        DashedDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun TicketItemRow(
    name: String,
    price: Long,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    maxQuantity: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${String.format("%,d", price).replace(",", ".")} đ", color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Nút giảm màu trắng
            Surface(
                onClick = { if (quantity > 0) onQuantityChange(quantity - 1) },
                enabled = quantity > 0,
                modifier = Modifier.size(width = 55.dp, height = 45.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.32f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        tint = if (quantity > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
                    )
                }
            }

            // Ô số lượng màu trắng
            Surface(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = 55.dp, height = 45.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$quantity", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Nút tăng màu trắng
            Surface(
                onClick = { if (quantity < maxQuantity) onQuantityChange(quantity + 1) },
                enabled = quantity < maxQuantity,
                modifier = Modifier.size(width = 55.dp, height = 45.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.32f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = if (quantity < maxQuantity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
                    )
                }
            }
        }
    }
}

@Composable
fun DashedDivider() {
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = dividerColor,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
        )
    }
}
