package com.pharmachain.ai.core.network

import com.pharmachain.ai.core.common.session.SessionManager
import com.pharmachain.ai.core.network.api.AdminApi
import com.pharmachain.ai.core.network.api.AuthApi
import com.pharmachain.ai.core.network.api.CatalogApi
import com.pharmachain.ai.core.network.api.DistributorAnalyticsApi
import com.pharmachain.ai.core.network.api.DistributorApi
import com.pharmachain.ai.core.network.api.ForecastApi
import com.pharmachain.ai.core.network.api.IntegrationApi
import com.pharmachain.ai.core.network.api.InventoryApi
import com.pharmachain.ai.core.network.api.OrdersApi
import com.pharmachain.ai.core.network.interceptor.AuthInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class NetworkClient(private val sessionManager: SessionManager) {

    // ARCHITECT DECISION: Default base URL pointing to PharmaChain AI API gateway with configurable endpoint
    private val baseUrl: String = "https://api.pharmachain.ai.eg/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS) // Generous for multipart file uploads
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val catalogApi: CatalogApi by lazy { retrofit.create(CatalogApi::class.java) }
    val ordersApi: OrdersApi by lazy { retrofit.create(OrdersApi::class.java) }
    val forecastApi: ForecastApi by lazy { retrofit.create(ForecastApi::class.java) }
    val distributorApi: DistributorApi by lazy { retrofit.create(DistributorApi::class.java) }
    val inventoryApi: InventoryApi by lazy { retrofit.create(InventoryApi::class.java) }
    val integrationApi: IntegrationApi by lazy { retrofit.create(IntegrationApi::class.java) }
    val distributorAnalyticsApi: DistributorAnalyticsApi by lazy { retrofit.create(DistributorAnalyticsApi::class.java) }
    val adminApi: AdminApi by lazy { retrofit.create(AdminApi::class.java) }
}
