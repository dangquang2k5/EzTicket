package huce.fit.myezticket.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. Cấu hình bảng màu tối (Dark Mode)
private val DarkColorScheme = darkColorScheme(
    primary = TicketGreen,       // Sử dụng biến màu từ Color.kt
    secondary = TicketGray,
    tertiary = TicketOrange,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    error = TicketRed
)

// 2. Cấu hình bảng màu sáng (Light Mode) - CHUẨN NHẤT
private val LightColorScheme = lightColorScheme(
    primary = TicketGreen,       // Màu xanh chủ đạo của MyEzTicket
    onPrimary = Color.White,     // Màu chữ trên nền xanh (thường là trắng)
    secondary = TicketGray,      // Màu phụ (cho các icon, text phụ)
    tertiary = TicketOrange,    // Đổi Pink40 hoặc các màu cũ thành TicketOrange
    error = TicketRed,           // Dùng cho giá vé, thông báo lỗi
    background = Color(0xFFFFFBFE),
    surface = Color.White,       // Màu nền của các Card (vé)
    onSurface = Color.Black      // Màu chữ trên các Card
)

@Composable
fun MyEzTicketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Bạn có thể tắt dynamicColor (màu theo hình nền) để app luôn giữ đúng màu thương hiệu
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Quyết định dùng bảng màu nào dựa trên chế độ máy
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Kết nối với file Type.kt
        content = content
    )
}