package com.pharmachain.ai.feature.pharmacist_dashboard

import com.pharmachain.ai.core.model.Order

data class SpendMonthData(
    val monthLabel: String,
    val spendEgp: Double
)

data class TopProductMetric(
    val name: String,
    val units: Int,
    val spendEgp: Double
)

data class DashboardOverviewData(
    val totalOrders: Int,
    val totalSpendEgp: Double,
    val averageOrderEgp: Double,
    val fulfillmentRatePct: Double,
    val pendingCount: Int,
    val shippedCount: Int,
    val deliveredCount: Int,
    val cancelledCount: Int,
    val confirmedCount: Int,
    val topProducts: List<TopProductMetric>,
    val monthlySpendTrend: List<SpendMonthData>,
    val recentOrders: List<Order>,
    // Backwards-compatible aliases
    val totalOrders30d: Int = totalOrders,
    val totalSpend30dEgp: Double = totalSpendEgp,
    val activeOrdersInTransit: Int = shippedCount + confirmedCount,
    val avgOrderCycleHours: Double = 18.5
)

sealed interface OverviewUiState {
    data object Loading : OverviewUiState
    data class Success(val data: DashboardOverviewData) : OverviewUiState
    data class Error(val message: String) : OverviewUiState
}
