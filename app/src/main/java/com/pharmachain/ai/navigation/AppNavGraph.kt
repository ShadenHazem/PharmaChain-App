package com.pharmachain.ai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pharmachain.ai.PharmaChainApp
import com.pharmachain.ai.core.common.locale.AppLanguage
import com.pharmachain.ai.core.common.session.SessionManager
import com.pharmachain.ai.core.designsystem.components.LoadingIndicator
import com.pharmachain.ai.core.model.Role
import com.pharmachain.ai.data.repository.AuthRepository
import com.pharmachain.ai.data.repository.CatalogRepository
import com.pharmachain.ai.data.repository.DistributorRepository
import com.pharmachain.ai.data.repository.ForecastRepository
import com.pharmachain.ai.data.repository.OrdersRepository
import com.pharmachain.ai.feature.admin.AdminDashboardScreen
import com.pharmachain.ai.feature.admin.AdminDashboardViewModel
import com.pharmachain.ai.feature.assistant.ui.AssistantScreen
import com.pharmachain.ai.feature.assistant.ui.AssistantViewModel
import com.pharmachain.ai.feature.auth.AuthViewModel
import com.pharmachain.ai.feature.auth.LoginScreen
import com.pharmachain.ai.feature.auth.RegisterScreen
import com.pharmachain.ai.feature.auth.RoleSelectionScreen
import com.pharmachain.ai.feature.cart.CartViewModel
import com.pharmachain.ai.feature.catalog.CatalogViewModel
import com.pharmachain.ai.feature.distributor_portal.DistributorDashboardScaffold
import com.pharmachain.ai.feature.distributor_portal.DistributorViewModel
import com.pharmachain.ai.feature.forecasting.ForecastWizardViewModel
import com.pharmachain.ai.feature.language.FirstLaunchLanguageScreen
import com.pharmachain.ai.feature.orders.OrderDetailScreen
import com.pharmachain.ai.feature.orders.OrdersViewModel
import com.pharmachain.ai.feature.pharmacist_dashboard.OverviewViewModel
import com.pharmachain.ai.feature.pharmacist_dashboard.PharmacistDashboardScaffold
import com.pharmachain.ai.feature.settings.SettingsScreen

@Composable
fun AppNavGraph(
    app: PharmaChainApp,
    currentLanguage: AppLanguage = AppLanguage.DEFAULT,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val sessionManager = app.sessionManager
    val languagePreferences = app.languagePreferences
    val authRepo = app.authRepository
    val catalogRepo = app.catalogRepository
    val ordersRepo = app.ordersRepository
    val forecastRepo = app.forecastRepository
    val distributorRepo = app.distributorRepository

    val authViewModel = remember { AuthViewModel(authRepo, sessionManager) }
    val overviewViewModel = remember { OverviewViewModel(ordersRepo) }
    val catalogViewModel = remember { CatalogViewModel(catalogRepo, ordersRepo) }
    val cartViewModel = remember { CartViewModel(ordersRepo) }
    val ordersViewModel = remember { OrdersViewModel(ordersRepo) }
    val forecastViewModel = remember { ForecastWizardViewModel(forecastRepo, catalogRepo, ordersRepo) }
    val distributorViewModel = remember { DistributorViewModel(distributorRepo) }
    val assistantViewModel = remember { AssistantViewModel(app.assistantRepository, sessionManager) }
    val adminViewModel = remember { AdminDashboardViewModel(app.adminRepository) }

    val isLanguageOnboardingCompleted by languagePreferences.isLanguageOnboardingCompletedFlow.collectAsStateWithLifecycle(
        initialValue = null
    )

    // While resolving preferences on first launch
    if (isLanguageOnboardingCompleted == null) {
        LoadingIndicator(message = "Loading…")
        return
    }

    // Start destination based on language onboarding & session
    val currentUser = sessionManager.currentUser.value
    val startDestination = when {
        isLanguageOnboardingCompleted == false -> Routes.FIRST_LAUNCH_LANGUAGE
        currentUser == null -> Routes.ROLE_SELECTION
        currentUser.role == Role.PHARMACIST -> Routes.PHARMACIST_GRAPH
        currentUser.role == Role.ADMIN -> Routes.ADMIN_DASHBOARD
        else -> Routes.DISTRIBUTOR_GRAPH
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // First-Launch Language Selection Prompt
        composable(Routes.FIRST_LAUNCH_LANGUAGE) {
            FirstLaunchLanguageScreen(
                languagePreferences = languagePreferences,
                onLanguageSelected = {
                    navController.navigate(Routes.ROLE_SELECTION) {
                        popUpTo(Routes.FIRST_LAUNCH_LANGUAGE) { inclusive = true }
                    }
                }
            )
        }

        // Shared Settings & Language Screen
        composable(Routes.SETTINGS) {
            SettingsScreen(
                languagePreferences = languagePreferences,
                sessionManager = sessionManager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAdmin = { navController.navigate(Routes.ADMIN_DASHBOARD) }
            )
        }

        // Role Selection
        composable(Routes.ROLE_SELECTION) {
            RoleSelectionScreen(
                viewModel = authViewModel,
                onRoleConfirmed = { role ->
                    authViewModel.selectRole(role)
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        // Login Screen
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAuthenticated = { user ->
                    when (user.role) {
                        Role.PHARMACIST -> navController.navigate(Routes.PHARMACIST_GRAPH) {
                            popUpTo(Routes.ROLE_SELECTION) { inclusive = true }
                        }
                        Role.ADMIN -> navController.navigate(Routes.ADMIN_DASHBOARD) {
                            popUpTo(Routes.ROLE_SELECTION) { inclusive = true }
                        }
                        Role.DISTRIBUTOR -> navController.navigate(Routes.DISTRIBUTOR_GRAPH) {
                            popUpTo(Routes.ROLE_SELECTION) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Register Screen
        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAuthenticated = { user ->
                    when (user.role) {
                        Role.PHARMACIST -> navController.navigate(Routes.PHARMACIST_GRAPH) {
                            popUpTo(Routes.ROLE_SELECTION) { inclusive = true }
                        }
                        Role.ADMIN -> navController.navigate(Routes.ADMIN_DASHBOARD) {
                            popUpTo(Routes.ROLE_SELECTION) { inclusive = true }
                        }
                        Role.DISTRIBUTOR -> navController.navigate(Routes.DISTRIBUTOR_GRAPH) {
                            popUpTo(Routes.ROLE_SELECTION) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Pharmacist Dashboard Graph
        composable(Routes.PHARMACIST_GRAPH) {
            PharmacistDashboardScaffold(
                overviewViewModel = overviewViewModel,
                catalogViewModel = catalogViewModel,
                cartViewModel = cartViewModel,
                ordersViewModel = ordersViewModel,
                forecastWizardViewModel = forecastViewModel,
                onNavigateToOrderDetail = { orderId ->
                    navController.navigate("order_detail/$orderId")
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToAssistant = {
                    navController.navigate(Routes.ASSISTANT)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.ROLE_SELECTION) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Pharmacist AI Assistant Screen
        composable(Routes.ASSISTANT) {
            AssistantScreen(
                viewModel = assistantViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Order Detail Screen
        composable(
            route = Routes.ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(
                orderId = orderId,
                viewModel = ordersViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Distributor Portal Graph
        composable(Routes.DISTRIBUTOR_GRAPH) {
            DistributorDashboardScaffold(
                viewModel = distributorViewModel,
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.ROLE_SELECTION) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Admin Control Tower
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                viewModel = adminViewModel,
                onNavigateBack = {
                    authViewModel.logout()
                    navController.navigate(Routes.ROLE_SELECTION) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
