package huce.fit.myezticket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import huce.fit.myezticket.screens.HomeScreen // Import màn hình chính từ package screens
import huce.fit.myezticket.ui.theme.MyEzTicketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bật tính năng tràn viền để app trông hiện đại hơn (chuẩn năm 2026)
        enableEdgeToEdge()

        setContent {
            MyEzTicketTheme {
                // Surface làm nền cho toàn bộ ứng dụng
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Bạn chỉ cần gọi HomeScreen ở đây.
                    // Toàn bộ việc lấy dữ liệu, hiện Banner, Header... HomeScreen đã lo hết rồi.
                    HomeScreen()
                }
            }
        }
    }
}