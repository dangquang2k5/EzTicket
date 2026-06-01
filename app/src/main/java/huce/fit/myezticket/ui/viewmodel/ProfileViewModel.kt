package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import huce.fit.myezticket.core.common.Resource
import huce.fit.myezticket.core.common.UiState
import huce.fit.myezticket.domain.model.User
import huce.fit.myezticket.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val userState: StateFlow<UiState<User>> = _userState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val updateState: StateFlow<UiState<Unit>> = _updateState.asStateFlow()

    val pinCode: StateFlow<String?> = authRepository.getPinCode().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun savePinCode(pin: String) {
        viewModelScope.launch {
            authRepository.savePinCode(pin)
        }
    }

    fun clearPinCode() {
        viewModelScope.launch {
            authRepository.clearPinCode()
        }
    }

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    private val _changePasswordState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState.asStateFlow()
    val changePasswordState: StateFlow<UiState<Unit>> = _changePasswordState.asStateFlow()

    // Form states for editing profile
    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _birthDate = MutableStateFlow("")
    val birthDate: StateFlow<String> = _birthDate.asStateFlow()

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender.asStateFlow()

    private val _avatarUrl = MutableStateFlow("")
    val avatarUrl: StateFlow<String> = _avatarUrl.asStateFlow()

    private val _selectedImageBytes = MutableStateFlow<ByteArray?>(null)
    val selectedImageBytes: StateFlow<ByteArray?> = _selectedImageBytes.asStateFlow()

    // Notification states
    private val _bookingNotificationEnabled = MutableStateFlow(true)
    val bookingNotificationEnabled: StateFlow<Boolean> = _bookingNotificationEnabled.asStateFlow()

    private val _promoNotificationEnabled = MutableStateFlow(true)
    val promoNotificationEnabled: StateFlow<Boolean> = _promoNotificationEnabled.asStateFlow()

    private val _systemNotificationEnabled = MutableStateFlow(true)
    val systemNotificationEnabled: StateFlow<Boolean> = _systemNotificationEnabled.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _userState.value = UiState.Loading
            when (val result = authRepository.getCurrentUserDetail()) {
                is Resource.Success -> {
                    val user = result.data
                    _userState.value = UiState.Success(user)
                    // Sync form states
                    _fullName.value = user.fullName
                    _phone.value = user.phone
                    _birthDate.value = user.birthDate
                    _gender.value = user.gender
                    _avatarUrl.value = user.avatarUrl
                    _bookingNotificationEnabled.value = user.bookingNotificationEnabled
                    _promoNotificationEnabled.value = user.promoNotificationEnabled
                    _systemNotificationEnabled.value = user.systemNotificationEnabled
                }
                is Resource.Error -> {
                    _userState.value = UiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onFullNameChange(value: String) {
        _fullName.value = value
    }

    fun onPhoneChange(value: String) {
        _phone.value = value
    }

    fun onBirthDateChange(value: String) {
        _birthDate.value = value
    }

    fun onGenderChange(value: String) {
        _gender.value = value
    }

    fun onSelectImage(bytes: ByteArray?) {
        _selectedImageBytes.value = bytes
    }

    fun resetUpdateState() {
        _updateState.value = UiState.Idle
    }

    fun resetDeleteState() {
        _deleteState.value = UiState.Idle
    }

    fun updateUserProfile() {
        val currentFullName = _fullName.value.trim()
        val currentPhone = _phone.value.trim()

        if (currentFullName.isBlank()) {
            _updateState.value = UiState.Error("Họ và tên không được để trống")
            return
        }

        if (!currentPhone.matches(Regex("^\\d{10}$"))) {
            _updateState.value = UiState.Error("Số điện thoại phải có đúng 10 chữ số")
            return
        }

        viewModelScope.launch {
            _updateState.value = UiState.Loading

            // 1. Upload avatar if selected
            var newAvatarUrl = _avatarUrl.value
            val imageBytes = _selectedImageBytes.value
            if (imageBytes != null) {
                when (val uploadResult = authRepository.uploadAvatar(imageBytes)) {
                    is Resource.Success -> {
                        newAvatarUrl = uploadResult.data
                        _avatarUrl.value = newAvatarUrl
                        _selectedImageBytes.value = null // clear after upload
                    }
                    is Resource.Error -> {
                        _updateState.value = UiState.Error("Không thể tải ảnh đại diện lên: ${uploadResult.message}")
                        return@launch
                    }
                    else -> {}
                }
            }

            // 2. Fetch current user data to retain fields that are not in the form
            val currentUser = when (val userRes = authRepository.getCurrentUserDetail()) {
                is Resource.Success -> userRes.data
                else -> null
            }

            if (currentUser == null) {
                _updateState.value = UiState.Error("Không tìm thấy thông tin tài khoản")
                return@launch
            }

            val updatedUser = currentUser.copy(
                fullName = currentFullName,
                phone = currentPhone,
                birthDate = _birthDate.value,
                gender = _gender.value,
                avatarUrl = newAvatarUrl
            )

            // 3. Save to Firestore
            when (val saveResult = authRepository.updateUserProfile(updatedUser)) {
                is Resource.Success -> {
                    _userState.value = UiState.Success(updatedUser)
                    _updateState.value = UiState.Success(Unit)
                }
                is Resource.Error -> {
                    _updateState.value = UiState.Error(saveResult.message)
                }
                else -> {}
            }
        }
    }

    fun updateNotificationSettings(booking: Boolean, promo: Boolean, system: Boolean) {
        _bookingNotificationEnabled.value = booking
        _promoNotificationEnabled.value = promo
        _systemNotificationEnabled.value = system

        viewModelScope.launch {
            authRepository.updateNotificationSettings(booking, promo, system)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            when (val result = authRepository.deleteAccount()) {
                is Resource.Success -> {
                    _deleteState.value = UiState.Success(Unit)
                }
                is Resource.Error -> {
                    _deleteState.value = UiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = UiState.Loading
            when (val result = authRepository.changePassword(currentPassword, newPassword)) {
                is Resource.Success -> {
                    _changePasswordState.value = UiState.Success(Unit)
                }
                is Resource.Error -> {
                    _changePasswordState.value = UiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
