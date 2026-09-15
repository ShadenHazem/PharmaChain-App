package com.pharmachain.ai.feature.catalog

import com.pharmachain.ai.core.model.MedicationWithListings

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Success(
        val medications: List<MedicationWithListings>,
        val selectedCategory: String = "All",
        val searchQuery: String = ""
    ) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}
