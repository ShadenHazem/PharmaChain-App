package com.pharmachain.ai.feature.forecasting

import com.pharmachain.ai.core.common.util.SpreadsheetFormat
import com.pharmachain.ai.core.model.ColumnMapping
import com.pharmachain.ai.core.model.ForecastDuration
import com.pharmachain.ai.core.model.ForecastJob
import com.pharmachain.ai.core.model.ForecastResult
import com.pharmachain.ai.core.model.SalesHistoryUpload
import com.pharmachain.ai.core.model.SmartCartItem
import com.pharmachain.ai.feature.forecasting.engine.DataCleaningAndMappingService
import com.pharmachain.ai.feature.forecasting.engine.ParsedRawFile

enum class WizardStep {
    UPLOAD,          // Step 1: Select File
    RAW_PREVIEW,     // Step 2: Raw Table Preview (First ~10 rows & detected format)
    MAPPING,         // Step 3: Bilingual Column Mapping
    VALIDATION,      // Step 4: Data Quality Review & Row issue handling
    CONSTRAINTS,     // Step 5: Forecast Constraints & Data Sufficiency
    RESULTS,         // Step 6: Forecast Results & Confidence
    SMART_CART,      // Smart Cart with recommendation "why"
    MANUAL_ENTRY     // Alternate Entry Point: Manual Reorder Quantities
}

data class ValidationReport(
    val totalRows: Int = 0,
    val validRows: Int = 0,
    val droppedEmptyRows: Int = 0,
    val partialRowsCount: Int = 0,
    val missingValues: Int = 0,
    val duplicateRows: Int = 0,
    val dateRange: String = "",
    val uniqueMedications: Int = 0,
    val isDataSufficient: Boolean = true,
    val validationErrors: List<String> = emptyList()
)

data class ManualProductEntry(
    val medicationId: String,
    val brandName: String,
    val genericName: String,
    val category: String,
    val monthlyEstimateQty: Int
)

data class ForecastWizardUiState(
    val currentStep: WizardStep = WizardStep.UPLOAD,
    val isProcessing: Boolean = false,
    val upload: SalesHistoryUpload? = null,
    val parsedFile: ParsedRawFile? = null,
    val cleanedDataset: DataCleaningAndMappingService.CleanedDataset? = null,
    val mappings: List<ColumnMapping> = emptyList(),
    val validationReport: ValidationReport? = null,
    val isValidationStale: Boolean = false,
    val excludedRowIndices: Set<Int> = emptySet(),
    val duration: ForecastDuration = ForecastDuration.FOURTEEN,
    val maxBudgetEgp: Double = 15000.0,
    val accountForSeasonality: Boolean = true,
    val isManualFlow: Boolean = false,
    val manualEntries: List<ManualProductEntry> = emptyList(),
    val activeJob: ForecastJob? = null,
    val forecastResults: List<ForecastResult> = emptyList(),
    val smartCartItems: List<SmartCartItem> = emptyList(),
    val errorMessage: String? = null
)
