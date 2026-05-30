package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import huce.fit.myezticket.core.common.UiState
import huce.fit.myezticket.domain.model.User
import huce.fit.myezticket.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onChangePasswordClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onHelpCenterClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val userState by profileViewModel.userState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tài khoản", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (userState) {
            is UiState.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            is UiState.Error -> {
                val message = (userState as UiState.Error).message
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { Text(message, color = MaterialTheme.colorScheme.error) }
            }
            is UiState.Success -> {
                val user = (userState as UiState.Success<User>).data
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (user.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(96.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Avatar placeholder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(96.dp).clip(CircleShape)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(user.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(user.email, fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(24.dp))
                    // Action items
                    ProfileItem(icon = Icons.Filled.Edit, text = "Chỉnh sửa thông tin", onClick = onEditProfileClick)
                    ProfileItem(icon = Icons.Filled.Notifications, text = "Cài đặt thông báo", onClick = onNotificationSettingsClick)
                    ProfileItem(icon = Icons.Filled.Help, text = "Trung tâm trợ giúp", onClick = onHelpCenterClick)
                    ProfileItem(icon = Icons.Filled.Lock, text = "Đổi mật khẩu", onClick = onChangePasswordClick)
                    ProfileItem(icon = Icons.Filled.Delete, text = "Xóa tài khoản", onClick = onDeleteAccountClick)
                    ProfileItem(icon = Icons.Filled.Logout, text = "Đăng xuất", onClick = onLogoutClick)
                }
            }
            UiState.Idle -> Unit
        }
    }
}

@Composable
private fun ProfileItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(text, fontSize = 16.sp)
    }
    Divider()
}
