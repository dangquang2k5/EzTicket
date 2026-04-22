package huce.fit.myezticket.ui.components



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun HomeBottomNavigation() {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color(0xFF00C853)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
            label = { Text("Trang chủ") },
            selected = true,
            onClick = { /* Chuyển màn hình */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = "Vé của tôi") },
            label = { Text("Vé của tôi") },
            selected = false,
            onClick = { /* Chuyển màn hình */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Tài khoản") },
            label = { Text("Tài khoản") },
            selected = false,
            onClick = { /* Chuyển màn hình */ }
        )
    }
}