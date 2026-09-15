package com.pharmachain.ai.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pharmachain.ai.core.model.CommissionStatus
import com.pharmachain.ai.core.model.ForecastDuration
import com.pharmachain.ai.core.model.JobStatus
import com.pharmachain.ai.core.model.MappingField
import com.pharmachain.ai.core.model.OrderStatus
import com.pharmachain.ai.core.model.Role
import com.pharmachain.ai.core.model.UploadStatus

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val role: Role,
    val fullName: String,
    val email: String,
    val phone: String,
    val createdAt: Long
)

@Entity(
    tableName = "pharmacy_profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class PharmacyProfileEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val pharmacyName: String,
    val licenseNumber: String,
    val governorate: String,
    val city: String,
    val addressLine: String,
    val latitude: Double?,
    val longitude: Double?,
    val isVerified: Boolean
)

@Entity(
    tableName = "distributor_profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class DistributorProfileEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val companyName: String,
    val taxRegistrationId: String,
    val reputationScore: Double, // 0.0 - 5.0
    val totalOrdersFulfilled: Int,
    val defaultCommissionRatePct: Double,
    val isVerified: Boolean
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey val id: String,
    val genericName: String,
    val brandName: String,
    val form: String,
    val strength: String,
    val category: String,
    val imageUrl: String?
)

@Entity(
    tableName = "product_listings",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DistributorProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["distributorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicationId"]), Index(value = ["distributorId"])]
)
data class ProductListingEntity(
    @PrimaryKey val id: String,
    val medicationId: String,
    val distributorId: String,
    val distributorName: String,
    val distributorReputation: Double,
    val price: Double,
    val stockQuantity: Int,
    val expiryDate: Long?,
    val updatedAt: Long
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val pharmacyId: String,
    val pharmacyName: String,
    val distributorId: String,
    val distributorName: String,
    val status: OrderStatus,
    val totalAmount: Double,
    val placedAt: Long,
    val updatedAt: Long,
    val deliveryAddress: String,
    val rejectionReason: String? = null
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val productListingId: String,
    val medicationName: String,
    val quantity: Int,
    val unitPriceAtOrder: Double,
    val lineTotal: Double
)

@Entity(
    tableName = "commission_records",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"]), Index(value = ["distributorId"])]
)
data class CommissionRecordEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val distributorId: String,
    val orderTotal: Double,
    val commissionRatePct: Double,
    val commissionAmount: Double,
    val status: CommissionStatus,
    val computedAt: Long
)

@Entity(tableName = "sales_history_uploads")
data class SalesHistoryUploadEntity(
    @PrimaryKey val id: String,
    val pharmacyId: String,
    val fileName: String,
    val fileSizeKb: Double,
    val remoteJobId: String,
    val status: UploadStatus,
    val rowCount: Int?,
    val uploadedAt: Long
)

@Entity(
    tableName = "column_mappings",
    foreignKeys = [
        ForeignKey(
            entity = SalesHistoryUploadEntity::class,
            parentColumns = ["id"],
            childColumns = ["uploadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["uploadId"])]
)
data class ColumnMappingEntity(
    @PrimaryKey val id: String,
    val uploadId: String,
    val sourceColumnName: String,
    val mappedField: MappingField,
    val serverConfidence: Double
)

@Entity(tableName = "forecast_jobs")
data class ForecastJobEntity(
    @PrimaryKey val id: String,
    val pharmacyId: String,
    val uploadId: String,
    val durationDays: ForecastDuration,
    val maxBudgetEGP: Double,
    val status: JobStatus,
    val createdAt: Long,
    val completedAt: Long?
)

@Entity(
    tableName = "forecast_results",
    foreignKeys = [
        ForeignKey(
            entity = ForecastJobEntity::class,
            parentColumns = ["id"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["jobId"])]
)
data class ForecastResultEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val medicationId: String,
    val medicationName: String,
    val category: String,
    val predictedQuantity: Int,
    val confidenceScore: Double,
    val seasonalTrendDetected: Boolean
)

@Entity(
    tableName = "smart_cart_items",
    foreignKeys = [
        ForeignKey(
            entity = ForecastJobEntity::class,
            parentColumns = ["id"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["jobId"])]
)
data class SmartCartItemEntity(
    @PrimaryKey val id: String,
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
    val scoringRationale: String,
    val isSelected: Boolean
)
