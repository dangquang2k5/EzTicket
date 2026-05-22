package huce.fit.myezticket.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import huce.fit.myezticket.data.model.Event
import huce.fit.myezticket.utils.formatVND

@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier.width(280.dp).padding(8.dp),
<<<<<<< HEAD
    onEventClick: (String) -> Unit
=======
    onEventClick: (String) -> Unit // Khai báo để nhận sự kiện click
) {
<<<<<<< HEAD
=======
        // Đã đổi sang màu của Theme (surface) thay vì Color.White
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
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
<<<<<<< HEAD
                // Tên sự kiện
=======
                // Tên sự kiện (Đổi màu chữ tự động theo Theme)
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
                Text(
                    text = event.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

<<<<<<< HEAD
                // Giá: hiển thị "Từ X.XXXđ"
                Text(
                    text = if (event.minPrice > 0) "Từ ${event.minPrice.formatVND()}đ" else "Miễn phí",
=======
                // ĐÃ SỬA: Gọi hàm .formatVND()
                Text(
                    text = "${event.minPrice.formatVND()}đ",
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

<<<<<<< HEAD
                Spacer(modifier = Modifier.height(2.dp))

                // Ngày tháng (thay vì địa chỉ)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.displayDate,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
=======
                // Địa điểm (Đổi sang màu phụ/secondary của Theme thay vì Color.Gray cứng)
                Text(
                    text = event.location,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
            }
        }
    }
}
<<<<<<< HEAD
=======

>>>>>>> 7b78929673373265cf8d4740a313e279a9d30c90
