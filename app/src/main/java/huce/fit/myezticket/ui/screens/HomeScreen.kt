package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import huce.fit.myezticket.data.FakeDataGenerator

// Import ViewModel
import huce.fit.myezticket.ui.viewmodel.EventViewModel

// Import tất cả các linh kiện
import huce.fit.myezticket.ui.components.BannerSlider
import huce.fit.myezticket.ui.components.CategoryEventSection
import huce.fit.myezticket.ui.components.HomeBottomNavigation
import huce.fit.myezticket.ui.components.HomeHeader
import huce.fit.myezticket.ui.components.SpecialEventSection

@Composable
fun HomeScreen(
    eventViewModel: EventViewModel = viewModel(),
    // THÊM: Biến này dùng để "hứng" hành động click từ các thẻ vé/banner
    onEventClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {} // NHẬN SỰ KIỆN TỪ NAVGRAPH
) {
    // 1. Theo dõi danh sách sự kiện chung và danh sách banner từ ViewModel
    val events by eventViewModel.events.collectAsState()
    val bannerEvents by eventViewModel.bannerEvents.collectAsState()

    Scaffold(
        topBar = { HomeHeader(onSearchClick = onSearchClick) },
        bottomBar = { HomeBottomNavigation() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Hiển thị Banner quảng cáo
            BannerSlider(
                bannerEvents = bannerEvents,
                onBannerClick = { eventId -> onEventClick(eventId) }
            )

            // ==========================================
            val specialEvents = events.filter { it.isHot } // CHỈ LẤY SỰ KIỆN CÓ CỜ HOT
            val musicEvents = events.filter { it.category == "Âm nhạc" }
            val workshopEvents = events.filter { it.category == "Hội thảo" }
            val sportEvents = events.filter { it.category == "Thể thao" }

            // 1. Khu vực "Sự kiện đặc biệt"
            if (specialEvents.isNotEmpty()) {
                CategoryEventSection(
                    title = "Sự kiện đặc biệt 🔥", // Thêm icon cho nổi bật
                    events = specialEvents,
                    onEventClick = { eventId -> onEventClick(eventId) }
                )
            }

            // 3. Khu vực "Âm nhạc sôi động"
            CategoryEventSection(
                title = "Âm nhạc sôi động",
                events = musicEvents,
                onEventClick = { eventId -> onEventClick(eventId) }
            )

            // 4. Khu vực "Hội thảo & Khóa học"
            CategoryEventSection(
                title = "Hội thảo & Khóa học",
                events = workshopEvents,
                onEventClick = { eventId -> onEventClick(eventId) }
            )

            // 5. Khu vực "Thể thao"
            CategoryEventSection(
                title = "Thể thao",
                events = sportEvents,
                onEventClick = { eventId -> onEventClick(eventId) }
            )

// NÚT TẠO DỮ LIỆU TẠM THỜI (Chỉ dùng khi phát triển)
            Button(
                onClick = {
                    // Gọi hàm từ file FakeDataGenerator mà chúng ta đã viết
                    FakeDataGenerator.seedDataToFirebase()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                // Sử dụng màu Error (Đỏ) để nhắc nhở đây là nút "nguy hiểm", chỉ bấm 1 lần
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "BẤM 1 LẦN DUY NHẤT ĐỂ TẠO DATA",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            // Khoảng trống dưới cùng để không bị che bởi Bottom Navigation
            Spacer(modifier = Modifier.height(20.dp))
        }
// ... (Các code bên dưới giữ nguyên)
    }
}