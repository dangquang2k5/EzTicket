package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import huce.fit.myezticket.ui.viewmodel.ProfileViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.NotificationsActive
import huce.fit.myezticket.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val pushEnabled by viewModel.pushNotificationEnabled.collectAsState()
    val bookingEnabled by viewModel.bookingNotificationEnabled.collectAsState()
    val promoEnabled by viewModel.promoNotificationEnabled.collectAsState()
    val systemEnabled by viewModel.systemNotificationEnabled.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF7F9F8),
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt thông báo", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Section 1: Kênh nhận thông báo
            SectionHeader(title = "KÊNH NHẬN THÔNG BÁO")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    NotificationToggleItem(
                        label = "Thông báo đẩy trên điện thoại",
                        description = "Nhận thông báo tức thì trên màn hình khóa & thanh trạng thái",
                        icon = Icons.Filled.NotificationsActive,
                        checked = pushEnabled,
                        onCheckedChange = { viewModel.updateNotificationSettings(it, bookingEnabled, promoEnabled, systemEnabled) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Nội dung thông báo
            SectionHeader(title = "NỘI DUNG THÔNG BÁO")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    NotificationToggleItem(
                        label = "Thông báo đặt vé",
                        description = "Cập nhật trạng thái đặt vé & thông tin vé đã mua",
                        icon = Icons.Filled.ConfirmationNumber,
                        checked = bookingEnabled,
                        onCheckedChange = { viewModel.updateNotificationSettings(pushEnabled, it, promoEnabled, systemEnabled) }
                    )
                    
                    HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                    
                    NotificationToggleItem(
                        label = "Thông báo khuyến mãi",
                        description = "Nhận thông tin ưu đãi & mã giảm giá mới nhất từ EzTicket",
                        icon = Icons.Filled.LocalOffer,
                        checked = promoEnabled,
                        onCheckedChange = { viewModel.updateNotificationSettings(pushEnabled, bookingEnabled, it, systemEnabled) }
                    )
                    
                    HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                    
                    NotificationToggleItem(
                        label = "Nhắc nhở sự kiện yêu thích",
                        description = "Nhắc nhở sự kiện quan tâm sắp diễn ra hoặc sắp mở bán vé",
                        icon = Icons.Filled.NotificationsActive,
                        checked = systemEnabled,
                        onCheckedChange = { viewModel.updateNotificationSettings(pushEnabled, bookingEnabled, promoEnabled, it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Settings Button
            PrimaryButton(
                text = "Lưu cài đặt",
                onClick = {
                    viewModel.updateNotificationSettings(pushEnabled, bookingEnabled, promoEnabled, systemEnabled)
                    onDone()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    )
}

@Composable
private fun NotificationToggleItem(
    label: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Icon container with soft green background
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF00B14F),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        // Title & Description Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Premium Green switch
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00B14F),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE2E2E2),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
