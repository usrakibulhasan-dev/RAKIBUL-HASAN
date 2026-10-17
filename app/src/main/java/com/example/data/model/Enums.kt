package com.example.data.model

enum class UserRole(val title: String) {
    ADMIN("System Admin"),
    INVENTORY_CONTROLLER("Inventory Controller"),
    WAREHOUSE_SUPERVISOR("Warehouse Supervisor"),
    STORE_KEEPER("Store Keeper"),
    AUDITOR("Viewer / Auditor")
}

enum class SteelFinish(val displayName: String, val code: String) {
    BLACK("Black Steel (Normal)", "BLK"),
    EPOXY_GREEN("Epoxy Green (Coated)", "EPX")
}

enum class SteelGrade(val code: String) {
    GRADE_60("Grade 60"),
    B500B("B500B"),
    ASTM_A615("ASTM A615 Gr 60"),
    BS_4449("BS 4449 Gr 500")
}

enum class MovementType(val title: String, val isIn: Boolean) {
    OPENING_STOCK("Opening Stock", true),
    RECEIVING_2TON("Raw Material Receiving (2-Ton)", true),
    ISSUE_PRODUCTION("Material Issue to Production", false),
    PRODUCTION_OUTPUT("Finished Goods Output", true),
    OFFCUT_LOG("Off-cut Recovery", true),
    SCRAP_LOG("Scrap Generation", true),
    TRANSFER_IN("Internal Transfer In", true),
    TRANSFER_OUT("Internal Transfer Out", false),
    BBS_DELIVERY("Customer Delivery (BBS)", false),
    CUSTOMER_RETURN("Customer Return", true),
    STOCK_ADJUSTMENT("Stock Adjustment", true),
    PHYSICAL_COUNT("Physical Count Reconciliation", true)
}

enum class LocationCategory(val title: String) {
    RAW_MATERIAL("Raw Material Yard"),
    PRODUCTION_FLOOR("Production Line"),
    FINISHED_GOODS("Ready / Finished Goods Yard"),
    OFFCUT_RACK("Off-cut Rack"),
    SCRAP_YARD("Scrap Bin"),
    EPOXY_YARD("Epoxy Coating Yard"),
    QUARANTINE("Quarantine / Return")
}

enum class ReconciliationStatus(val title: String) {
    MATCHED("Matched"),
    MISMATCHED("Mismatched"),
    MISSING("Missing"),
    DUPLICATE("Duplicate")
}

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    REVERSED
}
