package com.pharmachain.ai.feature.orders

import com.pharmachain.ai.core.model.Order
import com.pharmachain.ai.core.model.OrderStatus

sealed interface OrdersUiState {
    data object Loading : OrdersUiState
    data class Success(
        val orders: List<Order>,
        val selectedFilter: OrderStatus? = null
    ) : OrdersUiState
    data class Error(val message: String) : OrdersUiState
}

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState
    data class Success(val order: Order) : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
}
