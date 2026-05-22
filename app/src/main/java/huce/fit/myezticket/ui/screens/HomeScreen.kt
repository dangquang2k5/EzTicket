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
import androidx.lifecycle.viewmodel.compose.viewModel
import huce.fit.myezticket.data.FakeDataGenerator
import huce.fit.myezticket.ui.viewmodel.EventViewModel
import huce.fit.myezticket.ui.components.BannerSlider
import huce.fit.myezticket.ui.components.CategoryChipBar
import huce.fit.myezticket.ui.components.CategoryChip
import huce.fit.myezticket.ui.components.CategoryEventSection
import huce.fit.myezticket.ui.components.HomeBottomNavigation
import huce.fit.myezticket.ui.components.HomeHeader
import huce.fit.myezticket.ui.components.SpecialEventSection
import kotlinx.coroutines.launch

// ─── Các index item trong LazyColumn ──────────────────────────────────────────
// item 0  = Banner
// item 1  = ChipBar
// item 2  = Sự kiện đặc biệt
// item 3  = Âm nhạc
// item 4  = Hội thảo & Khóa học
// item 5  = Thể thao
// item 6  = Khác
// item 7  = Nút debug (tạm thời)

private const val IDX_MUSIC      = 3
private const val IDX_WORKSHOP   = 4
private const val IDX_SPORT      = 5
private const val IDX_OTHER      = 6

@Composable
fun HomeScreen(
    eventViewModel: EventViewModel = viewModel(),
    onEventClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSeeAllClick: (category: String) -> Unit = {},
    onMyTicketsClick: () -> Unit = {}
) {
    val events       by eventViewModel.events.collectAsState()
    val bannerEvents by eventViewModel.bannerEvents.collectAsState()

    val listState  = rememberLazyListState()
    val coroutine  = rememberCoroutineScope()

    // ── Lọc sự kiện theo từng danh mục ──────────────────────────────────────
    val specialEvents  = events.filter { it.isHot }
    val musicEvents    = events.filter { it.category == "Âm nhạc" }
    val workshopEvents = events.filter { it.category == "Hội thảo" }
    val sportEvents    = events.filter { it.category == "Thể thao" }
    val otherEvents    = events.filter { event ->
        event.category != "Âm nhạc" &&
        event.category != "Hội thảo" &&
        event.category != "Thể thao"
    }

    // ── Định nghĩa chips (sectionIndex = vị trí item trong LazyColumn) ───────
    val chips = listOf(
        CategoryChip(label = "Âm nhạc",   icon = "🎵", sectionIndex = IDX_MUSIC),
        CategoryChip(label = "Hội thảo",  icon = "🎓", sectionIndex = IDX_WORKSHOP),
        CategoryChip(label = "Thể thao",  icon = "⚽", sectionIndex = IDX_SPORT),
        CategoryChip(label = "Khác",      icon = "🎪", sectionIndex = IDX_OTHER)
    )

    var selectedChipIndex by remember { mutableIntStateOf(-1) } // -1 = không chip nào được chọn

    Scaffold(
        topBar  = { HomeHeader(onSearchClick = onSearchClick) },
        bottomBar = {
            HomeBottomNavigation(
                selectedIndex = 0,
                onHomeClick = { /* Đã ở trang chủ */ },
                onMyTicketsClick = onMyTicketsClick,
                onProfileClick = { /* Sẽ làm sau */ }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
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

            // ── Item 3: Âm nhạc sôi động ────────────────────────────────────
            item(key = "music") {
                CategoryEventSection(
                    title = "Âm nhạc sôi động 🎵",
                    events = musicEvents,
                    onEventClick = { eventId -> onEventClick(eventId) },
                    useGridLayout = true,
                    maxItems = 4,
                    onSeeAllClick = { onSeeAllClick("Âm nhạc") }
                )
            }

            // ── Item 4: Hội thảo & Khóa học ────────────────────────────────
            item(key = "workshop") {
                CategoryEventSection(
                    title = "Hội thảo & Khóa học 🎓",
                    events = workshopEvents,
                    onEventClick = { eventId -> onEventClick(eventId) },
                    useGridLayout = true,
                    maxItems = 4,
                    onSeeAllClick = { onSeeAllClick("Hội thảo") }
                )
            }

            // ── Item 5: Thể thao ────────────────────────────────────────────
            item(key = "sport") {
                CategoryEventSection(
                    title = "Thể thao ⚽",
                    events = sportEvents,
                    onEventClick = { eventId -> onEventClick(eventId) },
                    useGridLayout = true,
                    maxItems = 4,
                    onSeeAllClick = { onSeeAllClick("Thể thao") }
                )
            }

            // ── Item 6: Sự kiện khác (chỉ hiện nếu có data) ────────────────
            item(key = "other") {
                if (otherEvents.isNotEmpty()) {
                    CategoryEventSection(
                        title = "Sự kiện khác 🎪",
                        events = otherEvents,
                        onEventClick = { eventId -> onEventClick(eventId) },
                        useGridLayout = true,
                        maxItems = 4,
                        onSeeAllClick = { onSeeAllClick("Khác") }
                    )
                }
            }

            // ── Item 7: Nút debug (tạm thời – xóa khi production) ──────────
            item(key = "debug_btn") {
                Button(
                    onClick = { FakeDataGenerator.seedDataToFirebase() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
            }
        }
    }
}