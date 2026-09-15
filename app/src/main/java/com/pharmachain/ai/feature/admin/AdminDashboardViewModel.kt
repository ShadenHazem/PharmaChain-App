package com.pharmachain.ai.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmachain.ai.core.result.Result
import com.pharmachain.ai.data.AdminRepository
import com.pharmachain.ai.model.AdminOrder
import com.pharmachain.ai.model.AdminOrderDetail
import com.pharmachain.ai.model.AdminOrderStatus
import com.pharmachain.ai.model.PlatformAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AdminDashboardUiState {
    data object Loading : AdminDashboardUiState

    data class Success(
        val analytics: PlatformAnalytics,
        val orders: List<AdminOrder>,
        val filteredOrders: List<AdminOrder>,
        val selectedFilter: String = "ALL",
        val searchQuery: String = "",
        val selectedOrderDetail: AdminOrderDetail? = null,
        val isActionInProgress: Boolean = false,
        val isDetailLoading: Boolean = false,
        val actionSuccessMessage: String? = null,
        val actionErrorMessage: String? = null
    ) : AdminDashboardUiState

    data class Error(val message: String) : AdminDashboardUiState
}

class AdminDashboardViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminDashboardUiState>(AdminDashboardUiState.Loading)
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    private val currentFilter = MutableStateFlow("ALL")
    private val currentSearch = MutableStateFlow("")

    init {
        loadDashboardData()
    }

    fun refresh() {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = AdminDashboardUiState.Loading

            combine(
                adminRepository.getPlatformAnalytics(),
                adminRepository.getAdminOrders(),
                currentFilter,
                currentSearch
            ) { analyticsResult, ordersResult, filter, query ->
                when {
                    analyticsResult is Result.Error -> {
                        AdminDashboardUiState.Error(analyticsResult.message)
                    }
                    ordersResult is Result.Error -> {
                        AdminDashboardUiState.Error(ordersResult.message)
                    }
                    analyticsResult is Result.Success && ordersResult is Result.Success -> {
                        val allOrders = ordersResult.data
                        val filtered = filterOrders(allOrders, filter, query)
                        AdminDashboardUiState.Success(
                            analytics = analyticsResult.data,
                            orders = allOrders,
                            filteredOrders = filtered,
                            selectedFilter = filter,
                            searchQuery = query,
                            selectedOrderDetail = (_uiState.value as? AdminDashboardUiState.Success)?.selectedOrderDetail
                        )
                    }
                    else -> AdminDashboardUiState.Loading
                }
            }.collectLatest { state ->
                _uiState.value = state
            }
        }
    }

    fun setFilter(filter: String) {
        currentFilter.value = filter
        applyFilterAndSearch()
    }

    fun setSearchQuery(query: String) {
        currentSearch.value = query
        applyFilterAndSearch()
    }

    private fun applyFilterAndSearch() {
        _uiState.update { current ->
            if (current is AdminDashboardUiState.Success) {
                val filtered = filterOrders(current.orders, currentFilter.value, currentSearch.value)
                current.copy(
                    filteredOrders = filtered,
                    selectedFilter = currentFilter.value,
                    searchQuery = currentSearch.value
                )
            } else {
                current
            }
        }
    }

    private fun filterOrders(
        orders: List<AdminOrder>,
        filter: String,
        searchQuery: String
    ): List<AdminOrder> {
        var list = orders
        if (!filter.equals("ALL", ignoreCase = true)) {
            list = list.filter { it.status.name.equals(filter, ignoreCase = true) }
        }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.orderNumber.lowercase().contains(q) ||
                    it.buyerPharmacyName.lowercase().contains(q) ||
                    it.sellerDistributorName.lowercase().contains(q)
            }
        }
        return list
    }

    fun selectOrderForInspection(orderId: String) {
        viewModelScope.launch {
            _uiState.update { current ->
                if (current is AdminDashboardUiState.Success) {
                    current.copy(isDetailLoading = true)
                } else current
            }

            adminRepository.getAdminOrderDetail(orderId).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { current ->
                            if (current is AdminDashboardUiState.Success) {
                                current.copy(
                                    selectedOrderDetail = result.data,
                                    isDetailLoading = false
                                )
                            } else current
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { current ->
                            if (current is AdminDashboardUiState.Success) {
                                current.copy(
                                    isDetailLoading = false,
                                    actionErrorMessage = result.message
                                )
                            } else current
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { current ->
                            if (current is AdminDashboardUiState.Success) {
                                current.copy(isDetailLoading = true)
                            } else current
                        }
                    }
                }
            }
        }
    }

    fun dismissDetailModal() {
        _uiState.update { current ->
            if (current is AdminDashboardUiState.Success) {
                current.copy(selectedOrderDetail = null)
            } else current
        }
    }

    fun overrideOrderStatus(
        orderId: String,
        newStatus: String,
        adminNote: String,
        rollbackStock: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { current ->
                if (current is AdminDashboardUiState.Success) {
                    current.copy(isActionInProgress = true, actionErrorMessage = null, actionSuccessMessage = null)
                } else current
            }

            when (val result = adminRepository.overrideOrderStatus(orderId, newStatus, adminNote, rollbackStock)) {
                is Result.Success -> {
                    _uiState.update { current ->
                        if (current is AdminDashboardUiState.Success) {
                            val updatedOrder = result.data.order
                            val updatedOrders = current.orders.map {
                                if (it.id == updatedOrder.id) updatedOrder else it
                            }
                            val updatedFiltered = filterOrders(updatedOrders, current.selectedFilter, current.searchQuery)
                            val updatedDistribution = current.analytics.statusDistribution.toMutableMap()
                            // Recompute status distribution
                            AdminOrderStatus.entries.forEach { st ->
                                updatedDistribution[st.name] = updatedOrders.count { it.status == st }
                            }
                            val updatedAnalytics = current.analytics.copy(
                                activeOrdersCount = updatedOrders.count {
                                    it.status in listOf(
                                        AdminOrderStatus.PENDING,
                                        AdminOrderStatus.ACCEPTED,
                                        AdminOrderStatus.SHIPPED
                                    )
                                },
                                completedCount = updatedOrders.count { it.status == AdminOrderStatus.DELIVERED },
                                criticalDisputedCount = updatedOrders.count { it.status == AdminOrderStatus.DISPUTED },
                                statusDistribution = updatedDistribution
                            )

                            current.copy(
                                isActionInProgress = false,
                                selectedOrderDetail = result.data,
                                orders = updatedOrders,
                                filteredOrders = updatedFiltered,
                                analytics = updatedAnalytics,
                                actionSuccessMessage = "Order #${updatedOrder.orderNumber} successfully updated to ${updatedOrder.status.name}."
                            )
                        } else current
                    }
                }
                is Result.Error -> {
                    _uiState.update { current ->
                        if (current is AdminDashboardUiState.Success) {
                            current.copy(
                                isActionInProgress = false,
                                actionErrorMessage = result.message
                            )
                        } else current
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun clearActionFeedback() {
        _uiState.update { current ->
            if (current is AdminDashboardUiState.Success) {
                current.copy(actionSuccessMessage = null, actionErrorMessage = null)
            } else current
        }
    }
}
