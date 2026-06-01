package huce.fit.myezticket.ui

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.hilt.navigation.compose.hiltViewModel
import huce.fit.myezticket.ui.components.HomeBottomNavigation
import huce.fit.myezticket.ui.screens.*
import huce.fit.myezticket.ui.viewmodel.*

private const val NAVIGATION_ANIMATION_DURATION_MILLIS = 300
private val MAIN_TAB_ROUTES = setOf("home_screen", "my_tickets_screen", "profile_screen")

private fun NavHostController.navigateToMainTab(route: String) {
    navigate(route) {
        popUpTo("home_screen") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateFromPaymentSuccessToMainTab(route: String) {
    navigate(route) {
        popUpTo("home_screen") { saveState = false }
        launchSingleTop = true
    }
}

@Composable
fun SetupNavGraph(
    navController: NavHostController
) {
    val ticketViewModel: TicketViewModel = hiltViewModel()

    // Theo dõi màn hình hiện tại để hiển thị Bottom Navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("home_screen", "my_tickets_screen", "profile_screen")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Tràn viền hệ thống, để màn hình con tự xử lý insets
        bottomBar = {
            if (showBottomBar) {
                HomeBottomNavigation(
                    selectedIndex = when (currentRoute) {
                        "home_screen" -> 0
                        "my_tickets_screen" -> 1
                        "profile_screen" -> 2
                        else -> 0
                    },
                    onHomeClick = {
                        if (currentRoute != "home_screen") {
                            navController.navigateToMainTab("home_screen")
                        }
                    },
                    onMyTicketsClick = {
                        if (currentRoute != "my_tickets_screen") {
                            navController.navigateToMainTab("my_tickets_screen")
                        }
                    },
                    onProfileClick = {
                        if (currentRoute != "profile_screen") {
                            navController.navigateToMainTab("profile_screen")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login_screen",
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (showBottomBar) 80.dp else 0.dp // Chỉ chừa khoảng trống cho Bottom Bar khi hiển thị
                ),
            enterTransition = {
                val targetRoute = targetState.destination.route
                val isTabTransition = initialState.destination.route in MAIN_TAB_ROUTES && targetRoute in MAIN_TAB_ROUTES
                
                when {
                    isTabTransition -> {
                        fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing))
                    }
                    targetRoute == "search_screen" || targetRoute?.startsWith("search_screen/") == true || targetRoute == "notifications_screen" -> {
                        // Hiệu ứng trượt từ dưới lên (Slide Up) cao cấp cho tìm kiếm và thông báo
                        slideInVertically(
                            initialOffsetY = { it / 6 },
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(350))
                    }
                    else -> {
                        // Trượt ngang từ phải sang mượt mà
                        slideInHorizontally(
                            initialOffsetX = { it / 3 },
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(350))
                    }
                }
            },
            exitTransition = {
                val targetRoute = targetState.destination.route
                val isTabTransition = initialState.destination.route in MAIN_TAB_ROUTES && targetRoute in MAIN_TAB_ROUTES
                
                when {
                    isTabTransition -> {
                        fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))
                    }
                    else -> {
                        // Trượt mờ nhẹ về phía sau khi mở màn hình mới chồng lên
                        slideOutHorizontally(
                            targetOffsetX = { -it / 6 },
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(350))
                    }
                }
            },
            popEnterTransition = {
                val targetRoute = targetState.destination.route
                val isTabTransition = initialState.destination.route in MAIN_TAB_ROUTES && targetRoute in MAIN_TAB_ROUTES
                
                when {
                    isTabTransition -> {
                        fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing))
                    }
                    else -> {
                        // Khi quay lại, màn hình cũ trượt mượt từ phía sau ra trước
                        slideInHorizontally(
                            initialOffsetX = { -it / 6 },
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(350))
                    }
                }
            },
            popExitTransition = {
                val initialRoute = initialState.destination.route
                val isTabTransition = initialState.destination.route in MAIN_TAB_ROUTES && targetState.destination.route in MAIN_TAB_ROUTES
                
                when {
                    isTabTransition -> {
                        fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))
                    }
                    initialRoute == "search_screen" || initialRoute?.startsWith("search_screen/") == true || initialRoute == "notifications_screen" -> {
                        // Tìm kiếm/Thông báo trượt dọc xuống dưới khi đóng
                        slideOutVertically(
                            targetOffsetY = { it / 6 },
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(350))
                    }
                    else -> {
                        // Các màn hình khác trượt ngang biến mất sang bên phải
                        slideOutHorizontally(
                            targetOffsetX = { it / 3 },
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(350))
                    }
                }
            }
        ) {
            // 0. Màn hình Đăng nhập
            composable(route = "login_screen") {
                LoginScreen(
                    onNavigateToHome = { navController.navigate("home_screen") { popUpTo("login_screen") { inclusive = true } } },
                    onNavigateToRegister = { navController.navigate("register_screen") },
                    onNavigateToForgotPassword = { navController.navigate("forgot_password_screen") }
                )
            }
            // Quên mật khẩu
            composable(route = "forgot_password_screen") {
                ForgotPasswordScreen(
                    onNavigateToLogin = { navController.popBackStack("login_screen", inclusive = false) }
                )
            }
            // Đăng ký
            composable(route = "register_screen") {
                RegisterScreen(onNavigateToLogin = { navController.popBackStack("login_screen", inclusive = false) })
            }
            // Home
            composable(route = "home_screen") {
                val eventViewModel: EventViewModel = hiltViewModel()
                val favoriteViewModel: FavoriteViewModel = hiltViewModel()
                val notificationViewModel: NotificationViewModel = hiltViewModel()

                HomeScreen(
                    eventViewModel = eventViewModel,
                    favoriteViewModel = favoriteViewModel,
                    notificationViewModel = notificationViewModel,
                    onEventClick = { eventId -> navController.navigate("detail_screen/$eventId") },
                    onSearchClick = { navController.navigate("search_screen") },
                    onSeeAllClick = { category ->
                        val encoded = java.net.URLEncoder.encode(category, "UTF-8")
                        navController.navigate("search_screen/$encoded")
                    },
                    onMyTicketsClick = { navController.navigateToMainTab("my_tickets_screen") },
                    onFavoriteClick = { navController.navigate("favorites_screen") },
                    onNotificationClick = { navController.navigate("notifications_screen") }
                )
            }
            // Profile screen
            composable(route = "profile_screen") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    onEditProfileClick = { navController.navigate("edit_profile_screen") },
                    onNotificationSettingsClick = { navController.navigate("notification_settings_screen") },
                    onPinSetupClick = { navController.navigate("pin_setup_screen") },
                    onLogoutClick = {
                        profileViewModel.logout()
                        navController.navigate("login_screen") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onChangePasswordClick = { navController.navigate("change_password_screen") }
                )
            }
            // Pin setup screen
            composable(route = "pin_setup_screen") {
                PinSetupScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            // Change password screen
            composable(route = "change_password_screen") {
                ChangePasswordScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            // Edit profile screen
            composable(route = "edit_profile_screen") { backStackEntry ->
                val profileEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("profile_screen")
                }
                val profileViewModel: ProfileViewModel = hiltViewModel(profileEntry)
                EditProfileScreen(
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                    viewModel = profileViewModel
                )
            }
            // Notification settings screen
            composable(route = "notification_settings_screen") {
                NotificationSettingsScreen(
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            // Search screens
            composable(route = "search_screen") {
                val eventViewModel: EventViewModel = hiltViewModel()

                LaunchedEffect(Unit) { eventViewModel.resetSearch() }
                SearchScreen(eventViewModel = eventViewModel, onBackClick = { navController.popBackStack() }, onEventClick = { id -> navController.navigate("detail_screen/$id") })
            }
            composable(route = "search_screen/{category}", arguments = listOf(navArgument("category") { type = NavType.StringType })) { backStackEntry ->
                val eventViewModel: EventViewModel = hiltViewModel()

                val encoded = backStackEntry.arguments?.getString("category") ?: "Tất+cả"
                val category = java.net.URLDecoder.decode(encoded, "UTF-8")
                LaunchedEffect(category) { eventViewModel.setInitialCategory(category) }
                SearchScreen(eventViewModel = eventViewModel, onBackClick = { navController.popBackStack() }, onEventClick = { id -> navController.navigate("detail_screen/$id") })
            }
            // Detail screen
            composable(route = "detail_screen/{eventId}", arguments = listOf(navArgument("eventId") { type = NavType.StringType })) { backStackEntry ->
                val eventViewModel: EventViewModel = hiltViewModel()
                val favoriteViewModel: FavoriteViewModel = hiltViewModel()

                val eventId = backStackEntry.arguments?.getString("eventId")
                val events by eventViewModel.events.collectAsState()
                val favoriteIds by favoriteViewModel.favoriteIds.collectAsState()
                val event = events.find { it.id == eventId }
                event?.let {
                    EventDetailScreen(
                        event = it,
                        allEvents = events,
                        onBackClick = { navController.popBackStack() },
                        onBuyTicketClick = { scheduleIndex -> navController.navigate("ticket_selection_screen/${it.id}/$scheduleIndex") },
                        onEventClick = { id -> navController.navigate("detail_screen/$id") },
                        isFavorite = it.id in favoriteIds,
                        onToggleFavorite = { id -> favoriteViewModel.toggleFavorite(id) }
                    )
                }
            }
            // Favorites
            composable(route = "favorites_screen") {
                val eventViewModel: EventViewModel = hiltViewModel()
                val favoriteViewModel: FavoriteViewModel = hiltViewModel()
                val events by eventViewModel.events.collectAsState()

                LaunchedEffect(events) {
                    if (events.isNotEmpty()) {
                        favoriteViewModel.updateAllEvents(events)
                    }
                }

                FavoritesScreen(favoriteViewModel = favoriteViewModel, onBackClick = { navController.popBackStack() }, onEventClick = { id -> navController.navigate("detail_screen/$id") })
            }
            // Notifications list
            composable(route = "notifications_screen") {
                val notificationViewModel: NotificationViewModel = hiltViewModel()

                NotificationScreen(notificationViewModel = notificationViewModel, onBackClick = { navController.popBackStack() }, onNotificationClick = { id -> navController.navigate("detail_screen/$id") })
            }
            // Ticket selection
            composable(route = "ticket_selection_screen/{eventId}/{scheduleIndex}", arguments = listOf(navArgument("eventId") { type = NavType.StringType }, navArgument("scheduleIndex") { type = NavType.IntType })) { backStackEntry ->
                val eventViewModel: EventViewModel = hiltViewModel()

                val eventId = backStackEntry.arguments?.getString("eventId")
                val scheduleIndex = backStackEntry.arguments?.getInt("scheduleIndex") ?: 0
                val events by eventViewModel.events.collectAsState()
                val isCreatingPendingTicket by ticketViewModel.isSavingPayment.collectAsState()
                val event = events.find { it.id == eventId }
                event?.let {
                    TicketSelectionScreen(event = it, scheduleIndex = scheduleIndex, isCreatingPendingTicket = isCreatingPendingTicket, onBackClick = { navController.popBackStack() }, onContinueClick = { selectedTickets ->
                        val ticketsStr = Uri.encode(serializeSelectedTicketsArg(selectedTickets))
                        val orderCode = createOrderCode()
                        val expiresAtMillis = System.currentTimeMillis() + PAYMENT_TIMEOUT_MILLIS
                        ticketViewModel.savePendingTickets(
                            event = it,
                            scheduleIndex = scheduleIndex,
                            selectedTickets = selectedTickets,
                            orderCode = orderCode,
                            expiresAtMillis = expiresAtMillis
                        ) {
                            navController.navigate("questionnaire_screen/${it.id}/$scheduleIndex/$ticketsStr/${Uri.encode(orderCode)}/$expiresAtMillis")
                        }
                    })
                }
            }
            // Questionnaire
            composable(route = "questionnaire_screen/{eventId}/{scheduleIndex}/{tickets}/{orderCode}/{expiresAt}", arguments = listOf(navArgument("eventId") { type = NavType.StringType }, navArgument("scheduleIndex") { type = NavType.IntType }, navArgument("tickets") { type = NavType.StringType }, navArgument("orderCode") { type = NavType.StringType }, navArgument("expiresAt") { type = NavType.LongType })) { backStackEntry ->
                val eventViewModel: EventViewModel = hiltViewModel()

                val eventId = backStackEntry.arguments?.getString("eventId")
                val scheduleIndex = backStackEntry.arguments?.getInt("scheduleIndex") ?: 0
                val ticketsStr = backStackEntry.arguments?.getString("tickets")?.let(Uri::decode)
                val orderCode = backStackEntry.arguments?.getString("orderCode")?.let(Uri::decode).orEmpty()
                val expiresAtMillis = backStackEntry.arguments?.getLong("expiresAt") ?: 0L
                val events by eventViewModel.events.collectAsState()
                val event = events.find { it.id == eventId }
                event?.let {
                    QuestionnaireScreen(
                        event = it,
                        scheduleIndex = scheduleIndex,
                        selectedTicketsString = ticketsStr,
                        orderCode = orderCode,
                        expiresAtMillis = expiresAtMillis,
                        onBackClick = { navController.popBackStack() },
                        onTimeExpired = {
                            ticketViewModel.cancelPendingTickets(orderCode) {
                                navController.popBackStack()
                            }
                        },
                        onConfirmClick = { phoneNumber ->
                            navController.navigate("payment_method_screen/${it.id}/$scheduleIndex/${Uri.encode(ticketsStr.orEmpty())}/${Uri.encode(phoneNumber)}/${Uri.encode(orderCode)}/$expiresAtMillis")
                        }
                    )
                }
            }
            // Payment method
            composable(route = "payment_method_screen/{eventId}/{scheduleIndex}/{tickets}/{phoneNumber}/{orderCode}/{expiresAt}", arguments = listOf(navArgument("eventId") { type = NavType.StringType }, navArgument("scheduleIndex") { type = NavType.IntType }, navArgument("tickets") { type = NavType.StringType }, navArgument("phoneNumber") { type = NavType.StringType }, navArgument("orderCode") { type = NavType.StringType }, navArgument("expiresAt") { type = NavType.LongType })) { backStackEntry ->
                val eventViewModel: EventViewModel = hiltViewModel()

                val eventId = backStackEntry.arguments?.getString("eventId")
                val scheduleIndex = backStackEntry.arguments?.getInt("scheduleIndex") ?: 0
                val ticketsStr = backStackEntry.arguments?.getString("tickets")?.let(Uri::decode)
                val phoneNumber = backStackEntry.arguments?.getString("phoneNumber")?.let(Uri::decode).orEmpty()
                val orderCode = backStackEntry.arguments?.getString("orderCode")?.let(Uri::decode).orEmpty()
                val expiresAtMillis = backStackEntry.arguments?.getLong("expiresAt") ?: 0L
                val events by eventViewModel.events.collectAsState()
                val isSavingPayment by ticketViewModel.isSavingPayment.collectAsState()
                val paymentError by ticketViewModel.paymentError.collectAsState()
                val event = events.find { it.id == eventId }
                event?.let {
                    PaymentMethodScreen(event = it, scheduleIndex = scheduleIndex, selectedTicketsString = ticketsStr, phoneNumber = phoneNumber, orderCode = orderCode, expiresAtMillis = expiresAtMillis, isSavingPayment = isSavingPayment, paymentError = paymentError, onBackClick = { navController.popBackStack() }, onTimeExpired = {
                        ticketViewModel.cancelPendingTickets(orderCode) {}
                    }, onPaymentComplete = { paymentMethod ->
                        ticketViewModel.savePurchasedTickets(event = it, phoneNumber = phoneNumber, orderCode = orderCode, paymentMethod = paymentMethod) {
                            navController.navigate("payment_success_screen/${Uri.encode(orderCode)}") {
                                popUpTo("home_screen")
                                launchSingleTop = true
                            }
                        }
                    })
                }
            }
            // Payment success
            composable(route = "payment_success_screen/{orderCode}", arguments = listOf(navArgument("orderCode") { type = NavType.StringType })) { backStackEntry ->
                val orderCode = backStackEntry.arguments?.getString("orderCode")?.let(Uri::decode).orEmpty()
                PaymentSuccessScreen(orderCode = orderCode, onViewTicketsClick = { navController.navigateFromPaymentSuccessToMainTab("my_tickets_screen") }, onHomeClick = { navController.navigateFromPaymentSuccessToMainTab("home_screen") })
            }
            // My tickets
            composable(route = "my_tickets_screen") {
                val eventViewModel: EventViewModel = hiltViewModel()

                val events by eventViewModel.events.collectAsState()
                val purchasedTickets by ticketViewModel.purchasedTickets.collectAsState()
                val isLoadingTickets by ticketViewModel.isLoadingTickets.collectAsState()
                MyTicketsScreen(allEvents = events, purchasedTickets = purchasedTickets, isTicketsLoading = isLoadingTickets, onEventClick = { id -> navController.navigate("detail_screen/$id") }, onHomeClick = { navController.navigateToMainTab("home_screen") }, onPendingPaymentClick = { ticket ->
                    val ticketsStr = serializeSelectedTicketsArg(
                        aggregateTicketQuantities(
                            purchasedTickets.filter { it.orderCode == ticket.orderCode }
                        )
                    )
                    val expiresAtMillis = ticket.expiresAt?.toDate()?.time ?: 0L
                    navController.navigate("questionnaire_screen/${ticket.eventId}/${ticket.scheduleIndex}/${Uri.encode(ticketsStr)}/${Uri.encode(ticket.orderCode)}/$expiresAtMillis")
                })
            }
        }
    }
}
