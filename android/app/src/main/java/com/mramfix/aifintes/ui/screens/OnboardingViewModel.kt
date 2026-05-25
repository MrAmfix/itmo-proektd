package com.mramfix.aifintes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mramfix.aifintes.data.api.OnboardingRequest
import com.mramfix.aifintes.data.api.ProfileApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Шаги онбординга
 */
enum class OnboardingStep(val index: Int, val title: String) {
    ANTHROPOMETRY(0, "Антропометрия"),
    GOAL(1, "Цель"),
    LEVEL(2, "Уровень подготовки"),
    INJURIES(3, "Травмы"),
    EQUIPMENT(4, "Инвентарь");

    val total get() = entries.size
}

// Опции для шагов
object OnboardingOptions {
    val goals = listOf(
        "Похудение",
        "Набор мышечной массы",
        "Поддержание формы",
        "Реабилитация",
        "Сила и выносливость",
        "Гибкость"
    )

    val levels = listOf(
        "Новичок",
        "Любитель",
        "Средний",
        "Продвинутый",
        "Профессионал"
    )

    val injuries = listOf(
        "Спина",
        "Колени",
        "Плечи",
        "Шея",
        "Запястья",
        "Голеностоп",
        "Нет травм"
    )

    val equipment = listOf(
        "Гантели",
        "Штанга",
        "Эспандеры",
        "Турник",
        "Коврик",
        "Скакалка",
        "Фитбол",
        "Нет инвентаря"
    )
}

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.ANTHROPOMETRY,
    val weight: String = "",
    val height: String = "",
    val weightError: String? = null,
    val heightError: String? = null,
    val selectedGoal: String? = null,
    val selectedLevel: String? = null,
    val selectedInjuries: Set<String> = emptySet(),
    val selectedEquipment: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val apiError: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileApi: ProfileApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    val progress: Float get() {
        val state = _uiState.value
        return state.currentStep.index.toFloat() / (OnboardingStep.entries.size - 1).toFloat()
    }

    fun onWeightChanged(weight: String) {
        _uiState.update { it.copy(weight = weight, weightError = null) }
    }

    fun onHeightChanged(height: String) {
        _uiState.update { it.copy(height = height, heightError = null) }
    }

    fun onGoalSelected(goal: String) {
        _uiState.update { it.copy(selectedGoal = goal) }
    }

    fun onLevelSelected(level: String) {
        _uiState.update { it.copy(selectedLevel = level) }
    }

    fun onInjuryToggled(injury: String) {
        _uiState.update { state ->
            val injuries = state.selectedInjuries.toMutableSet()
            if (injury == "Нет травм") {
                // Если выбрано «Нет травм» — сбрасываем остальные
                if (injuries.contains("Нет травм")) {
                    injuries.clear()
                } else {
                    injuries.clear()
                    injuries.add("Нет травм")
                }
            } else {
                injuries.remove("Нет травм")
                if (injuries.contains(injury)) {
                    injuries.remove(injury)
                } else {
                    injuries.add(injury)
                }
            }
            state.copy(selectedInjuries = injuries)
        }
    }

    fun onEquipmentToggled(item: String) {
        _uiState.update { state ->
            val equipment = state.selectedEquipment.toMutableSet()
            if (item == "Нет инвентаря") {
                if (equipment.contains("Нет инвентаря")) {
                    equipment.clear()
                } else {
                    equipment.clear()
                    equipment.add("Нет инвентаря")
                }
            } else {
                equipment.remove("Нет инвентаря")
                if (equipment.contains(item)) {
                    equipment.remove(item)
                } else {
                    equipment.add(item)
                }
            }
            state.copy(selectedEquipment = equipment)
        }
    }

    fun nextStep() {
        val state = _uiState.value

        // Валидация текущего шага
        when (state.currentStep) {
            OnboardingStep.ANTHROPOMETRY -> {
                val weight = state.weight.toFloatOrNull()
                val height = state.height.toFloatOrNull()
                if (weight == null || weight <= 0) {
                    _uiState.update { it.copy(weightError = "Введите корректный вес") }
                    return
                }
                if (height == null || height <= 0) {
                    _uiState.update { it.copy(heightError = "Введите корректный рост") }
                    return
                }
            }
            OnboardingStep.GOAL -> {
                if (state.selectedGoal == null) return
            }
            OnboardingStep.LEVEL -> {
                if (state.selectedLevel == null) return
            }
            OnboardingStep.INJURIES -> {
                // Травмы — опционально, можно пропускать
            }
            OnboardingStep.EQUIPMENT -> return // Последний шаг
        }

        val nextIndex = state.currentStep.index + 1
        if (nextIndex < OnboardingStep.entries.size) {
            _uiState.update { it.copy(currentStep = OnboardingStep.entries[nextIndex]) }
        }
    }

    private fun goalToApiValue(goal: String) = when (goal) {
        "Похудение" -> "weight_loss"
        "Набор мышечной массы" -> "muscle_gain"
        "Поддержание формы" -> "maintenance"
        "Реабилитация" -> "rehabilitation"
        "Сила и выносливость" -> "strength"
        "Гибкость" -> "flexibility"
        else -> goal
    }

    private fun levelToApiValue(level: String) = when (level) {
        "Новичок" -> "beginner"
        "Любитель" -> "amateur"
        "Средний" -> "intermediate"
        "Продвинутый" -> "advanced"
        "Профессионал" -> "professional"
        else -> level
    }

    fun previousStep() {
        val state = _uiState.value
        val prevIndex = state.currentStep.index - 1
        if (prevIndex >= 0) {
            _uiState.update { it.copy(currentStep = OnboardingStep.entries[prevIndex]) }
        }
    }

    fun submitOnboarding() {
        val state = _uiState.value

        // Валидация последнего шага
        if (state.selectedEquipment.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, apiError = null) }

            try {
                val request = OnboardingRequest(
                    weight = state.weight.toFloat(),
                    height = state.height.toFloat(),
                    goal = goalToApiValue(state.selectedGoal!!),
                    level = levelToApiValue(state.selectedLevel!!),
                    injuries = state.selectedInjuries.filter { it != "Нет травм" },
                    equipment = state.selectedEquipment.filter { it != "Нет инвентаря" },
                    medical_disclaimer_accepted = true
                )
                val response = profileApi.submitOnboarding(request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, isComplete = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            apiError = "Ошибка сохранения данных (${response.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        apiError = "Ошибка сети. Проверьте подключение"
                    )
                }
            }
        }
    }
}
