package huce.fit.myezticket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.*

/**
 * Đại diện một ngày đã chọn (year, month 0-based, day)
 */
data class PickedDate(val year: Int, val month: Int, val day: Int) : Comparable<PickedDate> {
    override fun compareTo(other: PickedDate): Int {
        return when {
            year != other.year -> year - other.year
            month != other.month -> month - other.month
            else -> day - other.day
        }
    }
    fun format(): String = String.format("%02d/%02d/%04d", day, month + 1, year)
}

/**
 * Dialog lịch chọn khoảng ngày.
 *
 * Cách dùng:
 *  - Lần bấm 1: chọn ngày bắt đầu (vòng tròn xanh)
 *  - Lần bấm 2: chọn ngày kết thúc → vùng giữa được bôi xanh nhạt
 *  - Bấm lại bất kỳ ngày nào: reset về chọn ngày bắt đầu mới
 *  - Nút "Chọn": xác nhận
 *  - Nút "Hủy": đóng dialog không lưu gì
 */
@Composable
fun DateRangePickerDialog(
    initialFrom: PickedDate? = null,
    initialTo: PickedDate? = null,
    onDismiss: () -> Unit,
    onConfirm: (from: PickedDate, to: PickedDate?) -> Unit
) {
    // Trạng thái lịch: tháng/năm hiển thị
    val today = Calendar.getInstance()
    var displayYear by remember { mutableIntStateOf(initialFrom?.year ?: today.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(initialFrom?.month ?: today.get(Calendar.MONTH)) }

    // Trạng thái chọn ngày
    var startDate by remember { mutableStateOf(initialFrom) }
    var endDate by remember { mutableStateOf(initialTo) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryLight = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    // Nhãn ngày đã chọn ở header dialog
    val headerLabel = when {
        startDate != null && endDate != null ->
            "${startDate!!.format()}  →  ${endDate!!.format()}"
        startDate != null ->
            "Từ ${startDate!!.format()} (chọn ngày kết thúc)"
        else -> "Chọn khoảng ngày"
    }

    // Tên tháng tiếng Việt
    val monthName = listOf(
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
        "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
        "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    )

    // Tính số ngày trong tháng và ngày đầu tuần
    val daysInMonth = remember(displayYear, displayMonth) {
        val cal = Calendar.getInstance().apply { set(displayYear, displayMonth, 1) }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    // Thứ mấy của ngày 1 (0=CN, 1=T2, ... 6=T7) → chuyển sang cột (T2=0, ..., CN=6)
    val firstDayOfWeek = remember(displayYear, displayMonth) {
        val cal = Calendar.getInstance().apply { set(displayYear, displayMonth, 1) }
        val dow = cal.get(Calendar.DAY_OF_WEEK) // 1=CN..7=T7
        if (dow == Calendar.SUNDAY) 6 else dow - 2 // CN=6, T2=0
    }

    fun onDayClick(day: Int) {
        val clicked = PickedDate(displayYear, displayMonth, day)
        when {
            // Chưa có ngày nào → đặt start
            startDate == null -> {
                startDate = clicked
                endDate = null
            }
            // Đã có start, chưa có end → đặt end (đảm bảo start <= end)
            endDate == null -> {
                if (clicked < startDate!!) {
                    endDate = startDate
                    startDate = clicked
                } else if (clicked == startDate) {
                    // Bấm lại ngày start → chỉ giữ ngày đó (sẽ filter 1 ngày)
                    endDate = null
                } else {
                    endDate = clicked
                }
            }
            // Đã có cả hai → reset, đặt start mới
            else -> {
                startDate = clicked
                endDate = null
            }
        }
    }

    fun isStart(day: Int) = startDate?.let {
        it.year == displayYear && it.month == displayMonth && it.day == day
    } == true

    fun isEnd(day: Int) = endDate?.let {
        it.year == displayYear && it.month == displayMonth && it.day == day
    } == true

    fun isInRange(day: Int): Boolean {
        val s = startDate ?: return false
        val e = endDate ?: return false
        val d = PickedDate(displayYear, displayMonth, day)
        return d > s && d < e
    }

    // Xác định loại hình dạng highlight cho từng ngày trong range
    // "start" → nửa phải xanh nhạt + vòng tròn xanh đậm
    // "end"   → nửa trái xanh nhạt + vòng tròn xanh đậm
    // "mid"   → full nền xanh nhạt
    // "single"→ vòng tròn xanh đậm (không có range)
    // null    → không highlight

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(0.dp)) {

                // ── Header ──────────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = primaryColor,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column {
                        Text(
                            text = "Chọn khoảng ngày",
                            color = onPrimary.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = headerLabel,
                            color = onPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Điều hướng tháng ────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        if (displayMonth == 0) {
                            displayMonth = 11; displayYear--
                        } else displayMonth--
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Tháng trước",
                            tint = primaryColor
                        )
                    }

                    Text(
                        text = "${monthName[displayMonth]} $displayYear",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = onSurface
                    )

                    IconButton(onClick = {
                        if (displayMonth == 11) {
                            displayMonth = 0; displayYear++
                        } else displayMonth++
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Tháng sau",
                            tint = primaryColor
                        )
                    }
                }

                // ── Tiêu đề thứ ─────────────────────────────────────────────────
                val dayHeaders = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    dayHeaders.forEach { header ->
                        Text(
                            text = header,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (header == "CN") MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            else onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ── Lưới ngày ────────────────────────────────────────────────────
                val totalCells = firstDayOfWeek + daysInMonth
                val rows = (totalCells + 6) / 7

                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0..6) {
                                val cellIndex = row * 7 + col
                                val day = cellIndex - firstDayOfWeek + 1

                                if (day < 1 || day > daysInMonth) {
                                    // Ô trống
                                    Box(modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp))
                                } else {
                                    val isStartDay = isStart(day)
                                    val isEndDay = isEnd(day)
                                    val inRange = isInRange(day)
                                    val isSelected = isStartDay || isEndDay
                                    val hasRange = endDate != null

                                    // Xác định nền range (nửa-box)
                                    val isSingleStart = isStartDay && hasRange  // start của range
                                    val isSingleEnd = isEndDay && hasRange       // end của range
                                    val isSingleOnly = isStartDay && !hasRange  // chỉ chọn 1 ngày

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Vùng nền range (xanh nhạt)
                                        if (inRange) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(primaryLight)
                                            )
                                        } else if (isSingleStart) {
                                            // Nửa phải xanh nhạt
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(0.5f)
                                                    .align(Alignment.CenterEnd)
                                                    .background(primaryLight)
                                            )
                                        } else if (isSingleEnd) {
                                            // Nửa trái xanh nhạt
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(0.5f)
                                                    .align(Alignment.CenterStart)
                                                    .background(primaryLight)
                                            )
                                        }

                                        // Vòng tròn ngày
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isSelected -> primaryColor
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) { onDayClick(day) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Kiểm tra xem ngày này có phải Chủ nhật không
                                            val isSunday = (firstDayOfWeek + day - 1) % 7 == 6
                                            Text(
                                                text = "$day",
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> onPrimary
                                                    inRange -> primaryColor
                                                    isSunday -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                                    else -> onSurface
                                                },
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Chú thích ────────────────────────────────────────────────────
                if (startDate != null) {
                    Text(
                        text = when {
                            endDate != null -> "✓ Đã chọn: ${startDate!!.format()} → ${endDate!!.format()}"
                            else -> "Bấm tiếp để chọn ngày kết thúc"
                        },
                        fontSize = 12.sp,
                        color = if (endDate != null) primaryColor else onSurface.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                HorizontalDivider(color = onSurface.copy(alpha = 0.08f))

                // ── Nút hành động ───────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nút Hủy
                    TextButton(onClick = onDismiss) {
                        Text(
                            "Hủy",
                            color = onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Nút Chọn
                    Button(
                        onClick = {
                            val s = startDate
                            if (s != null) {
                                onConfirm(s, endDate)
                            }
                        },
                        enabled = startDate != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            disabledContainerColor = primaryColor.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            "Chọn",
                            color = onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
