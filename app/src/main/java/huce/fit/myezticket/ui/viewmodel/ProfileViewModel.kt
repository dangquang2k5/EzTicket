package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import huce.fit.myezticket.domain.model.User
import huce.fit.myezticket.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import huce.fit.myezticket.core.common.UiState

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getPinCodeUseCase: GetPinCodeUseCase,
    private val getPinFailedAttemptsUseCase: GetPinFailedAttemptsUseCase,
    private val getPinLockoutTimeUseCase: GetPinLockoutTimeUseCase,
    private val savePinCodeUseCase: SavePinCodeUseCase,
    private val clearPinCodeUseCase: ClearPinCodeUseCase,
    private val savePinFailedAttemptsUseCase: SavePinFailedAttemptsUseCase,
    private val savePinLockoutTimeUseCase: SavePinLockoutTimeUseCase,
    private val getCurrentUserDetailUseCase: GetCurrentUserDetailUseCase,
    private val getCurrentUserEmailUseCase: GetCurrentUserEmailUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val loginUseCase: LoginUseCase,
    private val getCurrentUserUidUseCase: GetCurrentUserUidUseCase
) : ViewModel() {

    val isUserLoggedIn: Boolean get() = getCurrentUserUidUseCase().isNotEmpty()

    private val _userState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val userState: StateFlow<UiState<User>> = _userState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val updateState: StateFlow<UiState<Unit>> = _updateState.asStateFlow()

    val pinCode: StateFlow<String?> = getPinCodeUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val failedAttempts: StateFlow<Int> = getPinFailedAttemptsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val lockoutTime: StateFlow<Long> = getPinLockoutTimeUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    private val _forgotPinState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val forgotPinState: StateFlow<UiState<Unit>> = _forgotPinState.asStateFlow()

    private val _verifyPasswordState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val verifyPasswordState: StateFlow<UiState<Unit>> = _verifyPasswordState.asStateFlow()

    fun savePinCode(pin: String) {
        viewModelScope.launch {
            savePinCodeUseCase(pin)
        }
    }

    fun clearPinCode() {
        viewModelScope.launch {
            clearPinCodeUseCase()
        }
    }

    fun handlePinFailed() {
        viewModelScope.launch {
            val currentAttempts = failedAttempts.value + 1
            savePinFailedAttemptsUseCase(currentAttempts)

            if (currentAttempts >= 5) {
                // Phạt 1 phút cho lần thứ 5, mỗi lần sai tiếp theo cộng thêm 5 phút
                val lockoutMinutes = 1 + 5 * (currentAttempts - 5)
                val lockoutDurationMs = lockoutMinutes * 60 * 1000L
                savePinLockoutTimeUseCase(System.currentTimeMillis() + lockoutDurationMs)
            }
        }
    }

    fun handlePinSuccess() {
        viewModelScope.launch {
            if (failedAttempts.value > 0) {
                savePinFailedAttemptsUseCase(0)
            }
            if (lockoutTime.value > 0L) {
                savePinLockoutTimeUseCase(0L)
            }
        }
    }

    fun resetForgotPinState() {
        _forgotPinState.value = UiState.Idle
    }

    /** Gửi email đặt lại PIN: dùng email của tài khoản đang đăng nhập để gửi link reset mật khẩu */
    fun sendForgotPinEmail() {
        val email = getCurrentUserEmailUseCase()
        if (email.isBlank()) {
            _forgotPinState.value = UiState.Error("Không tìm thấy email tài khoản")
            return
        }
        viewModelScope.launch {
            _forgotPinState.value = UiState.Loading
            when (val result = sendPasswordResetEmailUseCase(email)) {
                is huce.fit.myezticket.core.common.Resource.Success -> _forgotPinState.value = UiState.Success(Unit)
                is huce.fit.myezticket.core.common.Resource.Error   -> _forgotPinState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun resetVerifyPasswordState() {
        _verifyPasswordState.value = UiState.Idle
    }

    /** Xác thực mật khẩu đăng nhập để lấy lại mã PIN */
    fun verifyPasswordAndResetPin(password: String) {
        val email = getCurrentUserEmailUseCase()
        if (email.isBlank()) {
            _verifyPasswordState.value = UiState.Error("Không tìm thấy thông tin tài khoản.")
            return
        }
        if (password.isBlank()) {
            _verifyPasswordState.value = UiState.Error("Vui lòng nhập mật khẩu.")
            return
        }

        viewModelScope.launch {
            _verifyPasswordState.value = UiState.Loading
            when (val result = loginUseCase(email, password).first()) {
                is huce.fit.myezticket.core.common.Resource.Success<*> -> {
                    // Mật khẩu đúng -> Xóa mã PIN cũ
                    clearPinCodeUseCase()
                    _verifyPasswordState.value = UiState.Success(Unit)
                }
                is huce.fit.myezticket.core.common.Resource.Error -> {
                    _verifyPasswordState.value = UiState.Error("Mật khẩu không chính xác.")
                }
                else -> {}
            }
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
    private val _pushNotificationEnabled = MutableStateFlow(true)
    val pushNotificationEnabled: StateFlow<Boolean> = _pushNotificationEnabled.asStateFlow()

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
            when (val result = getCurrentUserDetailUseCase()) {
                is huce.fit.myezticket.core.common.Resource.Success -> {
                    val user = result.data
                    _userState.value = UiState.Success(user)
                    // Sync form states
                    _fullName.value = user.fullName
                    _phone.value = user.phone
                    _birthDate.value = user.birthDate
                    _gender.value = user.gender
                    _avatarUrl.value = user.avatarUrl
                    _pushNotificationEnabled.value = user.pushNotificationEnabled
                    _bookingNotificationEnabled.value = user.bookingNotificationEnabled
                    _promoNotificationEnabled.value = user.promoNotificationEnabled
                    _systemNotificationEnabled.value = user.systemNotificationEnabled
                }
                is huce.fit.myezticket.core.common.Resource.Error -> {
                    _userState.value = UiState.Error(result.message)
                }
                is huce.fit.myezticket.core.common.Resource.Loading -> {}
            }
        }
    }

    fun syncFormWithUserState() {
        val state = _userState.value
        if (state is UiState.Success) {
            val user = state.data
            _fullName.value = user.fullName
            _phone.value = user.phone
            _birthDate.value = user.birthDate
            _gender.value = user.gender
            _avatarUrl.value = user.avatarUrl
            _selectedImageBytes.value = null
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

    fun resetChangePasswordState() {
        _changePasswordState.value = UiState.Idle
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
                when (val uploadResult = uploadAvatarUseCase(imageBytes)) {
                    is huce.fit.myezticket.core.common.Resource.Success -> {
                        newAvatarUrl = uploadResult.data
                        _avatarUrl.value = newAvatarUrl
                        _selectedImageBytes.value = null // clear after upload
                    }
                    is huce.fit.myezticket.core.common.Resource.Error -> {
                        _updateState.value = UiState.Error("Không thể tải ảnh đại diện lên: ${uploadResult.message}")
                        return@launch
                    }
                    else -> {}
                }
            }

            // 2. Fetch current user data to retain fields that are not in the form
            val currentUser = when (val userRes = getCurrentUserDetailUseCase()) {
                is huce.fit.myezticket.core.common.Resource.Success -> userRes.data
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
            when (val saveResult = updateUserProfileUseCase(updatedUser)) {
                is huce.fit.myezticket.core.common.Resource.Success -> {
                    _userState.value = UiState.Success(updatedUser)
                    _updateState.value = UiState.Success(Unit)
                }
                is huce.fit.myezticket.core.common.Resource.Error -> {
                    _updateState.value = UiState.Error(saveResult.message)
                }
                else -> {}
            }
        }
    }

    fun updateNotificationSettings(push: Boolean, booking: Boolean, promo: Boolean, system: Boolean) {
        _pushNotificationEnabled.value = push
        _bookingNotificationEnabled.value = booking
        _promoNotificationEnabled.value = promo
        _systemNotificationEnabled.value = system

        viewModelScope.launch {
            updateNotificationSettingsUseCase(push, booking, promo, system)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            when (val result = deleteAccountUseCase()) {
                is huce.fit.myezticket.core.common.Resource.Success -> {
                    _deleteState.value = UiState.Success(Unit)
                }
                is huce.fit.myezticket.core.common.Resource.Error -> {
                    _deleteState.value = UiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = UiState.Loading
            when (val result = changePasswordUseCase(currentPassword, newPassword)) {
                is huce.fit.myezticket.core.common.Resource.Success -> {
                    _changePasswordState.value = UiState.Success(Unit)
                }
                is huce.fit.myezticket.core.common.Resource.Error -> {
                    _changePasswordState.value = UiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun logout() {
        logoutUseCase()
        _userState.value = UiState.Idle
        _fullName.value = ""
        _phone.value = ""
        _birthDate.value = ""
        _gender.value = ""
        _avatarUrl.value = ""
        _selectedImageBytes.value = null
    }
}
