package huce.fit.myezticket.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================
// HỆ THỐNG MÀU CHO MYEZTICKET
// =========================================

// 1. Màu thương hiệu chủ đạo (Brand Colors)
val TicketGreen = Color(0xFF00C853)     // Màu xanh lá chính (Giống Ticketbox)
val TicketGreenDark = Color(0xFF009624) // Màu xanh đậm hơn (Dùng hiệu ứng khi bấm nút)

// 2. Màu cảnh báo và Nhấn mạnh (Accent & Error)
val TicketRed = Color(0xFFFF1744)       // Màu đỏ nổi bật (Dùng cho Giá vé, Báo lỗi)
val TicketOrange = Color(0xFFFF9100)    // Màu cam (Dùng cho nhãn "Sắp hết vé", "Đang hot")

// 3. Màu văn bản phụ và Viền (Neutrals)
val TicketGray = Color(0xFF757575)      // Xám vừa (Dùng cho chữ địa điểm, thời gian)
val TicketLightGray = Color(0xFFEEEEEE) // Xám siêu nhạt (Dùng làm đường viền, gạch ngang)

// 4. Màu Nền (Backgrounds)
val BackgroundLight = Color(0xFFFFFBFE) // Trắng ngà (Nền của toàn bộ app cho đỡ mỏi mắt)
val SurfaceLight = Color(0xFFFFFFFF)    // Trắng tinh (Nền của các thẻ EventCard)