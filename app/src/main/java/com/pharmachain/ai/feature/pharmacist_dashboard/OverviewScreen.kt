package com.pharmachain.ai.feature.pharmacist_dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pharmachain.ai.R
import com.pharmachain.ai.core.common.util.CurrencyFormatter
import com.pharmachain.ai.core.common.util.DateUtils
import com.pharmachain.ai.core.designsystem.components.EmptyState
import com.pharmachain.ai.core.designsystem.components.ErrorState
import com.pharmachain.ai.core.designsystem.components.LoadingIndicator
import com.pharmachain.ai.core.designsystem.components.PharmaCard
import com.pharmachain.ai.core.designsystem.components.PharmaOutlinedCard
import com.pharmachain.ai.core.designsystem.theme.StatusCancelledBg
import com.pharmachain.ai.core.designsystem.theme.StatusCancelledText
import com.pharmachain.ai.core.designsystem.theme.StatusConfirmedBg
import com.pharmachain.ai.core.designsystem.theme.StatusConfirmedText
import com.pharmachain.ai.core.designsystem.theme.StatusDeliveredBg
import com.pharmachain.ai.core.designsystem.theme.StatusDeliveredText
import com.pharmachain.ai.core.designsystem.theme.StatusPendingBg
import com.pharmachain.ai.core.designsystem.theme.StatusPendingText
import com.pharmachain.ai.core.designsystem.theme.StatusShippedBg
import com.pharmachain.ai.core.designsystem.theme.StatusShippedText
import com.pharmachain.ai.core.model.Order
import com.pharmachain.ai.core.model.OrderStatus

@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel,
    onNavigateToCatalog: () -> Unit,
    onNavigateToForecasting: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    onNavigateToAssistant: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is OverviewUiState.Loading -> LoadingIndicator(message = stringResource(R.string.loading_analytics))
        is OverviewUiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadDashboardData() })
        is OverviewUiState.Success -> OverviewContent(
            data = state.data,
            onNavigateToCatalog = onNavigateToCatalog,
            onNavigateToForecasting = onNavigateToForecasting,
            onNavigateToOrderDetail = onNavigateToOrderDetail,
            onNavigateToAssistant = onNavigateToAssistant,
            modifier = modifier
        )
    }
}

@Composable
private fun OverviewContent(
    data: DashboardOverviewData,
    onNavigateToCatalog: () -> Unit,
    onNavigateToForecasting: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    onNavigateToAssistant: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("overview_screen_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Welcome & Pharmacy Header Card
        item {
            PharmacyHeaderCard(
                onExploreCatalog = onNavigateToCatalog,
                onRunAiForecast = onNavigateToForecasting
            )
        }

        // =========================================================================
        // GROUP 1: PRIMARY FINANCIAL & VELOCITY METRICS (AT THE TOP)
        // =========================================================================
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Financial Performance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Track your purchasing spend and supplier velocity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2x2 Financial Metric Cards Grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialMetricCard(
                            title = "Total Spend",
                            value = CurrencyFormatter.formatEgp(data.totalSpendEgp),
                            subtitle = "Cumulative purchasing",
                            icon = Icons.Default.Payments,
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            iconColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_total_revenue"
                        )
                        FinancialMetricCard(
                            title = "Average Order",
                            value = CurrencyFormatter.formatEgp(data.averageOrderEgp),
                            subtitle = "Per batch transaction",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                            iconColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_average_order"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialMetricCard(
                            title = "Fulfillment Rate",
                            value = "${data.fulfillmentRatePct.toInt()}%",
                            subtitle = "${data.deliveredCount} delivered of ${data.totalOrders}",
                            icon = Icons.Default.CheckCircle,
                            containerColor = Color(0xFFE8F5E9),
                            iconColor = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_fulfillment_rate"
                        )
                        FinancialMetricCard(
                            title = "Total Orders",
                            value = "${data.totalOrders}",
                            subtitle = "Across all distributors",
                            icon = Icons.Default.ShoppingCart,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                            iconColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_total_orders"
                        )
                    }
                }
            }
        }

        // =========================================================================
        // GROUP 2: ORDER STATUS BREAKDOWN GRID
        // =========================================================================
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Order Status Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Live pipeline by fulfillment state",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2x2 Responsive Status Grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OrderStatusKpiCard(
                            statusName = "PENDING",
                            count = data.pendingCount,
                            icon = Icons.Default.Schedule,
                            statusBgColor = StatusPendingBg,
                            statusTextColor = StatusPendingText,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_status_pending"
                        )
                        OrderStatusKpiCard(
                            statusName = "SHIPPED",
                            count = data.shippedCount,
                            icon = Icons.Default.LocalShipping,
                            statusBgColor = StatusShippedBg,
                            statusTextColor = StatusShippedText,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_status_shipped"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OrderStatusKpiCard(
                            statusName = "DELIVERED",
                            count = data.deliveredCount,
                            icon = Icons.Default.CheckCircle,
                            statusBgColor = StatusDeliveredBg,
                            statusTextColor = StatusDeliveredText,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_status_delivered"
                        )
                        OrderStatusKpiCard(
                            statusName = "CANCELLED",
                            count = data.cancelledCount,
                            icon = Icons.Default.Cancel,
                            statusBgColor = StatusCancelledBg,
                            statusTextColor = StatusCancelledText,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_status_cancelled"
                        )
                    }
                }
            }
        }

        // =========================================================================
        // GROUP 3: VISUAL ANALYTICS (Spend Over Time & Top Products)
        // =========================================================================
        item {
            SpendOverTimeChartCard(months = data.monthlySpendTrend)
        }

        item {
            TopProductsBarCard(products = data.topProducts)
        }

        // AI Pharmacist Assistant Quick Access Card
        item {
            PharmaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToAssistant)
                    .testTag("overview_assistant_card")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.assistant_quick_access_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ask questions regarding inventory trends, generic substitutes, or restock forecasts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = stringResource(R.string.assistant_btn_ask),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // =========================================================================
        // GROUP 4: RECENT ORDERS FEED
        // =========================================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Order Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (data.recentOrders.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.ReceiptLong,
                    title = stringResource(R.string.no_recent_activity),
                    description = stringResource(R.string.recent_orders_empty_desc)
                )
            }
        } else {
            items(data.recentOrders, key = { it.id }) { order ->
                RecentOrderCard(
                    order = order,
                    onClick = { onNavigateToOrderDetail(order.id) }
                )
            }
        }
    }
}

@Composable
private fun FinancialMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    testTag: String
) {
    PharmaOutlinedCard(
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OrderStatusKpiCard(
    statusName: String,
    count: Int,
    icon: ImageVector,
    statusBgColor: Color,
    statusTextColor: Color,
    modifier: Modifier = Modifier,
    testTag: String
) {
    PharmaOutlinedCard(
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBgColor
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = statusTextColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$count",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(statusBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusTextColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun SpendOverTimeChartCard(months: List<SpendMonthData>) {
    PharmaCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "spend_over_time_chart_card"
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spend over time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Monthly purchasing expenditure (EGP)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxSpend = (months.maxOfOrNull { it.spendEgp } ?: 1.0).coerceAtLeast(1.0)
            val primaryColor = MaterialTheme.colorScheme.primary
            val outlineColor = MaterialTheme.colorScheme.outlineVariant

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val pointSpacing = canvasWidth / (months.size - 1).coerceAtLeast(1)

                // Horizontal grid lines
                drawLine(
                    color = outlineColor.copy(alpha = 0.35f),
                    start = Offset(0f, canvasHeight * 0.25f),
                    end = Offset(canvasWidth, canvasHeight * 0.25f),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = outlineColor.copy(alpha = 0.35f),
                    start = Offset(0f, canvasHeight * 0.75f),
                    end = Offset(canvasWidth, canvasHeight * 0.75f),
                    strokeWidth = 1.dp.toPx()
                )

                // Construct smooth curve path
                val path = Path()
                val fillPath = Path()
                fillPath.moveTo(0f, canvasHeight)

                val points = months.mapIndexed { index, item ->
                    val x = index * pointSpacing
                    val y = canvasHeight - ((item.spendEgp / maxSpend) * (canvasHeight - 24.dp.toPx())).toFloat() - 10.dp.toPx()
                    Offset(x, y)
                }

                if (points.isNotEmpty()) {
                    path.moveTo(points.first().x, points.first().y)
                    fillPath.lineTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlPoint1 = Offset((p0.x + p1.x) / 2f, p0.y)
                        val controlPoint2 = Offset((p0.x + p1.x) / 2f, p1.y)
                        path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                        fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                    }

                    fillPath.lineTo(points.last().x, canvasHeight)
                    fillPath.close()

                    // Gradient fill underneath
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.25f),
                                primaryColor.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = canvasHeight
                        )
                    )

                    // Curve stroke
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Dots on data points
                    points.forEach { pt ->
                        drawCircle(
                            color = Color.White,
                            radius = 4.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 3.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                months.forEach { data ->
                    Text(
                        text = data.monthLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TopProductsBarCard(products: List<TopProductMetric>) {
    PharmaCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "top_products_bar_card"
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Top products",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Highest volume restock lines by pharmacy expenditure",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            val maxSpend = (products.maxOfOrNull { it.spendEgp } ?: 1.0).coerceAtLeast(1.0)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                products.forEach { prod ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prod.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = CurrencyFormatter.formatEgp(prod.spendEgp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Progress bar track
                        val fillFraction = (prod.spendEgp / maxSpend).toFloat().coerceIn(0.05f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fillFraction)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}

