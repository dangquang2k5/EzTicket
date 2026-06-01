package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.ui.components.QrCode
import kotlinx.coroutines.delay

private data class PaymentMethod(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    event: Event,
    scheduleIndex: Int,
    selectedTicketsString: String?,
    phoneNumber: String,
    orderCode: String,
    expiresAtMillis: Long,
    isSavingPayment: Boolean = false,
    paymentError: String? = null,
    onBackClick: () -> Unit,
    onTimeExpired: () -> Unit,
    onPaymentComplete: (paymentMethod: String) -> Unit
) {
    val schedule = event.schedules.getOrNull(scheduleIndex)
    val selectedTicketsMap = remember(selectedTicketsString) {
        parseSelectedTicketsArg(selectedTicketsString)
    }
    val totalAmount = selectedTicketsMap.entries.sumOf { (name, qty) ->
        (schedule?.ticketTypes?.find { it.name == name }?.price ?: 0L) * qty
    }
    val paymentMethods = remember {
        listOf(
            PaymentMethod(
                id = "momo",
                title = "Ví MoMo",
                subtitle = "Thanh toán qua ví điện tử MoMo",
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = Color(0xFFD82D8B)
            ),
            PaymentMethod(
                id = "zalopay",
                title = "ZaloPay",
                subtitle = "Thanh toán bằng ví ZaloPay",
                icon = Icons.Default.PhoneAndroid,
                accentColor = Color(0xFF0068FF)
            ),
            PaymentMethod(
                id = "vnpay",
                title = "VNPay",
                subtitle = "Quét QR bằng ứng dụng ngân hàng",
                icon = Icons.Default.CreditCard,
                accentColor = Color(0xFF00AEEF)
            ),
            PaymentMethod(
                id = "bank_transfer",
                title = "Chuyển khoản ngân hàng",
                subtitle = "Chuyển khoản nhanh bằng mã QR",
                icon = Icons.Default.AccountBalance,
                accentColor = Color(0xFF00C853)
            )
        )
    }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var isTimeExpired by remember(orderCode, expiresAtMillis) {
        mutableStateOf(expiresAtMillis <= System.currentTimeMillis())
    }
    val qrPayload = selectedMethod?.let { method ->
        buildPaymentPayload(
            method = method,
            orderCode = orderCode,
            event = event,
            phoneNumber = phoneNumber,
            totalAmount = totalAmount,
            tickets = selectedTicketsMap
        )
    }

    LaunchedEffect(orderCode, expiresAtMillis) {
        delay((expiresAtMillis - System.currentTimeMillis()).coerceAtLeast(0L))
        isTimeExpired = true
        onTimeExpired()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phương thức thanh toán", fontWeight = FontWeight.Bold) },
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
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.height(100.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tổng tiền", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                        Text(
                            "${formatVnd(totalAmount)} đ",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            selectedMethod?.let { method ->
                                onPaymentComplete(method.id)
                            }
                        },
                        enabled = selectedMethod != null && !isSavingPayment && !isTimeExpired,
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth(0.46f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            if (isTimeExpired) "Hết giờ" else if (isSavingPayment) "Đang lưu..." else "Hoàn tất",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OrderSummaryCard(
                eventName = event.name,
                orderCode = orderCode,
                phoneNumber = phoneNumber,
                selectedTicketsMap = selectedTicketsMap,
                totalAmount = totalAmount
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "Chọn phương thức",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            paymentMethods.forEach { method ->
                val isSelected = selectedMethod?.id == method.id
                PaymentMethodItem(
                    method = method,
                    selected = isSelected,
                    qrPayload = if (isSelected) qrPayload else null,
                    onClick = { selectedMethod = method }
                )
                Spacer(Modifier.height(10.dp))
            }

            if (paymentError != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    paymentError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun PaymentSuccessScreen(
    orderCode: String,
    onViewTicketsClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(68.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Thanh toán thành công",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Vé của bạn đã được lưu vào mục Vé đã mua.",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Mã đơn hàng", color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(orderCode, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onViewTicketsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Xem vé đã mua", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onHomeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Về trang chủ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    eventName: String,
    orderCode: String,
    phoneNumber: String,
    selectedTicketsMap: Map<String, Int>,
    totalAmount: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                eventName,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Text("Mã đơn: $orderCode", color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
            Text("SĐT: $phoneNumber", color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            selectedTicketsMap.forEach { (name, quantity) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("x$quantity", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tạm tính", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("${formatVnd(totalAmount)} đ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PaymentMethodItem(
    method: PaymentMethod,
    selected: Boolean,
    qrPayload: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(method.accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(method.icon, null, tint = method.accentColor, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(method.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(method.subtitle, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (selected) {
                    Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }
            if (selected && qrPayload != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    QrCode(
                        payload = qrPayload,
                        modifier = Modifier
                            .fillMaxWidth(0.64f)
                            .aspectRatio(1f)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Quét mã QR ${method.title}",
                        color = method.accentColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun buildPaymentPayload(
    method: PaymentMethod,
    orderCode: String,
    event: Event,
    phoneNumber: String,
    totalAmount: Long,
    tickets: Map<String, Int>
): String {
    return buildString {
        append("EZTICKET|")
        append("method=${method.id}|")
        append("order=$orderCode|")
        append("event=${event.id}|")
        append("phone=$phoneNumber|")
        append("amount=$totalAmount|")
        append("tickets=")
        append(tickets.entries.joinToString(",") { (name, quantity) -> "$name:$quantity" })
    }
}
