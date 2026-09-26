package com.example.jansaarthi.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.jansaarthi.ui.components.AppBottomNavigation
import com.example.jansaarthi.ui.screens.splash.SplashScreen
import com.example.jansaarthi.ui.screens.onboarding.OnboardingScreen
import com.example.jansaarthi.ui.screens.home.HomeScreen
import com.example.jansaarthi.ui.screens.schemes.SchemesScreen
import com.example.jansaarthi.ui.screens.calculator.CalculatorScreen
import com.example.jansaarthi.ui.screens.partners.PartnersScreen
import com.example.jansaarthi.ui.screens.profile.*
import com.example.jansaarthi.ui.screens.requirement.RequirementScreen

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    
    // Auth Routes
    const val LOGIN = "login"
    const val REGISTER = "register"
    
    // Bottom Nav Routes
    const val HOME = "home"
    const val SCHEMES = "schemes"
    const val CALCULATOR = "calculator"
    const val PARTNERS = "partners"
    const val PROFILE = "profile"
    
    // Profile Sub-Routes
    const val PROFILE_KYC = "profile_kyc"
    const val PROFILE_BUSINESS = "profile_business"
    const val PROFILE_CATEGORY = "profile_category"
    const val PROFILE_LANGUAGE = "profile_language"
    const val PROFILE_NOTIFICATIONS = "profile_notifications"
    const val PROFILE_SUPPORT = "profile_support"
    
    // Feature Routes
    const val REQUIREMENT = "requirement"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Routes.HOME, Routes.SCHEMES, Routes.CALCULATOR, Routes.PARTNERS
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(navController = navController, currentRoute = currentRoute ?: Routes.HOME)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        val isLoggedIn = com.example.jansaarthi.App.instance.authRepository.isLoggedIn()
                        navController.navigate(if (isLoggedIn) Routes.HOME else Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.LOGIN) {
                com.example.jansaarthi.ui.screens.auth.LoginScreen(
                    onNavigateToHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER)
                    }
                )
            }

            composable(Routes.REGISTER) {
                com.example.jansaarthi.ui.screens.auth.RegisterScreen(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Routes.HOME) {
                HomeScreen(navController)
            }
            composable(Routes.SCHEMES) {
                SchemesScreen(navController)
            }
            composable(Routes.CALCULATOR) {
                CalculatorScreen(navController)
            }
            composable(Routes.PARTNERS) {
                PartnersScreen(navController)
            }
            composable(Routes.PROFILE) {
                ProfileScreen(navController)
            }
            composable(Routes.PROFILE_KYC) { KycStatusScreen(navController) }
            composable(Routes.PROFILE_BUSINESS) { BusinessDetailsScreen(navController) }
            composable(Routes.PROFILE_CATEGORY) { SocialCategoryScreen(navController) }
            composable(Routes.PROFILE_LANGUAGE) { LanguageSelectionScreen(navController) }
            composable(Routes.PROFILE_NOTIFICATIONS) { NotificationSettingsScreen(navController) }
            composable(Routes.PROFILE_SUPPORT) { HelpSupportScreen(navController) }
            
            composable(Routes.REQUIREMENT) {
                RequirementScreen(
                    onBack = { navController.popBackStack() },
                    onContinue = { /* Navigate to profile verification */ }
                )
            }
        }
    }
}
