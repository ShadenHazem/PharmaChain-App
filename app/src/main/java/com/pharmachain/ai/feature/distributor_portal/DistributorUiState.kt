package com.pharmachain.ai.feature.distributor_portal

import com.pharmachain.ai.core.model.ApiAuthType
import com.pharmachain.ai.core.model.ApiIntegration
import com.pharmachain.ai.core.model.ApiSyncFrequency
import com.pharmachain.ai.core.model.CommissionRecord
import com.pharmachain.ai.core.model.ConflictResolutionAction
import com.pharmachain.ai.core.model.InventoryColumnMapping
import com.pharmachain.ai.core.model.InventoryMappingField
import com.pharmachain.ai.core.model.InventoryUpload
import com.pharmachain.ai.core.model.ListingSource
import com.pharmachain.ai.core.model.LowStockAlert
import com.pharmachain.ai.core.model.Order
import com.pharmachain.ai.core.model.OrderStatus
import com.pharmachain.ai.core.model.ProductListing
import com.pharmachain.ai.core.model.SyncConflictItem
import com.pharmachain.ai.core.network.dto.DistributorAnalyticsDto
import com.pharmachain.ai.core.network.dto.InventoryValidationResponseDto

enum class DistributorTab {
    OVERVIEW,
    INVENTORY,
    ORDERS,
    INTEGRATIONS,
    COMMISSION
}

enum class IntegrationSubTab {
    EXCEL_UPLOAD,
    API_SYNC
}

enum class ExcelUploadStep {
    SELECT_FILE,
    MAP_COLUMNS,
    VALIDATION_DIFF,
    APPLIED_SUCCESS
}

data class DistributorUiState(
    val selectedTab: DistributorTab = DistributorTab.OVERVIEW,
    val companyName: String = "Cairo Drugs",
    val reputationScore: Double = 4.7,
    val fulfillmentRatePct: Double = 98.4,
    val onTimeDeliveryPct: Double = 96.2,
    val commissionRatePct: Double = 4.5,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,

    // Overview & Analytics
    val selectedPeriodDays: Int = 30,
    val analytics: DistributorAnalyticsDto? = null,
    val lowStockAlerts: List<LowStockAlert> = emptyList(),

    // Inventory
    val listings: List<ProductListing> = emptyList(),
    val listingSources: Map<String, ListingSource> = emptyMap(),
    val inventorySearchQuery: String = "",

    // Orders
    val incomingOrders: List<Order> = emptyList(),
    val selectedOrderStatusFilter: OrderStatus? = null,
    val orderToReject: Order? = null,
    val rejectionReasonText: String = "",

    // Integrations - General
    val integrationSubTab: IntegrationSubTab = IntegrationSubTab.EXCEL_UPLOAD,

    // Integrations - Excel Upload Flow
    val excelStep: ExcelUploadStep = ExcelUploadStep.SELECT_FILE,
    val currentUpload: InventoryUpload? = null,
    val uploadHistory: List<InventoryUpload> = emptyList(),
    val detectedMappings: List<InventoryColumnMapping> = emptyList(),
    val userMappings: Map<String, InventoryMappingField> = emptyMap(),
    val validationResult: InventoryValidationResponseDto? = null,
    val isUploadingFile: Boolean = false,
    val isValidatingFile: Boolean = false,
    val isApplyingFile: Boolean = false,

    // Integrations - Direct API Integration
    val apiIntegration: ApiIntegration? = null,
    val isConnectingApi: Boolean = false,
    val isTestingApi: Boolean = false,
    val testApiSuccess: Boolean? = null,
    val testApiMessage: String? = null,
    val isSyncingNow: Boolean = false,
    val syncConflicts: List<SyncConflictItem> = emptyList(),
    val showApiConfigDialog: Boolean = false,

    // API Config Form State
    val apiProviderName: String = "Odoo ERP Enterprise",
    val apiBaseUrl: String = "https://erp.ibnsina-pharma.com/api/v2",
    val apiAuthType: ApiAuthType = ApiAuthType.API_KEY,
    val apiSecret: String = "",
    val apiSyncFrequency: ApiSyncFrequency = ApiSyncFrequency.HOURLY,

    // Commission
    val commissionLedger: List<CommissionRecord> = emptyList()
) {
    val pendingApprovalCount: Int
        get() = incomingOrders.count { it.status == OrderStatus.PENDING_APPROVAL }

    val activeLowStockCount: Int
        get() = lowStockAlerts.size

    val conflictCount: Int
        get() = syncConflicts.size
}
