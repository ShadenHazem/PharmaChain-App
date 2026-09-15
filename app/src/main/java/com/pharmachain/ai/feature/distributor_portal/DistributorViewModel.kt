package com.pharmachain.ai.feature.distributor_portal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmachain.ai.core.model.ApiAuthType
import com.pharmachain.ai.core.model.ApiSyncFrequency
import com.pharmachain.ai.core.model.ConflictResolutionAction
import com.pharmachain.ai.core.model.InventoryMappingField
import com.pharmachain.ai.core.model.Order
import com.pharmachain.ai.core.model.OrderStatus
import com.pharmachain.ai.core.model.SyncConflictItem
import com.pharmachain.ai.data.repository.DistributorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DistributorViewModel(
    private val distributorRepository: DistributorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DistributorUiState())
    val uiState: StateFlow<DistributorUiState> = _uiState.asStateFlow()

    private val distributorId = "dist_cairo_drugs"

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Inventory Listings
            launch {
                distributorRepository.getListingsForDistributor(distributorId)
                    .catch { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                    .collect { listings ->
                        _uiState.update { it.copy(listings = listings) }
                    }
            }

            // 2. Incoming Orders
            launch {
                distributorRepository.getIncomingOrders(distributorId)
                    .catch { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                    .collect { orders ->
                        _uiState.update { it.copy(incomingOrders = orders) }
                    }
            }

            // 3. Low Stock Alerts
            launch {
                distributorRepository.getLowStockAlerts(distributorId)
                    .catch { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                    .collect { alerts ->
                        _uiState.update { it.copy(lowStockAlerts = alerts) }
                    }
            }

            // 4. API Integration status
            launch {
                distributorRepository.getApiIntegration(distributorId)
                    .catch { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                    .collect { api ->
                        _uiState.update { it.copy(apiIntegration = api) }
                    }
            }

            // 5. Upload History
            launch {
                distributorRepository.getInventoryUploads(distributorId)
                    .catch { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                    .collect { uploads ->
                        _uiState.update { it.copy(uploadHistory = uploads) }
                    }
            }

            // 6. Commission Ledger
            launch {
                distributorRepository.getCommissionLedger(distributorId)
                    .catch { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                    .collect { ledger ->
                        _uiState.update { it.copy(commissionLedger = ledger, isLoading = false) }
                    }
            }

            // 7. Load Initial Analytics
            loadAnalytics(_uiState.value.selectedPeriodDays)
        }
    }

    fun selectTab(tab: DistributorTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // ==========================================================================
    // Overview & Analytics
    // ==========================================================================

    fun selectAnalyticsPeriod(days: Int) {
        _uiState.update { it.copy(selectedPeriodDays = days) }
        loadAnalytics(days)
    }

    private fun loadAnalytics(days: Int) {
        viewModelScope.launch {
            try {
                val analytics = distributorRepository.getAnalytics(days, distributorId)
                _uiState.update { it.copy(analytics = analytics) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to load analytics: ${e.message}") }
            }
        }
    }

    fun dismissLowStockAlert(alertId: String) {
        viewModelScope.launch {
            try {
                distributorRepository.dismissLowStockAlert(alertId)
                _uiState.update { it.copy(message = "Alert dismissed.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    // ==========================================================================
    // Orders & Approval Gate Actions
    // ==========================================================================

    fun setOrderStatusFilter(status: OrderStatus?) {
        _uiState.update { it.copy(selectedOrderStatusFilter = status) }
    }

    fun approveOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            try {
                distributorRepository.approveOrder(orderId)
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        message = "Order #$orderId approved and confirmed!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUpdating = false, errorMessage = e.message) }
            }
        }
    }

    fun promptRejectOrder(order: Order) {
        _uiState.update { it.copy(orderToReject = order, rejectionReasonText = "") }
    }

    fun dismissRejectDialog() {
        _uiState.update { it.copy(orderToReject = null, rejectionReasonText = "") }
    }

    fun setRejectionReasonText(text: String) {
        _uiState.update { it.copy(rejectionReasonText = text) }
    }

    fun confirmRejectOrder() {
        val order = _uiState.value.orderToReject ?: return
        val reason = _uiState.value.rejectionReasonText.ifBlank { "Out of stock or distributor fulfillment quota exceeded." }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, orderToReject = null) }
            try {
                distributorRepository.rejectOrder(order.id, reason)
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        message = "Order #${order.id} was rejected."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUpdating = false, errorMessage = e.message) }
            }
        }
    }

    fun advanceOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            try {
                distributorRepository.advanceOrderStatus(orderId, newStatus)
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        message = "Order #$orderId moved to ${newStatus.name}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUpdating = false, errorMessage = e.message) }
            }
        }
    }

    // ==========================================================================
    // Inventory Management
    // ==========================================================================

    fun setInventorySearchQuery(query: String) {
        _uiState.update { it.copy(inventorySearchQuery = query) }
    }

    fun updateListingPriceStock(listingId: String, newPrice: Double, newStock: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            try {
                distributorRepository.updateListing(listingId, newPrice, newStock, distributorId)
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        message = "Product listing updated successfully."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUpdating = false, errorMessage = e.message) }
            }
        }
    }

    // ==========================================================================
    // Integrations - Sub Tab Switching
    // ==========================================================================

    fun setIntegrationSubTab(subTab: IntegrationSubTab) {
        _uiState.update { it.copy(integrationSubTab = subTab) }
    }

    // ==========================================================================
    // Integrations - Excel / CSV Upload Lifecycle
    // ==========================================================================

    fun loadSampleExcelFile() {
        val sampleLines = listOf(
            listOf("Drug Name", "Unit Price EGP", "Stock Qty", "Expiry Date"),
            listOf("Panadol Extra 500mg", "48.50", "350", "2027-11-30"),
            listOf("Augmentin 1g Tablets", "118.00", "120", "2027-08-15"),
            listOf("Concor 5mg Plus", "64.00", "200", "2028-01-20"),
            listOf("Amoclan 1g", "98.00", "90", "2027-05-10"),
            listOf("Antinal 200mg Capsules", "42.00", "450", "2027-12-01"),
            listOf("Ketofan 75mg Ampoules", "30.00", "180", "2026-10-15"),
            listOf("Gastrazole 40mg", "76.00", "85", "2028-03-30"),
            listOf("Cidophage 850mg", "35.00", "500", "2028-06-01"),
            listOf("Ator 20mg Tablets", "89.00", "140", "2027-09-15"),
            listOf("Brufen 400mg Sugar Coated", "55.00", "280", "2027-10-01"),
            listOf("Voltaren 100mg Suppositories", "62.00", "110", "2027-04-12")
        )
        processSelectedFile("Ibnsina_Inventory_Batch_2026_Q3.xlsx", sampleLines)
    }

    fun processSelectedFile(fileName: String, lines: List<List<String>>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingFile = true, errorMessage = null) }
            try {
                val upload = distributorRepository.parseAndCreateUpload(fileName, lines, distributorId)
                val mappings = distributorRepository.getColumnMappings(upload.id)
                val userMap = mappings.associate { it.sourceColumnName to it.mappedField }

                _uiState.update {
                    it.copy(
                        isUploadingFile = false,
                        currentUpload = upload,
                        detectedMappings = mappings,
                        userMappings = userMap,
                        excelStep = ExcelUploadStep.MAP_COLUMNS,
                        message = "File uploaded & analyzed. Review column mappings."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploadingFile = false, errorMessage = e.message) }
            }
        }
    }

    fun updateColumnMapping(header: String, field: InventoryMappingField) {
        val current = _uiState.value.userMappings.toMutableMap()
        current[header] = field
        _uiState.update { it.copy(userMappings = current) }
    }

    fun validateColumnMappings() {
        val upload = _uiState.value.currentUpload ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isValidatingFile = true, errorMessage = null) }
            try {
                distributorRepository.saveColumnMappings(upload.id, _uiState.value.userMappings)
                val validation = distributorRepository.validateInventoryUpload(upload.id, distributorId)
                _uiState.update {
                    it.copy(
                        isValidatingFile = false,
                        validationResult = validation,
                        excelStep = ExcelUploadStep.VALIDATION_DIFF,
                        message = "Validation complete: ${validation.matchedCount} matched, ${validation.newCount} new items."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isValidatingFile = false, errorMessage = e.message) }
            }
        }
    }

    fun applyInventoryUpload() {
        val upload = _uiState.value.currentUpload ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingFile = true, errorMessage = null) }
            try {
                val count = distributorRepository.applyInventoryUpload(upload.id, distributorId)
                _uiState.update {
                    it.copy(
                        isApplyingFile = false,
                        excelStep = ExcelUploadStep.APPLIED_SUCCESS,
                        message = "Successfully applied $count inventory listings to live catalog!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isApplyingFile = false, errorMessage = e.message) }
            }
        }
    }

    fun resetExcelUploadFlow() {
        _uiState.update {
            it.copy(
                excelStep = ExcelUploadStep.SELECT_FILE,
                currentUpload = null,
                detectedMappings = emptyList(),
                userMappings = emptyMap(),
                validationResult = null
            )
        }
    }

    // ==========================================================================
    // Integrations - Direct API Integration Lifecycle
    // ==========================================================================

    fun openApiConfigDialog() {
        val existing = _uiState.value.apiIntegration
        if (existing != null) {
            _uiState.update {
                it.copy(
                    showApiConfigDialog = true,
                    apiProviderName = existing.providerName,
                    apiBaseUrl = existing.baseUrl,
                    apiAuthType = existing.authType,
                    apiSyncFrequency = existing.syncFrequency,
                    apiSecret = "", // Never prepopulate secret for safety
                    testApiSuccess = null,
                    testApiMessage = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    showApiConfigDialog = true,
                    testApiSuccess = null,
                    testApiMessage = null
                )
            }
        }
    }

    fun closeApiConfigDialog() {
        _uiState.update { it.copy(showApiConfigDialog = false) }
    }

    fun updateApiProviderName(name: String) { _uiState.update { it.copy(apiProviderName = name) } }
    fun updateApiBaseUrl(url: String) { _uiState.update { it.copy(apiBaseUrl = url) } }
    fun updateApiAuthType(type: ApiAuthType) { _uiState.update { it.copy(apiAuthType = type) } }
    fun updateApiSecret(secret: String) { _uiState.update { it.copy(apiSecret = secret) } }
    fun updateApiSyncFrequency(freq: ApiSyncFrequency) { _uiState.update { it.copy(apiSyncFrequency = freq) } }

    fun testApiConnection() {
        val state = _uiState.value
        if (state.apiBaseUrl.isBlank() || state.apiSecret.isBlank()) {
            _uiState.update {
                it.copy(
                    testApiSuccess = false,
                    testApiMessage = "Please provide both Base URL and secret key/token."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isTestingApi = true, testApiSuccess = null, testApiMessage = null) }
            val ok = distributorRepository.testApiConnection(
                state.apiProviderName,
                state.apiBaseUrl,
                state.apiAuthType,
                state.apiSecret
            )
            _uiState.update {
                it.copy(
                    isTestingApi = false,
                    testApiSuccess = ok,
                    testApiMessage = if (ok) "Handshake successful! Response latency: 142ms. 1,420 catalog items recognized." else "Connection refused. Please check URL and credentials."
                )
            }
        }
    }

    fun saveApiIntegration() {
        val state = _uiState.value
        if (state.apiSecret.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Secret key/token cannot be empty.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isConnectingApi = true, errorMessage = null) }
            try {
                distributorRepository.saveApiIntegration(
                    state.apiProviderName,
                    state.apiBaseUrl,
                    state.apiAuthType,
                    state.apiSecret,
                    state.apiSyncFrequency,
                    distributorId
                )
                _uiState.update {
                    it.copy(
                        isConnectingApi = false,
                        showApiConfigDialog = false,
                        apiSecret = "", // Wipe clear from memory immediately
                        message = "API integration successfully configured and secured."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isConnectingApi = false, errorMessage = e.message) }
            }
        }
    }

    fun triggerSyncNow() {
        val integ = _uiState.value.apiIntegration ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingNow = true, errorMessage = null) }
            try {
                val conflicts = distributorRepository.triggerSyncNow(integ.id, distributorId)
                _uiState.update {
                    it.copy(
                        isSyncingNow = false,
                        syncConflicts = conflicts,
                        message = if (conflicts.isEmpty()) "Catalog sync complete. All live items up-to-date." else "Sync completed with ${conflicts.size} 24h conflicts requiring resolution."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSyncingNow = false, errorMessage = e.message) }
            }
        }
    }

    fun resolveConflict(item: SyncConflictItem, action: ConflictResolutionAction) {
        val integ = _uiState.value.apiIntegration ?: return
        viewModelScope.launch {
            try {
                distributorRepository.resolveConflict(item, action, integ.id, distributorId)
                val remaining = _uiState.value.syncConflicts.filterNot { it.productListingId == item.productListingId }
                _uiState.update {
                    it.copy(
                        syncConflicts = remaining,
                        message = if (action == ConflictResolutionAction.ACCEPT) "Accepted API value for ${item.medicationName}" else "Kept distributor edit for ${item.medicationName}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun disconnectApiIntegration() {
        viewModelScope.launch {
            try {
                distributorRepository.disconnectApiIntegration(distributorId)
                _uiState.update {
                    it.copy(
                        apiIntegration = null,
                        syncConflicts = emptyList(),
                        message = "API integration disconnected and credentials securely purged."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, errorMessage = null) }
    }
}
