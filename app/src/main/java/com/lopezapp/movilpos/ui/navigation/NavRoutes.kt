package com.lopezapp.movilpos.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data object POSRoute : NavKey

@Serializable
data object POSCheckoutKey : NavKey

@Serializable
data class POSTicketReceiptKey(val saleId: String) : NavKey

@Serializable
data object InventoryRoute : NavKey

@Serializable
data object SettingsRoute : NavKey

@Serializable
data object SettingsAnimationKey : NavKey

@Serializable
data object SettingsCurrencyKey : NavKey

@Serializable
data object SettingsCategoriesKey : NavKey

@Serializable
data object SettingsBrandsKey : NavKey

@Serializable
data object SettingsUnitsKey : NavKey

@Serializable
data object SettingsTaxesKey : NavKey

@Serializable
data object SettingsPriceRulesKey : NavKey

@Serializable
data object SettingsPaymentMethodsKey : NavKey

@Serializable
data object SettingsBusinessInfoKey : NavKey

@Serializable
data object SettingsTicketKey : NavKey

@Serializable
data object SettingsElectronicBillingKey : NavKey

@Serializable
data class PriceRuleEditKey(val ruleId: String? = null) : NavKey

@Serializable
data object SuppliersRoute : NavKey

@Serializable
data class SupplierDetailRoute(val supplierId: String) : NavKey

@Serializable
data class SupplierEditRoute(val supplierId: String? = null) : NavKey

@Serializable
data object CustomersRoute : NavKey

@Serializable
data class CustomerDetailRoute(val customerId: String) : NavKey

@Serializable
data class CustomerEditRoute(val customerId: String? = null) : NavKey

@Serializable
data object PurchasesRoute : NavKey

@Serializable
data class PurchaseDetailRoute(val purchaseId: String) : NavKey

@Serializable
data class PurchaseEditRoute(val purchaseId: String? = null) : NavKey

@Serializable
data object SalesRoute : NavKey

@Serializable
data class SaleDetailRoute(val saleId: String) : NavKey

@Serializable
data object DteReportsRoute : NavKey

@Serializable
data class DteDetailRoute(val saleId: String) : NavKey

@Serializable
data object QuotationsRoute : NavKey

@Serializable
data class QuotationDetailRoute(val quotationId: String) : NavKey

@Serializable
data class QuotationEditRoute(val quotationId: String? = null) : NavKey
