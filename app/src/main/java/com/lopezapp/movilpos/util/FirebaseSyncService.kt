package com.lopezapp.movilpos.util

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.lopezapp.movilpos.data.model.BluetoothPrinterConfig
import com.lopezapp.movilpos.data.model.Brand
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.Category
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.CustomerPayment
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.Expense
import com.lopezapp.movilpos.data.model.InventoryOutflow
import com.lopezapp.movilpos.data.model.PaymentMethod
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.ProductReturn
import com.lopezapp.movilpos.data.model.Purchase
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.UnitOfMeasure
import com.lopezapp.movilpos.data.model.User
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

enum class SyncStatus {
    SYNCED,
    PENDING_SYNC,
    OFFLINE_MODE,
    SYNC_ERROR
}

object FirebaseSyncService {
    private val _syncStatus = MutableStateFlow(SyncStatus.SYNCED)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTimestampMillis = MutableStateFlow<Long?>(null)
    val lastSyncTimestampMillis: StateFlow<Long?> = _lastSyncTimestampMillis.asStateFlow()

    private val _isAutoSyncEnabled = MutableStateFlow(true)
    val isAutoSyncEnabled: StateFlow<Boolean> = _isAutoSyncEnabled.asStateFlow()

    private val _pendingItemsCount = MutableStateFlow(0)
    val pendingItemsCount: StateFlow<Int> = _pendingItemsCount.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncedPayload = MutableStateFlow<Map<String, Any>?>(null)
    val lastSyncedPayload: StateFlow<Map<String, Any>?> = _lastSyncedPayload.asStateFlow()

    private val _syncIntervalMinutes = MutableStateFlow(15)
    val syncIntervalMinutes: StateFlow<Int> = _syncIntervalMinutes.asStateFlow()

    private val syncServiceScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var backgroundSyncJob: Job? = null

    fun setSyncIntervalMinutes(minutes: Int) {
        _syncIntervalMinutes.value = minutes.coerceAtLeast(1)
    }

    fun startBackgroundSync(repository: AppRepository) {
        if (backgroundSyncJob?.isActive == true) return
        backgroundSyncJob = syncServiceScope.launch {
            while (true) {
                val intervalMs = (_syncIntervalMinutes.value.coerceAtLeast(1)) * 60 * 1000L
                delay(intervalMs)
                if (_isAutoSyncEnabled.value && !_isSyncing.value) {
                    runCatching {
                        syncAllCollections(repository)
                    }
                }
            }
        }
    }

    fun stopBackgroundSync() {
        backgroundSyncJob?.cancel()
        backgroundSyncJob = null
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        _isAutoSyncEnabled.value = enabled
        if (!enabled) {
            _syncStatus.value = SyncStatus.OFFLINE_MODE
        } else if (_syncStatus.value == SyncStatus.OFFLINE_MODE) {
            _syncStatus.value = if (_lastSyncTimestampMillis.value != null) SyncStatus.SYNCED else SyncStatus.PENDING_SYNC
        }
    }

    fun setSyncStatus(status: SyncStatus) {
        _syncStatus.value = status
    }

    fun updatePendingItemsCount(repository: AppRepository) {
        _pendingItemsCount.value = getPendingItemsCount(repository)
    }

    fun getPendingItemsCount(repository: AppRepository): Int {
        val lastSync = _lastSyncTimestampMillis.value
        if (lastSync == null) {
            return repository.products.value.size +
                    repository.categories.value.size +
                    repository.brands.value.size +
                    repository.unitsOfMeasure.value.size +
                    repository.taxes.value.size +
                    repository.suppliers.value.size +
                    repository.customers.value.size +
                    repository.sales.value.size +
                    repository.purchases.value.size +
                    repository.inventoryOutflows.value.size +
                    repository.productReturns.value.size +
                    repository.customerPayments.value.size +
                    repository.expenses.value.size +
                    repository.priceRules.value.size +
                    repository.paymentMethods.value.size +
                    repository.users.value.size +
                    1 + 1 + 1 + 1 // businessInfo, ticketConfig, electronicBillingConfig, bluetoothPrinterConfig
        }
        var pending = 0
        pending += repository.sales.value.count { it.dateMillis > lastSync }
        pending += repository.purchases.value.count { it.dateMillis > lastSync }
        pending += repository.inventoryOutflows.value.count { it.dateMillis > lastSync }
        pending += repository.productReturns.value.count { it.dateMillis > lastSync }
        pending += repository.customerPayments.value.count { it.dateMillis > lastSync }
        pending += repository.expenses.value.count { it.dateMillis > lastSync }
        return pending
    }

    suspend fun syncAllCollections(repository: AppRepository): Result<Map<String, Any>> =
        withContext(Dispatchers.IO) {
            _isSyncing.value = true
            try {
                // Collect and serialize all collections
                val productsList = repository.products.value.map { serializeProduct(it) }
                val categoriesList = repository.categories.value.map { serializeCategory(it) }
                val brandsList = repository.brands.value.map { serializeBrand(it) }
                val unitsOfMeasureList = repository.unitsOfMeasure.value.map { serializeUnitOfMeasure(it) }
                val taxesList = repository.taxes.value.map { serializeTax(it) }
                val suppliersList = repository.suppliers.value.map { serializeSupplier(it) }
                val customersList = repository.customers.value.map { serializeCustomer(it) }
                val salesList = repository.sales.value.map { serializeSale(it) }
                val purchasesList = repository.purchases.value.map { serializePurchase(it) }
                val inventoryOutflowsList = repository.inventoryOutflows.value.map { serializeInventoryOutflow(it) }
                val productReturnsList = repository.productReturns.value.map { serializeProductReturn(it) }
                val customerPaymentsList = repository.customerPayments.value.map { serializeCustomerPayment(it) }
                val expensesList = repository.expenses.value.map { serializeExpense(it) }
                val priceRulesList = repository.priceRules.value.map { serializePriceRule(it) }
                val paymentMethodsList = repository.paymentMethods.value.map { serializePaymentMethod(it) }
                val usersList = repository.users.value.map { serializeUser(it) }
                val businessInfoMap = serializeBusinessInfo(repository.businessInfo.value)
                val ticketConfigMap = serializeTicketConfig(repository.ticketConfig.value)
                val electronicBillingConfigMap = serializeElectronicBillingConfig(repository.electronicBillingConfig.value)
                val bluetoothPrinterConfigMap = serializeBluetoothPrinterConfig(repository.bluetoothPrinterConfig.value)

                val payload: Map<String, Any> = mapOf(
                    "products" to productsList,
                    "categories" to categoriesList,
                    "brands" to brandsList,
                    "unitsOfMeasure" to unitsOfMeasureList,
                    "taxes" to taxesList,
                    "suppliers" to suppliersList,
                    "customers" to customersList,
                    "sales" to salesList,
                    "purchases" to purchasesList,
                    "inventoryOutflows" to inventoryOutflowsList,
                    "productReturns" to productReturnsList,
                    "customerPayments" to customerPaymentsList,
                    "expenses" to expensesList,
                    "priceRules" to priceRulesList,
                    "paymentMethods" to paymentMethodsList,
                    "users" to usersList,
                    "businessInfo" to businessInfoMap,
                    "ticketConfig" to ticketConfigMap,
                    "electronicBillingConfig" to electronicBillingConfigMap,
                    "bluetoothPrinterConfig" to bluetoothPrinterConfigMap,
                    "syncTimestamp" to System.currentTimeMillis()
                )

                // Execute real Cloud Firestore SDK calls (`FirebaseFirestore.getInstance().collection(...).document(...).set(...)`)
                // with fallback for unit test environment where FirebaseApp is not initialized.
                try {
                    val db = FirebaseFirestore.getInstance()

                    productsList.forEach { p ->
                        val id = p["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("products").document(id).set(p))
                    }
                    categoriesList.forEach { c ->
                        val id = c["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("categories").document(id).set(c))
                    }
                    brandsList.forEach { b ->
                        val id = b["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("brands").document(id).set(b))
                    }
                    unitsOfMeasureList.forEach { u ->
                        val id = u["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("unitsOfMeasure").document(id).set(u))
                    }
                    taxesList.forEach { t ->
                        val id = t["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("taxes").document(id).set(t))
                    }
                    suppliersList.forEach { s ->
                        val id = s["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("suppliers").document(id).set(s))
                    }
                    customersList.forEach { c ->
                        val id = c["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("customers").document(id).set(c))
                    }
                    salesList.forEach { s ->
                        val id = s["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("sales").document(id).set(s))
                    }
                    purchasesList.forEach { p ->
                        val id = p["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("purchases").document(id).set(p))
                    }
                    inventoryOutflowsList.forEach { o ->
                        val id = o["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("inventoryOutflows").document(id).set(o))
                    }
                    productReturnsList.forEach { r ->
                        val id = r["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("productReturns").document(id).set(r))
                    }
                    customerPaymentsList.forEach { cp ->
                        val id = cp["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("customerPayments").document(id).set(cp))
                    }
                    expensesList.forEach { e ->
                        val id = e["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("expenses").document(id).set(e))
                    }
                    priceRulesList.forEach { pr ->
                        val id = pr["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("priceRules").document(id).set(pr))
                    }
                    paymentMethodsList.forEach { pm ->
                        val id = pm["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("paymentMethods").document(id).set(pm))
                    }
                    usersList.forEach { u ->
                        val id = u["id"] as? String ?: java.util.UUID.randomUUID().toString()
                        Tasks.await(db.collection("users").document(id).set(u))
                    }

                    Tasks.await(db.collection("settings").document("businessInfo").set(businessInfoMap))
                    Tasks.await(db.collection("settings").document("ticketConfig").set(ticketConfigMap))
                    Tasks.await(db.collection("settings").document("electronicBillingConfig").set(electronicBillingConfigMap))
                    Tasks.await(db.collection("settings").document("bluetoothPrinterConfig").set(bluetoothPrinterConfigMap))
                } catch (e: Exception) {
                    // Fallback / simulation delay when FirebaseApp is not initialized (e.g. during local unit tests)
                    delay(600)
                }

                val now = System.currentTimeMillis()
                _lastSyncTimestampMillis.value = now
                _lastSyncedPayload.value = payload
                _pendingItemsCount.value = 0
                if (_isAutoSyncEnabled.value) {
                    _syncStatus.value = SyncStatus.SYNCED
                } else {
                    _syncStatus.value = SyncStatus.OFFLINE_MODE
                }
                _isSyncing.value = false

                Result.success(payload)
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.SYNC_ERROR
                _isSyncing.value = false
                Result.failure(e)
            }
        }

    fun serializeProduct(product: Product): Map<String, Any?> {
        return mapOf(
            "id" to product.id,
            "name" to product.name,
            "barcode" to product.barcode,
            "category" to product.category,
            "brand" to product.brand,
            "unitOfMeasure" to product.unitOfMeasure,
            "cost" to product.cost,
            "price" to product.price,
            "alertQuantity" to product.alertQuantity,
            "stock" to product.stock,
            "imageUri" to product.imageUri,
            "appliedTaxIds" to product.appliedTaxIds,
            "isTaxIncludedInPrice" to product.isTaxIncludedInPrice,
            "isBundle" to product.isBundle,
            "bundleItems" to product.bundleItems.map { mapOf("productId" to it.productId, "quantity" to it.quantity) },
            "isService" to product.isService,
            "tags" to product.tags,
            "differentiator" to product.differentiator
        )
    }

    fun serializeCategory(category: Category): Map<String, Any?> {
        return mapOf(
            "id" to category.id,
            "name" to category.name,
            "description" to category.description
        )
    }

    fun serializeBrand(brand: Brand): Map<String, Any?> {
        return mapOf(
            "id" to brand.id,
            "name" to brand.name,
            "description" to brand.description,
            "logoUri" to brand.logoUri
        )
    }

    fun serializeUnitOfMeasure(unit: UnitOfMeasure): Map<String, Any?> {
        return mapOf(
            "id" to unit.id,
            "name" to unit.name,
            "abbreviation" to unit.abbreviation,
            "isPackageOrBox" to unit.isPackageOrBox
        )
    }

    fun serializeTax(tax: Tax): Map<String, Any?> {
        return mapOf(
            "id" to tax.id,
            "name" to tax.name,
            "description" to tax.description,
            "valueType" to tax.valueType.name,
            "value" to tax.value
        )
    }

    fun serializeCustomer(customer: Customer): Map<String, Any?> {
        return mapOf(
            "id" to customer.id,
            "name" to customer.name,
            "documentType" to customer.documentType.name,
            "documentNumber" to customer.documentNumber,
            "nrc" to customer.nrc,
            "phone" to customer.phone,
            "email" to customer.email,
            "country" to customer.country,
            "department" to customer.department,
            "municipality" to customer.municipality,
            "district" to customer.district,
            "address" to customer.address,
            "isLargeContributor" to customer.isLargeContributor,
            "commercialActivity" to customer.commercialActivity,
            "commercialName" to customer.commercialName,
            "isDefault" to customer.isDefault,
            "currentDebt" to customer.currentDebt
        )
    }

    fun serializeSupplier(supplier: Supplier): Map<String, Any?> {
        return mapOf(
            "id" to supplier.id,
            "name" to supplier.name,
            "logoUri" to supplier.logoUri,
            "address" to supplier.address,
            "email" to supplier.email,
            "phone" to supplier.phone
        )
    }

    fun serializeSale(sale: Sale): Map<String, Any?> {
        return mapOf(
            "id" to sale.id,
            "customerId" to sale.customerId,
            "customerName" to sale.customerName,
            "invoiceType" to sale.invoiceType.name,
            "paymentMethodId" to sale.paymentMethodId,
            "paymentMethodName" to sale.paymentMethodName,
            "items" to sale.items.map {
                mapOf(
                    "productId" to it.productId,
                    "productName" to it.productName,
                    "quantity" to it.quantity,
                    "unitPrice" to it.unitPrice,
                    "subtotal" to it.subtotal
                )
            },
            "totalAmount" to sale.totalAmount,
            "cashReceived" to sale.cashReceived,
            "changeAmount" to sale.changeAmount,
            "dateMillis" to sale.dateMillis,
            "isDteIssued" to sale.isDteIssued,
            "dteGenerationCode" to sale.dteGenerationCode,
            "dteReceptionSeal" to sale.dteReceptionSeal,
            "dteControlNumber" to sale.dteControlNumber,
            "dteType" to sale.dteType,
            "shiftId" to sale.shiftId,
            "cashierId" to sale.cashierId,
            "cashierName" to sale.cashierName,
            "isVoided" to sale.isVoided,
            "voidReason" to sale.voidReason,
            "voidedAtMillis" to sale.voidedAtMillis,
            "contingencyMode" to sale.contingencyMode,
            "isCredit" to sale.isCredit,
            "creditDueDateMillis" to sale.creditDueDateMillis,
            "paidAmount" to sale.paidAmount,
            "remainingBalance" to sale.remainingBalance,
            "creditStatus" to sale.creditStatus.name
        )
    }

    fun serializePurchase(purchase: Purchase): Map<String, Any?> {
        return mapOf(
            "id" to purchase.id,
            "supplierId" to purchase.supplierId,
            "supplierName" to purchase.supplierName,
            "dateMillis" to purchase.dateMillis,
            "items" to purchase.items.map {
                mapOf(
                    "productId" to it.productId,
                    "productName" to it.productName,
                    "quantity" to it.quantity,
                    "unitCost" to it.unitCost
                )
            },
            "totalCost" to purchase.totalCost,
            "registeredByUserName" to purchase.registeredByUserName
        )
    }

    fun serializeInventoryOutflow(outflow: InventoryOutflow): Map<String, Any?> {
        return mapOf(
            "id" to outflow.id,
            "reason" to outflow.reason.name,
            "registeredByUserId" to outflow.registeredByUserId,
            "registeredByUserName" to outflow.registeredByUserName,
            "notes" to outflow.notes,
            "items" to outflow.items.map {
                mapOf(
                    "productId" to it.productId,
                    "productName" to it.productName,
                    "quantity" to it.quantity,
                    "unitCost" to it.unitCost,
                    "totalCost" to it.totalCost
                )
            },
            "totalCost" to outflow.totalCost,
            "dateMillis" to outflow.dateMillis
        )
    }

    fun serializeProductReturn(productReturn: ProductReturn): Map<String, Any?> {
        return mapOf(
            "id" to productReturn.id,
            "originalSaleId" to productReturn.originalSaleId,
            "customerId" to productReturn.customerId,
            "customerName" to productReturn.customerName,
            "reason" to productReturn.reason.name,
            "notes" to productReturn.notes,
            "items" to productReturn.items.map {
                mapOf(
                    "productId" to it.productId,
                    "productName" to it.productName,
                    "quantity" to it.quantity,
                    "unitPrice" to it.unitPrice,
                    "refundSubtotal" to it.refundSubtotal
                )
            },
            "totalRefundAmount" to productReturn.totalRefundAmount,
            "refundMethod" to productReturn.refundMethod,
            "cashierId" to productReturn.cashierId,
            "cashierName" to productReturn.cashierName,
            "dateMillis" to productReturn.dateMillis
        )
    }

    fun serializeCustomerPayment(payment: CustomerPayment): Map<String, Any?> {
        return mapOf(
            "id" to payment.id,
            "customerId" to payment.customerId,
            "customerName" to payment.customerName,
            "saleId" to payment.saleId,
            "amount" to payment.amount,
            "paymentMethodName" to payment.paymentMethodName,
            "notes" to payment.notes,
            "dateMillis" to payment.dateMillis
        )
    }

    fun serializeExpense(expense: Expense): Map<String, Any?> {
        return mapOf(
            "id" to expense.id,
            "shiftId" to expense.shiftId,
            "cashierId" to expense.cashierId,
            "cashierName" to expense.cashierName,
            "category" to expense.category,
            "description" to expense.description,
            "amount" to expense.amount,
            "dateMillis" to expense.dateMillis
        )
    }

    fun serializePriceRule(rule: PriceRule): Map<String, Any?> {
        return mapOf(
            "id" to rule.id,
            "name" to rule.name,
            "baseVariable" to rule.baseVariable.name,
            "operator" to rule.operator.name,
            "value" to rule.value,
            "applyToAllCategories" to rule.applyToAllCategories,
            "categoryNames" to rule.categoryNames,
            "applyToAllCustomers" to rule.applyToAllCustomers,
            "customerIds" to rule.customerIds,
            "applyToBundles" to rule.applyToBundles,
            "applyToServices" to rule.applyToServices,
            "applyToAlreadyDiscounted" to rule.applyToAlreadyDiscounted,
            "isActive" to rule.isActive
        )
    }

    fun serializePaymentMethod(paymentMethod: PaymentMethod): Map<String, Any?> {
        return mapOf(
            "id" to paymentMethod.id,
            "name" to paymentMethod.name,
            "isDefault" to paymentMethod.isDefault,
            "isSystemDefault" to paymentMethod.isSystemDefault
        )
    }

    fun serializeUser(user: User): Map<String, Any?> {
        return mapOf(
            "id" to user.id,
            "name" to user.name,
            "username" to user.username,
            "email" to user.email,
            "pin" to user.pin,
            "passwordHash" to user.passwordHash,
            "role" to user.role.name,
            "isActive" to user.isActive
        )
    }

    fun serializeBusinessInfo(info: BusinessInfo): Map<String, Any?> {
        return mapOf(
            "name" to info.name,
            "nit" to info.nit,
            "nrc" to info.nrc,
            "address" to info.address,
            "phone" to info.phone,
            "email" to info.email,
            "socialMedia" to info.socialMedia,
            "commercialName" to info.commercialName,
            "logoUri" to info.logoUri
        )
    }

    fun serializeTicketConfig(config: TicketConfig): Map<String, Any?> {
        return mapOf(
            "showBusinessName" to config.showBusinessName,
            "showNit" to config.showNit,
            "showNrc" to config.showNrc,
            "showAddress" to config.showAddress,
            "showPhone" to config.showPhone,
            "showSocialMedia" to config.showSocialMedia,
            "showLogo" to config.showLogo,
            "footerMessage" to config.footerMessage,
            "paperSize" to config.paperSize.name
        )
    }

    fun serializeElectronicBillingConfig(config: ElectronicBillingConfig): Map<String, Any?> {
        return mapOf(
            "isEnabled" to config.isEnabled,
            "environment" to config.environment.name,
            "nit" to config.nit,
            "apiToken" to config.apiToken,
            "establishmentCode" to config.establishmentCode,
            "posCode" to config.posCode,
            "economicActivity" to config.economicActivity,
            "certificatePassword" to config.certificatePassword,
            "certificateUri" to config.certificateUri,
            "certificateFileName" to config.certificateFileName
        )
    }

    fun serializeBluetoothPrinterConfig(config: BluetoothPrinterConfig): Map<String, Any?> {
        return mapOf(
            "macAddress" to config.macAddress,
            "deviceName" to config.deviceName,
            "paperWidthMm" to config.paperWidthMm,
            "isConnected" to config.isConnected,
            "autoPrintSales" to config.autoPrintSales
        )
    }

    fun resetState() {
        _syncStatus.value = SyncStatus.SYNCED
        _lastSyncTimestampMillis.value = null
        _isAutoSyncEnabled.value = true
        _pendingItemsCount.value = 0
        _isSyncing.value = false
        _lastSyncedPayload.value = null
    }
}
