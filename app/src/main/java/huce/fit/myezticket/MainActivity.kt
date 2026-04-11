package huce.fit.myezticket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import huce.fit.myezticket.ui.theme.MyEzTicketTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() // Có thể thêm lại nếu muốn giao diện tràn viền
        setContent {
            MyEzTicketTheme {
                // Surface giúp quản lý màu nền (Background) cho toàn bộ app
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. Khởi tạo ViewModel
                    val eventViewModel: EventViewModel = viewModel()

                    // 2. Lấy dữ liệu (Quan sát trạng thái danh sách sự kiện)
                    val events by eventViewModel.events.collectAsState()

                    // 3. Giao diện chính
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // TODO: Thêm Banner ở đây sau

                        if (events.isEmpty()) {
                            Text(
                                text = "Đang tải dữ liệu hoặc danh sách trống...",
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            // Gọi component danh sách cuộn ngang của bạn
                            SpecialEventSection(events = events)

                            // Bạn có thể thêm các mục khác ở đây
                            // Ví dụ: TrendingEventSection(events = events)
                        }
                    }
                }
            }
        }
    }
}