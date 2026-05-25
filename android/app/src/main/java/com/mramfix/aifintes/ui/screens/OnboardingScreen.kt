package com.mramfix.aifintes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Онбординг", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Мультишаговая форма (реализуется в AN-8)")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onOnboardingComplete) {
            Text("Завершить онбординг (заглушка)")
        }
    }
}
