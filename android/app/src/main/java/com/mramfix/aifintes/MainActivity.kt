package com.mramfix.aifintes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mramfix.aifintes.ui.screens.HomeScreen
import com.mramfix.aifintes.ui.screens.LoginScreen
import com.mramfix.aifintes.ui.screens.OnboardingScreen
import com.mramfix.aifintes.ui.screens.RegisterScreen
import com.mramfix.aifintes.ui.theme.AIFintesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIFintesTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel = hiltViewModel()) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val navController = rememberNavController()

    // Выброс на регистрацию при logout
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == false) {
            navController.navigate("register") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    when (isLoggedIn) {
        null -> Box(modifier = Modifier.fillMaxSize())
        else -> {
            val startDestination = if (isLoggedIn == true) "home" else "register"
            NavHost(navController = navController, startDestination = startDestination) {
                composable("register") {
                    RegisterScreen(
                        onNavigateToLogin = {
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                            }
                        },
                        onRegisterSuccess = {
                            navController.navigate("onboarding") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    )
                }
                composable("login") {
                    LoginScreen(
                        onNavigateToRegister = {
                            navController.navigate("register") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onLoginSuccess = {
                            navController.navigate("home") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable("onboarding") {
                    OnboardingScreen(
                        onOnboardingComplete = {
                            navController.navigate("home") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable("home") {
                    HomeScreen()
                }
            }
        }
    }
}
