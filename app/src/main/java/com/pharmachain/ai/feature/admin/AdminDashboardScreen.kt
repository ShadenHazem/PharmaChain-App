package com.pharmachain.ai.feature.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pharmachain.ai.core.common.util.CurrencyFormatter
import com.pharmachain.ai.core.common.util.DateUtils
import com.pharmachain.ai.core.designsystem.components.EmptyState
import com.pharmachain.ai.core.designsystem.components.ErrorState
import com.pharmachain.ai.core.designsystem.components.LoadingIndicator
import com.pharmachain.ai.model.AdminOrder
import com.pharmachain.ai.model.AdminOrderStatus
import com.pharmachain.ai.model.PlatformAnalytics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is AdminDashboardUiState.Success) {
            state.actionSuccessMessage?.let { msg ->
                snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
                viewModel.clearActionFeedback()
            }
            state.actionErrorMessage?.let { err ->
                snackbarHostState.showSnackbar(err, duration = SnackbarDuration.Long)
                viewModel.clearActionFeedback()
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Admin Control Tower",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Platform Governance & Dispute Center",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.testTag("admin_refresh_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is AdminDashboardUiState.Loading -> {
                    LoadingIndicator(
                        message = "Synchronizing platform orders and analytics...",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is AdminDashboardUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is AdminDashboardUiState.Success -> {
                    AdminDashboardContent(
                        state = state,
                        onFilterSelected = { viewModel.setFilter(it) },
                        onSearchChanged = { viewModel.setSearchQuery(it) },
                        onInspectOrder = { viewModel.selectOrderForInspection(it) }
                    )

                    // Order Action Bottom Sheet
                    state.selectedOrderDetail?.let { detail ->
                        AdminOrderActionBottomSheet(
                            orderDetail = detail,
                            isActionInProgress = state.isActionInProgress,
                            onDismiss = { viewModel.dismissDetailModal() },
                            onConfirmOverride = { id, status, note, rollback ->
                                viewModel.overrideOrderStatus(id, status, note, rollback)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardContent(
    state: AdminDashboardUiState.Success,
    onFilterSelected: (String) -> Unit,
    onSearchChanged: (String) -> Unit,
    onInspectOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_orders_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Platform Analytics Overview
        item {
            PlatformAnalyticsOverview(analytics = state.analytics)
        }

        // 2. Search & Status Filter Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "B2B Order Oversight",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchChanged,
                    placeholder = { Text("Search by #Order, Pharmacy, or Distributor...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_order_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips
                val filterOptions = listOf("ALL") + AdminOrderStatus.entries.map { it.name }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterOptions) { filter ->
                        val isSelected = state.selectedFilter.equals(filter, ignoreCase = true)
                        val count = if (filter == "ALL") {
                            state.orders.size
                        } else {
                            state.orders.count { it.status.name.equals(filter, ignoreCase = true) }
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterSelected(filter) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = filter,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$count",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (filter == "DISPUTED") {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                }
                            ),
                            modifier = Modifier.testTag("admin_filter_chip_$filter")
                        )
                    }
                }
            }
        }

        // 3. Orders List Header count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${state.filteredOrders.size} orders",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (state.isDetailLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Loading order detail...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 4. Order Cards
        if (state.filteredOrders.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.FilterList,
                    title = "No Orders Match Criteria",
                    description = "Try selecting a different status filter or clearing your search term.",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            items(state.filteredOrders, key = { it.id }) { order ->
                AdminOrderCard(
                    order = order,
                    onInspect = { onInspectOrder(order.id) }
                )
            }
        }
    }
}

@Composable
private fun PlatformAnalyticsOverview(
    analytics: PlatformAnalytics,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_analytics_overview")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Platform KPI Health",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Realtime Telemetry",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4-Card Responsive Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiCard(
                    title = "Total Marketplace GMV",
                    value = CurrencyFormatter.formatEgp(analytics.totalGmv),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    testTag = "admin_kpi_gmv"
                )
                MetricKpiCard(
                    title = "In-Flight Orders",
                    value = "${analytics.activeOrdersCount}",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                    testTag = "admin_kpi_active"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiCard(
                    title = "Active Disputes",
                    value = "${analytics.criticalDisputedCount}",
                    color = if (analytics.criticalDisputedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                    isAlert = analytics.criticalDisputedCount > 0,
                    testTag = "admin_kpi_disputes"
                )
                val totalNonCancelled = analytics.completedCount + analytics.activeOrdersCount
                val completionRate = if (totalNonCancelled > 0) {
                    ((analytics.completedCount.toDouble() / (totalNonCancelled + analytics.criticalDisputedCount)) * 100).toInt()
                } else 100

                MetricKpiCard(
                    title = "Fulfilled Rate",
                    value = "$completionRate%",
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f),
                    testTag = "admin_kpi_completion"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status distribution segmented progress
            Text(
                text = "Consignment Status Distribution",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            StatusDistributionBar(distribution = analytics.statusDistribution)
        }
    }
}

@Composable
private fun MetricKpiCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    isAlert: Boolean = false,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isAlert) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier.testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun StatusDistributionBar(
    distribution: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val total = distribution.values.sum().coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth()) {
        // Visual Multi-segment bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            distribution.forEach { (status, count) ->
                if (count > 0) {
                    val weight = count.toFloat() / total
                    val barColor = getStatusColor(AdminOrderStatus.fromString(status))
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .height(10.dp)
                            .background(barColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            distribution.entries.take(4).forEach { (status, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(getStatusColor(AdminOrderStatus.fromString(status)))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$status: $count",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: AdminOrder,
    onInspect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (order.status == AdminOrderStatus.DISPUTED) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_order_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Order Number + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = DateUtils.formatDateTime(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Parties: Buyer & Distributor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Buyer Pharmacy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = order.buyerPharmacyName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Distributor",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = order.sellerDistributorName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom line: item count, total price, and Intervene button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${order.itemCount} item(s) consignment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatEgp(order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onInspect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (order.status == AdminOrderStatus.DISPUTED) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("admin_inspect_order_${order.id}")
                ) {
                    Icon(
                        imageVector = if (order.status == AdminOrderStatus.DISPUTED) Icons.Default.Warning else Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (order.status == AdminOrderStatus.DISPUTED) "Resolve Dispute" else "Inspect / Manage",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: AdminOrderStatus) {
    val bgColor = when (status) {
        AdminOrderStatus.DISPUTED -> MaterialTheme.colorScheme.errorContainer
        AdminOrderStatus.DELIVERED -> Color(0xFFE8F5E9)
        AdminOrderStatus.SHIPPED -> Color(0xFFE1F5FE)
        AdminOrderStatus.ACCEPTED -> Color(0xFFFFF8E1)
        AdminOrderStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
        AdminOrderStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    val textColor = when (status) {
        AdminOrderStatus.DISPUTED -> MaterialTheme.colorScheme.error
        AdminOrderStatus.DELIVERED -> Color(0xFF2E7D32)
        AdminOrderStatus.SHIPPED -> Color(0xFF0288D1)
        AdminOrderStatus.ACCEPTED -> Color(0xFFF57F17)
        AdminOrderStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
        AdminOrderStatus.CANCELLED -> MaterialTheme.colorScheme.outline
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun getStatusColor(status: AdminOrderStatus): Color {
    return when (status) {
        AdminOrderStatus.DELIVERED -> Color(0xFF4CAF50)
        AdminOrderStatus.SHIPPED -> Color(0xFF03A9F4)
        AdminOrderStatus.ACCEPTED -> Color(0xFFFFB300)
        AdminOrderStatus.PENDING -> Color(0xFF9E9E9E)
        AdminOrderStatus.DISPUTED -> Color(0xFFE53935)
        AdminOrderStatus.CANCELLED -> Color(0xFF757575)
    }
}
