package com.pharmachain.ai.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmachain.ai.core.model.MedicationWithListings
import com.pharmachain.ai.core.model.ProductListing
import com.pharmachain.ai.core.model.SmartCartItem
import com.pharmachain.ai.data.repository.CatalogRepository
import com.pharmachain.ai.data.repository.OrdersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val catalogRepository: CatalogRepository,
    private val ordersRepository: OrdersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")

    init {
        loadCatalog()
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            catalogRepository.seedInitialCatalogIfEmpty()

            combine(
                catalogRepository.getMedications(""),
                _searchQuery,
                _selectedCategory
            ) { meds, query, category ->
                var filtered = meds
                if (category != "All") {
                    filtered = filtered.filter { it.medication.category.contains(category, ignoreCase = true) }
                }
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.medication.brandName.contains(query, ignoreCase = true) ||
                        it.medication.genericName.contains(query, ignoreCase = true)
                    }
                }
                filtered
            }.catch { e ->
                _uiState.value = CatalogUiState.Error(e.message ?: "Failed to load medication catalog")
            }.collect { filteredMeds ->
                _uiState.value = CatalogUiState.Success(
                    medications = filteredMeds,
                    selectedCategory = _selectedCategory.value,
                    searchQuery = _searchQuery.value
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun placeDirectOrder(medicationWithListings: MedicationWithListings, listing: ProductListing, quantity: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            val item = SmartCartItem(
                id = "direct_${System.currentTimeMillis()}",
                jobId = "direct_job",
                medicationId = listing.medicationId,
                medicationName = medicationWithListings.medication.brandName,
                selectedProductListingId = listing.id,
                distributorId = listing.distributorId,
                distributorName = listing.distributorName,
                quantity = quantity,
                unitPrice = listing.price,
                lineTotal = listing.price * quantity,
                distributorReputationAtSelection = listing.distributorReputation,
                scoringRationale = "Direct Catalog Order"
            )
            ordersRepository.createOrdersFromSmartCart(listOf(item))
            onComplete()
        }
    }
}
