package com.pharmachain.ai.feature.forecasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmachain.ai.core.model.ColumnMapping
import com.pharmachain.ai.core.model.ForecastDuration
import com.pharmachain.ai.core.model.ForecastResult
import com.pharmachain.ai.core.model.MappingField
import com.pharmachain.ai.core.model.SmartCartItem
import com.pharmachain.ai.data.repository.CatalogRepository
import com.pharmachain.ai.data.repository.ForecastRepository
import com.pharmachain.ai.data.repository.OrdersRepository
import com.pharmachain.ai.feature.forecasting.engine.DataCleaningAndMappingService
import com.pharmachain.ai.feature.forecasting.engine.ParsedRawFile
import com.pharmachain.ai.feature.forecasting.engine.SmartCartScoringEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ForecastWizardViewModel(
    private val forecastRepository: ForecastRepository,
    private val catalogRepository: CatalogRepository,
    private val ordersRepository: OrdersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForecastWizardUiState())
    val uiState: StateFlow<ForecastWizardUiState> = _uiState.asStateFlow()

    fun handleFileUpload(parsedData: ParsedRawFile, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            delay(400) // Brief UI transition delay

            // Pre-process and clean dataset
            val cleaned = DataCleaningAndMappingService.cleanAndNormalizeDataset(
                rawHeaders = parsedData.headers,
                rawRows = parsedData.rows
            )

            val upload = forecastRepository.saveUpload(
                fileName = parsedData.fileName,
                fileSizeKb = parsedData.fileSizeKb,
                customHeaders = parsedData.headers,
                actualRowCount = parsedData.rows.size
            )
            val mappings = forecastRepository.getColumnMappings(upload.id)

            _uiState.update {
                it.copy(
                    isProcessing = false,
                    upload = upload,
                    parsedFile = parsedData,
                    cleanedDataset = cleaned,
                    mappings = mappings,
                    validationReport = null,
                    isValidationStale = true,
                    isManualFlow = false,
                    currentStep = WizardStep.RAW_PREVIEW
                )
            }
            onSuccess?.invoke()
        }
    }

    fun proceedToMapping(onSuccess: (() -> Unit)? = null) {
        _uiState.update { it.copy(currentStep = WizardStep.MAPPING) }
        onSuccess?.invoke()
    }

    fun updateColumnMapping(columnName: String, mappedField: MappingField) {
        val updated = _uiState.value.mappings.map {
            if (it.sourceColumnName == columnName) it.copy(mappedField = mappedField) else it
        }
        _uiState.update {
            it.copy(
                mappings = updated,
                isValidationStale = true,
                validationReport = null
            )
        }
    }

    fun toggleRowExclusion(rowIndex: Int) {
        val currentSet = _uiState.value.excludedRowIndices.toMutableSet()
        if (currentSet.contains(rowIndex)) {
            currentSet.remove(rowIndex)
        } else {
            currentSet.add(rowIndex)
        }
        _uiState.update { it.copy(excludedRowIndices = currentSet) }

        // Recalculate validation report
        val cleanedRows = _uiState.value.cleanedDataset?.cleanedRows ?: emptyList()
        val fileName = _uiState.value.upload?.fileName ?: "Uploaded Sales File"
        val freshReport = DataCleaningAndMappingService.performValidationOnMappedData(
            cleanedRows = cleanedRows,
            mappings = _uiState.value.mappings,
            fileName = fileName,
            excludedRowIndices = currentSet
        )
        _uiState.update { it.copy(validationReport = freshReport) }
    }

    fun confirmMappingsAndValidate(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            val currentMappings = _uiState.value.mappings
            forecastRepository.saveMappings(currentMappings)
            delay(400)

            val cleanedRows = _uiState.value.cleanedDataset?.cleanedRows ?: emptyList()
            val fileName = _uiState.value.upload?.fileName ?: "Uploaded Sales File"

            val freshReport = DataCleaningAndMappingService.performValidationOnMappedData(
                cleanedRows = cleanedRows,
                mappings = currentMappings,
                fileName = fileName,
                excludedRowIndices = _uiState.value.excludedRowIndices
            )

            _uiState.update {
                it.copy(
                    isProcessing = false,
                    validationReport = freshReport,
                    isValidationStale = false,
                    currentStep = WizardStep.VALIDATION
                )
            }
            onSuccess?.invoke()
        }
    }

    fun proceedToConstraints(onSuccess: (() -> Unit)? = null) {
        _uiState.update { it.copy(currentStep = WizardStep.CONSTRAINTS) }
        onSuccess?.invoke()
    }

    // ==========================================
    // MANUAL ENTRY FALLBACK WORKFLOW
    // ==========================================

    fun startManualEntry() {
        startManualEntryFlow()
    }

    fun startManualEntryFlow() {
        _uiState.update {
            it.copy(
                currentStep = WizardStep.MANUAL_ENTRY,
                isManualFlow = true,
                manualEntries = emptyList()
            )
        }
    }

    fun addOrUpdateManualEntry(medicationId: String, brandName: String, genericName: String, category: String, qty: Int) {
        val existing = _uiState.value.manualEntries.filter { it.medicationId != medicationId }
        val updated = existing + ManualProductEntry(
            medicationId = medicationId,
            brandName = brandName,
            genericName = genericName,
            category = category,
            monthlyEstimateQty = qty
        )
        _uiState.update { it.copy(manualEntries = updated) }
    }

    fun removeManualEntry(medicationId: String) {
        val updated = _uiState.value.manualEntries.filter { it.medicationId != medicationId }
        _uiState.update { it.copy(manualEntries = updated) }
    }

    fun proceedFromManualToConstraints(onSuccess: (() -> Unit)? = null) {
        confirmManualEntriesAndProceed(onSuccess)
    }

    fun confirmManualEntriesAndProceed(onSuccess: (() -> Unit)? = null) {
        if (_uiState.value.manualEntries.isEmpty()) return
        _uiState.update {
            it.copy(
                currentStep = WizardStep.CONSTRAINTS,
                isManualFlow = true
            )
        }
        onSuccess?.invoke()
    }

    fun setDuration(duration: ForecastDuration) {
        _uiState.update { it.copy(duration = duration) }
    }

    fun setMaxBudget(budget: Double) {
        _uiState.update { it.copy(maxBudgetEgp = budget) }
    }

    fun setSeasonality(enabled: Boolean) {
        _uiState.update { it.copy(accountForSeasonality = enabled) }
    }

    fun runForecastJob(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            if (_uiState.value.isManualFlow) {
                // Generate predictions directly from manual estimates
                val durationMultiplier = when (_uiState.value.duration) {
                    ForecastDuration.SEVEN -> 0.25
                    ForecastDuration.FOURTEEN -> 0.50
                    ForecastDuration.THIRTY -> 1.0
                }
                val manualResults = _uiState.value.manualEntries.map { entry ->
                    val predQty = maxOf(1, (entry.monthlyEstimateQty * durationMultiplier).toInt())
                    ForecastResult(
                        id = "res_man_${UUID.randomUUID().toString().take(8)}",
                        jobId = "job_man_${System.currentTimeMillis()}",
                        medicationId = entry.medicationId,
                        medicationName = entry.brandName,
                        category = entry.category,
                        predictedQuantity = predQty,
                        confidenceScore = 0.70, // Explicitly lower confidence for manual estimation
                        seasonalTrendDetected = false
                    )
                }
                delay(600)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        forecastResults = manualResults,
                        currentStep = WizardStep.RESULTS
                    )
                }
                onSuccess?.invoke()
                return@launch
            }

            val upload = _uiState.value.upload ?: return@launch
            val cleanedRows = _uiState.value.cleanedDataset?.cleanedRows ?: emptyList()
            val mappings = _uiState.value.mappings

            val job = forecastRepository.createAndRunForecastJobFromUploadedData(
                uploadId = upload.id,
                duration = _uiState.value.duration,
                budget = _uiState.value.maxBudgetEgp,
                accountForSeasonality = _uiState.value.accountForSeasonality,
                cleanedRows = cleanedRows,
                mappings = mappings
            )
            delay(800)

            val results = forecastRepository.getForecastResults(job.id).first()
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    activeJob = job,
                    forecastResults = results,
                    currentStep = WizardStep.RESULTS
                )
            }
            onSuccess?.invoke()
        }
    }

    fun generateSmartCart(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            val results = _uiState.value.forecastResults
            val medsWithListings = catalogRepository.getMedications().first()
            val jobId = _uiState.value.activeJob?.id ?: "job_cart_${System.currentTimeMillis()}"

            val cartItems = SmartCartScoringEngine.generateSmartCart(
                jobId = jobId,
                forecastResults = results,
                medicationsWithListings = medsWithListings,
                maxBudgetEGP = _uiState.value.maxBudgetEgp
            )

            forecastRepository.saveSmartCartItems(cartItems)
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    smartCartItems = cartItems,
                    currentStep = WizardStep.SMART_CART
                )
            }
            onSuccess?.invoke()
        }
    }

    fun toggleCartItem(itemId: String) {
        val updated = _uiState.value.smartCartItems.map {
            if (it.id == itemId) it.copy(isSelected = !it.isSelected) else it
        }
        _uiState.update { it.copy(smartCartItems = updated) }
    }

    fun updateCartItemQuantity(itemId: String, newQty: Int) {
        if (newQty <= 0) return
        val updated = _uiState.value.smartCartItems.map {
            if (it.id == itemId) it.copy(quantity = newQty, lineTotal = it.unitPrice * newQty) else it
        }
        _uiState.update { it.copy(smartCartItems = updated) }
    }

    fun placeSmartCartOrders(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            val selectedItems = _uiState.value.smartCartItems.filter { it.isSelected }
            ordersRepository.createOrdersFromSmartCart(selectedItems)
            delay(600)
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    currentStep = WizardStep.UPLOAD,
                    upload = null,
                    activeJob = null,
                    forecastResults = emptyList(),
                    smartCartItems = emptyList(),
                    validationReport = null,
                    isValidationStale = false,
                    isManualFlow = false,
                    manualEntries = emptyList()
                )
            }
            onSuccess()
        }
    }

    fun navigateBack(): WizardStep? {
        val prevStep = when (_uiState.value.currentStep) {
            WizardStep.UPLOAD -> null
            WizardStep.MANUAL_ENTRY -> WizardStep.UPLOAD
            WizardStep.RAW_PREVIEW -> WizardStep.UPLOAD
            WizardStep.MAPPING -> WizardStep.RAW_PREVIEW
            WizardStep.VALIDATION -> WizardStep.MAPPING
            WizardStep.CONSTRAINTS -> if (_uiState.value.isManualFlow) WizardStep.MANUAL_ENTRY else WizardStep.VALIDATION
            WizardStep.RESULTS -> WizardStep.CONSTRAINTS
            WizardStep.SMART_CART -> WizardStep.RESULTS
        }
        if (prevStep != null) {
            _uiState.update { it.copy(currentStep = prevStep) }
        }
        return prevStep
    }

    fun navigateToStep(step: WizardStep) {
        _uiState.update { it.copy(currentStep = step) }
    }

    fun resetWizard() {
        _uiState.update {
            ForecastWizardUiState()
        }
    }
}
