package huce.fit.myezticket.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import huce.fit.myezticket.core.common.UiState
import huce.fit.myezticket.domain.model.User
import huce.fit.myezticket.ui.components.MainTabHeader
import huce.fit.myezticket.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onChangePasswordClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onPinSetupClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val userState by profileViewModel.userState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            MainTabHeader(title = "Tài khoản")
        },
        containerColor = Color(0xFFF7F9F8)
    ) { paddingValues ->
        when (userState) {
            is UiState.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
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
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Banner Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Placeholder for doodles or gradient design
                    }

                    // Overlapping Round Avatar
                    Box(
                        modifier = Modifier
                            .offset(y = (-40).dp)
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Avatar placeholder",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // User Info Details (Name & Email)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(y = (-32).dp)
                    ) {
                        Text(
                            text = user.fullName.ifBlank { "Người dùng EZTicket" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = user.email,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    // Main Action Cards
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-12).dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        // Section 1: Cài đặt tài khoản
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cài đặt tài khoản",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column {
                                ProfileRowItem(
                                    icon = Icons.Filled.Edit,
                                    text = "Chỉnh sửa thông tin",
                                    onClick = onEditProfileClick
                                )
                                HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                                ProfileRowItem(
                                    icon = Icons.Filled.Lock,
                                    text = "Thiết lập mã PIN",
                                    onClick = onPinSetupClick
                                )
                                HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                                ProfileRowItem(
                                    icon = Icons.Filled.Notifications,
                                    text = "Cài đặt thông báo",
                                    onClick = onNotificationSettingsClick
                                )
                                HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                                ProfileRowItem(
                                    icon = Icons.Filled.VpnKey,
                                    text = "Đổi mật khẩu",
                                    onClick = onChangePasswordClick
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Section 2: Hỗ trợ
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Help,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hỗ trợ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            ProfileRowItem(
                                icon = Icons.Filled.Help,
                                text = "Trung tâm trợ giúp",
                                onClick = {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://ctsv.huce.edu.vn/ho-tro-sinh-vien")
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Section 3: Phiên bản & Đăng xuất
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            ProfileRowItem(
                                icon = Icons.Filled.Logout,
                                text = "Đăng xuất",
                                onClick = onLogoutClick,
                                textColor = Color.Red,
                                iconColor = Color.Red
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
            UiState.Idle -> Unit
        }
    }
}

@Composable
fun ProfileRowItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    textColor: Color = Color.Black,
    iconColor: Color = Color(0xFF00B14F)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Arrow",
            tint = Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
    }
}
