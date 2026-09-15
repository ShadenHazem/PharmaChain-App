package com.pharmachain.ai.feature.forecasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmachain.ai.core.model.ColumnMapping
import com.pharmachain.ai.core.model.MappingField
import com.pharmachain.ai.data.repository.ForecastRepository
import com.pharmachain.ai.feature.forecasting.engine.DataCleaningAndMappingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ColumnMappingUiState(
    val uploadId: String = "",
    val mappings: List<ColumnMapping> = emptyList(),
    val isProcessing: Boolean = false,
    val hasLowConfidenceWarnings: Boolean = false,
    val errorMessage: String? = null
)

class ColumnMappingViewModel(
    private val forecastRepository: ForecastRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ColumnMappingUiState())
    val uiState: StateFlow<ColumnMappingUiState> = _uiState.asStateFlow()

    fun loadMappingsForUpload(uploadId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, uploadId = uploadId) }
            val mappings = forecastRepository.getColumnMappings(uploadId)
            val hasLowConfidence = mappings.any { it.serverConfidence < 0.85 && it.mappedField != MappingField.IGNORE }
            _uiState.update {
                it.copy(
                    mappings = mappings,
                    isProcessing = false,
                    hasLowConfidenceWarnings = hasLowConfidence
                )
            }
        }
    }

    fun setMappings(mappings: List<ColumnMapping>) {
        val hasLowConfidence = mappings.any { it.serverConfidence < 0.85 && it.mappedField != MappingField.IGNORE }
        _uiState.update {
            it.copy(
                mappings = mappings,
                hasLowConfidenceWarnings = hasLowConfidence
            )
        }
    }

    fun updateMapping(columnName: String, mappedField: MappingField) {
        val updated = _uiState.value.mappings.map { mapping ->
            if (mapping.sourceColumnName == columnName) {
                // If manually changed by pharmacist, boost confidence or mark as user-verified
                val newConfidence = if (mappedField == mapping.mappedField) {
                    mapping.serverConfidence
                } else {
                    1.0 // User-verified selection
                }
                mapping.copy(mappedField = mappedField, serverConfidence = newConfidence)
            } else {
                mapping
            }
        }
        val hasLowConfidence = updated.any { it.serverConfidence < 0.85 && it.mappedField != MappingField.IGNORE }
        _uiState.update {
            it.copy(
                mappings = updated,
                hasLowConfidenceWarnings = hasLowConfidence
            )
        }
    }

    fun saveMappings(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            forecastRepository.saveMappings(_uiState.value.mappings)
            _uiState.update { it.copy(isProcessing = false) }
            onSuccess()
        }
    }
}
