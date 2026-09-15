package com.pharmachain.ai.feature.assistant.data

import android.util.Log
import com.pharmachain.ai.core.common.session.SessionManager
import com.pharmachain.ai.core.database.PharmaChainDatabase
import com.pharmachain.ai.core.database.entity.ChatMessageEntity
import com.pharmachain.ai.core.database.entity.ChatMessageRole
import com.pharmachain.ai.core.model.Order
import com.pharmachain.ai.data.repository.CatalogRepository
import com.pharmachain.ai.data.repository.ForecastRepository
import com.pharmachain.ai.data.repository.OrdersRepository
import com.pharmachain.ai.feature.assistant.context.DataContextBuilder
import com.pharmachain.ai.feature.assistant.domain.AssistantQueryClassifier
import com.pharmachain.ai.feature.assistant.domain.QueryCategory
import com.pharmachain.ai.feature.assistant.network.GeminiAssistantClient
import com.pharmachain.ai.feature.assistant.network.GeminiContent
import com.pharmachain.ai.feature.assistant.network.GeminiGenerationResult
import com.pharmachain.ai.feature.assistant.network.GeminiPart
import com.pharmachain.ai.feature.assistant.prompt.AssistantSystemPrompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class AssistantRepository(
    private val database: PharmaChainDatabase,
    private val ordersRepository: OrdersRepository,
    private val catalogRepository: CatalogRepository,
    private val forecastRepository: ForecastRepository,
    private val sessionManager: SessionManager
) {
    private val TAG = "AssistantRepository"

    fun getConversationHistory(userId: String): Flow<List<ChatMessageEntity>> {
        return database.chatMessageDao().getMessagesForUser(userId)
    }

    suspend fun seedWelcomeMessageIfEmpty(userId: String, isArabic: Boolean) = withContext(Dispatchers.IO) {
        val count = database.chatMessageDao().getMessageCount(userId)
        if (count == 0) {
            val welcomeText = if (isArabic) {
                "مرحباً بك دكتور! أنا المساعد الذكي لصيدليتك في PharmaChain AI.\n\nيمكنني مساعدتك في: الاستعلام عن حالة طلباتك، حساب إجمالي المصاريف والمشتريات، مقارنة أسعار الأدوية بين الموزعين، أو كيفية استخدام التنبؤ بالطلب.\n\nكيف يمكنني مساعدتك اليوم؟"
            } else {
                "Welcome Doctor! I am your PharmaChain AI pharmacy ordering assistant.\n\nI can help you check your recent orders, calculate your monthly spending, compare distributor medication prices, or explain how demand forecasting works.\n\nHow can I help you today?"
            }
            database.chatMessageDao().insertMessage(
                ChatMessageEntity(
                    id = "msg_${UUID.randomUUID().toString().take(8)}",
                    userId = userId,
                    role = ChatMessageRole.ASSISTANT,
                    content = welcomeText,
                    timestamp = System.currentTimeMillis(),
                    tokensUsed = 0
                )
            )
        }
    }

    suspend fun sendMessage(
        userMessage: String,
        userId: String
    ): Result<ChatMessageEntity> = withContext(Dispatchers.IO) {
        try {
            // 1. Insert user message into Room
            val userMsgEntity = ChatMessageEntity(
                id = "msg_${UUID.randomUUID().toString().take(8)}",
                userId = userId,
                role = ChatMessageRole.USER,
                content = userMessage.trim(),
                timestamp = System.currentTimeMillis(),
                tokensUsed = 0
            )
            database.chatMessageDao().insertMessage(userMsgEntity)

            // 2. Resolve user & pharmacy info
            val currentUser = sessionManager.currentUser.value
            val pharmacyProfile = database.pharmacyDao().getProfileByUserIdSync(userId)
            val pharmacyName = pharmacyProfile?.pharmacyName ?: "El-Ezaby Pharmacy"
            val pharmacyId = pharmacyProfile?.id ?: "pharm_demo"

            // 3. Classify query intent using lightweight heuristics
            val category = AssistantQueryClassifier.classify(userMessage)
            Log.d(TAG, "Query classified as: $category for text: '$userMessage'")

            // 4. Fetch grounded data context from Room repositories based on classification
            val dataContext = when (category) {
                QueryCategory.ORDERS -> {
                    val orders = ordersRepository.getOrdersForPharmacy(pharmacyId).first()
                    DataContextBuilder.buildUserHeader(currentUser, pharmacyName) + "\n\n" +
                            DataContextBuilder.buildOrdersContext(orders, maxOrders = 6) + "\n\n" +
                            DataContextBuilder.buildSpendSummaryContext(orders)
                }
                QueryCategory.SPENDING -> {
                    val orders = ordersRepository.getOrdersForPharmacy(pharmacyId).first()
                    DataContextBuilder.buildUserHeader(currentUser, pharmacyName) + "\n\n" +
                            DataContextBuilder.buildSpendSummaryContext(orders) + "\n\n" +
                            DataContextBuilder.buildOrdersContext(orders, maxOrders = 4)
                }
                QueryCategory.CATALOG -> {
                    val catalog = catalogRepository.getMedications().first()
                    DataContextBuilder.buildUserHeader(currentUser, pharmacyName) + "\n\n" +
                            DataContextBuilder.buildCatalogContext(catalog, queryFilter = null, maxItems = 10)
                }
                QueryCategory.FORECASTING -> {
                    val lastJob = database.forecastDao().getJobsForPharmacy(pharmacyId).firstOrNull()?.firstOrNull()
                    val forecastResults = if (lastJob != null) {
                        forecastRepository.getForecastResults(lastJob.id).first()
                    } else {
                        emptyList()
                    }
                    DataContextBuilder.buildUserHeader(currentUser, pharmacyName) + "\n\n" +
                            DataContextBuilder.buildForecastContext(forecastResults, maxResults = 8) + "\n\n" +
                            DataContextBuilder.buildAppHowToContext()
                }
                QueryCategory.HOW_TO -> {
                    DataContextBuilder.buildUserHeader(currentUser, pharmacyName) + "\n\n" +
                            DataContextBuilder.buildAppHowToContext()
                }
                QueryCategory.CLINICAL_WARNING, QueryCategory.GENERAL_SNAPSHOT -> {
                    val orders = ordersRepository.getOrdersForPharmacy(pharmacyId).first()
                    val catalog = catalogRepository.getMedications().first()
                    val lastJob = database.forecastDao().getJobsForPharmacy(pharmacyId).firstOrNull()?.firstOrNull()
                    val forecastResults = if (lastJob != null) {
                        forecastRepository.getForecastResults(lastJob.id).first()
                    } else {
                        emptyList()
                    }
                    DataContextBuilder.buildGeneralSnapshot(
                        user = currentUser,
                        pharmacyName = pharmacyName,
                        orders = orders,
                        catalogSample = catalog,
                        forecastResults = forecastResults
                    )
                }
            }

            // 5. Construct full System Instruction + Data Context
            val fullSystemPrompt = "${AssistantSystemPrompt.SYSTEM_INSTRUCTION}\n\n$dataContext"

            // 6. Fetch short rolling conversation turns (last 6 messages)
            val recentMessages = database.chatMessageDao().getRecentMessages(userId, limit = 6).reversed()

            val contents = mutableListOf<GeminiContent>()
            recentMessages.forEach { msg ->
                val roleStr = if (msg.role == ChatMessageRole.USER) "user" else "model"
                contents.add(
                    GeminiContent(
                        role = roleStr,
                        parts = listOf(GeminiPart(text = msg.content))
                    )
                )
            }

            // If empty history, add current user message
            if (contents.isEmpty() || contents.lastOrNull()?.role != "user") {
                contents.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = userMessage))
                    )
                )
            }

            // 7. Call Gemini API
            val result = GeminiAssistantClient.generateGroundedResponse(
                systemInstructionText = fullSystemPrompt,
                conversationTurns = contents
            )

            when (result) {
                is GeminiGenerationResult.Success -> {
                    val assistantMsgEntity = ChatMessageEntity(
                        id = "msg_${UUID.randomUUID().toString().take(8)}",
                        userId = userId,
                        role = ChatMessageRole.ASSISTANT,
                        content = result.text,
                        timestamp = System.currentTimeMillis(),
                        tokensUsed = result.totalTokens
                    )
                    database.chatMessageDao().insertMessage(assistantMsgEntity)
                    Result.success(assistantMsgEntity)
                }
                is GeminiGenerationResult.KeyMissing -> {
                    // Fallback response for private prototype testing when API key is not yet set
                    val fallbackText = generateLocalFallbackResponse(userMessage, category, dataContext)
                    val assistantMsgEntity = ChatMessageEntity(
                        id = "msg_${UUID.randomUUID().toString().take(8)}",
                        userId = userId,
                        role = ChatMessageRole.ASSISTANT,
                        content = fallbackText,
                        timestamp = System.currentTimeMillis(),
                        tokensUsed = 0
                    )
                    database.chatMessageDao().insertMessage(assistantMsgEntity)
                    Result.success(assistantMsgEntity)
                }
                is GeminiGenerationResult.Error -> {
                    Log.w(TAG, "Gemini API failed with: ${result.errorMessage}. Generating local grounded response.")
                    val fallbackText = generateLocalFallbackResponse(userMessage, category, dataContext)
                    val assistantMsgEntity = ChatMessageEntity(
                        id = "msg_${UUID.randomUUID().toString().take(8)}",
                        userId = userId,
                        role = ChatMessageRole.ASSISTANT,
                        content = fallbackText,
                        timestamp = System.currentTimeMillis(),
                        tokensUsed = 0
                    )
                    database.chatMessageDao().insertMessage(assistantMsgEntity)
                    Result.success(assistantMsgEntity)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in sendMessage: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun generateLocalFallbackResponse(
        userQuery: String,
        category: QueryCategory,
        dataContext: String
    ): String {
        val lower = userQuery.lowercase()
        val isArabic = userQuery.any { it in '\u0600'..'\u06FF' }

        if (category == QueryCategory.CLINICAL_WARNING) {
            return if (isArabic) {
                "أعتذر منك دكتور، بصفتي مساعداً إدارياً وتجارياً لمنصة PharmaChain AI، لا يمكنني تقديم نصائح طبية أو سريرية بخصوص الجرعات، التفاعلات الدوائية، أو توجيهات المرضى. يرجى مراجعة المراجع الصيدلانية السريرية المعتمدة."
            } else {
                "I apologize Doctor, as a PharmaChain AI platform ordering assistant, I cannot provide clinical or medical advice (dosage, interactions, or patient care). Please consult official clinical reference guidelines or a specialist."
            }
        }

        return if (isArabic) {
            "إليك البيانات المستخرجة مباشرة من قاعدة بيانات حسابك:\n\n$dataContext\n\n*(ملاحظة: يمكنك إضافة مفتاح GEMINI_API_KEY في إعدادات Secrets لتفعيل الردود الذكية الكاملة)*"
        } else {
            "Here is the verified data extracted directly from your pharmacy account:\n\n$dataContext\n\n*(Note: Configure GEMINI_API_KEY in Secrets for natural conversational phrasing)*"
        }
    }

    suspend fun clearHistory(userId: String) = withContext(Dispatchers.IO) {
        database.chatMessageDao().clearMessagesForUser(userId)
    }
}
