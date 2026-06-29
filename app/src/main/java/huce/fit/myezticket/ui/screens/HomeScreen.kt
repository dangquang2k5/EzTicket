package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.hilt.navigation.compose.hiltViewModel
import huce.fit.myezticket.ui.viewmodel.EventViewModel
import huce.fit.myezticket.ui.viewmodel.FavoriteViewModel
import huce.fit.myezticket.ui.viewmodel.NotificationViewModel
import huce.fit.myezticket.ui.components.BannerSlider
import huce.fit.myezticket.ui.components.CategoryChipBar
import huce.fit.myezticket.ui.components.CategoryChip
import huce.fit.myezticket.ui.components.CategoryEventSection
import huce.fit.myezticket.ui.components.HomeBottomNavigation
import huce.fit.myezticket.ui.components.HomeHeader
import kotlinx.coroutines.launch

// ─── Các index item trong LazyColumn ──────────────────────────────────────────
// item 0  = Banner
// item 1  = ChipBar
// item 2  = Sự kiện đặc biệt
// item 3  = Nghệ thuật
// item 4  = Âm nhạc
// item 5  = Thể thao
// item 6  = Các sự kiện khác

private const val IDX_ART        = 3
private const val IDX_MUSIC      = 4
private const val IDX_SPORT      = 5
private const val IDX_OTHER      = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    eventViewModel: EventViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onMyTicketsClick: () -> Unit = {},
    onSeeAllClick: (category: String) -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    val events       by eventViewModel.events.collectAsState()
    val bannerEvents by eventViewModel.bannerEvents.collectAsState()
    val unreadCount  by notificationViewModel.unreadCount.collectAsState()
    val favoriteIds  by favoriteViewModel.favoriteIds.collectAsState()
    val isRefreshing by eventViewModel.isRefreshing.collectAsState()

    // Cập nhật events vào FavoriteViewModel để check thông báo
    LaunchedEffect(events) {
        if (events.isNotEmpty()) {
            favoriteViewModel.updateAllEvents(events)
        }
    }

    val listState  = rememberLazyListState()
    val coroutine  = rememberCoroutineScope()

    // ── Lọc sự kiện theo từng danh mục ──────────────────────────────────────
    val specialEvents  = events.filter { it.isHot }
    val artEvents      = events.filter { it.category == "Nghệ thuật" }
    val musicEvents    = events.filter { it.category == "Âm nhạc" }
    val sportEvents    = events.filter { it.category == "Thể thao" }
    val otherEvents    = events.filter { event ->
        event.category != "Nghệ thuật" &&
        event.category != "Âm nhạc" &&
        event.category != "Thể thao"
    }

    // ── Định nghĩa chips (sectionIndex = vị trí item trong LazyColumn) ───────
    val chips = listOf(
        CategoryChip(label = "Nghệ thuật", icon = "🎨", sectionIndex = IDX_ART),
        CategoryChip(label = "Âm nhạc",    icon = "🎵", sectionIndex = IDX_MUSIC),
        CategoryChip(label = "Thể thao",    icon = "⚽", sectionIndex = IDX_SPORT),
        CategoryChip(label = "Khác",        icon = "🎪", sectionIndex = IDX_OTHER)
    )

    var selectedChipIndex by remember { mutableIntStateOf(-1) } // -1 = không chip nào được chọn

    Scaffold(
        topBar  = {
            HomeHeader(
                onSearchClick       = onSearchClick,
                onFavoriteClick     = onFavoriteClick,
                onNotificationClick = onNotificationClick,
                unreadNotificationCount = unreadCount
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            val screenWidth = maxWidth
            val maxItems = if (screenWidth >= 600.dp) 6 else 4

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { eventViewModel.refreshEvents() },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // ── Item 0: Banner ──────────────────────────────────────────────
                    item(key = "banner") {
                        BannerSlider(
                            bannerEvents = bannerEvents,
                            onBannerClick = { eventId -> onEventClick(eventId) }
                        )
                    }

                    // ── Item 1: Category Chip Bar ───────────────────────────────────
                    item(key = "chip_bar") {
                        CategoryChipBar(
                            chips = chips,
                            selectedIndex = selectedChipIndex,
                            onChipClick = { chipIndex, sectionIndex ->
                                selectedChipIndex = chipIndex
                                coroutine.launch {
                                    listState.animateScrollToItem(index = sectionIndex)
                                }
                            }
                        )
                    }

                    // ── Item 2: Sự kiện đặc biệt (cuộn ngang) ──────────────────────
                    item(key = "special") {
                        if (specialEvents.isNotEmpty()) {
                            CategoryEventSection(
                                title = "Sự kiện đặc biệt 🔥",
                                events = specialEvents,
                                onEventClick = { eventId -> onEventClick(eventId) }
                            )
                        }
                    }

                    // ── Item 3: Nghệ thuật 🎨 ───────────────────────────────────────
                    item(key = "art") {
                        CategoryEventSection(
                            title = "Nghệ thuật 🎨",
                            events = artEvents,
                            onEventClick = { eventId -> onEventClick(eventId) },
                            useGridLayout = true,
                            maxItems = maxItems,
                            onSeeAllClick = { onSeeAllClick("Nghệ thuật") }
                        )
                    }

                    // ── Item 4: Âm nhạc sôi động ────────────────────────────────────
                    item(key = "music") {
                        CategoryEventSection(
                            title = "Âm nhạc sôi động 🎵",
                            events = musicEvents,
                            onEventClick = { eventId -> onEventClick(eventId) },
                            useGridLayout = true,
                            maxItems = maxItems,
                            onSeeAllClick = { onSeeAllClick("Âm nhạc") }
                        )
                    }

                    // ── Item 5: Thể thao ────────────────────────────────────────────
                    item(key = "sport") {
                        CategoryEventSection(
                            title = "Thể thao ⚽",
                            events = sportEvents,
                            onEventClick = { eventId -> onEventClick(eventId) },
                            useGridLayout = true,
                            maxItems = maxItems,
                            onSeeAllClick = { onSeeAllClick("Thể thao") }
                        )
                    }

                    // ── Item 6: Các sự kiện khác (chỉ hiện nếu có data) ────────────
                    item(key = "other") {
                        if (otherEvents.isNotEmpty()) {
                            CategoryEventSection(
                                title = "Các sự kiện khác 🎪",
                                events = otherEvents,
                                onEventClick = { eventId -> onEventClick(eventId) },
                                useGridLayout = true,
                                maxItems = maxItems,
                                onSeeAllClick = { onSeeAllClick("Các sự kiện khác") }
                            )
                        }
                    }
                }
            }
        }
    }
}