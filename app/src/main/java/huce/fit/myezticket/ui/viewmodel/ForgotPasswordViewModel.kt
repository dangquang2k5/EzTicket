package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import huce.fit.myezticket.core.common.Resource
import huce.fit.myezticket.core.common.UiState
import huce.fit.myezticket.domain.usecase.ResetPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _email.value = value
        resetState()
    }

    fun sendResetPasswordEmail() {
        resetPasswordUseCase(_email.value).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = UiState.Loading
                }
                is Resource.Success -> {
                    _uiState.value = UiState.Success(Unit)
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
