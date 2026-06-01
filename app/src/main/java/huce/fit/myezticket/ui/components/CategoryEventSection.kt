package huce.fit.myezticket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import huce.fit.myezticket.domain.model.Event

/**
 * Section hiển thị danh sách sự kiện theo danh mục.
 *
 * @param useGridLayout  Nếu true: hiển thị lưới 2 cột dọc. Nếu false: cuộn ngang.
 * @param maxItems       Số sự kiện tối đa hiển thị ban đầu khi dùng lưới (null = hiện tất cả).
 * @param onSeeAllClick  Callback khi nhấn "Xem tất cả →". Nếu null → không hiển thị nút.
 */
@Composable
fun CategoryEventSection(
    title: String,
    events: List<Event>,
    onEventClick: (String) -> Unit,
    useGridLayout: Boolean = false,
    maxItems: Int? = null,
    onSeeAllClick: (() -> Unit)? = null
) {
    if (events.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Header: Tiêu đề + nút "Xem tất cả →" ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 20.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (onSeeAllClick != null) {
                TextButton(onClick = onSeeAllClick) {
                    Text(
                        text = "Xem tất cả →",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (useGridLayout) {
            // Layout lưới 2 cột
            val displayEvents = if (maxItems != null) events.take(maxItems) else events
            val rows = displayEvents.chunked(2)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        rowItems.forEach { event ->
                            EventCard(
                                event = event,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp),
                                onEventClick = onEventClick
                            )
                        }
                        // Nếu hàng lẻ (chỉ 1 item), thêm Spacer cân bằng
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            // Layout cuộn ngang (mặc định)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        event = event,
                        onEventClick = onEventClick
                    )
                }
            }
        }
    }
}