package com.pharmachain.ai.core.network.api

import com.pharmachain.ai.core.network.dto.AdminOrderDetailDto
import com.pharmachain.ai.core.network.dto.AdminOrderDto
import com.pharmachain.ai.core.network.dto.ApplyInventoryResponseDto
import com.pharmachain.ai.core.network.dto.AuthResponseDto
import com.pharmachain.ai.core.network.dto.CommissionRecordDto
import com.pharmachain.ai.core.network.dto.CreateIntegrationRequestDto
import com.pharmachain.ai.core.network.dto.CreateJobRequestDto
import com.pharmachain.ai.core.network.dto.CreateOrderRequestDto
import com.pharmachain.ai.core.network.dto.DistributorAnalyticsDto
import com.pharmachain.ai.core.network.dto.ForecastJobStatusDto
import com.pharmachain.ai.core.network.dto.IntegrationResponseDto
import com.pharmachain.ai.core.network.dto.InventoryUploadItemDto
import com.pharmachain.ai.core.network.dto.InventoryValidationResponseDto
import com.pharmachain.ai.core.network.dto.JobCreatedResponseDto
import com.pharmachain.ai.core.network.dto.LoginRequestDto
import com.pharmachain.ai.core.network.dto.LowStockAlertDto
import com.pharmachain.ai.core.network.dto.MedicationWithListingsDto
import com.pharmachain.ai.core.network.dto.OrderDto
import com.pharmachain.ai.core.network.dto.OverrideOrderStatusRequestDto
import com.pharmachain.ai.core.network.dto.PlatformAnalyticsDto
import com.pharmachain.ai.core.network.dto.PreviewResponseDto
import com.pharmachain.ai.core.network.dto.ProductListingDto
import com.pharmachain.ai.core.network.dto.RefreshTokenRequestDto
import com.pharmachain.ai.core.network.dto.RegisterRequestDto
import com.pharmachain.ai.core.network.dto.RejectOrderRequestDto
import com.pharmachain.ai.core.network.dto.ReputationBreakdownDto
import com.pharmachain.ai.core.network.dto.ResolveConflictRequestDto
import com.pharmachain.ai.core.network.dto.SaveMappingRequestDto
import com.pharmachain.ai.core.network.dto.SmartCartItemDto
import com.pharmachain.ai.core.network.dto.SubmitMappingRequestDto
import com.pharmachain.ai.core.network.dto.SyncConflictDto
import com.pharmachain.ai.core.network.dto.SyncStatusResponseDto
import com.pharmachain.ai.core.network.dto.TestConnectionResponseDto
import com.pharmachain.ai.core.network.dto.UpdateLowStockThresholdDto
import com.pharmachain.ai.core.network.dto.UpdateOrderStatusDto
import com.pharmachain.ai.core.network.dto.UpdateProductDto
import com.pharmachain.ai.core.network.dto.UploadInventoryResponseDto
import com.pharmachain.ai.core.network.dto.UploadSalesResponseDto
import com.pharmachain.ai.core.network.dto.ValidationResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequestDto): Response<AuthResponseDto>
}

interface CatalogApi {
    @GET("api/v1/catalog/medications")
    suspend fun getMedications(
        @Query("query") query: String? = null,
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1
    ): Response<List<MedicationWithListingsDto>>
}

interface OrdersApi {
    @POST("api/v1/orders")
    suspend fun createOrder(@Body request: CreateOrderRequestDto): Response<OrderDto>

    @GET("api/v1/orders")
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1
    ): Response<List<OrderDto>>

    @GET("api/v1/orders/{id}")
    suspend fun getOrderById(@Path("id") id: String): Response<OrderDto>

    @PATCH("api/v1/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body request: UpdateOrderStatusDto
    ): Response<OrderDto>

    @PATCH("api/v1/orders/{id}/approve")
    suspend fun approveOrder(@Path("id") id: String): Response<OrderDto>

    @PATCH("api/v1/orders/{id}/reject")
    suspend fun rejectOrder(
        @Path("id") id: String,
        @Body request: RejectOrderRequestDto
    ): Response<OrderDto>
}

interface InventoryApi {
    @Multipart
    @POST("api/v1/distributor/inventory/uploads")
    suspend fun uploadInventoryFile(
        @Part file: MultipartBody.Part
    ): Response<UploadInventoryResponseDto>

    @GET("api/v1/distributor/inventory/uploads/{id}/preview")
    suspend fun getInventoryUploadPreview(
        @Path("id") uploadId: String
    ): Response<UploadInventoryResponseDto>

    @POST("api/v1/distributor/inventory/uploads/{id}/mapping")
    suspend fun saveInventoryMapping(
        @Path("id") uploadId: String,
        @Body request: SaveMappingRequestDto
    ): Response<Map<String, String>>

    @GET("api/v1/distributor/inventory/uploads/{id}/validation")
    suspend fun getInventoryValidation(
        @Path("id") uploadId: String
    ): Response<InventoryValidationResponseDto>

    @POST("api/v1/distributor/inventory/uploads/{id}/apply")
    suspend fun applyInventoryUpload(
        @Path("id") uploadId: String
    ): Response<ApplyInventoryResponseDto>

    @GET("api/v1/distributor/inventory/uploads")
    suspend fun getInventoryUploadHistory(
        @Query("page") page: Int = 1
    ): Response<List<InventoryUploadItemDto>>
}

interface IntegrationApi {
    @POST("api/v1/distributor/integrations")
    suspend fun createOrUpdateIntegration(
        @Body request: CreateIntegrationRequestDto
    ): Response<IntegrationResponseDto>

    @POST("api/v1/distributor/integrations/{id}/test-connection")
    suspend fun testIntegrationConnection(
        @Path("id") id: String
    ): Response<TestConnectionResponseDto>

    @POST("api/v1/distributor/integrations/{id}/sync-now")
    suspend fun triggerSyncNow(
        @Path("id") id: String
    ): Response<SyncStatusResponseDto>

    @GET("api/v1/distributor/integrations/{id}/sync-status")
    suspend fun getSyncStatus(
        @Path("id") id: String
    ): Response<SyncStatusResponseDto>

    @GET("api/v1/distributor/integrations/{id}/conflicts")
    suspend fun getSyncConflicts(
        @Path("id") id: String
    ): Response<List<SyncConflictDto>>

    @POST("api/v1/distributor/integrations/{id}/conflicts/resolve")
    suspend fun resolveConflicts(
        @Path("id") id: String,
        @Body request: ResolveConflictRequestDto
    ): Response<Map<String, Boolean>>

    @DELETE("api/v1/distributor/integrations/{id}")
    suspend fun deleteIntegration(
        @Path("id") id: String
    ): Response<Unit>
}

interface DistributorAnalyticsApi {
    @GET("api/v1/distributor/analytics")
    suspend fun getAnalytics(
        @Query("periodDays") periodDays: Int = 30
    ): Response<DistributorAnalyticsDto>

    @GET("api/v1/distributor/analytics/low-stock-alerts")
    suspend fun getLowStockAlerts(): Response<List<LowStockAlertDto>>

    @PATCH("api/v1/distributor/products/{id}/low-stock-threshold")
    suspend fun updateLowStockThreshold(
        @Path("id") id: String,
        @Body request: UpdateLowStockThresholdDto
    ): Response<ProductListingDto>
}

interface ForecastApi {
    @Multipart
    @POST("api/v1/forecast/uploads")
    suspend fun uploadSalesHistory(
        @Part file: MultipartBody.Part
    ): Response<UploadSalesResponseDto>

    @GET("api/v1/forecast/uploads/{uploadId}/preview")
    suspend fun getUploadPreview(
        @Path("uploadId") uploadId: String
    ): Response<PreviewResponseDto>

    @POST("api/v1/forecast/uploads/{uploadId}/mapping")
    suspend fun submitColumnMapping(
        @Path("uploadId") uploadId: String,
        @Body request: SubmitMappingRequestDto
    ): Response<Map<String, String>>

    @GET("api/v1/forecast/uploads/{uploadId}/validation")
    suspend fun getValidationResult(
        @Path("uploadId") uploadId: String
    ): Response<ValidationResponseDto>

    @POST("api/v1/forecast/jobs")
    suspend fun createForecastJob(
        @Body request: CreateJobRequestDto
    ): Response<JobCreatedResponseDto>

    @GET("api/v1/forecast/jobs/{jobId}")
    suspend fun getJobStatus(
        @Path("jobId") jobId: String
    ): Response<ForecastJobStatusDto>

    @GET("api/v1/forecast/jobs/{jobId}/smart-cart")
    suspend fun getSmartCartItems(
        @Path("jobId") jobId: String
    ): Response<List<SmartCartItemDto>>
}

interface DistributorApi {
    @GET("api/v1/distributor/commission-ledger")
    suspend fun getCommissionLedger(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1
    ): Response<List<CommissionRecordDto>>

    @GET("api/v1/distributor/reputation-breakdown")
    suspend fun getReputationBreakdown(): Response<ReputationBreakdownDto>

    @PATCH("api/v1/distributor/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body request: UpdateProductDto
    ): Response<ProductListingDto>
}

interface AdminApi {
    @GET("api/v1/admin/analytics")
    suspend fun getPlatformAnalytics(): Response<PlatformAnalyticsDto>

    @GET("api/v1/admin/orders")
    suspend fun getAdminOrders(
        @Query("status") status: String? = null,
        @Query("query") query: String? = null,
        @Query("page") page: Int = 1
    ): Response<List<AdminOrderDto>>

    @GET("api/v1/admin/orders/{id}")
    suspend fun getAdminOrderDetail(
        @Path("id") id: String
    ): Response<AdminOrderDetailDto>

    @POST("api/v1/admin/orders/{id}/override-status")
    suspend fun overrideOrderStatus(
        @Path("id") id: String,
        @Body request: OverrideOrderStatusRequestDto
    ): Response<AdminOrderDetailDto>
}

