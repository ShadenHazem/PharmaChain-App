package com.pharmachain.ai.core.network

import com.pharmachain.ai.data.local.MedicationEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Supabase PostgREST queries on table 'medications'.
 * Supports filtering, sorting, pagination, and pattern matching.
 */
interface PharmaChainApiService {

    @GET("rest/v1/medications")
    suspend fun getMedications(
        @Query("select") select: String = "*",
        @Query("order") order: String = "product_name.asc",
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<List<MedicationEntity>>

    @GET("rest/v1/medications")
    suspend fun searchMedications(
        @Query("product_name") productNameFilter: String, // e.g., "ilike.*aspirin*"
        @Query("select") select: String = "*",
        @Query("order") order: String = "price_egp.asc",
        @Query("limit") limit: Int = 50
    ): Response<List<MedicationEntity>>
}
