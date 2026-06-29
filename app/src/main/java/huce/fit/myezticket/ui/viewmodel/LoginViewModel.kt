package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import huce.fit.myezticket.core.common.Resource
import huce.fit.myezticket.core.common.UiState
import huce.fit.myezticket.domain.usecase.LoginUseCase
import huce.fit.myezticket.domain.usecase.LoginWithGoogleUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    private val _countdown = MutableStateFlow(0)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private var countdownJob: Job? = null

    fun onEmailChange(value: String) {
        _email.value = value
        resetError()
    }

    fun onPasswordChange(value: String) {
        _password.value = value
        resetError()
    }

    private fun resetError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = UiState.Idle
        }
    }

    fun login() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _uiState.value = UiState.Error("Thông tin đăng nhập không thể để trống. Vui lòng thử lại.")
            return
        }

        if (_countdown.value > 0) return

        loginUseCase(_email.value, _password.value).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = UiState.Loading
                }
                is Resource.Success -> {
                    _uiState.value = UiState.Success("Đăng nhập thành công")
                }
                is Resource.Error -> {
                    // Extract failed attempts if provided
                    val failedAttemptsStr = result.exception?.message
                    val isLocked = result.message == "Tài khoản đang tạm khóa"
                    
                    _uiState.value = UiState.Error(result.message, isLocked)

                    if (failedAttemptsStr != null && !isLocked) {
                        try {
                            val attempts = failedAttemptsStr.toInt()
                            if (attempts in 2..4) {
                                startCountdown(30) // Lock login button for 30s
                            }
                        } catch (e: Exception) {}
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        _countdown.value = seconds
        countdownJob = viewModelScope.launch {
            for (i in (seconds - 1) downTo 0) {
                delay(1000)
                _countdown.value = i
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }

    fun loginWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            _uiState.value = UiState.Error("ID Token Google không hợp lệ.")
            return
        }

        loginWithGoogleUseCase(idToken).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = UiState.Loading
                }
                is Resource.Success -> {
                    _uiState.value = UiState.Success("Đăng nhập bằng Google thành công")
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun setError(message: String) {
        _uiState.value = UiState.Error(message)
    }
}
