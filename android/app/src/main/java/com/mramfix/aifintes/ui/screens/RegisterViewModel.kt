package com.mramfix.aifintes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mramfix.aifintes.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val apiError: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email.trim(), emailError = null, apiError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, apiError = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null, apiError = null) }
    }

    fun register() {
        val state = _uiState.value

        // Валидация
        var hasError = false

        if (state.email.isBlank() || !isValidEmail(state.email)) {
            _uiState.update { it.copy(emailError = "Введите корректный email") }
            hasError = true
        }

        if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Пароль должен быть не менее 6 символов") }
            hasError = true
        }

        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Пароли не совпадают") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, apiError = null) }
            val result = authRepository.register(state.email, state.password)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            apiError = when (e) {
                                is com.mramfix.aifintes.data.repository.AuthException ->
                                    if (e.code == 409) "Пользователь с таким email уже существует"
                                    else "Ошибка сервера (${e.code})"
                                else -> "Ошибка сети. Проверьте подключение"
                            }
                        )
                    }
                }
            )
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
