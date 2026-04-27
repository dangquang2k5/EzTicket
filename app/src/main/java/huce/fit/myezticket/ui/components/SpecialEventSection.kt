package huce.fit.myezticket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import huce.fit.myezticket.R // Đảm bảo import R của project để dùng đa ngôn ngữ
import huce.fit.myezticket.data.model.Event

@Composable
fun SpecialEventSection(
    events: List<Event>,
    onEventClick: (String) -> Unit // Nhận hàm xử lý click từ HomeScreen truyền xuống
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Tiêu đề mục (Đã cập nhật đa ngôn ngữ và màu sắc Theme)
        Text(
            text = stringResource(id = R.string.special_events),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground, // Tự đổi màu chữ khi bật Dark Mode
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 12.dp)
        )

        // 2. Danh sách cuộn ngang
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(events) { event ->
                // Gọi EventCard và truyền tiếp hành động click vào trong
                EventCard(
                    event = event,
                    onEventClick = onEventClick
                )
            }
        }
    }
}