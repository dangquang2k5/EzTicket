package huce.fit.myezticket.ui

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import huce.fit.myezticket.ui.components.HomeBottomNavigation
import huce.fit.myezticket.ui.screens.HomeScreen
import huce.fit.myezticket.ui.screens.EventDetailScreen
import huce.fit.myezticket.ui.screens.FavoritesScreen
import huce.fit.myezticket.ui.screens.NotificationScreen
import huce.fit.myezticket.ui.screens.SearchScreen
import huce.fit.myezticket.ui.screens.TicketSelectionScreen
import huce.fit.myezticket.ui.screens.QuestionnaireScreen
import huce.fit.myezticket.ui.screens.MyTicketsScreen
import huce.fit.myezticket.ui.screens.PaymentMethodScreen
import huce.fit.myezticket.ui.screens.PaymentSuccessScreen
import huce.fit.myezticket.ui.screens.parseSelectedTicketsArg
import huce.fit.myezticket.ui.screens.LoginScreen
import huce.fit.myezticket.ui.screens.RegisterScreen
import huce.fit.myezticket.ui.screens.ProfileScreen
import huce.fit.myezticket.ui.screens.EditProfileScreen
import huce.fit.myezticket.ui.screens.NotificationSettingsScreen
import huce.fit.myezticket.ui.screens.ChangePasswordScreen
import huce.fit.myezticket.ui.viewmodel.EventViewModel
import huce.fit.myezticket.ui.viewmodel.FavoriteViewModel
import huce.fit.myezticket.ui.viewmodel.NotificationViewModel
import huce.fit.myezticket.ui.viewmodel.TicketViewModel

@Composable
fun SetupNavGraph(
    navController: NavHostController
) {

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
                    onProfileClick = {
                        if (currentRoute != "profile_screen") {
                            navController.navigate("profile_screen") {
                                popUpTo("home_screen") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login_screen",
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
            enterTransition = {
                val mainTabs = listOf("home_screen", "my_tickets_screen")
                val isTabTransition = initialState.destination.route in mainTabs && targetState.destination.route in mainTabs
                if (isTabTransition) {
                    fadeIn(animationSpec = tween(250))
                } else {
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { it }
                    ) + fadeIn(tween(300))
                }
            },
            exitTransition = {
                val mainTabs = listOf("home_screen", "my_tickets_screen")
                val isTabTransition = initialState.destination.route in mainTabs && targetState.destination.route in mainTabs
                if (isTabTransition) {
                    fadeOut(tween(250))
                } else {
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { -it / 3 }
                    ) + fadeOut(tween(150))
                }
            },
            popEnterTransition = {
                val mainTabs = listOf("home_screen", "my_tickets_screen")
                val isTabTransition = initialState.destination.route in mainTabs && targetState.destination.route in mainTabs
                if (isTabTransition) {
                    fadeIn(tween(250))
                } else {
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { -it / 3 }
                    ) + fadeIn(tween(300))
                }
            },
            popExitTransition = {
                val mainTabs = listOf("home_screen", "my_tickets_screen")
                val isTabTransition = initialState.destination.route in mainTabs && targetState.destination.route in mainTabs
                if (isTabTransition) {
                    fadeOut(tween(250))
                } else {
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { it }
                    ) + fadeOut(tween(150))
                }
            }
        ) {
            // 0. Màn hình Đăng nhập
            composable(route = "login_screen") {
                LoginScreen(
                    onNavigateToHome = { navController.navigate("home_screen") { popUpTo("login_screen") { inclusive = true } } },
                    onNavigateToRegister = { navController.navigate("register_screen") },
                    onNavigateToForgotPassword = { /* TODO */ }
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
                    onMyTicketsClick = { navController.navigate("my_tickets_screen") },
                    onFavoriteClick = { navController.navigate("favorites_screen") },
                    onNotificationClick = { navController.navigate("notifications_screen") }
                )
            }
            // Profile screen
            composable(route = "profile_screen") {
                ProfileScreen(
                    onEditProfileClick = { navController.navigate("edit_profile_screen") },
                    onNotificationSettingsClick = { navController.navigate("notification_settings_screen") },
                    onHelpCenterClick = { /* TODO */ },
                    onDeleteAccountClick = { /* TODO */ },
                    onLogoutClick = { /* TODO */ },
                    onBackClick = { navController.popBackStack() },
                    onChangePasswordClick = { navController.navigate("change_password_screen") }
                )
            }
            // Change password screen
            composable(route = "change_password_screen") {
                ChangePasswordScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            // Edit profile screen
            composable(route = "edit_profile_screen") {
                EditProfileScreen(
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
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
                val favoriteViewModel: FavoriteViewModel = hiltViewModel()

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
                val event = events.find { it.id == eventId }
                event?.let {
                    TicketSelectionScreen(event = it, scheduleIndex = scheduleIndex, onBackClick = { navController.popBackStack() }, onContinueClick = { selectedTickets ->
                        val ticketsStr = Uri.encode(selectedTickets.entries.joinToString(";") { "${it.key}:${it.value}" })
                        navController.navigate("questionnaire_screen/${it.id}/$scheduleIndex/$ticketsStr")
                    })
                }
            }
            // Questionnaire
            composable(route = "questionnaire_screen/{eventId}/{scheduleIndex}/{tickets}", arguments = listOf(navArgument("eventId") { type = NavType.StringType }, navArgument("scheduleIndex") { type = NavType.IntType }, navArgument("tickets") { type = NavType.StringType })) { backStackEntry ->
                val eventViewModel: EventViewModel = hiltViewModel()

                val eventId = backStackEntry.arguments?.getString("eventId")
                val scheduleIndex = backStackEntry.arguments?.getInt("scheduleIndex") ?: 0
                val ticketsStr = backStackEntry.arguments?.getString("tickets")?.let(Uri::decode)
                val events by eventViewModel.events.collectAsState()
                val event = events.find { it.id == eventId }
                event?.let {
                    QuestionnaireScreen(event = it, scheduleIndex = scheduleIndex, selectedTicketsString = ticketsStr, onBackClick = { navController.popBackStack() }, onConfirmClick = { phoneNumber ->
                        navController.navigate("payment_method_screen/${it.id}/$scheduleIndex/${Uri.encode(ticketsStr.orEmpty())}/${Uri.encode(phoneNumber)}")
                    })
                }
            }
            // Payment method
            composable(route = "payment_method_screen/{eventId}/{scheduleIndex}/{tickets}/{phoneNumber}", arguments = listOf(navArgument("eventId") { type = NavType.StringType }, navArgument("scheduleIndex") { type = NavType.IntType }, navArgument("tickets") { type = NavType.StringType }, navArgument("phoneNumber") { type = NavType.StringType })) { backStackEntry ->
                val eventViewModel: EventViewModel = hiltViewModel()
                val ticketViewModel: TicketViewModel = hiltViewModel()

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
                    PaymentMethodScreen(event = it, scheduleIndex = scheduleIndex, selectedTicketsString = ticketsStr, phoneNumber = phoneNumber, isSavingPayment = isSavingPayment, paymentError = paymentError, onBackClick = { navController.popBackStack() }, onPaymentComplete = { paymentMethod, orderCode ->
                        ticketViewModel.savePurchasedTickets(event = it, scheduleIndex = scheduleIndex, selectedTickets = selectedTickets, phoneNumber = phoneNumber, orderCode = orderCode, paymentMethod = paymentMethod) {
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
                PaymentSuccessScreen(orderCode = orderCode, onViewTicketsClick = { navController.navigate("my_tickets_screen") { popUpTo("home_screen"); launchSingleTop = true } }, onHomeClick = { navController.navigate("home_screen") { popUpTo("home_screen") { inclusive = true }; launchSingleTop = true } })
            }
            // My tickets
            composable(route = "my_tickets_screen") {
                val eventViewModel: EventViewModel = hiltViewModel()
                val ticketViewModel: TicketViewModel = hiltViewModel()

                val events by eventViewModel.events.collectAsState()
                val purchasedTickets by ticketViewModel.purchasedTickets.collectAsState()
                val isLoadingTickets by ticketViewModel.isLoadingTickets.collectAsState()
                MyTicketsScreen(allEvents = events, purchasedTickets = purchasedTickets, isTicketsLoading = isLoadingTickets, onEventClick = { id -> navController.navigate("detail_screen/$id") }, onHomeClick = { navController.navigate("home_screen") { popUpTo("home_screen"); launchSingleTop = true } })
            }
        }
    }
}
