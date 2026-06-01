package huce.fit.myezticket.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Danh mục sự kiện dùng cho chip bar
 */
data class CategoryChip(
    val label: String,  // tên hiển thị, ví dụ "Âm nhạc"
    val icon: String,   // emoji icon
    val sectionIndex: Int // index tương ứng trong LazyColumn (để scroll đến)
)

/**
 * Thanh chips cuộn ngang ở HomeScreen.
 * Khi nhấn vào chip → gọi [onChipClick] với [sectionIndex] để HomeScreen
 * tự động scroll đến section tương ứng.
 *
 * @param chips         Danh sách các chip hiển thị
 * @param selectedIndex Index chip đang được chọn (highlight)
 * @param onChipClick   Callback trả về (chipIndex, sectionIndex)
 */
@Composable
fun CategoryChipBar(
    chips: List<CategoryChip>,
    selectedIndex: Int,
    onChipClick: (chipIndex: Int, sectionIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(chips) { chipIndex, chip ->
            val isSelected = chipIndex == selectedIndex

            val containerColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surface,
                animationSpec = tween(durationMillis = 250),
                label = "chipColor_$chipIndex"
            )

            val contentColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                animationSpec = tween(durationMillis = 250),
                label = "chipTextColor_$chipIndex"
            )

            Surface(
                onClick = { onChipClick(chipIndex, chip.sectionIndex) },
                shape = RoundedCornerShape(50),
                color = containerColor,
                tonalElevation = if (isSelected) 0.dp else 2.dp,
                shadowElevation = if (isSelected) 4.dp else 1.dp,
                modifier = Modifier.height(38.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = chip.icon,
                        fontSize = 16.sp
                    )
                    Text(
                        text = chip.label,
                        color = contentColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
