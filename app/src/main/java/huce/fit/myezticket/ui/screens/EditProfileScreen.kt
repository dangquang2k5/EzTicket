package huce.fit.myezticket.ui.screens

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import huce.fit.myezticket.core.common.UiState
import huce.fit.myezticket.ui.viewmodel.ProfileViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val fullName by viewModel.fullName.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()
    val selectedImageBytes by viewModel.selectedImageBytes.collectAsState()
    val updateState = viewModel.updateState.collectAsState()

    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showGenderMenu by remember { mutableStateOf(false) }

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            viewModel.onSelectImage(bytes)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) { Icon(Icons.Filled.ArrowBack, "Quay lại") }
                },
                actions = {
                    TextButton(onClick = { viewModel.updateUserProfile() }) { Text("Hoàn thành") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(modifier = Modifier.size(120.dp).clip(CircleShape).clickable { launcher.launch("image/*") }) {
                if (selectedImageBytes != null) {
                    val bitmap = BitmapFactory.decodeByteArray(selectedImageBytes, 0, selectedImageBytes!!.size)
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Avatar", contentScale = ContentScale.Crop)
                } else if (avatarUrl.isNotBlank()) {
                    AsyncImage(model = avatarUrl, contentDescription = "Avatar", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Placeholder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Full name
            OutlinedTextField(
                value = fullName,
                onValueChange = { viewModel.onFullNameChange(it) },
                label = { Text("Họ và tên") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { viewModel.onPhoneChange(it) },
                label = { Text("Số điện thoại") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Birth date picker
            OutlinedTextField(
                value = birthDate,
                onValueChange = { },
                readOnly = true,
                label = { Text("Ngày sinh") },
                trailingIcon = {
                    Icon(Icons.Filled.CalendarToday, "Chọn ngày", modifier = Modifier.clickable { showDatePicker = true })
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (showDatePicker) {
                val today = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        viewModel.onBirthDateChange("${dayOfMonth}/${month + 1}/${year}")
                        showDatePicker = false
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Gender selector
            OutlinedTextField(
                value = gender,
                onValueChange = { },
                readOnly = true,
                label = { Text("Giới tính") },
                trailingIcon = {
                    Icon(Icons.Filled.ArrowDropDown, "Chọn giới tính", modifier = Modifier.clickable { showGenderMenu = true })
                },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(expanded = showGenderMenu, onDismissRequest = { showGenderMenu = false }) {
                listOf("Nam", "Nữ", "Khác").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.onGenderChange(option)
                            showGenderMenu = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Show update state (error / loading)
            when (val state = updateState.value) {
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is UiState.Success -> LaunchedEffect(Unit) { onDone() }
                else -> {}
            }
        }
    }
}
