package com.pharmachain.ai.feature.distributor_portal

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pharmachain.ai.R
import com.pharmachain.ai.core.common.util.CurrencyFormatter
import com.pharmachain.ai.core.common.util.DateUtils
import com.pharmachain.ai.core.common.util.SpreadsheetReader
import com.pharmachain.ai.core.designsystem.components.PharmaCard
import com.pharmachain.ai.core.model.ApiAuthType
import com.pharmachain.ai.core.model.ApiIntegration
import com.pharmachain.ai.core.model.ApiSyncFrequency
import com.pharmachain.ai.core.model.ConflictResolutionAction
import com.pharmachain.ai.core.model.InventoryColumnMapping
import com.pharmachain.ai.core.model.InventoryMappingField
import com.pharmachain.ai.core.model.InventoryUpload
import com.pharmachain.ai.core.model.InventoryUploadStatus
import com.pharmachain.ai.core.model.SyncConflictItem
import com.pharmachain.ai.core.network.dto.InventoryValidationResponseDto
import com.pharmachain.ai.core.network.dto.InventoryValidationRowDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorIntegrationsTab(
    state: DistributorUiState,
    onSubTabSelected: (IntegrationSubTab) -> Unit,
    // Excel upload actions
    onLoadSampleExcel: () -> Unit,
    onUploadFileSelected: (String, List<List<String>>) -> Unit = { _, _ -> },
    onColumnMappingChanged: (String, InventoryMappingField) -> Unit,
    onValidateMappings: () -> Unit,
    onApplyUpload: () -> Unit,
    onResetExcelFlow: () -> Unit,
    // API Sync actions
    onOpenApiConfig: () -> Unit,
    onCloseApiConfig: () -> Unit,
    onApiProviderNameChange: (String) -> Unit,
    onApiBaseUrlChange: (String) -> Unit,
    onApiAuthTypeChange: (ApiAuthType) -> Unit,
    onApiSecretChange: (String) -> Unit,
    onApiSyncFrequencyChange: (ApiSyncFrequency) -> Unit,
    onTestApiConnection: () -> Unit,
    onSaveApiIntegration: () -> Unit,
    onTriggerSyncNow: () -> Unit,
    onResolveConflict: (SyncConflictItem, ConflictResolutionAction) -> Unit,
    onDisconnectApi: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("distributor_integrations_tab")
    ) {
        SecondaryTabRow(
            selectedTabIndex = if (state.integrationSubTab == IntegrationSubTab.EXCEL_UPLOAD) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = state.integrationSubTab == IntegrationSubTab.EXCEL_UPLOAD,
                onClick = { onSubTabSelected(IntegrationSubTab.EXCEL_UPLOAD) },
                text = { Text(stringResource(R.string.subtab_excel_upload)) },
                icon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                modifier = Modifier.testTag("subtab_excel_upload")
            )
            Tab(
                selected = state.integrationSubTab == IntegrationSubTab.API_SYNC,
                onClick = { onSubTabSelected(IntegrationSubTab.API_SYNC) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.subtab_api_sync))
                        if (state.conflictCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${state.conflictCount}",
                                        color = MaterialTheme.colorScheme.onError,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                icon = { Icon(Icons.Default.CloudSync, contentDescription = null) },
                modifier = Modifier.testTag("subtab_api_sync")
            )
        }

        when (state.integrationSubTab) {
            IntegrationSubTab.EXCEL_UPLOAD -> {
                ExcelUploadSubTab(
                    excelStep = state.excelStep,
                    currentUpload = state.currentUpload,
                    detectedMappings = state.detectedMappings,
                    userMappings = state.userMappings,
                    validationResult = state.validationResult,
                    uploadHistory = state.uploadHistory,
                    isUploading = state.isUploadingFile,
                    isValidating = state.isValidatingFile,
                    isApplying = state.isApplyingFile,
                    onLoadSample = onLoadSampleExcel,
                    onUploadFileSelected = onUploadFileSelected,
                    onMappingChanged = onColumnMappingChanged,
                    onValidate = onValidateMappings,
                    onApply = onApplyUpload,
                    onReset = onResetExcelFlow
                )
            }
            IntegrationSubTab.API_SYNC -> {
                ApiSyncSubTab(
                    integration = state.apiIntegration,
                    isSyncingNow = state.isSyncingNow,
                    conflicts = state.syncConflicts,
                    onOpenConfig = onOpenApiConfig,
                    onTriggerSync = onTriggerSyncNow,
                    onResolveConflict = onResolveConflict,
                    onDisconnect = onDisconnectApi
                )
            }
        }
    }

    if (state.showApiConfigDialog) {
        ApiConfigDialog(
            providerName = state.apiProviderName,
            baseUrl = state.apiBaseUrl,
            authType = state.apiAuthType,
            secret = state.apiSecret,
            syncFrequency = state.apiSyncFrequency,
            isTesting = state.isTestingApi,
            testSuccess = state.testApiSuccess,
            testMessage = state.testApiMessage,
            isSaving = state.isConnectingApi,
            onProviderNameChange = onApiProviderNameChange,
            onBaseUrlChange = onApiBaseUrlChange,
            onAuthTypeChange = onApiAuthTypeChange,
            onSecretChange = onApiSecretChange,
            onSyncFrequencyChange = onApiSyncFrequencyChange,
            onTest = onTestApiConnection,
            onSave = onSaveApiIntegration,
            onDismiss = onCloseApiConfig
        )
    }
}

// ==============================================================================
// 1. EXCEL / CSV UPLOAD SUBTAB
// ==============================================================================

@Composable
private fun ExcelUploadSubTab(
    excelStep: ExcelUploadStep,
    currentUpload: InventoryUpload?,
    detectedMappings: List<InventoryColumnMapping>,
    userMappings: Map<String, InventoryMappingField>,
    validationResult: InventoryValidationResponseDto?,
    uploadHistory: List<InventoryUpload>,
    isUploading: Boolean,
    isValidating: Boolean,
    isApplying: Boolean,
    onLoadSample: () -> Unit,
    onUploadFileSelected: (String, List<List<String>>) -> Unit,
    onMappingChanged: (String, InventoryMappingField) -> Unit,
    onValidate: () -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("excel_upload_subtab"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            // Step Progress Indicator
            StepIndicatorRow(currentStep = excelStep)
        }

        when (excelStep) {
            ExcelUploadStep.SELECT_FILE -> {
                item {
                    UploadDropzoneCard(
                        isUploading = isUploading,
                        onLoadSample = onLoadSample,
                        onFileParsed = onUploadFileSelected
                    )
                }
            }
            ExcelUploadStep.MAP_COLUMNS -> {
                item {
                    ColumnMappingCard(
                        upload = currentUpload,
                        mappings = detectedMappings,
                        userMappings = userMappings,
                        isValidating = isValidating,
                        onMappingChanged = onMappingChanged,
                        onValidate = onValidate,
                        onCancel = onReset
                    )
                }
            }
            ExcelUploadStep.VALIDATION_DIFF -> {
                item {
                    ValidationDiffCard(
                        validation = validationResult,
                        isApplying = isApplying,
                        onApply = onApply,
                        onBackToMapping = onReset
                    )
                }
            }
            ExcelUploadStep.APPLIED_SUCCESS -> {
                item {
                    UploadSuccessCard(onUploadAnother = onReset)
                }
            }
        }

        // Upload History Section
        item {
            UploadHistorySection(history = uploadHistory)
        }
    }
}

@Composable
private fun StepIndicatorRow(currentStep: ExcelUploadStep) {
    val steps = listOf(
        stringResource(R.string.step_select_file) to (currentStep == ExcelUploadStep.SELECT_FILE),
        stringResource(R.string.step_map_columns) to (currentStep == ExcelUploadStep.MAP_COLUMNS),
        stringResource(R.string.step_review_diff) to (currentStep == ExcelUploadStep.VALIDATION_DIFF),
        stringResource(R.string.step_applied) to (currentStep == ExcelUploadStep.APPLIED_SUCCESS)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEach { (label, isActive) ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UploadDropzoneCard(
    isUploading: Boolean,
    onLoadSample: () -> Unit,
    onFileParsed: (String, List<List<String>>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isReadingFile by remember { mutableStateOf(false) }
    var parseError by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isReadingFile = true
            parseError = null
            coroutineScope.launch {
                try {
                    val fileName = getFileNameFromUri(context, uri)
                    val parsed = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            SpreadsheetReader.read(stream, fileName)
                        } ?: throw IllegalStateException("Unable to open file stream")
                    }
                    val combinedRows = mutableListOf<List<String>>()
                    combinedRows.add(parsed.headers)
                    combinedRows.addAll(parsed.rows)
                    isReadingFile = false
                    onFileParsed(fileName, combinedRows)
                } catch (e: Exception) {
                    isReadingFile = false
                    parseError = e.localizedMessage ?: "Failed to read Excel file"
                }
            }
        }
    }

    PharmaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isUploading || isReadingFile) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isReadingFile || isUploading) stringResource(R.string.analyzing_file_structure) else stringResource(R.string.upload_inventory_spreadsheet),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.spreadsheet_support_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 18.sp
            )

            if (parseError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        filePickerLauncher.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/vnd.ms-excel",
                                "text/csv",
                                "text/comma-separated-values",
                                "application/csv",
                                "*/*"
                            )
                        )
                    },
                    enabled = !isUploading && !isReadingFile,
                    modifier = Modifier.testTag("upload_device_excel_btn")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.select_spreadsheet_btn))
                }

                FilledTonalButton(
                    onClick = onLoadSample,
                    enabled = !isUploading && !isReadingFile,
                    modifier = Modifier.testTag("upload_sample_excel_btn")
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.sample_batch_btn))
                }
            }
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var fileName = "inventory_${System.currentTimeMillis()}.xlsx"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                val queriedName = cursor.getString(nameIndex)
                if (!queriedName.isNullOrBlank()) {
                    fileName = queriedName
                }
            }
        }
    } catch (_: Exception) {
        uri.lastPathSegment?.let { segment ->
            if (segment.isNotBlank()) fileName = segment.substringAfterLast("/")
        }
    }
    return fileName
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnMappingCard(
    upload: InventoryUpload?,
    mappings: List<InventoryColumnMapping>,
    userMappings: Map<String, InventoryMappingField>,
    isValidating: Boolean,
    onMappingChanged: (String, InventoryMappingField) -> Unit,
    onValidate: () -> Unit,
    onCancel: () -> Unit
) {
    PharmaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.map_inventory_columns_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.file_rows_label, upload?.fileName ?: "batch.xlsx", upload?.rowCount ?: 0),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            mappings.forEach { mapping ->
                val selectedField = userMappings[mapping.sourceColumnName] ?: mapping.mappedField
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mapping.sourceColumnName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.confidence_percent, (mapping.serverConfidence * 100).toInt()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        MappingFieldDropdown(
                            selected = selectedField,
                            onSelect = { onMappingChanged(mapping.sourceColumnName, it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onValidate,
                    enabled = !isValidating,
                    modifier = Modifier.testTag("validate_mappings_btn")
                ) {
                    if (isValidating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(R.string.validate_review_diff_btn))
                    }
                }
            }
        }
    }
}

@Composable
private fun MappingFieldDropdown(
    selected: InventoryMappingField,
    onSelect: (InventoryMappingField) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            InventoryMappingField.entries.forEach { field ->
                DropdownMenuItem(
                    text = { Text(field.name.replace("_", " ")) },
                    onClick = {
                        expanded = false
                        onSelect(field)
                    }
                )
            }
        }
    }
}

@Composable
private fun ValidationDiffCard(
    validation: InventoryValidationResponseDto?,
    isApplying: Boolean,
    onApply: () -> Unit,
    onBackToMapping: () -> Unit
) {
    PharmaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.validation_summary_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Summary Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.matched_label), style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                        Text("${validation?.matchedCount ?: 0}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.new_items_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text("${validation?.newCount ?: 0}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.errors_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        Text("${validation?.errorCount ?: 0}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.proposed_catalog_changes),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            validation?.validationRows?.take(5)?.forEach { diff ->
                DiffRowItem(diff = diff)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBackToMapping) {
                    Text(stringResource(R.string.back_to_mapping))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onApply,
                    enabled = !isApplying,
                    modifier = Modifier.testTag("apply_inventory_upload_btn")
                ) {
                    if (isApplying) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(R.string.apply_to_catalog_btn, validation?.totalRows ?: 0))
                    }
                }
            }
        }
    }
}

@Composable
private fun DiffRowItem(diff: InventoryValidationRowDto) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = diff.matchedMedicationName ?: diff.productNameOrSku,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        R.string.price_diff_format,
                        CurrencyFormatter.formatEgp(diff.currentPrice ?: 0.0),
                        CurrencyFormatter.formatEgp(diff.incomingPrice ?: 0.0)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(
                        R.string.stock_diff_format,
                        diff.currentStock ?: 0,
                        diff.incomingStock ?: 0
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun UploadSuccessCard(onUploadAnother: () -> Unit) {
    PharmaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.upload_success_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.upload_success_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onUploadAnother,
                modifier = Modifier.testTag("upload_another_file_btn")
            ) {
                Text(stringResource(R.string.upload_another_btn))
            }
        }
    }
}

@Composable
private fun UploadHistorySection(history: List<InventoryUpload>) {
    PharmaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.spreadsheet_history_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (history.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_upload_history),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                history.forEach { upload ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = upload.fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(
                                    R.string.upload_history_row_subtitle,
                                    DateUtils.formatDateTime(upload.uploadedAt),
                                    upload.rowCount ?: 0
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = when (upload.status) {
                                InventoryUploadStatus.APPLIED -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                InventoryUploadStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            Text(
                                text = upload.status.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (upload.status) {
                                    InventoryUploadStatus.APPLIED -> Color(0xFF2E7D32)
                                    InventoryUploadStatus.ERROR -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================================
// 2. DIRECT API / ERP INTEGRATION SUBTAB
// ==============================================================================

@Composable
private fun ApiSyncSubTab(
    integration: ApiIntegration?,
    isSyncingNow: Boolean,
    conflicts: List<SyncConflictItem>,
    onOpenConfig: () -> Unit,
    onTriggerSync: () -> Unit,
    onResolveConflict: (SyncConflictItem, ConflictResolutionAction) -> Unit,
    onDisconnect: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("api_sync_subtab"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            ApiConnectionStatusCard(
                integration = integration,
                isSyncingNow = isSyncingNow,
                onOpenConfig = onOpenConfig,
                onTriggerSync = onTriggerSync,
                onDisconnect = onDisconnect
            )
        }

        if (conflicts.isNotEmpty()) {
            item {
                SyncConflictResolutionSection(
                    conflicts = conflicts,
                    onResolveConflict = onResolveConflict
                )
            }
        }

        item {
            SecurityGuaranteesCard()
        }
    }
}

@Composable
private fun ApiConnectionStatusCard(
    integration: ApiIntegration?,
    isSyncingNow: Boolean,
    onOpenConfig: () -> Unit,
    onTriggerSync: () -> Unit,
    onDisconnect: () -> Unit
) {
    val isConnected = integration?.isActive == true

    PharmaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (isConnected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isConnected) integration.providerName else stringResource(R.string.erp_direct_api),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isConnected) stringResource(R.string.api_status_connected) else stringResource(R.string.api_status_not_connected),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isConnected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onOpenConfig, modifier = Modifier.testTag("configure_api_btn")) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.configure_api_btn))
                }
            }

            if (isConnected && integration != null) {
                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailRow(stringResource(R.string.api_base_endpoint), integration.baseUrl)
                        DetailRow(stringResource(R.string.api_auth_protocol), integration.authType.name.replace("_", " "))
                        DetailRow(stringResource(R.string.api_sync_schedule), integration.syncFrequency.name.replace("_", " "))
                        DetailRow(stringResource(R.string.api_last_sync), if (integration.lastSyncAt != null) DateUtils.formatDateTime(integration.lastSyncAt) else stringResource(R.string.status_pending))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("disconnect_api_btn")
                    ) {
                        Text(stringResource(R.string.disconnect_btn))
                    }

                    Button(
                        onClick = onTriggerSync,
                        enabled = !isSyncingNow,
                        modifier = Modifier.testTag("sync_now_api_btn")
                    ) {
                        if (isSyncingNow) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.syncing_progress))
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.sync_now_btn))
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.erp_promo_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onOpenConfig,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_api_integration_btn")
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.configure_api_btn))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SyncConflictResolutionSection(
    conflicts: List<SyncConflictItem>,
    onResolveConflict: (SyncConflictItem, ConflictResolutionAction) -> Unit
) {
    PharmaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.conflict_resolution_title, conflicts.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = stringResource(R.string.conflict_resolution_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            conflicts.forEach { conflict ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = conflict.medicationName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                R.string.conflict_manual_edit_format,
                                CurrencyFormatter.formatEgp(conflict.currentPrice),
                                conflict.currentStock,
                                DateUtils.formatDateTime(conflict.lastUpdatedAt)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(
                                R.string.conflict_incoming_api_format,
                                CurrencyFormatter.formatEgp(conflict.incomingPrice),
                                conflict.incomingStock
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0D47A1)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onResolveConflict(conflict, ConflictResolutionAction.REJECT) },
                                modifier = Modifier.testTag("keep_my_edit_btn_${conflict.productListingId}")
                            ) {
                                Text(stringResource(R.string.keep_my_edit_btn))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onResolveConflict(conflict, ConflictResolutionAction.ACCEPT) },
                                modifier = Modifier.testTag("accept_api_btn_${conflict.productListingId}")
                            ) {
                                Text(stringResource(R.string.accept_api_btn))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityGuaranteesCard() {
    PharmaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.security_guarantee_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.security_guarantee_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==============================================================================
// 3. API CONFIGURATION DIALOG
// ==============================================================================

@Composable
private fun ApiConfigDialog(
    providerName: String,
    baseUrl: String,
    authType: ApiAuthType,
    secret: String,
    syncFrequency: ApiSyncFrequency,
    isTesting: Boolean,
    testSuccess: Boolean?,
    testMessage: String?,
    isSaving: Boolean,
    onProviderNameChange: (String) -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onAuthTypeChange: (ApiAuthType) -> Unit,
    onSecretChange: (String) -> Unit,
    onSyncFrequencyChange: (ApiSyncFrequency) -> Unit,
    onTest: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.api_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = providerName,
                    onValueChange = onProviderNameChange,
                    label = { Text(stringResource(R.string.api_provider_label)) },
                    placeholder = { Text(stringResource(R.string.api_provider_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_provider_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = onBaseUrlChange,
                    label = { Text(stringResource(R.string.api_base_url_label)) },
                    placeholder = { Text(stringResource(R.string.api_base_url_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_base_url_input"),
                    singleLine = true
                )

                Text(stringResource(R.string.api_auth_type_label), style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ApiAuthType.entries.forEach { type ->
                        val isSelected = authType == type
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onAuthTypeChange(type) }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = when (type) {
                                        ApiAuthType.API_KEY -> stringResource(R.string.auth_type_api_key)
                                        ApiAuthType.OAUTH2_CLIENT_CREDENTIALS -> stringResource(R.string.auth_type_oauth2)
                                        ApiAuthType.BASIC_AUTH -> stringResource(R.string.auth_type_basic)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = secret,
                    onValueChange = onSecretChange,
                    label = { Text(stringResource(R.string.api_secret_label)) },
                    placeholder = { Text(stringResource(R.string.api_secret_placeholder)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_secret_input"),
                    singleLine = true
                )

                Text(stringResource(R.string.api_sync_frequency_label), style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ApiSyncFrequency.entries.take(3).forEach { freq ->
                        val isSelected = syncFrequency == freq
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSyncFrequencyChange(freq) }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = freq.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Test Connection Response Feedback
                if (testMessage != null) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (testSuccess == true) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = testMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (testSuccess == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onTest,
                    enabled = !isTesting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_api_connection_btn")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text(stringResource(R.string.api_test_connection_btn))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier.testTag("save_api_config_btn")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.api_save_connect_btn))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
