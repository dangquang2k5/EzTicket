package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

// Import ViewModel
import huce.fit.myezticket.ui.viewmodel.EventViewModel

// Import tất cả các linh kiện
import huce.fit.myezticket.ui.components.BannerSlider
import huce.fit.myezticket.ui.components.HomeBottomNavigation
import huce.fit.myezticket.ui.components.HomeHeader
import huce.fit.myezticket.ui.components.SpecialEventSection

@Composable
fun HomeScreen(
    eventViewModel: EventViewModel = viewModel(),
    // THÊM: Biến này dùng để "hứng" hành động click từ các thẻ vé/banner
    onEventClick: (String) -> Unit = {}
) {
    // 1. Theo dõi danh sách sự kiện chung và danh sách banner từ ViewModel
    val events by eventViewModel.events.collectAsState()
    val bannerEvents by eventViewModel.bannerEvents.collectAsState()

    Scaffold(
        topBar = { HomeHeader() },
        bottomBar = { HomeBottomNavigation() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 2. Hiển thị Banner quảng cáo (Truyền danh sách banner và bắt sự kiện click)
            BannerSlider(
                bannerEvents = bannerEvents,
                onBannerClick = { eventId ->
                    onEventClick(eventId) // Chuyền ID lên cho NavGraph chuyển màn hình
                }
            )

            // 3. Hiển thị danh sách Sự kiện đặc biệt
            if (events.isNotEmpty()) {
                SpecialEventSection(
                    events = events,
                    onEventClick = { eventId ->
                        onEventClick(eventId) // Chuyền ID lên cho NavGraph
                    }
                )
            }
        }
    }
}