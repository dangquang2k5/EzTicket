package huce.fit.myezticket.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

// Import ViewModel và Model từ package gốc
import huce.fit.myezticket.EventViewModel
import huce.fit.myezticket.Event

// Import tất cả các linh kiện từ package components
import huce.fit.myezticket.components.*

@Composable
fun HomeScreen(eventViewModel: EventViewModel = viewModel()) {
    // Theo dõi danh sách sự kiện từ ViewModel
    val events by eventViewModel.events.collectAsState()

    Scaffold(
        topBar = { HomeHeader() },           // Lấy từ components
        bottomBar = { HomeBottomNavigation() } // Lấy từ components
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Hiển thị Banner quảng cáo
            BannerSlider()

            // 2. Hiển thị danh sách Sự kiện đặc biệt (lấy dữ liệu thật từ Firebase)
            if (events.isNotEmpty()) {
                SpecialEventSection(events = events)
            }

            // 3. Bạn có thể thêm các mục khác bên dưới nếu muốn
            // Ví dụ: Sự kiện xu hướng
            // SpecialEventSection(events = events.reversed())
        }
    }
}