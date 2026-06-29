package huce.fit.myezticket.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val isDraggedState = pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(key1 = isDraggedState.value) {
        // Nếu người dùng đang giữ/kéo Pager, tạm ngưng auto-play
        if (isDraggedState.value) return@LaunchedEffect

        while (true) {
            delay(3000) // Thời gian nghỉ xem ảnh (3 giây)

            val currentPage = pagerState.currentPage
            val nextPage = (currentPage + 1) % bannerEvents.size

            if (nextPage == 0 && currentPage > 0) {
                // Trượt lùi từ từ lần lượt từng trang về trang 0
                for (page in (currentPage - 1) downTo 0) {
                    pagerState.animateScrollToPage(
                        page = page,
                        animationSpec = tween(durationMillis = 650)
                    )
                    delay(100) // Nghỉ ngắn 100ms giữa các bước trượt lùi
                }
            } else {
                // Trượt tiến thông thường (1 trang)
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(durationMillis = 1000)
                )
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val currentMaxWidth = maxWidth
        val isWide = currentMaxWidth > 600.dp
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = if (isWide) 680.dp else currentMaxWidth)
                        .aspectRatio(2f),
                    pageSize = PageSize.Fill
                ) { page ->
                    val event = bannerEvents[page]
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            // Bắt sự kiện Click và truyền ID ra ngoài
                            .clickable { onBannerClick(event.id) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = event.image_url.ifEmpty { event.image_url },
                                contentDescription = "Banner ${event.name}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Nút "Xem chi tiết" ở góc dưới bên phải
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Black.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "Xem chi tiết →",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Chấm chỉ số Pager (Dots Indicator) dưới Banner
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(if (isSelected) 16.dp else 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }
    }
}