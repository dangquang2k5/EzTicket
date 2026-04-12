package huce.fit.myezticket.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import huce.fit.myezticket.Event

@Composable
fun SpecialEventSection(events: List<Event>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Tiêu đề mục (Ví dụ: Sự kiện đặc biệt)
        Text(
            text = "Sự kiện đặc biệt",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 12.dp)
        )

        // 2. Danh sách cuộn ngang
        // LazyRow giúp các EventCard xếp hàng ngang và vuốt cực mượt
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp), // Khoảng hở ở hai đầu danh sách
            horizontalArrangement = Arrangement.spacedBy(4.dp) // Khoảng cách giữa các thẻ vé
        ) {
            items(events) { event ->
                // Gọi cái EventCard mà bạn đã vẽ ở Bước 2
                EventCard(event = event)
            }
        }
    }
}