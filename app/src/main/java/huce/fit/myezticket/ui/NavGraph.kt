package huce.fit.myezticket.ui

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.lifecycle.viewmodel.compose.viewModel
import huce.fit.myezticket.ui.components.HomeBottomNavigation
import huce.fit.myezticket.ui.screens.HomeScreen
import huce.fit.myezticket.ui.screens.EventDetailScreen
import huce.fit.myezticket.ui.screens.SearchScreen
import huce.fit.myezticket.ui.screens.TicketSelectionScreen
import huce.fit.myezticket.ui.screens.QuestionnaireScreen
import huce.fit.myezticket.ui.screens.MyTicketsScreen
import huce.fit.myezticket.ui.screens.PaymentMethodScreen
import huce.fit.myezticket.ui.screens.parseSelectedTicketsArg
import huce.fit.myezticket.ui.screens.LoginScreen
import huce.fit.myezticket.ui.viewmodel.EventViewModel
import huce.fit.myezticket.ui.viewmodel.TicketViewModel

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    eventViewModel: EventViewModel
) {
    val ticketViewModel: TicketViewModel = viewModel()

    // Theo dõi màn hình hiện tại để hiển thị Bottom Navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("home_screen", "my_tickets_screen")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                HomeBottomNavigation(
                    selectedIndex = when (currentRoute) {
                        "home_screen" -> 0
                        "my_tickets_screen" -> 1
                        else -> 0
                    },
                    onHomeClick = {
                        if (currentRoute != "home_screen") {
                            navController.navigate("home_screen") {
                                popUpTo("home_screen") { inclusive = true }
                            }
                        }
                    },
                    onMyTicketsClick = {
                        if (currentRoute != "my_tickets_screen") {
                            navController.navigate("my_tickets_screen") {
                                popUpTo("home_screen") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onProfileClick = { /* Sẽ làm sau */ }
                )
            }
        }
    ) { paddingValues ->
        // NavHost định nghĩa các "điểm đến" trong app
        NavHost(
            navController = navController,
            startDestination = "login_screen", // Đã chuyển màn bắt đầu thành Login
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(150))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(150))
            }
        ) {
        // 0. Màn hình Đăng nhập
        composable(route = "login_screen") {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate("home_screen") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register_screen")
                },
                onNavigateToForgotPassword = {
                    // navController.navigate("forgot_password_screen")
                }
            )
        }
        
        // 0.1 Màn hình Đăng ký
        composable(route = "register_screen") {
            huce.fit.myezticket.ui.screens.RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack("login_screen", inclusive = false)
                }
            )
        }


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
                },
                onSeeAllClick = { category ->
                    // Encode category để tránh lỗi nếu có ký tự đặc biệt (& / space…)
                    val encoded = java.net.URLEncoder.encode(category, "UTF-8")
                    navController.navigate("search_screen/$encoded")
                },
                onMyTicketsClick = {
                    navController.navigate("my_tickets_screen")
                }
            )
        }

        // 2a. Màn hình Tìm kiếm không có filter
        composable(route = "search_screen") {
            LaunchedEffect(Unit) {
                eventViewModel.resetSearch()
            }
            SearchScreen(
                eventViewModel = eventViewModel,
                onBackClick = { navController.popBackStack() },
                onEventClick = { eventId -> navController.navigate("detail_screen/$eventId") }
            )
        }

        // 2b. Màn hình Tìm kiếm với filter danh mục từ HomeScreen "Xem tất cả"
        composable(
            route = "search_screen/{category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("category") ?: "Tất+cả"
            val category = java.net.URLDecoder.decode(encoded, "UTF-8")
            LaunchedEffect(category) {
                eventViewModel.setInitialCategory(category)
            }
            SearchScreen(
                eventViewModel = eventViewModel,
                onBackClick = { navController.popBackStack() },
                onEventClick = { eventId -> navController.navigate("detail_screen/$eventId") }
            )
        }

        // 3. Màn hình Chi tiết (nhận tham số là eventId)
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
                    allEvents = events,
                    onBackClick = { navController.popBackStack() },
                    onBuyTicketClick = { scheduleIndex ->
                        navController.navigate("ticket_selection_screen/${it.id}/$scheduleIndex")
                    },
                    onEventClick = { eventId ->
                        navController.navigate("detail_screen/$eventId")
                    }
                )
            }
        }

        // 4. Màn hình Chọn số lượng vé
        composable(
            route = "ticket_selection_screen/{eventId}/{scheduleIndex}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("scheduleIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val scheduleIndex = backStackEntry.arguments?.getInt("scheduleIndex") ?: 0
            val events by eventViewModel.events.collectAsState()
            val event = events.find { it.id == eventId }
            event?.let {
                TicketSelectionScreen(
                    event = it,
                    scheduleIndex = scheduleIndex,
                    onBackClick = { navController.popBackStack() },
                    onContinueClick = { selectedTickets ->
                        // Chuyển Map thành chuỗi để truyền qua URL: "Loại 1:2;Loại 2:1"
                        val ticketsStr = Uri.encode(selectedTickets.entries.joinToString(";") { "${it.key}:${it.value}" })
                        navController.navigate("questionnaire_screen/${it.id}/$scheduleIndex/$ticketsStr")
                    }
                )
            }
        }

        // 5. Màn hình Bảng câu hỏi (nhận tham số tickets)
        composable(
            route = "questionnaire_screen/{eventId}/{scheduleIndex}/{tickets}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("scheduleIndex") { type = NavType.IntType },
                navArgument("tickets") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val scheduleIndex = backStackEntry.arguments?.getInt("scheduleIndex") ?: 0
            val ticketsStr = backStackEntry.arguments?.getString("tickets")?.let(Uri::decode)
            val events by eventViewModel.events.collectAsState()
            val event = events.find { it.id == eventId }
            event?.let {
                QuestionnaireScreen(
                    event = it,
                    scheduleIndex = scheduleIndex,
                    selectedTicketsString = ticketsStr,
                    onBackClick = { navController.popBackStack() },
                    onConfirmClick = { phoneNumber ->
                        navController.navigate(
                            "payment_method_screen/${it.id}/$scheduleIndex/${Uri.encode(ticketsStr.orEmpty())}/${Uri.encode(phoneNumber)}"
                        )
                    }
                )
            }
        }

        // 6. Màn hình Phương thức thanh toán
        composable(
            route = "payment_method_screen/{eventId}/{scheduleIndex}/{tickets}/{phoneNumber}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("scheduleIndex") { type = NavType.IntType },
                navArgument("tickets") { type = NavType.StringType },
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val scheduleIndex = backStackEntry.arguments?.getInt("scheduleIndex") ?: 0
            val ticketsStr = backStackEntry.arguments?.getString("tickets")?.let(Uri::decode)
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber")?.let(Uri::decode).orEmpty()
            val events by eventViewModel.events.collectAsState()
            val isSavingPayment by ticketViewModel.isSavingPayment.collectAsState()
            val paymentError by ticketViewModel.paymentError.collectAsState()
            val event = events.find { it.id == eventId }
            event?.let {
                val selectedTickets = parseSelectedTicketsArg(ticketsStr)
                PaymentMethodScreen(
                    event = it,
                    scheduleIndex = scheduleIndex,
                    selectedTicketsString = ticketsStr,
                    phoneNumber = phoneNumber,
                    isSavingPayment = isSavingPayment,
                    paymentError = paymentError,
                    onBackClick = { navController.popBackStack() },
                    onPaymentComplete = { paymentMethod, orderCode ->
                        ticketViewModel.savePurchasedTickets(
                            event = it,
                            scheduleIndex = scheduleIndex,
                            selectedTickets = selectedTickets,
                            phoneNumber = phoneNumber,
                            orderCode = orderCode,
                            paymentMethod = paymentMethod
                        ) {
                            navController.navigate("my_tickets_screen") {
                                popUpTo("home_screen")
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }

        // 7. Màn hình Vé của tôi
        composable(route = "my_tickets_screen") {
            val events by eventViewModel.events.collectAsState()
            val purchasedTickets by ticketViewModel.purchasedTickets.collectAsState()
            val isLoadingTickets by ticketViewModel.isLoadingTickets.collectAsState()
            MyTicketsScreen(
                allEvents = events,
                purchasedTickets = purchasedTickets,
                isTicketsLoading = isLoadingTickets,
                onEventClick = { eventId ->
                    navController.navigate("detail_screen/$eventId")
                },
                onHomeClick = {
                    navController.navigate("home_screen") {
                        popUpTo("home_screen")
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
}
