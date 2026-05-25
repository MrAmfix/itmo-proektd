package com.mramfix.aifintes.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mramfix.aifintes.data.api.ProfileResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мой профиль", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = viewModel::logout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Выйти"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> ProfileContent(
                    profile = uiState.profile,
                    onLogout = viewModel::logout
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(profile: ProfileResponse?, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (profile == null) {
            Text("Данные профиля не найдены", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            // Антропометрия
            ProfileSection(title = "Параметры тела") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    profile.weight?.let {
                        StatChip(label = "Вес", value = "${it.toInt()} кг", modifier = Modifier.weight(1f))
                    }
                    profile.height?.let {
                        StatChip(label = "Рост", value = "${it.toInt()} см", modifier = Modifier.weight(1f))
                    }
                }
            }

            // Цель
            profile.goal?.let {
                ProfileSection(title = "Цель тренировок") {
                    Text(
                        text = goalToRussian(it),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Уровень
            profile.level?.let {
                ProfileSection(title = "Уровень подготовки") {
                    Text(
                        text = levelToRussian(it),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Травмы
            if (!profile.injuries.isNullOrEmpty()) {
                ProfileSection(title = "Травмы") {
                    profile.injuries.forEach { injury ->
                        Text("• $injury", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Инвентарь
            if (!profile.equipment.isNullOrEmpty()) {
                ProfileSection(title = "Инвентарь") {
                    profile.equipment.forEach { item ->
                        Text("• $item", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Выйти из аккаунта")
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun goalToRussian(goal: String) = when (goal) {
    "weight_loss" -> "Похудение"
    "muscle_gain" -> "Набор мышечной массы"
    "maintenance" -> "Поддержание формы"
    "rehabilitation" -> "Реабилитация"
    "strength" -> "Сила и выносливость"
    "flexibility" -> "Гибкость"
    else -> goal
}

private fun levelToRussian(level: String) = when (level) {
    "beginner" -> "Новичок"
    "amateur" -> "Любитель"
    "intermediate" -> "Средний"
    "advanced" -> "Продвинутый"
    "professional" -> "Профессионал"
    else -> level
}
