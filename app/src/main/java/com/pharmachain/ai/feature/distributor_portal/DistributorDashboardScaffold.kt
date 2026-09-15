package com.pharmachain.ai.feature.distributor_portal

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pharmachain.ai.R
import com.pharmachain.ai.core.designsystem.components.PharmaChainLogoMark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorDashboardScaffold(
    viewModel: DistributorViewModel,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message, uiState.errorMessage) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PharmaChainLogoMark(size = 34.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = uiState.companyName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.distributor_badge, uiState.reputationScore),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("distributor_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("distributor_logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = stringResource(R.string.logout),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // Tab 1: Overview
                NavigationBarItem(
                    selected = uiState.selectedTab == DistributorTab.OVERVIEW,
                    onClick = { viewModel.selectTab(DistributorTab.OVERVIEW) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = stringResource(R.string.dist_tab_overview)) },
                    label = { Text(stringResource(R.string.dist_tab_overview)) },
                    modifier = Modifier.testTag("dist_tab_overview_btn")
                )

                // Tab 2: Inventory
                NavigationBarItem(
                    selected = uiState.selectedTab == DistributorTab.INVENTORY,
                    onClick = { viewModel.selectTab(DistributorTab.INVENTORY) },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = stringResource(R.string.dist_tab_inventory)) },
                    label = { Text(stringResource(R.string.dist_tab_inventory)) },
                    modifier = Modifier.testTag("dist_tab_inventory_btn")
                )

                // Tab 3: Orders
                NavigationBarItem(
                    selected = uiState.selectedTab == DistributorTab.ORDERS,
                    onClick = { viewModel.selectTab(DistributorTab.ORDERS) },
                    icon = {
                        if (uiState.pendingApprovalCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Text("${uiState.pendingApprovalCount}")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = stringResource(R.string.dist_tab_orders))
                            }
                        } else {
                            Icon(Icons.Default.ReceiptLong, contentDescription = stringResource(R.string.dist_tab_orders))
                        }
                    },
                    label = { Text(stringResource(R.string.dist_tab_orders)) },
                    modifier = Modifier.testTag("dist_tab_orders_btn")
                )

                // Tab 4: Integrations
                NavigationBarItem(
                    selected = uiState.selectedTab == DistributorTab.INTEGRATIONS,
                    onClick = { viewModel.selectTab(DistributorTab.INTEGRATIONS) },
                    icon = {
                        if (uiState.conflictCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Text("${uiState.conflictCount}")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = stringResource(R.string.dist_tab_integrations))
                            }
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = stringResource(R.string.dist_tab_integrations))
                        }
                    },
                    label = { Text(stringResource(R.string.dist_tab_integrations)) },
                    modifier = Modifier.testTag("dist_tab_integrations_btn")
                )

                // Tab 5: Commission
                NavigationBarItem(
                    selected = uiState.selectedTab == DistributorTab.COMMISSION,
                    onClick = { viewModel.selectTab(DistributorTab.COMMISSION) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = stringResource(R.string.dist_tab_ledger)) },
                    label = { Text(stringResource(R.string.dist_tab_ledger)) },
                    modifier = Modifier.testTag("dist_tab_commission_btn")
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Crossfade(
            targetState = uiState.selectedTab,
            label = "DistributorTabCrossfade",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tab ->
            when (tab) {
                DistributorTab.OVERVIEW -> DistributorOverviewTab(
                    analytics = uiState.analytics,
                    selectedPeriodDays = uiState.selectedPeriodDays,
                    lowStockAlerts = uiState.lowStockAlerts,
                    onSelectPeriod = { viewModel.selectAnalyticsPeriod(it) },
                    onDismissAlert = { viewModel.dismissLowStockAlert(it) },
                    onNavigateToInventory = { viewModel.selectTab(DistributorTab.INVENTORY) },
                    onNavigateToOrders = { viewModel.selectTab(DistributorTab.ORDERS) },
                    pendingApprovalCount = uiState.pendingApprovalCount
                )

                DistributorTab.INVENTORY -> DistributorInventoryTab(
                    listings = uiState.listings,
                    listingSources = uiState.listingSources,
                    searchQuery = uiState.inventorySearchQuery,
                    onSearchQueryChange = { viewModel.setInventorySearchQuery(it) },
                    onUpdateListing = { id, price, stock -> viewModel.updateListingPriceStock(id, price, stock) },
                    onNavigateToUpload = {
                        viewModel.selectTab(DistributorTab.INTEGRATIONS)
                        viewModel.setIntegrationSubTab(IntegrationSubTab.EXCEL_UPLOAD)
                    }
                )

                DistributorTab.ORDERS -> DistributorOrdersTab(
                    orders = uiState.incomingOrders,
                    selectedStatusFilter = uiState.selectedOrderStatusFilter,
                    onStatusFilterSelected = { viewModel.setOrderStatusFilter(it) },
                    onApproveOrder = { viewModel.approveOrder(it) },
                    onPromptRejectOrder = { viewModel.promptRejectOrder(it) },
                    onAdvanceStatus = { id, status -> viewModel.advanceOrderStatus(id, status) },
                    orderToReject = uiState.orderToReject,
                    rejectionReasonText = uiState.rejectionReasonText,
                    onRejectionReasonChange = { viewModel.setRejectionReasonText(it) },
                    onConfirmReject = { viewModel.confirmRejectOrder() },
                    onDismissRejectDialog = { viewModel.dismissRejectDialog() }
                )

                DistributorTab.INTEGRATIONS -> DistributorIntegrationsTab(
                    state = uiState,
                    onSubTabSelected = { viewModel.setIntegrationSubTab(it) },
                    onLoadSampleExcel = { viewModel.loadSampleExcelFile() },
                    onUploadFileSelected = { fileName, lines -> viewModel.processSelectedFile(fileName, lines) },
                    onColumnMappingChanged = { header, field -> viewModel.updateColumnMapping(header, field) },
                    onValidateMappings = { viewModel.validateColumnMappings() },
                    onApplyUpload = { viewModel.applyInventoryUpload() },
                    onResetExcelFlow = { viewModel.resetExcelUploadFlow() },
                    onOpenApiConfig = { viewModel.openApiConfigDialog() },
                    onCloseApiConfig = { viewModel.closeApiConfigDialog() },
                    onApiProviderNameChange = { viewModel.updateApiProviderName(it) },
                    onApiBaseUrlChange = { viewModel.updateApiBaseUrl(it) },
                    onApiAuthTypeChange = { viewModel.updateApiAuthType(it) },
                    onApiSecretChange = { viewModel.updateApiSecret(it) },
                    onApiSyncFrequencyChange = { viewModel.updateApiSyncFrequency(it) },
                    onTestApiConnection = { viewModel.testApiConnection() },
                    onSaveApiIntegration = { viewModel.saveApiIntegration() },
                    onTriggerSyncNow = { viewModel.triggerSyncNow() },
                    onResolveConflict = { item, action -> viewModel.resolveConflict(item, action) },
                    onDisconnectApi = { viewModel.disconnectApiIntegration() }
                )

                DistributorTab.COMMISSION -> DistributorCommissionTab(
                    ledger = uiState.commissionLedger,
                    commissionRate = uiState.commissionRatePct,
                    reputationScore = uiState.reputationScore,
                    fulfillmentRate = uiState.fulfillmentRatePct,
                    onTimeDelivery = uiState.onTimeDeliveryPct
                )
            }
        }
    }
}
