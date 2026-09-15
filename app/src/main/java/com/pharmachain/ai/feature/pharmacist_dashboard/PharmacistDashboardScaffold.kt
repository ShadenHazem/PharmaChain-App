package com.pharmachain.ai.feature.pharmacist_dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pharmachain.ai.R
import com.pharmachain.ai.core.designsystem.components.PharmaChainLogoMark
import com.pharmachain.ai.feature.cart.CartViewModel
import com.pharmachain.ai.feature.catalog.CatalogScreen
import com.pharmachain.ai.feature.catalog.CatalogViewModel
import com.pharmachain.ai.feature.forecasting.ForecastWizardNavHost
import com.pharmachain.ai.feature.forecasting.ForecastWizardViewModel
import com.pharmachain.ai.feature.orders.OrdersScreen
import com.pharmachain.ai.feature.orders.OrdersViewModel

data class PharmacistTabItem(
    val titleRes: Int,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistDashboardScaffold(
    overviewViewModel: OverviewViewModel,
    catalogViewModel: CatalogViewModel,
    cartViewModel: CartViewModel,
    ordersViewModel: OrdersViewModel,
    forecastWizardViewModel: ForecastWizardViewModel,
    onNavigateToOrderDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        PharmacistTabItem(R.string.tab_overview, Icons.Default.Dashboard, "tab_overview_btn"),
        PharmacistTabItem(R.string.tab_catalog, Icons.Default.Storefront, "tab_catalog_btn"),
        PharmacistTabItem(R.string.tab_orders, Icons.Default.ReceiptLong, "tab_orders_btn"),
        PharmacistTabItem(R.string.tab_forecasting, Icons.AutoMirrored.Filled.TrendingUp, "tab_forecasting_btn")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PharmaChainLogoMark(size = 34.dp)
                        Spacer(modifier = Modifier.padding(start = 10.dp))
                        Text(
                            text = stringResource(tabs[selectedTabIndex].titleRes),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToAssistant,
                        modifier = Modifier.testTag("pharmacist_assistant_topbar_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = stringResource(R.string.assistant_title),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("pharmacist_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("pharmacist_logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = stringResource(R.string.logout),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAssistant,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("pharmacist_assistant_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = stringResource(R.string.assistant_title)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("pharmacist_bottom_nav")
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(tab.titleRes)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(tab.titleRes),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> OverviewScreen(
                    viewModel = overviewViewModel,
                    onNavigateToCatalog = { selectedTabIndex = 1 },
                    onNavigateToForecasting = { selectedTabIndex = 3 },
                    onNavigateToOrderDetail = onNavigateToOrderDetail,
                    onNavigateToAssistant = onNavigateToAssistant
                )
                1 -> CatalogScreen(
                    viewModel = catalogViewModel,
                    cartViewModel = cartViewModel,
                    onDirectOrder = { selectedTabIndex = 2 }
                )
                2 -> OrdersScreen(
                    viewModel = ordersViewModel,
                    onNavigateToOrderDetail = onNavigateToOrderDetail
                )
                3 -> ForecastWizardNavHost(
                    wizardViewModel = forecastWizardViewModel,
                    onHandoffToOrders = { selectedTabIndex = 2 }
                )
            }
        }
    }
}
