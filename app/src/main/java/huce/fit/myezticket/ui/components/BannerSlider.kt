package huce.fit.myezticket.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import huce.fit.myezticket.domain.model.Event
import kotlinx.coroutines.delay

@Composable
fun BannerSlider(
    bannerEvents: List<Event>,           // Bổ sung tham số này
    onBannerClick: (String) -> Unit      // Bổ sung tham số này
) {
    if (bannerEvents.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { bannerEvents.size })

    // LOGIC TỰ ĐỘNG CHẠY (AUTO-PLAY)
    // ==========================================
    LaunchedEffect(key1 = pagerState.settledPage) {
        // 1. THỜI GIAN NGHỈ (Thời gian dừng lại để người dùng xem ảnh)
        delay(3000) // Đang để 3 giây

        val nextPage = (pagerState.currentPage + 1) % bannerEvents.size

        // 2. THỜI GIAN TRƯỢT (Đã thêm animationSpec để trượt chậm lại)
        pagerState.animateScrollToPage(
            page = nextPage,
            animationSpec = tween(
                durationMillis = 1000 // Chỉnh thành 1000 mili-giây (1 giây) để ảnh trượt từ từ
            )
        )
    }

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(180.dp)
        ) { page ->
            val event = bannerEvents[page]
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    // Bắt sự kiện Click và truyền ID ra ngoài
                    .clickable { onBannerClick(event.id) },
                shape = RoundedCornerShape(12.dp)
            ) {
                AsyncImage(
                    model = event.image_url.ifEmpty { event.image_url },
                    contentDescription = "Banner ${event.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}