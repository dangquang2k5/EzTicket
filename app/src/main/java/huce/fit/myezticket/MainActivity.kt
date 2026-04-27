package huce.fit.myezticket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

// Các thư viện Android
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController

// Các file trong dự án của bạn
import huce.fit.myezticket.ui.SetupNavGraph
import huce.fit.myezticket.ui.theme.MyEzTicketTheme
import huce.fit.myezticket.ui.viewmodel.EventViewModel // THIẾU DÒNG NÀY TRONG CODE CỦA BẠN

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bật tính năng tràn viền để app trông hiện đại hơn
        enableEdgeToEdge()

        setContent {
            MyEzTicketTheme {
                // 1. CHÍNH LÀ 2 DÒNG NÀY: Khởi tạo biến trước khi gọi
                val navController = rememberNavController()
                val eventViewModel: EventViewModel = viewModel()

                // 2. Surface làm nền cho toàn bộ ứng dụng
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 3. Bây giờ mới truyền 2 biến đã khởi tạo vào NavGraph
                    SetupNavGraph(
                        navController = navController,
                        eventViewModel = eventViewModel
                    )
                }
            }
        }
    }
}