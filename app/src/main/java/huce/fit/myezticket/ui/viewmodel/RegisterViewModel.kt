package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import huce.fit.myezticket.core.common.Resource
import huce.fit.myezticket.core.common.UiState
import huce.fit.myezticket.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val registerUseCase: RegisterUseCase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RegisterViewModel(registerUseCase) as T
        }
    }

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    fun onPhoneChange(value: String) {
        _phone.value = value
        resetError()
    }

    fun onEmailChange(value: String) {
        _email.value = value
        resetError()
    }

    fun onPasswordChange(value: String) {
        _password.value = value
        resetError()
    }

    fun onConfirmPasswordChange(value: String) {
        _confirmPassword.value = value
        resetError()
    }

    private fun resetError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = UiState.Idle
        }
    }

    fun register() {
        if (_phone.value.isBlank() || _email.value.isBlank() || _password.value.isBlank() || _confirmPassword.value.isBlank()) {
            _uiState.value = UiState.Error("Vui lòng điền đầy đủ thông tin.")
            return
        }

        registerUseCase(_phone.value, _email.value, _password.value, _confirmPassword.value).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = UiState.Loading
                }
                is Resource.Success -> {
                    _uiState.value = UiState.Success("Đăng ký thành công")
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
