package com.example.jansaarthi.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.jansaarthi.App
import com.example.jansaarthi.ui.components.AppBottomNavigation
import com.example.jansaarthi.ui.screens.splash.SplashScreen
import com.example.jansaarthi.ui.screens.onboarding.OnboardingScreen
import com.example.jansaarthi.ui.screens.home.HomeScreen
import com.example.jansaarthi.ui.screens.schemes.SchemesScreen
import com.example.jansaarthi.ui.screens.calculator.CalculatorScreen
import com.example.jansaarthi.ui.screens.partners.PartnersScreen
import com.example.jansaarthi.ui.screens.profile.*
import com.example.jansaarthi.ui.screens.rag.RagScreen
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
    const val PROFILE_BASIC = "profile_basic"
    const val PROFILE_CATEGORY_INCOME = "profile_category_income"
    const val PROFILE_BUSINESS_CREDIT = "profile_business_credit"
    const val PROFILE_IDENTITY_VERIFICATION = "profile_identity_verification"
    const val PROFILE_BUSINESS = "profile_business"
    const val PROFILE_CATEGORY = "profile_category"
    const val PROFILE_LANGUAGE = "profile_language"
    const val PROFILE_NOTIFICATIONS = "profile_notifications"
    const val PROFILE_SUPPORT = "profile_support"
    
    // Feature Routes
    const val REQUIREMENT = "requirement"
    const val RAG_QUERY = "rag_query"
    const val SCHEME_DETAILS = "scheme_details"
    const val RECOMMENDATIONS = "recommendations"
    const val ELIGIBILITY = "eligibility"
    const val PARTNER_DETAILS = "partner_details"
    const val DOCUMENTS = "documents"
    const val ACTION_PLAN = "action_plan"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.SPLASH
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isLoggedIn = App.instance.authRepository.isLoggedIn()
    val bottomBarRoutes = listOf(
        Routes.HOME, Routes.SCHEMES, Routes.CALCULATOR, Routes.PARTNERS
    )
    val showBottomBar = isLoggedIn && (currentRoute in bottomBarRoutes)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(navController = navController, currentRoute = currentRoute ?: Routes.HOME)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
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
                        val authStatus = App.instance.authRepository.isLoggedIn()
                        navController.navigate(if (authStatus) Routes.HOME else Routes.LOGIN) {
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
                AuthGuard(navController) { HomeScreen(navController) }
            }
            composable(Routes.SCHEMES) {
                AuthGuard(navController) { SchemesScreen(navController) }
            }
            composable(Routes.CALCULATOR) {
                AuthGuard(navController) { CalculatorScreen(navController) }
            }
            composable(Routes.PARTNERS) {
                AuthGuard(navController) { PartnersScreen(navController) }
            }
            composable(Routes.PROFILE) {
                AuthGuard(navController) { ProfileScreen(navController) }
            }
            composable(Routes.PROFILE_BASIC) {
                AuthGuard(navController) { BasicDetailsScreen(navController) }
            }
            composable(Routes.PROFILE_CATEGORY_INCOME) {
                AuthGuard(navController) { CategoryIncomeScreen(navController) }
            }
            composable(Routes.PROFILE_BUSINESS_CREDIT) {
                AuthGuard(navController) { BusinessCreditScreen(navController) }
            }
            composable(Routes.PROFILE_IDENTITY_VERIFICATION) {
                AuthGuard(navController) { IdentityVerificationScreen(navController) }
            }
            composable(Routes.PROFILE_SUPPORT) {
                AuthGuard(navController) { com.example.jansaarthi.ui.screens.PlaceholderScreen(navController, "Help & Support") }
            }
                                                                        
            composable(Routes.REQUIREMENT) {
                AuthGuard(navController) {
                    RequirementScreen(
                        onBack = { navController.popBackStack() },
                        onContinue = { navController.navigate(Routes.RECOMMENDATIONS) },
                        onAskAiClick = { navController.navigate(Routes.RAG_QUERY) }
                    )
                }
            }

            composable(Routes.RAG_QUERY) {
                AuthGuard(navController) {
                    RagScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Routes.SCHEME_DETAILS) {
                AuthGuard(navController) { com.example.jansaarthi.ui.screens.PlaceholderScreen(navController, "Scheme Details") }
            }
            composable(Routes.RECOMMENDATIONS) {
                AuthGuard(navController) { com.example.jansaarthi.ui.screens.PlaceholderScreen(navController, "Recommendations", Routes.ELIGIBILITY) }
            }
            composable(Routes.ELIGIBILITY) {
                AuthGuard(navController) { com.example.jansaarthi.ui.screens.PlaceholderScreen(navController, "Eligibility", Routes.CALCULATOR) }
            }
            composable(Routes.PARTNER_DETAILS) {
                AuthGuard(navController) { com.example.jansaarthi.ui.screens.PlaceholderScreen(navController, "Partner Details", Routes.DOCUMENTS) }
            }
            composable(Routes.DOCUMENTS) {
                AuthGuard(navController) { com.example.jansaarthi.ui.screens.PlaceholderScreen(navController, "Documents", Routes.ACTION_PLAN) }
            }
            composable(Routes.ACTION_PLAN) {
                AuthGuard(navController) { com.example.jansaarthi.ui.screens.PlaceholderScreen(navController, "Action Plan", Routes.HOME) }
            }
        }
    }
}

/**
 * Navigation guard ensuring unauthenticated users cannot access protected screens.
 * If user is not authenticated, redirects directly to Login and clears backstack.
 */
@Composable
private fun AuthGuard(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val isLoggedIn = App.instance.authRepository.isLoggedIn()
    if (!isLoggedIn) {
        LaunchedEffect(Unit) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    } else {
        content()
    }
}
