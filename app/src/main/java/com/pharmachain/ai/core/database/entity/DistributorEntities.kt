package com.pharmachain.ai.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pharmachain.ai.core.model.ApiAuthType
import com.pharmachain.ai.core.model.ApiSyncFrequency
import com.pharmachain.ai.core.model.ApiSyncStatus
import com.pharmachain.ai.core.model.InventoryMappingField
import com.pharmachain.ai.core.model.InventoryUploadStatus
import com.pharmachain.ai.core.model.ListingSourceType

/**
 * Tracks distributor inventory file uploads (CSV / Excel).
 */
@Entity(
    tableName = "distributor_inventory_uploads",
    foreignKeys = [
        ForeignKey(
            entity = DistributorProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["distributorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["distributorId"])]
)
data class InventoryUploadEntity(
    @PrimaryKey val id: String,
    val distributorId: String,
    val fileName: String,
    val remoteJobId: String,
    val status: InventoryUploadStatus,
    val rowCount: Int?,
    val successCount: Int?,
    val errorCount: Int?,
    val uploadedAt: Long,
    val appliedAt: Long?
)

/**
 * Column mappings detected/configured for distributor inventory imports.
 */
@Entity(
    tableName = "distributor_column_mappings",
    foreignKeys = [
        ForeignKey(
            entity = InventoryUploadEntity::class,
            parentColumns = ["id"],
            childColumns = ["uploadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["uploadId"])]
)
data class InventoryColumnMappingEntity(
    @PrimaryKey val id: String,
    val uploadId: String,
    val sourceColumnName: String,
    val mappedField: InventoryMappingField,
    val serverConfidence: Double
)

/**
 * API Integration configuration metadata for distributor ERP / custom systems.
 * CRITICAL: The actual secret is NEVER stored here. Room holds only metadata and [credentialRef]
 * pointing to EncryptedSharedPreferences.
 */
@Entity(
    tableName = "distributor_api_integrations",
    foreignKeys = [
        ForeignKey(
            entity = DistributorProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["distributorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["distributorId"], unique = true)]
)
data class ApiIntegrationEntity(
    @PrimaryKey val id: String,
    val distributorId: String,
    val providerName: String,
    val baseUrl: String,
    val authType: ApiAuthType,
    val credentialRef: String,
    val syncFrequency: ApiSyncFrequency,
    val lastSyncAt: Long?,
    val lastSyncStatus: ApiSyncStatus,
    val lastSyncErrorMessage: String?,
    val isActive: Boolean
)

/**
 * Tracks the source of truth for each ProductListing's price & stock to resolve conflicts.
 */
@Entity(
    tableName = "listing_sources",
    foreignKeys = [
        ForeignKey(
            entity = ProductListingEntity::class,
            parentColumns = ["id"],
            childColumns = ["productListingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productListingId"]),
        Index(value = ["lastUpdatedByUploadId"]),
        Index(value = ["lastUpdatedByIntegrationId"])
    ]
)
data class ListingSourceEntity(
    @PrimaryKey val productListingId: String,
    val source: ListingSourceType,
    val lastUpdatedAt: Long,
    val lastUpdatedByUploadId: String?,
    val lastUpdatedByIntegrationId: String?
)

/**
 * Cached snapshot for distributor offline-first analytics display.
 */
@Entity(
    tableName = "distributor_analytics_snapshots"
)
data class DistributorAnalyticsSnapshotEntity(
    @PrimaryKey val distributorId: String,
    val periodStart: Long,
    val periodEnd: Long,
    val totalRevenue: Double,
    val totalOrders: Int,
    val totalUnitsSold: Int,
    val topSellingMedicationId: String?,
    val avgOrderValue: Double,
    val fulfillmentRatePct: Double,
    val avgApprovalTimeMinutes: Double,
    val computedAt: Long
)

/**
 * Low stock alerts triggering replenishment actions.
 */
@Entity(
    tableName = "distributor_low_stock_alerts",
    foreignKeys = [
        ForeignKey(
            entity = DistributorProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["distributorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductListingEntity::class,
            parentColumns = ["id"],
            childColumns = ["productListingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["distributorId"]), Index(value = ["productListingId"])]
)
data class LowStockAlertEntity(
    @PrimaryKey val id: String,
    val distributorId: String,
    val medicationId: String,
    val productListingId: String,
    val currentStock: Int,
    val thresholdStock: Int,
    val triggeredAt: Long,
    val isDismissed: Boolean
)
