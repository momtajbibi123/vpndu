package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.VpnViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: VpnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase dynamically with the provided project configurations
        try {
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setApiKey("AIzaSyDy5ssDCpw2KKxWYQcMKpCrANzwJNBVSqE")
                .setApplicationId("1:741705555888:web:2cac1199c8b0981ca80075")
                .setDatabaseUrl("https://labour-attendance-e4b6c-default-rtdb.firebaseio.com")
                .setProjectId("labour-attendance-e4b6c")
                .setStorageBucket("labour-attendance-e4b6c.firebasestorage.app")
                .build()

            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
                val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Navigation Host
                    NavHost(
                        navController = navController,
                        startDestination = "SPLASH",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. Splash Screen
                        composable("SPLASH") {
                            SplashScreen(
                                viewModel = viewModel,
                                onSplashFinished = {
                                    if (onboardingCompleted) {
                                        if (isUserLoggedIn) {
                                            navController.navigate("HOME") {
                                                popUpTo("SPLASH") { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate("AUTH") {
                                                popUpTo("SPLASH") { inclusive = true }
                                            }
                                        }
                                    } else {
                                        navController.navigate("ONBOARDING") {
                                            popUpTo("SPLASH") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        // 2. Onboarding Screen
                        composable("ONBOARDING") {
                            OnboardingScreen(
                                viewModel = viewModel,
                                onOnboardingFinished = {
                                    navController.navigate("AUTH") {
                                        popUpTo("ONBOARDING") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Auth (Login/Signup) Screen
                        composable("AUTH") {
                            AuthScreen(
                                viewModel = viewModel,
                                onAuthSuccess = {
                                    navController.navigate("HOME") {
                                        popUpTo("AUTH") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 4. Home Screen
                        composable("HOME") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToServerList = { navController.navigate("SERVERS") },
                                onNavigateToPaywall = { navController.navigate("PAYWALL") },
                                onNavigateToStats = { navController.navigate("STATS") },
                                onNavigateToSettings = { navController.navigate("SETTINGS") }
                            )
                        }

                        // 5. Server List Screen
                        composable("SERVERS") {
                            ServerListScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToPaywall = { navController.navigate("PAYWALL") }
                            )
                        }

                        // 6. Paywall Screen
                        composable("PAYWALL") {
                            PaywallScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 7. Settings Screen
                        composable("SETTINGS") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToPaywall = { navController.navigate("PAYWALL") }
                            )
                        }

                        // 8. Stats / Account / Notifications Screen
                        composable("STATS") {
                            StatsAndMoreScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToPaywall = { navController.navigate("PAYWALL") },
                                onLogoutSuccess = {
                                    navController.navigate("AUTH") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
