package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import huce.fit.myezticket.domain.model.Event

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
    isSavingPayment: Boolean = false,
    paymentError: String? = null,
    onBackClick: () -> Unit,
    onPaymentComplete: (paymentMethod: String, orderCode: String) -> Unit
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
    val orderCode = remember(event.id, scheduleIndex, selectedTicketsString, phoneNumber) {
        "EZT${System.currentTimeMillis().toString().takeLast(8)}"
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phương thức thanh toán", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
            )
        },
        bottomBar = {
            Surface(color = Color(0xFF121212), modifier = Modifier.height(100.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tổng tiền", color = Color.White, fontSize = 16.sp)
                        Text(
                            "${formatVnd(totalAmount)} đ",
                            color = Color(0xFF4CAF50),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            selectedMethod?.let { method ->
                                onPaymentComplete(method.id, orderCode)
                            }
                        },
                        enabled = selectedMethod != null && !isSavingPayment,
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth(0.46f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                    ) {
                        Text(
                            if (isSavingPayment) "Đang lưu..." else "Hoàn tất",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
        containerColor = Color.Black
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
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            paymentMethods.forEach { method ->
                PaymentMethodItem(
                    method = method,
                    selected = selectedMethod?.id == method.id,
                    onClick = { selectedMethod = method }
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(8.dp))
            PaymentQrCard(
                selectedMethod = selectedMethod,
                qrPayload = qrPayload
            )
            if (paymentError != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    paymentError,
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(24.dp))
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                eventName,
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Text("Mã đơn: $orderCode", color = Color.DarkGray, fontSize = 13.sp)
            Text("SĐT: $phoneNumber", color = Color.DarkGray, fontSize = 13.sp)
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            selectedTicketsMap.forEach { (name, quantity) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name, color = Color.Black, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("x$quantity", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tạm tính", color = Color.Black, fontWeight = FontWeight.Bold)
                Text("${formatVnd(totalAmount)} đ", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PaymentMethodItem(
    method: PaymentMethod,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF102216) else Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, if (selected) Color(0xFF00C853) else Color(0xFF333333)),
        shape = RoundedCornerShape(12.dp)
    ) {
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
                Text(method.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(method.subtitle, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (selected) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00C853), modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun PaymentQrCard(
    selectedMethod: PaymentMethod?,
    qrPayload: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                selectedMethod?.title ?: "Chưa chọn phương thức",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(12.dp))
            if (selectedMethod != null && qrPayload != null) {
                PaymentQrCode(
                    payload = qrPayload,
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .aspectRatio(1f)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Mã QR ${selectedMethod.title}",
                    color = selectedMethod.accentColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("QR", color = Color.Gray, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PaymentQrCode(
    payload: String,
    modifier: Modifier = Modifier
) {
    val matrix = remember(payload) { createQrMatrix(payload) }

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        val matrixWidth = matrix.width
        val matrixHeight = matrix.height
        val cellSize = minOf(size.width / matrixWidth, size.height / matrixHeight)
        val left = (size.width - cellSize * matrixWidth) / 2f
        val top = (size.height - cellSize * matrixHeight) / 2f

        for (y in 0 until matrixHeight) {
            for (x in 0 until matrixWidth) {
                if (matrix.get(x, y)) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(left + x * cellSize, top + y * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}

private fun createQrMatrix(payload: String): BitMatrix {
    val hints = mapOf<EncodeHintType, Any>(
        EncodeHintType.CHARACTER_SET to "UTF-8",
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to 1
    )
    return MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, 180, 180, hints)
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
