package com.lopezapp.movilpos.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data object POSRoute : NavKey

@Serializable
data object InventoryRoute : NavKey

@Serializable
data object SettingsRoute : NavKey

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
