package com.pharmachain.ai.navigation

object Routes {
    // Language Onboarding
    const val FIRST_LAUNCH_LANGUAGE = "first_launch_language"

    // Settings (Shared across roles)
    const val SETTINGS = "settings"

    // Admin Control Tower
    const val ADMIN_DASHBOARD = "admin_dashboard"

    // Auth Graph
    const val AUTH_GRAPH = "auth_graph"
    const val ROLE_SELECTION = "role_selection"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Pharmacist Graph
    const val PHARMACIST_GRAPH = "pharmacist_graph"
    const val TAB_OVERVIEW = "tab_overview"
    const val TAB_CATALOG = "tab_catalog"
    const val TAB_ORDERS = "tab_orders"
    const val TAB_FORECASTING = "tab_forecasting"
    const val ASSISTANT = "assistant"
    const val ORDER_DETAIL = "order_detail/{orderId}"

    // Forecasting Wizard Sub-Graph
    const val FORECAST_WIZARD_GRAPH = "forecast_wizard_graph"
    const val WIZARD_UPLOAD = "wizard_upload"
    const val WIZARD_MAPPING = "wizard_mapping"
    const val WIZARD_VALIDATION = "wizard_validation"
    const val WIZARD_CONSTRAINTS = "wizard_constraints"
    const val WIZARD_RESULTS = "wizard_results"
    const val WIZARD_SMART_CART = "wizard_smart_cart"

    // Distributor Graph
    const val DISTRIBUTOR_GRAPH = "distributor_graph"
    const val DIST_TAB_INVENTORY = "dist_tab_inventory"
    const val DIST_TAB_INCOMING = "dist_tab_incoming"
    const val DIST_TAB_LEDGER = "dist_tab_ledger"
    const val DIST_TAB_REPUTATION = "dist_tab_reputation"
}
