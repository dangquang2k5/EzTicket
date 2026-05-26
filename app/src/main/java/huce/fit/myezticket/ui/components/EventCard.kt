package huce.fit.myezticket.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.utils.formatVND

@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier.width(280.dp).padding(8.dp),
    onEventClick: (String) -> Unit,
    // Tùy chọn: hiển thị icon ❤️ nếu truyền vào
    isFavorite: Boolean = false,
    onFavoriteClick: ((String) -> Unit)? = null
) {
    Card(
        modifier = modifier.clickable { onEventClick(event.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // 1. Ảnh Poster + nút ❤️ góc trên phải
            Box {
                AsyncImage(
                    model = event.image_url,
                    contentDescription = event.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )

                // Badge trạng thái sự kiện COMING_SOON
                if (event.status == "COMING_SOON") {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF6F00)
                    ) {
                        Text(
                            "Sắp mở bán 🔥",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Icon ❤️ — chỉ hiển thị khi có callback
                if (onFavoriteClick != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(30.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.35f)
                    ) {
                        IconButton(
                            onClick = { onFavoriteClick(event.id) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite
                                              else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Bỏ yêu thích" else "Yêu thích",
                                tint = if (isFavorite) Color(0xFFFF4081) else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. Nội dung chữ bên dưới ảnh
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = event.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (event.minPrice > 0) "Từ ${event.minPrice.formatVND()}đ" else "Miễn phí",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

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
            }
        }
    }
}
