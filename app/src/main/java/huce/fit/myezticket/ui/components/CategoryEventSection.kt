package huce.fit.myezticket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import huce.fit.myezticket.data.model.Event

@Composable
fun CategoryEventSection(
    title: String, // ĐÃ SỬA: Biến tên tiêu đề thành tham số truyền vào
    events: List<Event>,
    onEventClick: (String) -> Unit
) {
    // Nếu danh mục này không có sự kiện nào thì ẩn luôn khu vực này đi cho đẹp
    if (events.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        // Tiêu đề danh mục động
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 12.dp)
        )

        // Danh sách cuộn ngang
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