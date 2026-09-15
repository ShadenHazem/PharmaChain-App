package com.pharmachain.ai.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmachain.ai.core.model.Order
import com.pharmachain.ai.core.model.OrderStatus
import com.pharmachain.ai.data.repository.OrdersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val ordersRepository: OrdersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow<OrderStatus?>(null)

    private val _orderDetailState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val orderDetailState: StateFlow<OrderDetailUiState> = _orderDetailState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = OrdersUiState.Loading
            ordersRepository.seedInitialOrdersIfEmpty()

            combine(
                ordersRepository.getOrdersForPharmacy(),
                _selectedFilter
            ) { orders, filter ->
                if (filter != null) {
                    orders.filter { it.status == filter }
                } else {
                    orders
                }
            }.catch { e ->
                _uiState.value = OrdersUiState.Error(e.message ?: "Failed to load orders")
            }.collect { filteredOrders ->
                _uiState.value = OrdersUiState.Success(
                    orders = filteredOrders,
                    selectedFilter = _selectedFilter.value
                )
            }
        }
    }

    fun setFilter(status: OrderStatus?) {
        _selectedFilter.value = status
    }

    fun loadOrderDetail(orderId: String) {
        viewModelScope.launch {
            _orderDetailState.value = OrderDetailUiState.Loading
            ordersRepository.getOrderById(orderId)
                .catch { e ->
                    _orderDetailState.value = OrderDetailUiState.Error(e.message ?: "Failed to load order details")
                }
                .collect { order ->
                    if (order != null) {
                        _orderDetailState.value = OrderDetailUiState.Success(order)
                    } else {
                        _orderDetailState.value = OrderDetailUiState.Error("Order not found")
                    }
                }
        }
    }
}
