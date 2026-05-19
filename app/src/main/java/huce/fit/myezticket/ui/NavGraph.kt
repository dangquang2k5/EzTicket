package huce.fit.myezticket.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import huce.fit.myezticket.ui.screens.HomeScreen
import huce.fit.myezticket.ui.screens.EventDetailScreen
import huce.fit.myezticket.ui.screens.SearchScreen
import huce.fit.myezticket.ui.viewmodel.EventViewModel

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    eventViewModel: EventViewModel
) {
    // NavHost định nghĩa các "điểm đến" trong app
    NavHost(
        navController = navController,
        startDestination = "home_screen" // Màn hình bắt đầu
    ) {
        // 1. Màn hình Trang chủ
        composable(route = "home_screen") {
            HomeScreen(
                eventViewModel = eventViewModel,
                onEventClick = { eventId ->
                    // Khi click vào vé, nhảy sang màn hình detail kèm theo ID
                    navController.navigate("detail_screen/$eventId")
                },

                onSearchClick = {
                    navController.navigate("search_screen")
                }
            )
        }

        // 2. Màn hình Tìm kiếm riêng biệt (MỚI THÊM)
        composable(route = "search_screen") {
            SearchScreen(
                eventViewModel = eventViewModel,
                onBackClick = { navController.popBackStack() },
                onEventClick = { eventId -> navController.navigate("detail_screen/$eventId") }
            )
        }

        // 2. Màn hình Chi tiết (nhận tham số là eventId)
        composable(
            route = "detail_screen/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val events by eventViewModel.events.collectAsState()

            // Tìm sự kiện tương ứng trong danh sách để hiển thị
            val event = events.find { it.id == eventId }

            event?.let {
                EventDetailScreen(
                    event = it,
                    allEvents = events, // THÊM ĐÚNG 1 DÒNG Này để lấy toàn bộ sự kiện sang trang eventdetailscreen
                    onBackClick = { navController.popBackStack() } // Quay lại trang trước
                )
            }
        }
    }
}