package com.pharmachain.ai.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ==============================================================================
// Section 6: Inventory Upload DTOs
// ==============================================================================

@JsonClass(generateAdapter = true)
data class UploadInventoryResponseDto(
    @Json(name = "uploadId") val uploadId: String,
    @Json(name = "distributorId") val distributorId: String,
    @Json(name = "fileName") val fileName: String,
    @Json(name = "remoteJobId") val remoteJobId: String,
    @Json(name = "detectedHeaders") val detectedHeaders: List<String>,
    @Json(name = "suggestedMappings") val suggestedMappings: Map<String, String>,
    @Json(name = "previewRows") val previewRows: List<List<String>>,
    @Json(name = "totalRowsEstimated") val totalRowsEstimated: Int
)

@JsonClass(generateAdapter = true)
data class SaveMappingRequestDto(
    @Json(name = "mappings") val mappings: Map<String, String> // header -> mappedField name
)

@JsonClass(generateAdapter = true)
data class InventoryValidationRowDto(
    @Json(name = "rowIndex") val rowIndex: Int,
    @Json(name = "productNameOrSku") val productNameOrSku: String,
    @Json(name = "matchedMedicationId") val matchedMedicationId: String?,
    @Json(name = "matchedMedicationName") val matchedMedicationName: String?,
    @Json(name = "incomingPrice") val incomingPrice: Double?,
    @Json(name = "currentPrice") val currentPrice: Double?,
    @Json(name = "incomingStock") val incomingStock: Int?,
    @Json(name = "currentStock") val currentStock: Int?,
    @Json(name = "matchStatus") val matchStatus: String, // "MATCHED", "NEW", "ERROR"
    @Json(name = "errorMessage") val errorMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class InventoryValidationResponseDto(
    @Json(name = "uploadId") val uploadId: String,
    @Json(name = "totalRows") val totalRows: Int,
    @Json(name = "matchedCount") val matchedCount: Int,
    @Json(name = "newCount") val newCount: Int,
    @Json(name = "errorCount") val errorCount: Int,
    @Json(name = "validationRows") val validationRows: List<InventoryValidationRowDto>
)

@JsonClass(generateAdapter = true)
data class ApplyInventoryResponseDto(
    @Json(name = "uploadId") val uploadId: String,
    @Json(name = "status") val status: String,
    @Json(name = "updatedCount") val updatedCount: Int,
    @Json(name = "createdCount") val createdCount: Int,
    @Json(name = "skippedCount") val skippedCount: Int,
    @Json(name = "appliedAt") val appliedAt: Long
)

@JsonClass(generateAdapter = true)
data class InventoryUploadItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "fileName") val fileName: String,
    @Json(name = "status") val status: String,
    @Json(name = "rowCount") val rowCount: Int?,
    @Json(name = "successCount") val successCount: Int?,
    @Json(name = "errorCount") val errorCount: Int?,
    @Json(name = "uploadedAt") val uploadedAt: Long,
    @Json(name = "appliedAt") val appliedAt: Long?
)

// ==============================================================================
// Section 6: API Integration DTOs
// ==============================================================================

@JsonClass(generateAdapter = true)
data class CreateIntegrationRequestDto(
    @Json(name = "providerName") val providerName: String,
    @Json(name = "baseUrl") val baseUrl: String,
    @Json(name = "authType") val authType: String,
    @Json(name = "credentialRef") val credentialRef: String,
    @Json(name = "syncFrequency") val syncFrequency: String
)

@JsonClass(generateAdapter = true)
data class IntegrationResponseDto(
    @Json(name = "id") val id: String,
    @Json(name = "distributorId") val distributorId: String,
    @Json(name = "providerName") val providerName: String,
    @Json(name = "baseUrl") val baseUrl: String,
    @Json(name = "authType") val authType: String,
    @Json(name = "credentialRef") val credentialRef: String,
    @Json(name = "syncFrequency") val syncFrequency: String,
    @Json(name = "lastSyncAt") val lastSyncAt: Long?,
    @Json(name = "lastSyncStatus") val lastSyncStatus: String,
    @Json(name = "lastSyncErrorMessage") val lastSyncErrorMessage: String?,
    @Json(name = "isActive") val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class TestConnectionResponseDto(
    @Json(name = "isSuccessful") val isSuccessful: Boolean,
    @Json(name = "responseTimeMs") val responseTimeMs: Long,
    @Json(name = "message") val message: String,
    @Json(name = "sampleProductsFound") val sampleProductsFound: Int = 0
)

@JsonClass(generateAdapter = true)
data class SyncStatusResponseDto(
    @Json(name = "integrationId") val integrationId: String,
    @Json(name = "lastSyncAt") val lastSyncAt: Long?,
    @Json(name = "lastSyncStatus") val lastSyncStatus: String,
    @Json(name = "lastSyncErrorMessage") val lastSyncErrorMessage: String?,
    @Json(name = "totalSynced") val totalSynced: Int,
    @Json(name = "conflictCount") val conflictCount: Int
)

@JsonClass(generateAdapter = true)
data class SyncConflictDto(
    @Json(name = "productListingId") val productListingId: String,
    @Json(name = "medicationName") val medicationName: String,
    @Json(name = "currentPrice") val currentPrice: Double,
    @Json(name = "incomingPrice") val incomingPrice: Double,
    @Json(name = "currentStock") val currentStock: Int,
    @Json(name = "incomingStock") val incomingStock: Int,
    @Json(name = "lastSource") val lastSource: String,
    @Json(name = "lastUpdatedAt") val lastUpdatedAt: Long
)

@JsonClass(generateAdapter = true)
data class ResolveConflictRequestDto(
    @Json(name = "productListingId") val productListingId: String,
    @Json(name = "action") val action: String // "ACCEPT", "REJECT"
)

// ==============================================================================
// Section 6: Analytics & Low Stock DTOs
// ==============================================================================

@JsonClass(generateAdapter = true)
data class DistributorAnalyticsDto(
    @Json(name = "distributorId") val distributorId: String,
    @Json(name = "periodDays") val periodDays: Int,
    @Json(name = "totalRevenue") val totalRevenue: Double,
    @Json(name = "totalOrders") val totalOrders: Int,
    @Json(name = "totalUnitsSold") val totalUnitsSold: Int,
    @Json(name = "avgOrderValue") val avgOrderValue: Double,
    @Json(name = "fulfillmentRatePct") val fulfillmentRatePct: Double,
    @Json(name = "avgApprovalTimeMinutes") val avgApprovalTimeMinutes: Double,
    @Json(name = "topSellingMedications") val topSellingMedications: List<TopSellingMedicationDto>,
    @Json(name = "dailyRevenueTrend") val dailyRevenueTrend: List<DailyRevenuePointDto>
)

@JsonClass(generateAdapter = true)
data class TopSellingMedicationDto(
    @Json(name = "medicationId") val medicationId: String,
    @Json(name = "medicationName") val medicationName: String,
    @Json(name = "unitsSold") val unitsSold: Int,
    @Json(name = "revenue") val revenue: Double
)

@JsonClass(generateAdapter = true)
data class DailyRevenuePointDto(
    @Json(name = "dayTimestamp") val dayTimestamp: Long,
    @Json(name = "revenue") val revenue: Double,
    @Json(name = "orderCount") val orderCount: Int
)

@JsonClass(generateAdapter = true)
data class LowStockAlertDto(
    @Json(name = "id") val id: String,
    @Json(name = "distributorId") val distributorId: String,
    @Json(name = "medicationId") val medicationId: String,
    @Json(name = "medicationName") val medicationName: String,
    @Json(name = "productListingId") val productListingId: String,
    @Json(name = "currentStock") val currentStock: Int,
    @Json(name = "thresholdStock") val thresholdStock: Int,
    @Json(name = "unitPrice") val unitPrice: Double,
    @Json(name = "triggeredAt") val triggeredAt: Long
)

@JsonClass(generateAdapter = true)
data class UpdateLowStockThresholdDto(
    @Json(name = "thresholdStock") val thresholdStock: Int
)

@JsonClass(generateAdapter = true)
data class RejectOrderRequestDto(
    @Json(name = "reason") val reason: String
)
