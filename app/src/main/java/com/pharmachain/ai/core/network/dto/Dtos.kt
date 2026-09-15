package com.pharmachain.ai.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    @Json(name = "role") val role: String,
    @Json(name = "fullName") val fullName: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "password") val password: String,
    @Json(name = "pharmacyName") val pharmacyName: String? = null,
    @Json(name = "licenseNumber") val licenseNumber: String? = null,
    @Json(name = "governorate") val governorate: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "addressLine") val addressLine: String? = null,
    @Json(name = "companyName") val companyName: String? = null,
    @Json(name = "taxRegistrationId") val taxRegistrationId: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "refreshToken") val refreshToken: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "role") val role: String,
    @Json(name = "fullName") val fullName: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequestDto(
    @Json(name = "refreshToken") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class MedicationWithListingsDto(
    @Json(name = "id") val id: String,
    @Json(name = "genericName") val genericName: String,
    @Json(name = "brandName") val brandName: String,
    @Json(name = "form") val form: String,
    @Json(name = "strength") val strength: String,
    @Json(name = "category") val category: String,
    @Json(name = "imageUrl") val imageUrl: String?,
    @Json(name = "listings") val listings: List<ProductListingDto>
)

@JsonClass(generateAdapter = true)
data class ProductListingDto(
    @Json(name = "id") val id: String,
    @Json(name = "medicationId") val medicationId: String,
    @Json(name = "distributorId") val distributorId: String,
    @Json(name = "distributorName") val distributorName: String,
    @Json(name = "distributorReputation") val distributorReputation: Double,
    @Json(name = "price") val price: Double,
    @Json(name = "stockQuantity") val stockQuantity: Int,
    @Json(name = "expiryDate") val expiryDate: Long?,
    @Json(name = "updatedAt") val updatedAt: Long
)

@JsonClass(generateAdapter = true)
data class OrderItemCreateDto(
    @Json(name = "productListingId") val productListingId: String,
    @Json(name = "medicationName") val medicationName: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unitPrice") val unitPrice: Double
)

@JsonClass(generateAdapter = true)
data class CreateOrderRequestDto(
    @Json(name = "distributorId") val distributorId: String,
    @Json(name = "items") val items: List<OrderItemCreateDto>,
    @Json(name = "deliveryAddress") val deliveryAddress: String
)

@JsonClass(generateAdapter = true)
data class OrderDto(
    @Json(name = "id") val id: String,
    @Json(name = "pharmacyId") val pharmacyId: String,
    @Json(name = "pharmacyName") val pharmacyName: String,
    @Json(name = "distributorId") val distributorId: String,
    @Json(name = "distributorName") val distributorName: String,
    @Json(name = "status") val status: String,
    @Json(name = "totalAmount") val totalAmount: Double,
    @Json(name = "placedAt") val placedAt: Long,
    @Json(name = "updatedAt") val updatedAt: Long,
    @Json(name = "deliveryAddress") val deliveryAddress: String,
    @Json(name = "items") val items: List<OrderItemDto>
)

@JsonClass(generateAdapter = true)
data class OrderItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "orderId") val orderId: String,
    @Json(name = "productListingId") val productListingId: String,
    @Json(name = "medicationName") val medicationName: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unitPriceAtOrder") val unitPriceAtOrder: Double,
    @Json(name = "lineTotal") val lineTotal: Double
)

@JsonClass(generateAdapter = true)
data class UpdateOrderStatusDto(
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class UploadSalesResponseDto(
    @Json(name = "uploadId") val uploadId: String,
    @Json(name = "status") val status: String,
    @Json(name = "fileName") val fileName: String,
    @Json(name = "rowCount") val rowCount: Int
)

@JsonClass(generateAdapter = true)
data class SuggestedMappingDto(
    @Json(name = "sourceColumn") val sourceColumn: String,
    @Json(name = "suggestedField") val suggestedField: String,
    @Json(name = "confidence") val confidence: Double
)

@JsonClass(generateAdapter = true)
data class PreviewResponseDto(
    @Json(name = "detectedColumns") val detectedColumns: List<String>,
    @Json(name = "suggestedMapping") val suggestedMapping: List<SuggestedMappingDto>,
    @Json(name = "sampleRows") val sampleRows: List<Map<String, String>>
)

@JsonClass(generateAdapter = true)
data class ColumnMappingItemDto(
    @Json(name = "sourceColumnName") val sourceColumnName: String,
    @Json(name = "mappedField") val mappedField: String
)

@JsonClass(generateAdapter = true)
data class SubmitMappingRequestDto(
    @Json(name = "columnMappings") val columnMappings: List<ColumnMappingItemDto>
)

@JsonClass(generateAdapter = true)
data class RowValidationErrorDto(
    @Json(name = "rowIndex") val rowIndex: Int,
    @Json(name = "errorReason") val errorReason: String,
    @Json(name = "rawContent") val rawContent: String
)

@JsonClass(generateAdapter = true)
data class ValidationResponseDto(
    @Json(name = "isValid") val isValid: Boolean,
    @Json(name = "totalRows") val totalRows: Int,
    @Json(name = "validRows") val validRows: Int,
    @Json(name = "errorRows") val errorRows: List<RowValidationErrorDto>,
    @Json(name = "errorSummary") val errorSummary: String
)

@JsonClass(generateAdapter = true)
data class CreateJobRequestDto(
    @Json(name = "uploadId") val uploadId: String,
    @Json(name = "durationDays") val durationDays: String, // SEVEN, FOURTEEN, THIRTY
    @Json(name = "maxBudgetEGP") val maxBudgetEGP: Double
)

@JsonClass(generateAdapter = true)
data class JobCreatedResponseDto(
    @Json(name = "jobId") val jobId: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class ForecastResultItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "medicationId") val medicationId: String,
    @Json(name = "medicationName") val medicationName: String,
    @Json(name = "category") val category: String,
    @Json(name = "predictedQuantity") val predictedQuantity: Int,
    @Json(name = "confidenceScore") val confidenceScore: Double,
    @Json(name = "seasonalTrendDetected") val seasonalTrendDetected: Boolean
)

@JsonClass(generateAdapter = true)
data class ForecastJobStatusDto(
    @Json(name = "jobId") val jobId: String,
    @Json(name = "status") val status: String,
    @Json(name = "results") val results: List<ForecastResultItemDto>
)

@JsonClass(generateAdapter = true)
data class SmartCartItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "jobId") val jobId: String,
    @Json(name = "medicationId") val medicationId: String,
    @Json(name = "medicationName") val medicationName: String,
    @Json(name = "selectedProductListingId") val selectedProductListingId: String,
    @Json(name = "distributorId") val distributorId: String,
    @Json(name = "distributorName") val distributorName: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unitPrice") val unitPrice: Double,
    @Json(name = "lineTotal") val lineTotal: Double,
    @Json(name = "distributorReputationAtSelection") val distributorReputationAtSelection: Double,
    @Json(name = "scoringRationale") val scoringRationale: String
)

@JsonClass(generateAdapter = true)
data class CommissionRecordDto(
    @Json(name = "id") val id: String,
    @Json(name = "orderId") val orderId: String,
    @Json(name = "distributorId") val distributorId: String,
    @Json(name = "orderTotal") val orderTotal: Double,
    @Json(name = "commissionRatePct") val commissionRatePct: Double,
    @Json(name = "commissionAmount") val commissionAmount: Double,
    @Json(name = "status") val status: String,
    @Json(name = "computedAt") val computedAt: Long
)

@JsonClass(generateAdapter = true)
data class ReputationBreakdownDto(
    @Json(name = "overallScore") val overallScore: Double,
    @Json(name = "fulfillmentRatePct") val fulfillmentRatePct: Double,
    @Json(name = "avgDeliveryHours") val avgDeliveryHours: Double,
    @Json(name = "pharmacistRatingAvg") val pharmacistRatingAvg: Double,
    @Json(name = "totalReviewsCount") val totalReviewsCount: Int
)

@JsonClass(generateAdapter = true)
data class UpdateProductDto(
    @Json(name = "price") val price: Double?,
    @Json(name = "stockQuantity") val stockQuantity: Int?
)
