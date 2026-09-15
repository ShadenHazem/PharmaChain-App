package com.pharmachain.ai.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmachain.ai.core.common.result.NetworkResult
import com.pharmachain.ai.core.model.CartItem
import com.pharmachain.ai.core.model.Medication
import com.pharmachain.ai.core.model.ProductListing
import com.pharmachain.ai.data.repository.OrdersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submittedOrderIds: List<String>? = null
) {
    val totalBoxes: Int get() = items.sumOf { it.quantity }
    val totalItems: Int get() = items.size
    val totalPrice: Double get() = items.sumOf { it.lineTotal }
    val isEmpty: Boolean get() = items.isEmpty()
}

class CartViewModel(
    private val ordersRepository: OrdersRepository
) : ViewModel() {

    private val _cartState = MutableStateFlow(CartUiState())
    val cartState: StateFlow<CartUiState> = _cartState.asStateFlow()
    val uiState: StateFlow<CartUiState> get() = _cartState.asStateFlow()

    fun addToCart(
        medication: Medication,
        listing: ProductListing,
        quantity: Int
    ) {
        if (quantity <= 0) return
        _cartState.update { current ->
            val existingIndex = current.items.indexOfFirst { it.medicationId == medication.id }
            val updatedItems = current.items.toMutableList()
            if (existingIndex >= 0) {
                val existing = updatedItems[existingIndex]
                updatedItems[existingIndex] = existing.copy(
                    selectedListingId = listing.id,
                    distributorId = listing.distributorId,
                    distributorName = listing.distributorName,
                    distributorReputation = listing.distributorReputation,
                    unitPrice = listing.price,
                    quantity = existing.quantity + quantity
                )
            } else {
                updatedItems.add(
                    CartItem(
                        medicationId = medication.id,
                        brandName = medication.brandName,
                        genericName = medication.genericName,
                        category = medication.category,
                        form = medication.form,
                        strength = medication.strength,
                        selectedListingId = listing.id,
                        distributorId = listing.distributorId,
                        distributorName = listing.distributorName,
                        distributorReputation = listing.distributorReputation,
                        unitPrice = listing.price,
                        quantity = quantity
                    )
                )
            }
            current.copy(items = updatedItems, error = null)
        }
    }

    fun addToCart(
        medicationId: String,
        brandName: String,
        genericName: String,
        category: String = "General",
        form: String = "Tablet",
        strength: String = "Standard",
        distributorId: String,
        distributorName: String,
        distributorReputation: Double = 4.8,
        unitPrice: Double,
        quantity: Int
    ) {
        if (quantity <= 0) return
        _cartState.update { current ->
            val existingIndex = current.items.indexOfFirst { it.medicationId == medicationId }
            val updatedItems = current.items.toMutableList()
            if (existingIndex >= 0) {
                val existing = updatedItems[existingIndex]
                updatedItems[existingIndex] = existing.copy(
                    distributorId = distributorId,
                    distributorName = distributorName,
                    distributorReputation = distributorReputation,
                    unitPrice = unitPrice,
                    quantity = existing.quantity + quantity
                )
            } else {
                updatedItems.add(
                    CartItem(
                        medicationId = medicationId,
                        brandName = brandName,
                        genericName = genericName,
                        category = category,
                        form = form,
                        strength = strength,
                        selectedListingId = "listing_${medicationId}_$distributorId",
                        distributorId = distributorId,
                        distributorName = distributorName,
                        distributorReputation = distributorReputation,
                        unitPrice = unitPrice,
                        quantity = quantity
                    )
                )
            }
            current.copy(items = updatedItems, error = null)
        }
    }

    fun updateQuantity(medicationId: String, newQuantity: Int) {
        _cartState.update { current ->
            if (newQuantity <= 0) {
                current.copy(items = current.items.filterNot { it.medicationId == medicationId })
            } else {
                val updatedItems = current.items.map { item ->
                    if (item.medicationId == medicationId) item.copy(quantity = newQuantity) else item
                }
                current.copy(items = updatedItems)
            }
        }
    }

    fun removeFromCart(medicationId: String) {
        _cartState.update { current ->
            current.copy(items = current.items.filterNot { it.medicationId == medicationId })
        }
    }

    fun clearCart() {
        _cartState.update { CartUiState() }
    }

    fun submitCart(
        pharmacyId: String = "pharm_demo",
        pharmacyName: String = "El-Ezaby Pharmacy",
        deliveryAddress: String = "24 Abbas El Akkad St, Nasr City, Cairo",
        onSuccess: () -> Unit = {}
    ) {
        submitOrder(
            pharmacyId = pharmacyId,
            pharmacyName = pharmacyName,
            deliveryAddress = deliveryAddress,
            onSuccess = { onSuccess() }
        )
    }

    fun submitOrder(
        pharmacyId: String = "pharm_demo",
        pharmacyName: String = "El-Ezaby Pharmacy",
        deliveryAddress: String = "24 Abbas El Akkad St, Nasr City, Cairo",
        onSuccess: (List<String>) -> Unit = {}
    ) {
        val currentItems = _cartState.value.items
        if (currentItems.isEmpty()) {
            _cartState.update { it.copy(error = "Your cart is currently empty") }
            return
        }

        viewModelScope.launch {
            _cartState.update { it.copy(isSubmitting = true, error = null) }
            val result = ordersRepository.submitBatchOrder(
                items = currentItems,
                pharmacyId = pharmacyId,
                pharmacyName = pharmacyName,
                deliveryAddress = deliveryAddress
            )
            when (result) {
                is NetworkResult.Success -> {
                    _cartState.update {
                        CartUiState(
                            items = emptyList(),
                            isSubmitting = false,
                            submittedOrderIds = result.data
                        )
                    }
                    onSuccess(result.data)
                }
                is NetworkResult.Error -> {
                    _cartState.update {
                        it.copy(
                            isSubmitting = false,
                            error = result.message
                        )
                    }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun dismissSuccess() {
        _cartState.update { it.copy(submittedOrderIds = null) }
    }

    fun dismissError() {
        _cartState.update { it.copy(error = null) }
    }
}
