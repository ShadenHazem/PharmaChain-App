package com.pharmachain.ai.feature.assistant.domain

enum class QueryCategory {
    ORDERS,
    SPENDING,
    CATALOG,
    FORECASTING,
    HOW_TO,
    CLINICAL_WARNING,
    GENERAL_SNAPSHOT
}

object AssistantQueryClassifier {

    private val orderKeywords = listOf(
        "order", "orders", "status", "delivery", "delivered", "shipped", "tracking", "pending", "dispatch",
        "طلب", "طلبات", "أوردر", "أوردرات", "حالة", "توصيل", "شحن", "معلق", "تأكيد", "فاتورة"
    )

    private val spendKeywords = listOf(
        "spend", "spending", "spent", "cost", "total", "budget", "money", "egp", "financial", "paid",
        "مصاريف", "مشتريات", "إجمالي", "صرفت", "فلوس", "ميزانية", "جنيه", "دفع", "حساب"
    )

    private val catalogKeywords = listOf(
        "price", "prices", "cost", "catalog", "medicine", "medication", "meds", "drug", "stock", "listing",
        "distributor", "ibnsina", "ucp", "ramco", "soficopharm", "panadol", "augmentin", "concor", "antinal",
        "telfast", "cipro", "ketofan", "gastrazole", "cidophage", "ator",
        "سعر", "أسعار", "كتالوج", "أدوية", "دواء", "مخزون", "صنف", "أصناف", "موزع", "موزعين",
        "ابن سينا", "المتحدة", "رامكو", "صوفيكوفارم", "بنادول", "اوجمنتين", "كونكور", "أنتينال", "تلفاست"
    )

    private val forecastKeywords = listOf(
        "forecast", "forecasting", "demand", "prediction", "predict", "smart cart", "reorder", "season", "pos", "upload", "excel", "csv",
        "تنبؤ", "توقع", "توقعات", "طلب متوقع", "سلة ذكية", "سله", "مخزون قادم", "موسمية", "ملف", "إكسل"
    )

    private val howToKeywords = listOf(
        "how", "how to", "how do i", "guide", "tutorial", "features", "app", "free", "tier",
        "كيف", "طريقة", "شرح", "مساعدة", "استخدام", "مجاني", "التطبيق", "خطوات"
    )

    private val clinicalKeywords = listOf(
        "dose", "dosage", "how much to take", "side effect", "side effects", "contraindication", "interaction",
        "clinical", "cure", "treat", "prescribe", "pediatric dose", "overdose", "pregnancy safe",
        "جرعة", "جرعات", "أعراض جانبية", "تفاعل دوائي", "موانع استعمال", "موانع استخدام", "علاج", "روشتة", "حمل", "أطفال"
    )

    fun classify(query: String): QueryCategory {
        val lower = query.lowercase().trim()

        if (clinicalKeywords.any { lower.contains(it) }) {
            return QueryCategory.CLINICAL_WARNING
        }

        val orderMatch = orderKeywords.count { lower.contains(it) }
        val spendMatch = spendKeywords.count { lower.contains(it) }
        val catalogMatch = catalogKeywords.count { lower.contains(it) }
        val forecastMatch = forecastKeywords.count { lower.contains(it) }
        val howToMatch = howToKeywords.count { lower.contains(it) }

        val maxMatches = maxOf(orderMatch, spendMatch, catalogMatch, forecastMatch, howToMatch)

        if (maxMatches == 0) {
            return QueryCategory.GENERAL_SNAPSHOT
        }

        return when {
            orderMatch == maxMatches && orderMatch > 0 -> QueryCategory.ORDERS
            spendMatch == maxMatches && spendMatch > 0 -> QueryCategory.SPENDING
            catalogMatch == maxMatches && catalogMatch > 0 -> QueryCategory.CATALOG
            forecastMatch == maxMatches && forecastMatch > 0 -> QueryCategory.FORECASTING
            howToMatch == maxMatches && howToMatch > 0 -> QueryCategory.HOW_TO
            else -> QueryCategory.GENERAL_SNAPSHOT
        }
    }
}
