package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import huce.fit.myezticket.ui.viewmodel.ProfileViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val bookingEnabled by viewModel.bookingNotificationEnabled.collectAsState()
    val promoEnabled by viewModel.promoNotificationEnabled.collectAsState()
    val systemEnabled by viewModel.systemNotificationEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt thông báo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.updateNotificationSettings(bookingEnabled, promoEnabled, systemEnabled)
                        onDone()
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "Hoàn thành")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
        ) {
            NotificationToggleItem(
                label = "Thông báo đặt vé",
                checked = bookingEnabled,
                onCheckedChange = { viewModel.updateNotificationSettings(it, promoEnabled, systemEnabled) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            NotificationToggleItem(
                label = "Thông báo khuyến mãi",
                checked = promoEnabled,
                onCheckedChange = { viewModel.updateNotificationSettings(bookingEnabled, it, systemEnabled) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            NotificationToggleItem(
                label = "Thông báo hệ thống",
                checked = systemEnabled,
                onCheckedChange = { viewModel.updateNotificationSettings(bookingEnabled, promoEnabled, it) }
            )
        }
    }
}

@Composable
private fun NotificationToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, modifier = Modifier.weight(1f), fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
