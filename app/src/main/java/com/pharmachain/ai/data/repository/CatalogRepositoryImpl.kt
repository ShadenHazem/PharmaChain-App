package com.pharmachain.ai.data.repository

import com.pharmachain.ai.core.network.PharmaChainApiService
import com.pharmachain.ai.data.local.MedicationDao
import com.pharmachain.ai.data.local.MedicationEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Interface defining Clean Architecture catalog repository operations.
 */
interface ISupabaseCatalogRepository {
    fun getMedications(): Flow<List<MedicationEntity>>
    fun searchMedications(query: String): Flow<List<MedicationEntity>>
    suspend fun syncRemoteCatalog(): Result<Int>
}

/**
 * Single Source of Truth repository implementation.
 * Exposes local Room DB as Flow<List<MedicationEntity>> and synchronizes remote data from Supabase PostgREST.
 */
class CatalogRepositoryImpl(
    private val medicationDao: MedicationDao,
    private val apiService: PharmaChainApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ISupabaseCatalogRepository {

    override fun getMedications(): Flow<List<MedicationEntity>> {
        return medicationDao.getAllMedications().flowOn(ioDispatcher)
    }

    override fun searchMedications(query: String): Flow<List<MedicationEntity>> {
        return if (query.isBlank()) {
            medicationDao.getAllMedications().flowOn(ioDispatcher)
        } else {
            medicationDao.searchMedications(query.trim()).flowOn(ioDispatcher)
        }
    }

    override suspend fun syncRemoteCatalog(): Result<Int> = withContext(ioDispatcher) {
        try {
            val response = apiService.getMedications(
                select = "*",
                order = "product_name.asc",
                limit = 1000
            )

            if (response.isSuccessful) {
                val remoteMedications = response.body() ?: emptyList()
                if (remoteMedications.isNotEmpty()) {
                    medicationDao.insertOrUpdateAll(remoteMedications)
                }
                Result.success(remoteMedications.size)
            } else {
                Result.failure(Exception("Supabase sync failed: HTTP ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
