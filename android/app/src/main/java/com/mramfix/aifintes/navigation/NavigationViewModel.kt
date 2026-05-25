package com.mramfix.aifintes.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mramfix.aifintes.data.auth.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Состояния навигации приложения:
 * - Login: пользователь не авторизован
 * - Onboarding: авторизован, но онбординг не пройден
 * - Home: авторизован и онбординг пройден
 */
enum class AppScreen {
    LOGIN, ONBOARDING, HOME
}

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _onboardingCompleted = MutableStateFlow(false)

    /**
     * Определяет стартовый экран на основе состояния:
     * - Нет токенов → LOGIN
     * - Токены есть, онбординг не пройден → ONBOARDING
     * - Токены есть, онбординг пройден → HOME
     */
    val startDestination: StateFlow<String> = combine(
        tokenManager.hasTokens,
        _onboardingCompleted
    ) { hasTokens, onboardingCompleted ->
        when {
            !hasTokens -> Routes.LOGIN
            !onboardingCompleted -> Routes.ONBOARDING
            else -> Routes.HOME
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Routes.LOGIN)

    fun onLoginSuccess() {
        // Токены уже сохранены в TokenManager при логине
    }

    fun onRegisterSuccess() {
        // Токены уже сохранены в TokenManager при регистрации
    }

    fun onOnboardingComplete() {
        viewModelScope.launch {
            _onboardingCompleted.value = true
        }
    }

    /**
     * Определяет следующий экран после авторизации
     */
    fun nextDestination(): String {
        return if (_onboardingCompleted.value) Routes.HOME else Routes.ONBOARDING
    }
}
