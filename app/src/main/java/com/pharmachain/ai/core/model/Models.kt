package com.pharmachain.ai.core.model

enum class Role {
    PHARMACIST,
    DISTRIBUTOR,
    ADMIN
}

data class User(
    val id: String,
    val role: Role,
    val fullName: String,
    val email: String,
    val phone: String,
    val createdAt: Long
)

data class PharmacyProfile(
    val id: String,
    val userId: String,
    val pharmacyName: String,
    val licenseNumber: String,
    val governorate: String,
    val city: String,
    val addressLine: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isVerified: Boolean = true
)

data class DistributorProfile(
    val id: String,
    val userId: String,
    val companyName: String,
    val taxRegistrationId: String,
    val reputationScore: Double, // 0.0 - 5.0 server computed rolling avg
    val totalOrdersFulfilled: Int,
    val defaultCommissionRatePct: Double, // e.g. 4.5 = 4.5%
    val isVerified: Boolean = true
)

data class Medication(
    val id: String,
    val genericName: String,
    val brandName: String,
    val form: String, // e.g. Tablet, Syrup, Vial, Cream
    val strength: String, // e.g. 500mg, 10ml
    val category: String, // e.g. Antibiotics, Cardiovascular, Analgesics
    val imageUrl: String? = null
)

data class ProductListing(
    val id: String,
    val medicationId: String,
    val distributorId: String,
    val distributorName: String,
    val distributorReputation: Double,
    val price: Double,
    val stockQuantity: Int,
    val expiryDate: Long? = null,
    val updatedAt: Long
)

data class MedicationWithListings(
    val medication: Medication,
    val listings: List<ProductListing>,
    val topOffer: ProductListing? = null,
    val minPrice: Double = listings.minOfOrNull { it.price } ?: 0.0,
    val maxPrice: Double = listings.maxOfOrNull { it.price } ?: 0.0
)

data class CartItem(
    val medicationId: String,
    val brandName: String,
    val genericName: String,
    val category: String,
    val form: String,
    val strength: String,
    val selectedListingId: String,
    val distributorId: String,
    val distributorName: String,
    val distributorReputation: Double,
    val unitPrice: Double,
    val quantity: Int
) {
    val lineTotal: Double get() = unitPrice * quantity
}

enum class OrderStatus {
    PENDING_APPROVAL,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REJECTED,
    EXPIRED
}

data class Order(
    val id: String,
    val pharmacyId: String,
    val pharmacyName: String,
    val distributorId: String,
    val distributorName: String,
    val status: OrderStatus,
    val totalAmount: Double,
    val placedAt: Long,
    val updatedAt: Long,
    val deliveryAddress: String,
    val items: List<OrderItem> = emptyList(),
    val rejectionReason: String? = null
)

// ==============================================================================
// Distributor Portal Models (Uploads, Integrations, Sources, Analytics, Alerts)
// ==============================================================================

enum class InventoryUploadStatus {
    UPLOADED,
    PARSING,
    PARSED,
    MAPPING_REQUIRED,
    VALIDATED,
    APPLYING,
    APPLIED,
    ERROR
}

data class InventoryUpload(
    val id: String,
    val distributorId: String,
    val fileName: String,
    val remoteJobId: String,
    val status: InventoryUploadStatus,
    val rowCount: Int? = null,
    val successCount: Int? = null,
    val errorCount: Int? = null,
    val uploadedAt: Long,
    val appliedAt: Long? = null
)

enum class InventoryMappingField(val displayName: String) {
    PRODUCT_NAME_OR_SKU("Product Name / SKU"),
    PRICE("Price (EGP)"),
    STOCK_QUANTITY("Stock Quantity"),
    EXPIRY_DATE("Expiry Date"),
    IGNORE("Ignore Column")
}

data class InventoryColumnMapping(
    val id: String,
    val uploadId: String,
    val sourceColumnName: String,
    val mappedField: InventoryMappingField,
    val serverConfidence: Double = 0.95
)

enum class ApiAuthType(val displayName: String) {
    API_KEY("API Key / Header Secret"),
    OAUTH2_CLIENT_CREDENTIALS("OAuth2 Client Credentials"),
    BASIC_AUTH("Basic Auth (Username / Password)")
}

enum class ApiSyncFrequency(val displayName: String) {
    MANUAL("Manual (On Demand)"),
    HOURLY("Hourly"),
    EVERY_SIX_HOURS("Every 6 Hours"),
    DAILY("Daily (Midnight EET)")
}

enum class ApiSyncStatus {
    NEVER_RUN,
    SUCCESS,
    PARTIAL_FAILURE,
    FAILED
}

data class ApiIntegration(
    val id: String,
    val distributorId: String,
    val providerName: String,
    val baseUrl: String,
    val authType: ApiAuthType,
    val credentialRef: String,
    val syncFrequency: ApiSyncFrequency,
    val lastSyncAt: Long? = null,
    val lastSyncStatus: ApiSyncStatus = ApiSyncStatus.NEVER_RUN,
    val lastSyncErrorMessage: String? = null,
    val isActive: Boolean = true
)

enum class ListingSourceType {
    MANUAL_ENTRY,
    EXCEL_UPLOAD,
    API_SYNC
}

data class ListingSource(
    val productListingId: String,
    val source: ListingSourceType,
    val lastUpdatedAt: Long,
    val lastUpdatedByUploadId: String? = null,
    val lastUpdatedByIntegrationId: String? = null
)

data class DistributorAnalyticsSnapshot(
    val distributorId: String,
    val periodStart: Long,
    val periodEnd: Long,
    val totalRevenue: Double,
    val totalOrders: Int,
    val totalUnitsSold: Int,
    val topSellingMedicationId: String? = null,
    val avgOrderValue: Double,
    val fulfillmentRatePct: Double,
    val avgApprovalTimeMinutes: Double,
    val computedAt: Long
)

data class LowStockAlert(
    val id: String,
    val distributorId: String,
    val medicationId: String,
    val medicationName: String,
    val productListingId: String,
    val currentStock: Int,
    val thresholdStock: Int = 10,
    val unitPrice: Double = 0.0,
    val triggeredAt: Long,
    val isDismissed: Boolean = false
)

data class SyncConflictItem(
    val productListingId: String,
    val medicationName: String,
    val currentPrice: Double,
    val incomingPrice: Double,
    val currentStock: Int,
    val incomingStock: Int,
    val lastSource: ListingSourceType,
    val lastUpdatedAt: Long
)

enum class ConflictResolutionAction {
    ACCEPT,
    REJECT
}

data class OrderItem(
    val id: String,
    val orderId: String,
    val productListingId: String,
    val medicationName: String,
    val quantity: Int,
    val unitPriceAtOrder: Double,
    val lineTotal: Double
)

enum class CommissionStatus {
    PENDING,
    INVOICED,
    PAID
}

data class CommissionRecord(
    val id: String,
    val orderId: String,
    val distributorId: String,
    val orderTotal: Double,
    val commissionRatePct: Double,
    val commissionAmount: Double,
    val status: CommissionStatus,
    val computedAt: Long
)

enum class UploadStatus {
    UPLOADED,
    PARSING,
    PARSED,
    VALIDATED,
    ERROR
}

data class SalesHistoryUpload(
    val id: String,
    val pharmacyId: String,
    val fileName: String,
    val fileSizeKb: Double,
    val remoteJobId: String,
    val status: UploadStatus,
    val rowCount: Int? = null,
    val uploadedAt: Long
)

enum class MappingField(val displayName: String) {
    DATE("Sale Date"),
    SKU_OR_NAME("SKU / Medication Name"),
    QUANTITY_SOLD("Quantity Sold"),
    UNIT_PRICE("Unit Price (EGP)"),
    IGNORE("Ignore Column")
}

data class ColumnMapping(
    val id: String,
    val uploadId: String,
    val sourceColumnName: String,
    val mappedField: MappingField,
    val serverConfidence: Double = 0.95
)

enum class ForecastDuration(val days: Int) {
    SEVEN(7),
    FOURTEEN(14),
    THIRTY(30)
}

enum class JobStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED
}

data class ForecastJob(
    val id: String,
    val pharmacyId: String,
    val uploadId: String,
    val durationDays: ForecastDuration,
    val maxBudgetEGP: Double,
    val status: JobStatus,
    val createdAt: Long,
    val completedAt: Long? = null
)

data class ForecastResult(
    val id: String,
    val jobId: String,
    val medicationId: String,
    val medicationName: String,
    val category: String,
    val predictedQuantity: Int,
    val confidenceScore: Double, // 0.0 - 1.0
    val seasonalTrendDetected: Boolean
)

data class SmartCartItem(
    val id: String,
    val jobId: String,
    val medicationId: String,
    val medicationName: String,
    val selectedProductListingId: String,
    val distributorId: String,
    val distributorName: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double,
    val distributorReputationAtSelection: Double,
    val scoringRationale: String, // e.g. "Best price (42.50 EGP), 4.9★ rating"
    val isSelected: Boolean = true
)
