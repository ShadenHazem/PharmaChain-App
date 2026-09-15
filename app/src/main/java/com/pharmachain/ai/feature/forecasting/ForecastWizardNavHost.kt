package com.pharmachain.ai.feature.forecasting

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

object WizardRoutes {
    const val UPLOAD = "wizard_upload"
    const val RAW_PREVIEW = "wizard_raw_preview"
    const val MANUAL_ENTRY = "wizard_manual_entry"
    const val MAPPING = "wizard_mapping"
    const val VALIDATION = "wizard_validation"
    const val CONSTRAINTS = "wizard_constraints"
    const val RESULTS = "wizard_results"
    const val SMART_CART = "wizard_smart_cart"
}

@Composable
fun ForecastWizardNavHost(
    wizardViewModel: ForecastWizardViewModel,
    onHandoffToOrders: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val uiState by wizardViewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Handle system back gesture/button, disabled on Upload (step 1) and during active processing
    val canGoBack = currentRoute != null && currentRoute != WizardRoutes.UPLOAD && !uiState.isProcessing

    BackHandler(enabled = canGoBack) {
        navController.popBackStack()
    }

    NavHost(
        navController = navController,
        startDestination = WizardRoutes.UPLOAD,
        modifier = modifier.fillMaxSize()
    ) {
        composable(WizardRoutes.UPLOAD) {
            FileUploadScreen(
                isProcessing = uiState.isProcessing,
                onFileUploadSelected = { parsedFile ->
                    wizardViewModel.handleFileUpload(parsedFile) {
                        navController.navigate(WizardRoutes.RAW_PREVIEW)
                    }
                },
                onStartManualEntry = {
                    wizardViewModel.startManualEntry()
                    navController.navigate(WizardRoutes.MANUAL_ENTRY)
                }
            )
        }

        composable(WizardRoutes.RAW_PREVIEW) {
            RawPreviewScreen(
                parsedFile = uiState.parsedFile,
                isProcessing = uiState.isProcessing,
                onProceedToMapping = {
                    wizardViewModel.proceedToMapping {
                        navController.navigate(WizardRoutes.MAPPING)
                    }
                },
                onBackToUpload = {
                    navController.popBackStack()
                }
            )
        }

        composable(WizardRoutes.MANUAL_ENTRY) {
            ManualEntryScreen(
                manualEntries = uiState.manualEntries,
                onAddOrUpdateEntry = { medId, brand, gen, cat, qty ->
                    wizardViewModel.addOrUpdateManualEntry(medId, brand, gen, cat, qty)
                },
                onRemoveEntry = { medId ->
                    wizardViewModel.removeManualEntry(medId)
                },
                onProceedToConstraints = {
                    wizardViewModel.proceedFromManualToConstraints {
                        navController.navigate(WizardRoutes.CONSTRAINTS)
                    }
                },
                onBackToUpload = {
                    navController.popBackStack()
                }
            )
        }

        composable(WizardRoutes.MAPPING) {
            ColumnMappingScreen(
                mappings = uiState.mappings,
                isProcessing = uiState.isProcessing,
                onMappingChanged = { col, field ->
                    wizardViewModel.updateColumnMapping(col, field)
                },
                onConfirmMappings = {
                    wizardViewModel.confirmMappingsAndValidate {
                        navController.navigate(WizardRoutes.VALIDATION)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(WizardRoutes.VALIDATION) {
            DataValidationScreen(
                report = uiState.validationReport,
                isValidationStale = uiState.isValidationStale,
                onProceed = {
                    wizardViewModel.proceedToConstraints {
                        navController.navigate(WizardRoutes.CONSTRAINTS)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRevalidate = {
                    wizardViewModel.confirmMappingsAndValidate()
                }
            )
        }

        composable(WizardRoutes.CONSTRAINTS) {
            ForecastConstraintsScreen(
                duration = uiState.duration,
                maxBudgetEgp = uiState.maxBudgetEgp,
                accountForSeasonality = uiState.accountForSeasonality,
                isProcessing = uiState.isProcessing,
                onDurationChanged = { wizardViewModel.setDuration(it) },
                onBudgetChanged = { wizardViewModel.setMaxBudget(it) },
                onSeasonalityChanged = { wizardViewModel.setSeasonality(it) },
                onRunForecast = {
                    wizardViewModel.runForecastJob {
                        navController.navigate(WizardRoutes.RESULTS)
                    }
                },
                onNavigateBack = {
                    if (!uiState.isProcessing) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(WizardRoutes.RESULTS) {
            ForecastResultsScreen(
                results = uiState.forecastResults,
                isProcessing = uiState.isProcessing,
                onGenerateSmartCart = {
                    wizardViewModel.generateSmartCart {
                        navController.navigate(WizardRoutes.SMART_CART)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(WizardRoutes.SMART_CART) {
            SmartCartScreen(
                items = uiState.smartCartItems,
                maxBudgetEgp = uiState.maxBudgetEgp,
                isProcessing = uiState.isProcessing,
                onToggleItem = { wizardViewModel.toggleCartItem(it) },
                onUpdateQuantity = { id, qty -> wizardViewModel.updateCartItemQuantity(id, qty) },
                onPlaceOrders = {
                    wizardViewModel.placeSmartCartOrders {
                        navController.navigate(WizardRoutes.UPLOAD) {
                            popUpTo(WizardRoutes.UPLOAD) { inclusive = true }
                        }
                        onHandoffToOrders()
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

