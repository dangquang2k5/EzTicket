package huce.fit.myezticket.utils

import java.text.NumberFormat
import java.util.Locale

// Hàm mở rộng giúp mọi biến kiểu Long đều có thể gọi .formatVND()
fun Long.formatVND(): String {
    // Sử dụng Locale của Việt Nam để tự động dùng dấu chấm phân cách hàng nghìn
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return formatter.format(this)
}