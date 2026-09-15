package com.pharmachain.ai

import android.app.Application
import com.pharmachain.ai.core.common.locale.LanguageManager
import com.pharmachain.ai.core.common.preferences.LanguagePreferences
import com.pharmachain.ai.core.common.session.SessionManager
import com.pharmachain.ai.core.database.PharmaChainDatabase
import com.pharmachain.ai.core.database.seed.DemoDataSeeder
import com.pharmachain.ai.core.network.NetworkClient
import com.pharmachain.ai.data.AdminRepository
import com.pharmachain.ai.data.AdminRepositoryImpl
import com.pharmachain.ai.data.repository.AuthRepository
import com.pharmachain.ai.data.repository.CatalogRepository
import com.pharmachain.ai.data.repository.DistributorRepository
import com.pharmachain.ai.data.repository.ForecastRepository
import com.pharmachain.ai.data.repository.OrdersRepository
import com.pharmachain.ai.feature.assistant.data.AssistantRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PharmaChainApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: PharmaChainDatabase
        private set

    lateinit var sessionManager: SessionManager
        private set

    lateinit var languagePreferences: LanguagePreferences
        private set

    lateinit var networkClient: NetworkClient
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var catalogRepository: CatalogRepository
        private set

    lateinit var ordersRepository: OrdersRepository
        private set

    lateinit var forecastRepository: ForecastRepository
        private set

    lateinit var distributorRepository: DistributorRepository
        private set

    lateinit var assistantRepository: AssistantRepository
        private set

    lateinit var adminRepository: AdminRepository
        private set

    override fun onCreate() {
        super.onCreate()

        languagePreferences = LanguagePreferences(this)
        sessionManager = SessionManager(this)
        networkClient = NetworkClient(sessionManager)
        database = PharmaChainDatabase.getDatabase(this)

        authRepository = AuthRepository(database, networkClient, sessionManager)
        catalogRepository = CatalogRepository(database, networkClient)
        ordersRepository = OrdersRepository(database, sessionManager)
        forecastRepository = ForecastRepository(database, catalogRepository)
        distributorRepository = DistributorRepository(database, sessionManager, networkClient)
        adminRepository = AdminRepositoryImpl(networkClient)
        assistantRepository = AssistantRepository(
            database = database,
            ordersRepository = ordersRepository,
            catalogRepository = catalogRepository,
            forecastRepository = forecastRepository,
            sessionManager = sessionManager
        )

        // Initialize language and apply locale immediately
        applicationScope.launch {
            val selectedLanguage = languagePreferences.getSelectedLanguage()
            LanguageManager.syncCurrentLanguage(selectedLanguage)
        }

        // Seed sample pharmaceutical data on first run
        applicationScope.launch {
            catalogRepository.seedInitialCatalogIfEmpty()
            DemoDataSeeder.seedDemoDistributorCatalog(database)
            ordersRepository.seedInitialOrdersIfEmpty()
            distributorRepository.refreshLowStockAlerts(DemoDataSeeder.DEMO_DISTRIBUTOR_ID)
            distributorRepository.refreshLowStockAlerts("dist_ibnsina")
        }
    }
}
