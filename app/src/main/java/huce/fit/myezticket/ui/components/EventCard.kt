package huce.fit.myezticket.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import huce.fit.myezticket.data.model.Event
import huce.fit.myezticket.utils.formatVND

@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier.width(280.dp).padding(8.dp),
    onEventClick: (String) -> Unit // Khai báo để nhận sự kiện click
) {
    Card(
        modifier = modifier.clickable { onEventClick(event.id) },
        shape = RoundedCornerShape(12.dp),
        // Đã đổi sang màu của Theme (surface) thay vì Color.White
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // 1. Ảnh Poster
            AsyncImage(
                model = event.image_url,
                contentDescription = event.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )

            // 2. Nội dung chữ bên dưới ảnh
            Column(modifier = Modifier.padding(8.dp)) {
                // Tên sự kiện (Đổi màu chữ tự động theo Theme)
                Text(
                    text = event.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ĐÃ SỬA: Gọi hàm .formatVND()
                Text(
                    text = "${event.minPrice.formatVND()}đ",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Địa điểm (Đổi sang màu phụ/secondary của Theme thay vì Color.Gray cứng)
                Text(
                    text = event.location,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

