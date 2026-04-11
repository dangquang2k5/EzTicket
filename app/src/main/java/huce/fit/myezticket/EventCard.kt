package huce.fit.myezticket

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // Thư viện Coil bạn vừa Sync ở Bước 0
import androidx.compose.ui.tooling.preview.Preview
@Composable
fun EventCard(event: Event) {
    // Card tạo khung bo góc và đổ bóng cho cái vé
    Card(
        modifier = Modifier
            .width(180.dp)       // Độ rộng của thẻ (phù hợp để cuộn ngang)
            .padding(8.dp),      // Khoảng cách giữa các thẻ
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // 1. Ảnh Poster - Lấy từ link image_url trên Firebase
            AsyncImage(
                model = event.image_url,
                contentDescription = event.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp), // Chiều cao ảnh (tỉ lệ dọc cho đẹp)
                contentScale = ContentScale.Crop // Cắt ảnh cho vừa khung, không bị méo
            )

            // 2. Nội dung chữ bên dưới ảnh
            Column(modifier = Modifier.padding(8.dp)) {
                // Tên sự kiện - Bold và tối đa 1 dòng
                Text(
                    text = event.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Giá vé - Màu đỏ nổi bật
                Text(
                    text = "${event.price} đ",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Địa điểm - Chữ nhỏ hơn một chút
                Text(
                    text = event.location,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}






@Preview(showBackground = true)
@Composable
fun EventCardPreview() {
    // Tạo một dữ liệu giả (Dummy Data) để xem trước
    val dummyEvent = Event(
        name = "Show ca nhạc mẫu",
        price = 500000,
        location = "Hà Nội",
        image_url = "https://cdn-media.sforum.vn/storage/app/media/anh-dep-82.jpg"
    )
    EventCard(event = dummyEvent)
}