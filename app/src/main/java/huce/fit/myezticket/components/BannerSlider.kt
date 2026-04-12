package huce.fit.myezticket.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun BannerSlider() {
    // Giả sử bạn có 3 ảnh banner mẫu
    val banners = listOf(
        "https://via.placeholder.com/800x400/00C853/FFFFFF?text=Banner+1",
        "https://via.placeholder.com/800x400/FFD600/000000?text=Banner+2",
        "https://via.placeholder.com/800x400/00B0FF/FFFFFF?text=Banner+3"
    )
    val pagerState = rememberPagerState(pageCount = { banners.size })

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) { page ->
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                AsyncImage(
                    model = banners[page],
                    contentDescription = "Banner",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}