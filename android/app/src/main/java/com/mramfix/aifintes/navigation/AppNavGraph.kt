package com.mramfix.aifintes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mramfix.aifintes.ui.screens.*

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: NavigationViewModel = hiltViewModel()
) {
    val startDestination by viewModel.startDestination.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onLoginSuccess = {
                    viewModel.onLoginSuccess()
                    navigateAndClear(navController, viewModel.nextDestination())
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    viewModel.onRegisterSuccess()
                    navigateAndClear(navController, viewModel.nextDestination())
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onOnboardingComplete = {
                    viewModel.onOnboardingComplete()
                    navigateAndClear(navController, Routes.HOME)
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}

/**
 * Навигация с очисткой стека (аналог FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK)
 */
private fun navigateAndClear(navController: NavHostController, destination: String) {
    navController.navigate(destination) {
        popUpTo(0) { inclusive = true }
    }
}
