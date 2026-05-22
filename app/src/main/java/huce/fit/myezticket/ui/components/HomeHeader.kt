package huce.fit.myezticket.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeader(onSearchClick: () -> Unit = {} // BỔ SUNG THAM SỐ NÀY
 ) {
    TopAppBar(
        title = {
            Text(
                text = "EzTicket",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search, // ĐÃ SỬA LẠI THÀNH imageVector
                    contentDescription = "Search"
                )
            }
            IconButton(onClick = { /* Xử lý thông báo */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications, // ĐÃ SỬA LẠI THÀNH imageVector
                    contentDescription = "Notifications"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}