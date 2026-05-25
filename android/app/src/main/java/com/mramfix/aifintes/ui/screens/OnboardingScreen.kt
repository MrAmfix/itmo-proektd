package com.mramfix.aifintes.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onOnboardingComplete()
        }
    }

    Scaffold(
        topBar = {
            Column {
                // Прогресс-бар
                LinearProgressIndicator(
                    progress = { uiState.currentStep.index.toFloat() / (OnboardingStep.entries.size - 1).toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                // Заголовок шага
                Text(
                    text = "Шаг ${uiState.currentStep.index + 1} из ${OnboardingStep.entries.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Заголовок шага
            Text(
                text = uiState.currentStep.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (uiState.currentStep) {
                    OnboardingStep.ANTHROPOMETRY -> "Укажите ваши параметры для точного расчёта"
                    OnboardingStep.GOAL -> "Выберите основную цель тренировок"
                    OnboardingStep.LEVEL -> "Оцените свой уровень подготовки"
                    OnboardingStep.INJURIES -> "Укажите травмы (если есть)"
                    OnboardingStep.EQUIPMENT -> "Каким инвентарём располагаете?"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Содержимое шага
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { it / 3 } togetherWith
                    fadeOut() + slideOutHorizontally { -it / 3 }
                },
                label = "step_content"
            ) { step ->
                when (step) {
                    OnboardingStep.ANTHROPOMETRY -> AnthropometryStep(uiState, viewModel)
                    OnboardingStep.GOAL -> GoalStep(uiState, viewModel)
                    OnboardingStep.LEVEL -> LevelStep(uiState, viewModel)
                    OnboardingStep.INJURIES -> InjuriesStep(uiState, viewModel)
                    OnboardingStep.EQUIPMENT -> EquipmentStep(uiState, viewModel)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Ошибка API
            if (uiState.apiError != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.apiError!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Кнопки навигации
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Кнопка «Назад»
                if (uiState.currentStep != OnboardingStep.ANTHROPOMETRY) {
                    OutlinedButton(
                        onClick = viewModel::previousStep,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Назад")
                    }
                }

                // Кнопка «Далее» / «Завершить»
                if (uiState.currentStep == OnboardingStep.EQUIPMENT) {
                    Button(
                        onClick = viewModel::submitOnboarding,
                        enabled = !uiState.isLoading && uiState.selectedEquipment.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Завершить")
                        }
                    }
                } else {
                    val canProceed = when (uiState.currentStep) {
                        OnboardingStep.ANTHROPOMETRY ->
                            uiState.weight.isNotBlank() && uiState.height.isNotBlank()
                        OnboardingStep.GOAL -> uiState.selectedGoal != null
                        OnboardingStep.LEVEL -> uiState.selectedLevel != null
                        OnboardingStep.INJURIES -> true // Опционально
                        OnboardingStep.EQUIPMENT -> false
                    }

                    Button(
                        onClick = viewModel::nextStep,
                        enabled = canProceed,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Далее")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

// === Шаг 1: Антропометрия ===
@Composable
private fun AnthropometryStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.weight,
            onValueChange = viewModel::onWeightChanged,
            label = { Text("Вес (кг)") },
            suffix = { Text("кг") },
            isError = state.weightError != null,
            supportingText = state.weightError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = state.height,
            onValueChange = viewModel::onHeightChanged,
            label = { Text("Рост (см)") },
            suffix = { Text("см") },
            isError = state.heightError != null,
            supportingText = state.heightError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// === Шаг 2: Цель ===
@Composable
private fun GoalStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OnboardingOptions.goals.forEach { goal ->
            Card(
                onClick = { viewModel.onGoalSelected(goal) },
                colors = CardDefaults.cardColors(
                    containerColor = if (state.selectedGoal == goal)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.selectedGoal == goal,
                        onClick = { viewModel.onGoalSelected(goal) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(goal, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

// === Шаг 3: Уровень ===
@Composable
private fun LevelStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OnboardingOptions.levels.forEach { level ->
            Card(
                onClick = { viewModel.onLevelSelected(level) },
                colors = CardDefaults.cardColors(
                    containerColor = if (state.selectedLevel == level)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.selectedLevel == level,
                        onClick = { viewModel.onLevelSelected(level) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(level, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

// === Шаг 4: Травмы ===
@Composable
private fun InjuriesStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Можно выбрать несколько вариантов",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OnboardingOptions.injuries.forEach { injury ->
            Card(
                onClick = { viewModel.onInjuryToggled(injury) },
                colors = CardDefaults.cardColors(
                    containerColor = if (injury in state.selectedInjuries)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = injury in state.selectedInjuries,
                        onCheckedChange = { viewModel.onInjuryToggled(injury) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(injury, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

// === Шаг 5: Инвентарь ===
@Composable
private fun EquipmentStep(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Можно выбрать несколько вариантов",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OnboardingOptions.equipment.forEach { item ->
            Card(
                onClick = { viewModel.onEquipmentToggled(item) },
                colors = CardDefaults.cardColors(
                    containerColor = if (item in state.selectedEquipment)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item in state.selectedEquipment,
                        onCheckedChange = { viewModel.onEquipmentToggled(item) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(item, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
