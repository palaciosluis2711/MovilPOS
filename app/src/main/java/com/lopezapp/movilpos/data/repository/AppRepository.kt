package com.lopezapp.movilpos.data.repository

import com.lopezapp.movilpos.data.model.Brand
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.BluetoothPrinterConfig
import com.lopezapp.movilpos.data.model.BarcodeLabelConfig
import com.lopezapp.movilpos.data.model.BatchLabelItem
import com.lopezapp.movilpos.data.model.CashShift
import com.lopezapp.movilpos.data.model.CreditStatus
import com.lopezapp.movilpos.data.model.CustomerPayment
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.Expense
import com.lopezapp.movilpos.data.model.Category
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.DocumentType
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.LabelSize
import com.lopezapp.movilpos.data.model.PaymentMethod
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Purchase
import com.lopezapp.movilpos.data.model.PurchaseItem
import com.lopezapp.movilpos.data.model.Quotation
import com.lopezapp.movilpos.data.model.QuotationItem
import com.lopezapp.movilpos.data.model.Role
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.model.ShiftStatus
import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TaxValueType
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.UnitOfMeasure
import com.lopezapp.movilpos.data.model.User
import android.content.Context
import android.net.Uri
import com.lopezapp.movilpos.util.DteApiClient
import com.lopezapp.movilpos.util.DteJsonGenerator
import com.lopezapp.movilpos.util.JwsSigner
import com.lopezapp.movilpos.util.ReceptionResponse
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.UUID

class AppRepository(
    private val dteApiClient: DteApiClient = DteApiClient.defaultInstance
) {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = object : StateFlow<List<Product>> {
        override val replayCache: List<List<Product>>
            get() = listOf(value)
        override val value: List<Product>
            get() = calculateDynamicStocks(_products.value)
        override suspend fun collect(collector: FlowCollector<List<Product>>): Nothing {
            _products.collect { list ->
                collector.emit(calculateDynamicStocks(list))
            }
        }
    }

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _brands = MutableStateFlow<List<Brand>>(emptyList())
    val brands: StateFlow<List<Brand>> = _brands.asStateFlow()

    private val _unitsOfMeasure = MutableStateFlow<List<UnitOfMeasure>>(emptyList())
    val unitsOfMeasure: StateFlow<List<UnitOfMeasure>> = _unitsOfMeasure.asStateFlow()

    private val _taxes = MutableStateFlow<List<Tax>>(emptyList())
    val taxes: StateFlow<List<Tax>> = _taxes.asStateFlow()

    private val _suppliers = MutableStateFlow<List<Supplier>>(emptyList())
    val suppliers: StateFlow<List<Supplier>> = _suppliers.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()

    private val _contingencyDtes = MutableStateFlow<List<Sale>>(emptyList())
    val contingencyDtes: StateFlow<List<Sale>> = _contingencyDtes.asStateFlow()

    private val _quotations = MutableStateFlow<List<Quotation>>(emptyList())
    val quotations: StateFlow<List<Quotation>> = _quotations.asStateFlow()

    private val _priceRules = MutableStateFlow<List<PriceRule>>(emptyList())
    val priceRules: StateFlow<List<PriceRule>> = _priceRules.asStateFlow()

    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    private val _businessInfo = MutableStateFlow(BusinessInfo())
    val businessInfo: StateFlow<BusinessInfo> = _businessInfo.asStateFlow()

    private val _ticketConfig = MutableStateFlow(TicketConfig())
    val ticketConfig: StateFlow<TicketConfig> = _ticketConfig.asStateFlow()

    private val _electronicBillingConfig = MutableStateFlow(ElectronicBillingConfig())
    val electronicBillingConfig: StateFlow<ElectronicBillingConfig> = _electronicBillingConfig.asStateFlow()

    private val _bluetoothPrinterConfig = MutableStateFlow(BluetoothPrinterConfig())
    val bluetoothPrinterConfig: StateFlow<BluetoothPrinterConfig> = _bluetoothPrinterConfig.asStateFlow()

    private val _labelSizes = MutableStateFlow<List<LabelSize>>(
        listOf(
            LabelSize(id = "std_1", name = "50 x 25 mm", widthMm = 50.0, heightMm = 25.0, isFavorite = false),
            LabelSize(id = "std_2", name = "40 x 30 mm", widthMm = 40.0, heightMm = 30.0, isFavorite = false),
            LabelSize(id = "std_3", name = "58 x 40 mm", widthMm = 58.0, heightMm = 40.0, isFavorite = false),
            LabelSize(id = "std_4", name = "100 x 50 mm", widthMm = 100.0, heightMm = 50.0, isFavorite = false)
        )
    )
    val labelSizes: StateFlow<List<LabelSize>> = _labelSizes.asStateFlow()

    private val _barcodeLabelConfig = MutableStateFlow(BarcodeLabelConfig())
    val barcodeLabelConfig: StateFlow<BarcodeLabelConfig> = _barcodeLabelConfig.asStateFlow()

    private val _batchItems = MutableStateFlow<List<BatchLabelItem>>(emptyList())
    val batchItems: StateFlow<List<BatchLabelItem>> = _batchItems.asStateFlow()

    fun preloadSingleProduct(product: Product) {
        _batchItems.value = listOf(
            BatchLabelItem(
                productId = product.id,
                productName = product.name,
                barcode = product.barcode ?: product.id,
                price = product.price,
                category = product.category,
                brand = product.brand,
                quantity = 1
            )
        )
    }

    fun addProductToBatch(product: Product, quantity: Int = 1) {
        _batchItems.update { currentList ->
            val existingIndex = currentList.indexOfFirst { it.productId == product.id }
            if (existingIndex >= 0) {
                currentList.mapIndexed { index, item ->
                    if (index == existingIndex) {
                        item.copy(quantity = item.quantity + quantity)
                    } else {
                        item
                    }
                }
            } else {
                currentList + BatchLabelItem(
                    productId = product.id,
                    productName = product.name,
                    barcode = product.barcode ?: product.id,
                    price = product.price,
                    category = product.category,
                    brand = product.brand,
                    quantity = quantity
                )
            }
        }
    }

    fun updateItemQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(productId)
            return
        }
        _batchItems.update { list ->
            list.map { if (it.productId == productId) it.copy(quantity = quantity) else it }
        }
    }

    fun removeItem(productId: String) {
        _batchItems.update { list -> list.filter { it.productId != productId } }
    }

    fun clearBatch() {
        _batchItems.value = emptyList()
    }

    fun updateBarcodeLabelConfig(config: BarcodeLabelConfig) {
        _barcodeLabelConfig.value = config
    }

    fun addFavoriteSize(name: String, widthMm: Double, heightMm: Double) {
        val newSize = LabelSize(
            name = name,
            widthMm = widthMm,
            heightMm = heightMm,
            isFavorite = true
        )
        _labelSizes.update { it + newSize }
    }

    fun removeFavoriteSize(sizeId: String) {
        _labelSizes.update { list -> list.filter { it.id != sizeId } }
        val currentConfig = _barcodeLabelConfig.value
        if (currentConfig.selectedSizeId == sizeId) {
            _barcodeLabelConfig.value = currentConfig.copy(selectedSizeId = "std_1")
        }
    }

    private val _users = MutableStateFlow<List<User>>(
        listOf(
            User(id = "admin_1", name = "Administrador", pin = "1234", role = Role.ADMINISTRATOR)
        )
    )
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _customerPayments = MutableStateFlow<List<CustomerPayment>>(emptyList())
    val customerPayments: StateFlow<List<CustomerPayment>> = _customerPayments.asStateFlow()

    private val _activeShift = MutableStateFlow<CashShift?>(null)
    val activeShift: StateFlow<CashShift?> = _activeShift.asStateFlow()

    private val _shiftHistory = MutableStateFlow<List<CashShift>>(emptyList())
    val shiftHistory: StateFlow<List<CashShift>> = _shiftHistory.asStateFlow()

    init {
        // Load initial dummy data
        _products.value = listOf(
            Product(name = "Coffee", price = 2.5, stock = 100, category = "Bebidas", brand = "Nestlé"),
            Product(name = "Tea", price = 1.5, stock = 50, category = "Bebidas", brand = "General"),
            Product(name = "Sandwich", price = 4.0, stock = 20, category = "Alimentos", brand = "Sin marca")
        )
        _categories.value = listOf(
            Category(name = "Bebidas", description = "Bebidas calientes y frías"),
            Category(name = "Alimentos", description = "Comidas y snacks"),
            Category(name = "Limpieza", description = "Artículos de limpieza"),
            Category(name = "General", description = "Categoría por defecto")
        )
        _brands.value = listOf(
            Brand(name = "Sin marca", description = "Sin marca específica"),
            Brand(name = "Coca-Cola", description = "Bebidas gaseosas y refrescos"),
            Brand(name = "Nestlé", description = "Alimentos y bebidas"),
            Brand(name = "General", description = "Marca general")
        )
        _unitsOfMeasure.value = listOf(
            UnitOfMeasure(name = "Unidad", abbreviation = "ud", isPackageOrBox = false),
            UnitOfMeasure(name = "Kilogramo", abbreviation = "kg", isPackageOrBox = false),
            UnitOfMeasure(name = "Libra", abbreviation = "lb", isPackageOrBox = false),
            UnitOfMeasure(name = "Caja", abbreviation = "cj", isPackageOrBox = true),
            UnitOfMeasure(name = "Paquete", abbreviation = "paq", isPackageOrBox = true)
        )
        _taxes.value = listOf(
            Tax(name = "IVA", description = "Impuesto al Valor Agregado", valueType = TaxValueType.PERCENTAGE, value = 16.0)
        )
        _suppliers.value = listOf(
            Supplier(
                name = "Distribuidora Central S.A.",
                address = "Av. Principal 123, Ciudad",
                email = "contacto@distribuidoracentral.com",
                phone = "+1 800 555 0199"
            ),
            Supplier(
                name = "Comercializadora del Norte",
                address = "Calle Industrial 45",
                email = "ventas@comercializadoranorte.com",
                phone = "+1 800 555 0200"
            )
        )
        _customers.value = listOf(
            Customer(
                name = "Cliente General",
                documentType = DocumentType.DUI,
                documentNumber = "00000000-0",
                country = "El Salvador",
                department = "San Salvador",
                municipality = "San Salvador Centro",
                district = "San Salvador",
                isDefault = true
            )
        )
        _purchases.value = listOf(
            Purchase(
                supplierId = _suppliers.value.firstOrNull()?.id ?: "",
                supplierName = _suppliers.value.firstOrNull()?.name ?: "Distribuidora Central S.A.",
                dateMillis = System.currentTimeMillis(),
                items = listOf(
                    PurchaseItem(
                        productId = _products.value.firstOrNull()?.id ?: "",
                        productName = _products.value.firstOrNull()?.name ?: "Coffee",
                        quantity = 10,
                        unitCost = 1.5
                    )
                ),
                totalCost = 15.0
            )
        )
        _paymentMethods.value = listOf(
            PaymentMethod(name = "Efectivo", isDefault = true),
            PaymentMethod(name = "Tarjeta de Crédito / Débito"),
            PaymentMethod(name = "Transferencia Bancaria"),
            PaymentMethod(name = "Cheque")
        )
        _sales.value = listOf(
            Sale(
                customerId = _customers.value.firstOrNull()?.id ?: "",
                customerName = _customers.value.firstOrNull()?.name ?: "Cliente General",
                invoiceType = InvoiceType.CONSUMIDOR_FINAL,
                paymentMethodId = _paymentMethods.value.firstOrNull()?.id ?: "",
                paymentMethodName = _paymentMethods.value.firstOrNull()?.name ?: "Efectivo",
                items = listOf(
                    SaleItem(
                        productId = _products.value.firstOrNull()?.id ?: "",
                        productName = _products.value.firstOrNull()?.name ?: "Coffee",
                        quantity = 2,
                        unitPrice = 2.5,
                        subtotal = 5.0
                    )
                ),
                totalAmount = 5.0,
                cashReceived = 10.0,
                changeAmount = 5.0,
                dateMillis = System.currentTimeMillis()
            )
        )
        _quotations.value = listOf(
            Quotation(
                customerName = "Cliente General",
                customerEmail = "cliente@ejemplo.com",
                dateMillis = System.currentTimeMillis(),
                expirationDateMillis = System.currentTimeMillis() + (15L * 24 * 3600 * 1000),
                items = listOf(
                    QuotationItem(
                        productId = _products.value.firstOrNull()?.id ?: "",
                        productName = _products.value.firstOrNull()?.name ?: "Coffee",
                        quantity = 5,
                        unitPrice = 2.5,
                        isDiscounted = false
                    )
                ),
                totalAmount = 12.5
            )
        )
        _expenses.value = listOf(
            Expense(
                id = "exp_1",
                category = "Servicios",
                description = "Pago de insumos de oficina",
                amount = 12.50,
                dateMillis = System.currentTimeMillis()
            )
        )
    }

    fun addProduct(product: Product) {
        _products.update { currentList ->
            currentList + product
        }
    }

    fun updateProduct(product: Product) {
        _products.update { currentList ->
            currentList.map { if (it.id == product.id) product else it }
        }
    }

    fun deleteProduct(productId: String) {
        _products.update { currentList ->
            currentList.filter { it.id != productId }
        }
    }

    fun addCategory(category: Category) {
        _categories.update { currentList ->
            currentList + category
        }
    }

    fun updateCategory(category: Category) {
        _categories.update { currentList ->
            currentList.map { if (it.id == category.id) category else it }
        }
    }

    fun deleteCategory(categoryId: String) {
        _categories.update { currentList ->
            currentList.filter { it.id != categoryId }
        }
    }

    fun addBrand(brand: Brand) {
        _brands.update { currentList ->
            currentList + brand
        }
    }

    fun updateBrand(brand: Brand) {
        _brands.update { currentList ->
            currentList.map { if (it.id == brand.id) brand else it }
        }
    }

    fun deleteBrand(brandId: String) {
        _brands.update { currentList ->
            currentList.filter { it.id != brandId }
        }
    }

    fun addUnitOfMeasure(unitOfMeasure: UnitOfMeasure) {
        _unitsOfMeasure.update { currentList ->
            currentList + unitOfMeasure
        }
    }

    fun updateUnitOfMeasure(unitOfMeasure: UnitOfMeasure) {
        _unitsOfMeasure.update { currentList ->
            currentList.map { if (it.id == unitOfMeasure.id) unitOfMeasure else it }
        }
    }

    fun deleteUnitOfMeasure(unitId: String) {
        _unitsOfMeasure.update { currentList ->
            currentList.filter { it.id != unitId }
        }
    }

    fun addTax(tax: Tax) {
        _taxes.update { currentList ->
            currentList + tax
        }
    }

    fun updateTax(tax: Tax) {
        _taxes.update { currentList ->
            currentList.map { if (it.id == tax.id) tax else it }
        }
    }

    fun deleteTax(taxId: String) {
        _taxes.update { currentList ->
            currentList.filter { it.id != taxId }
        }
    }

    fun addSupplier(supplier: Supplier) {
        _suppliers.update { currentList ->
            currentList + supplier
        }
    }

    fun updateSupplier(supplier: Supplier) {
        _suppliers.update { currentList ->
            currentList.map { if (it.id == supplier.id) supplier else it }
        }
    }

    fun deleteSupplier(supplierId: String) {
        _suppliers.update { currentList ->
            currentList.filter { it.id != supplierId }
        }
    }

    fun addCustomer(customer: Customer) {
        _customers.update { currentList ->
            val updatedList = if (customer.isDefault) {
                currentList.map { it.copy(isDefault = false) }
            } else {
                currentList
            }
            updatedList + customer
        }
    }

    fun updateCustomer(customer: Customer) {
        _customers.update { currentList ->
            val updatedList = if (customer.isDefault) {
                currentList.map { if (it.id == customer.id) customer else it.copy(isDefault = false) }
            } else {
                currentList.map { if (it.id == customer.id) customer else it }
            }
            updatedList
        }
    }

    fun deleteCustomer(customerId: String) {
        _customers.update { currentList ->
            currentList.filter { it.id != customerId }
        }
    }

    fun setDefaultCustomer(customerId: String) {
        _customers.update { currentList ->
            currentList.map { it.copy(isDefault = (it.id == customerId)) }
        }
    }

    fun addPurchase(purchase: Purchase) {
        _purchases.update { currentList ->
            currentList + purchase
        }
        
        val stockIncrements = mutableMapOf<String, Int>()
        val costUpdates = mutableMapOf<String, Double>()
        
        purchase.items.forEach { purchaseItem ->
            val purchasedProduct = _products.value.find { it.id == purchaseItem.productId }
            if (purchasedProduct != null) {
                if (purchasedProduct.isBundle) {
                    purchasedProduct.bundleItems.forEach { bundleItem ->
                        val currentInc = stockIncrements[bundleItem.productId] ?: 0
                        stockIncrements[bundleItem.productId] = currentInc + (purchaseItem.quantity * bundleItem.quantity)
                    }
                    costUpdates[purchasedProduct.id] = purchaseItem.unitCost
                } else {
                    val currentInc = stockIncrements[purchasedProduct.id] ?: 0
                    stockIncrements[purchasedProduct.id] = currentInc + purchaseItem.quantity
                    costUpdates[purchasedProduct.id] = purchaseItem.unitCost
                }
            }
        }

        _products.update { currentProducts ->
            currentProducts.map { product ->
                val inc = stockIncrements[product.id] ?: 0
                val newCost = costUpdates[product.id] ?: product.cost
                if (inc > 0 || costUpdates.containsKey(product.id)) {
                    product.copy(
                        stock = product.stock + inc,
                        cost = newCost
                    )
                } else {
                    product
                }
            }
        }
    }

    fun deletePurchase(purchaseId: String) {
        _purchases.update { currentList ->
            currentList.filter { it.id != purchaseId }
        }
    }

    fun addPriceRule(priceRule: PriceRule) {
        _priceRules.update { currentList ->
            currentList + priceRule
        }
    }

    fun updatePriceRule(priceRule: PriceRule) {
        _priceRules.update { currentList ->
            currentList.map { if (it.id == priceRule.id) priceRule else it }
        }
    }

    fun deletePriceRule(ruleId: String) {
        _priceRules.update { currentList ->
            currentList.filter { it.id != ruleId }
        }
    }

    fun addPaymentMethod(paymentMethod: PaymentMethod) {
        _paymentMethods.update { currentList ->
            val updatedList = if (paymentMethod.isDefault) {
                currentList.map { it.copy(isDefault = false) }
            } else {
                currentList
            }
            updatedList + paymentMethod
        }
    }

    fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        _paymentMethods.update { currentList ->
            val updatedList = if (paymentMethod.isDefault) {
                currentList.map { if (it.id == paymentMethod.id) paymentMethod else it.copy(isDefault = false) }
            } else {
                currentList.map { if (it.id == paymentMethod.id) paymentMethod else it }
            }
            updatedList
        }
    }

    fun deletePaymentMethod(paymentMethodId: String) {
        _paymentMethods.update { currentList ->
            currentList.filter { it.id != paymentMethodId }
        }
    }

    fun updateBusinessInfo(businessInfo: BusinessInfo) {
        _businessInfo.value = businessInfo
    }

    fun updateTicketConfig(config: TicketConfig) {
        _ticketConfig.value = config
    }
    
    fun updateElectronicBillingConfig(config: ElectronicBillingConfig) {
        _electronicBillingConfig.value = config
    }

    fun updateBluetoothPrinterConfig(config: BluetoothPrinterConfig) {
        _bluetoothPrinterConfig.value = config
    }

    fun addUser(user: User) {
        _users.update { currentList ->
            currentList + user
        }
    }

    fun updateUser(user: User) {
        _users.update { currentList ->
            currentList.map { if (it.id == user.id) user else it }
        }
        val currentShift = _activeShift.value
        if (currentShift != null && currentShift.cashierId == user.id) {
            val updatedShift = currentShift.copy(cashierName = user.name)
            _activeShift.value = updatedShift
            _shiftHistory.update { history ->
                val index = history.indexOfFirst { it.id == updatedShift.id }
                if (index >= 0) {
                    history.map { if (it.id == updatedShift.id) updatedShift else it }
                } else {
                    history + updatedShift
                }
            }
        }
    }

    fun deleteUser(userId: String) {
        _users.update { currentList ->
            currentList.filter { it.id != userId }
        }
    }

    fun validateAdminPin(pin: String): Boolean {
        return _users.value.any { user ->
            user.isActive &&
            (user.role == Role.ADMINISTRATOR || user.role == Role.SUPERVISOR) &&
            user.pin == pin
        }
    }

    fun openShift(cashier: User, initialFloat: Double): CashShift {
        val shift = CashShift(
            cashierId = cashier.id,
            cashierName = cashier.name,
            initialFloat = initialFloat,
            status = ShiftStatus.OPEN
        )
        _activeShift.value = shift
        _shiftHistory.update { history ->
            history + shift
        }
        return shift
    }

    fun closeShift(countedCash: Double): CashShift? {
        val currentShift = _activeShift.value ?: return null
        val closedShift = currentShift.copy(
            closedAtMillis = System.currentTimeMillis(),
            actualCashCounted = countedCash,
            status = ShiftStatus.CLOSED
        )
        _shiftHistory.update { history ->
            val index = history.indexOfFirst { it.id == closedShift.id }
            if (index >= 0) {
                history.map { if (it.id == closedShift.id) closedShift else it }
            } else {
                history + closedShift
            }
        }
        _activeShift.value = null
        return closedShift
    }

    fun updateActiveShiftSales(cashAmount: Double, cardAmount: Double, otherAmount: Double) {
        val shift = _activeShift.value ?: return
        val newCashSales = shift.totalCashSales + cashAmount
        val newCardSales = shift.totalCardSales + cardAmount
        val newOtherSales = shift.totalOtherSales + otherAmount
        val updatedShift = shift.copy(
            totalCashSales = newCashSales,
            totalCardSales = newCardSales,
            totalOtherSales = newOtherSales
        )
        _activeShift.value = updatedShift
        _shiftHistory.update { history ->
            val index = history.indexOfFirst { it.id == updatedShift.id }
            if (index >= 0) {
                history.map { if (it.id == updatedShift.id) updatedShift else it }
            } else {
                history + updatedShift
            }
        }
    }

    fun addSale(sale: Sale) {
        val currentShift = _activeShift.value
        val saleToSave = if (currentShift != null) {
            sale.copy(
                shiftId = currentShift.id,
                cashierId = if (sale.cashierId.isBlank()) currentShift.cashierId else sale.cashierId,
                cashierName = if (sale.cashierName.isBlank()) currentShift.cashierName else sale.cashierName
            )
        } else {
            sale
        }

        val finalSale = if (saleToSave.isCredit) {
            saleToSave.copy(
                remainingBalance = saleToSave.totalAmount,
                paidAmount = 0.0,
                creditStatus = CreditStatus.UNPAID
            )
        } else {
            saleToSave
        }

        _sales.update { currentList ->
            currentList + finalSale
        }

        if (finalSale.isCredit) {
            updateCustomerDebt(finalSale.customerId)
        }

        val quantitySoldMap = finalSale.items.groupBy { it.productId }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }

        _products.update { currentProducts ->
            currentProducts.map { product ->
                if (!product.isService) {
                    val totalQtySold = quantitySoldMap[product.id] ?: 0
                    if (totalQtySold > 0) {
                        product.copy(stock = (product.stock - totalQtySold).coerceAtLeast(0))
                    } else {
                        product
                    }
                } else {
                    product
                }
            }
        }

        if (currentShift != null) {
            val pmName = finalSale.paymentMethodName.lowercase()
            val cash = if (pmName.contains("efectivo")) finalSale.totalAmount else 0.0
            val card = if (pmName.contains("tarjeta") || pmName.contains("card")) finalSale.totalAmount else 0.0
            val other = if (!pmName.contains("efectivo") && !pmName.contains("tarjeta") && !pmName.contains("card")) finalSale.totalAmount else 0.0
            updateActiveShiftSales(cashAmount = cash, cardAmount = card, otherAmount = other)
        }
    }

    fun deleteSale(saleId: String) {
        val deletedSale = _sales.value.find { it.id == saleId }
        _sales.update { currentList ->
            currentList.filter { it.id != saleId }
        }
        _contingencyDtes.update { currentList ->
            currentList.filter { it.id != saleId }
        }
        if (deletedSale != null && deletedSale.isCredit) {
            updateCustomerDebt(deletedSale.customerId)
        }
    }

    fun addToContingencyQueue(sale: Sale) {
        val contingencySale = sale.copy(contingencyMode = true)
        _contingencyDtes.update { current ->
            if (current.any { it.id == contingencySale.id }) {
                current.map { if (it.id == contingencySale.id) contingencySale else it }
            } else {
                current + contingencySale
            }
        }
        val isNewSale = !_sales.value.any { it.id == contingencySale.id }
        _sales.update { currentSales ->
            if (currentSales.any { it.id == contingencySale.id }) {
                currentSales.map { if (it.id == contingencySale.id) contingencySale else it }
            } else {
                currentSales + contingencySale
            }
        }
        if (isNewSale) {
            val quantitySoldMap = contingencySale.items.groupBy { it.productId }
                .mapValues { entry -> entry.value.sumOf { it.quantity } }

            _products.update { currentProducts ->
                currentProducts.map { product ->
                    if (!product.isService) {
                        val totalQtySold = quantitySoldMap[product.id] ?: 0
                        if (totalQtySold > 0) {
                            product.copy(stock = (product.stock - totalQtySold).coerceAtLeast(0))
                        } else {
                            product
                        }
                    } else {
                        product
                    }
                }
            }

            val currentShift = _activeShift.value
            if (currentShift != null) {
                val pmName = contingencySale.paymentMethodName.lowercase()
                val cash = if (pmName.contains("efectivo")) contingencySale.totalAmount else 0.0
                val card = if (pmName.contains("tarjeta") || pmName.contains("card")) contingencySale.totalAmount else 0.0
                val other = if (!pmName.contains("efectivo") && !pmName.contains("tarjeta") && !pmName.contains("card")) contingencySale.totalAmount else 0.0
                updateActiveShiftSales(cashAmount = cash, cardAmount = card, otherAmount = other)
            }
        }
    }

    fun retryContingencyTransmissions(
        context: Context? = null,
        certificateInputStream: InputStream? = null
    ): Result<Int> {
        val config = _electronicBillingConfig.value
        val pendingList = _contingencyDtes.value
        if (pendingList.isEmpty()) return Result.success(0)

        if (config.isSimulationMode) {
            return runCatching {
                val businessInfo = _businessInfo.value
                var successCount = 0
                val remainingContingency = pendingList.toMutableList()

                for (sale in pendingList) {
                    val dteType = sale.dteType ?: (if (sale.invoiceType == InvoiceType.CREDITO_FISCAL) "03" else "01")
                    val generationCode = sale.dteGenerationCode ?: UUID.randomUUID().toString().uppercase()
                    val controlNumber = sale.dteControlNumber ?: "DTE-$dteType-${sale.id.take(8).uppercase()}-000000000000001"
                    val saleToTransmit = sale.copy(
                        dteType = dteType,
                        dteGenerationCode = generationCode,
                        dteControlNumber = controlNumber
                    )

                    val dteJson = DteJsonGenerator.generateDteJson(
                        sale = saleToTransmit,
                        businessInfo = businessInfo,
                        config = config
                    )

                    val signedJwsPayload = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.$dteJson.SIGNATURE"

                    val transmitResult = runCatching {
                        dteApiClient.transmitDte(
                            environment = config.environment,
                            token = "test-token",
                            signedJwsPayload = signedJwsPayload,
                            dteType = dteType,
                            generationCode = generationCode
                        ).getOrElse {
                            ReceptionResponse(
                                estado = "PROCESADO",
                                selloRecibido = "MH-DTE-" + System.currentTimeMillis(),
                                codigoGeneracion = generationCode
                            )
                        }
                    }

                    if (transmitResult.isSuccess) {
                        val receptionResponse = transmitResult.getOrNull()
                        val seal = receptionResponse?.selloRecibido ?: ("MH-DTE-" + System.currentTimeMillis())

                        val updatedSale = saleToTransmit.copy(
                            isDteIssued = true,
                            contingencyMode = false,
                            dteReceptionSeal = seal
                        )

                        _sales.update { currentSales ->
                            if (currentSales.any { it.id == updatedSale.id }) {
                                currentSales.map { if (it.id == updatedSale.id) updatedSale else it }
                            } else {
                                currentSales + updatedSale
                            }
                        }

                        remainingContingency.removeAll { it.id == sale.id }
                        successCount++
                    }
                }

                _contingencyDtes.value = remainingContingency
                successCount
            }
        }

        // Real Mode (isSimulationMode == false)
        val hasCredentials = !config.certificateUri.isNullOrBlank() &&
                config.apiToken.isNotBlank() &&
                config.nit.isNotBlank()

        if (!hasCredentials) {
            return Result.failure(Exception("Faltan credenciales DTE o certificado .p12 para retransmitir"))
        }

        val certStream: InputStream? = certificateInputStream ?: context?.let { ctx ->
            config.certificateUri?.let { uriStr ->
                runCatching {
                    ctx.contentResolver.openInputStream(Uri.parse(uriStr))
                }.getOrNull() ?: runCatching {
                    File(uriStr).takeIf { it.exists() }?.inputStream()
                }.getOrNull()
            }
        } ?: config.certificateUri?.let { uriStr ->
            runCatching { File(uriStr).takeIf { it.exists() }?.inputStream() }.getOrNull()
        }

        val certBytes = runCatching {
            certStream?.use { it.readBytes() }
        }.getOrNull()

        if (certBytes == null || certBytes.isEmpty()) {
            return Result.failure(Exception("Faltan credenciales DTE o certificado .p12 para retransmitir"))
        }

        val certValidation = JwsSigner.validateCertificate(
            certificateInputStream = ByteArrayInputStream(certBytes),
            password = config.certificatePassword
        )
        if (certValidation.isFailure) {
            return Result.failure(Exception("Faltan credenciales DTE o certificado .p12 para retransmitir"))
        }

        return runCatching {
            val businessInfo = _businessInfo.value

            val authResult = dteApiClient.authenticate(
                environment = config.environment,
                nit = config.nit,
                apiKey = config.apiToken
            )
            val authToken = authResult.getOrNull()

            var successCount = 0
            val remainingContingency = pendingList.toMutableList()

            for (sale in pendingList) {
                val dteType = sale.dteType ?: (if (sale.invoiceType == InvoiceType.CREDITO_FISCAL) "03" else "01")
                val generationCode = sale.dteGenerationCode ?: UUID.randomUUID().toString().uppercase()
                val controlNumber = sale.dteControlNumber ?: "DTE-$dteType-${sale.id.take(8).uppercase()}-000000000000001"
                val saleToTransmit = sale.copy(
                    dteType = dteType,
                    dteGenerationCode = generationCode,
                    dteControlNumber = controlNumber
                )

                val dteJson = DteJsonGenerator.generateDteJson(
                    sale = saleToTransmit,
                    businessInfo = businessInfo,
                    config = config
                )

                val signedJwsResult = JwsSigner.signDteJson(
                    jsonPayload = dteJson,
                    certificateInputStream = ByteArrayInputStream(certBytes),
                    password = config.certificatePassword
                )

                if (signedJwsResult.isSuccess) {
                    val signedJwsPayload = signedJwsResult.getOrThrow()
                    val transmitResult = dteApiClient.transmitDte(
                        environment = config.environment,
                        token = authToken ?: "test-token",
                        signedJwsPayload = signedJwsPayload,
                        dteType = dteType,
                        generationCode = generationCode
                    )

                    if (transmitResult.isSuccess) {
                        val receptionResponse = transmitResult.getOrNull()
                        val seal = receptionResponse?.selloRecibido ?: ("MH-DTE-" + System.currentTimeMillis())

                        val updatedSale = saleToTransmit.copy(
                            isDteIssued = true,
                            contingencyMode = false,
                            dteReceptionSeal = seal
                        )

                        _sales.update { currentSales ->
                            if (currentSales.any { it.id == updatedSale.id }) {
                                currentSales.map { if (it.id == updatedSale.id) updatedSale else it }
                            } else {
                                currentSales + updatedSale
                            }
                        }

                        remainingContingency.removeAll { it.id == sale.id }
                        successCount++
                    }
                }
            }

            _contingencyDtes.value = remainingContingency
            successCount
        }
    }

    fun voidSaleDte(saleId: String, reason: String): Result<Unit> {
        val sale = _sales.value.find { it.id == saleId }
            ?: return Result.failure(Exception("Venta no encontrada"))

        val voidedAt = System.currentTimeMillis()
        val updatedSale = sale.copy(
            isVoided = true,
            voidReason = reason,
            voidedAtMillis = voidedAt
        )

        val businessInfo = _businessInfo.value
        val config = _electronicBillingConfig.value

        val invalidationJson = DteJsonGenerator.generateDteInvalidationJson(
            sale = updatedSale,
            reason = reason,
            businessInfo = businessInfo
        )

        if (config.isEnabled && config.nit.isNotBlank() && config.apiToken.isNotBlank() && !config.apiToken.equals("test", ignoreCase = true)) {
            runCatching {
                val authResult = dteApiClient.authenticate(
                    environment = config.environment,
                    nit = config.nit,
                    apiKey = config.apiToken
                )
                val token = authResult.getOrThrow()
                dteApiClient.voidDte(
                    environment = config.environment,
                    token = token,
                    signedJwsPayload = invalidationJson,
                    generationCode = sale.dteGenerationCode ?: UUID.randomUUID().toString()
                )
            }
        }

        _sales.update { currentSales ->
            currentSales.map { if (it.id == saleId) updatedSale else it }
        }

        if (sale.isCredit) {
            updateCustomerDebt(sale.customerId)
        }

        return Result.success(Unit)
    }

    fun addQuotation(quotation: Quotation) {
        _quotations.update { currentList ->
            currentList + quotation
        }
    }

    fun updateQuotation(quotation: Quotation) {
        _quotations.update { currentList ->
            currentList.map { if (it.id == quotation.id) quotation else it }
        }
    }

    fun deleteQuotation(quotationId: String) {
        _quotations.update { currentList ->
            currentList.filter { it.id != quotationId }
        }
    }

    fun addExpense(expense: Expense) {
        val currentShift = _activeShift.value
        val expenseToSave = if (currentShift != null) {
            expense.copy(
                shiftId = expense.shiftId ?: currentShift.id,
                cashierId = if (expense.cashierId.isBlank()) currentShift.cashierId else expense.cashierId,
                cashierName = if (expense.cashierName.isBlank()) currentShift.cashierName else expense.cashierName
            )
        } else {
            expense
        }

        _expenses.update { currentList ->
            currentList + expenseToSave
        }

        if (currentShift != null) {
            val updatedShift = currentShift.copy(
                totalExpenses = currentShift.totalExpenses + expenseToSave.amount
            )
            _activeShift.value = updatedShift
            _shiftHistory.update { history ->
                val index = history.indexOfFirst { it.id == updatedShift.id }
                if (index >= 0) {
                    history.map { if (it.id == updatedShift.id) updatedShift else it }
                } else {
                    history + updatedShift
                }
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        _expenses.update { currentList ->
            currentList.filter { it.id != expenseId }
        }
    }

    fun addCustomerPayment(payment: CustomerPayment) {
        _customerPayments.update { currentList ->
            currentList + payment
        }

        if (payment.saleId != null) {
            _sales.update { currentSales ->
                currentSales.map { sale ->
                    if (sale.id == payment.saleId) {
                        val newPaidAmount = sale.paidAmount + payment.amount
                        val newRemainingBalance = maxOf(0.0, sale.totalAmount - newPaidAmount)
                        val newCreditStatus = when {
                            newRemainingBalance <= 0.0 -> CreditStatus.PAID
                            newPaidAmount > 0.0 -> CreditStatus.PARTIALLY_PAID
                            else -> sale.creditStatus
                        }
                        sale.copy(
                            paidAmount = newPaidAmount,
                            remainingBalance = newRemainingBalance,
                            creditStatus = newCreditStatus
                        )
                    } else {
                        sale
                    }
                }
            }
        } else {
            var remainingPayment = payment.amount
            if (remainingPayment > 0.0) {
                _sales.update { currentSales ->
                    val updatedList = currentSales.toMutableList()
                    val pendingIndices = updatedList.indices.filter { idx ->
                        val s = updatedList[idx]
                        s.customerId == payment.customerId && s.isCredit && !s.isVoided && s.remainingBalance > 0.0
                    }.sortedBy { updatedList[it].dateMillis }

                    for (idx in pendingIndices) {
                        if (remainingPayment <= 0.0) break
                        val sale = updatedList[idx]
                        val applyAmount = minOf(remainingPayment, sale.remainingBalance)
                        val newPaidAmount = sale.paidAmount + applyAmount
                        val newRemainingBalance = maxOf(0.0, sale.totalAmount - newPaidAmount)
                        val newCreditStatus = when {
                            newRemainingBalance <= 0.0 -> CreditStatus.PAID
                            newPaidAmount > 0.0 -> CreditStatus.PARTIALLY_PAID
                            else -> sale.creditStatus
                        }
                        updatedList[idx] = sale.copy(
                            paidAmount = newPaidAmount,
                            remainingBalance = newRemainingBalance,
                            creditStatus = newCreditStatus
                        )
                        remainingPayment -= applyAmount
                    }
                    updatedList
                }
            }
        }

        updateCustomerDebt(payment.customerId)
    }

    private fun updateCustomerDebt(customerId: String) {
        if (customerId.isBlank()) return
        val creditSales = _sales.value.filter { it.customerId == customerId && it.isCredit && !it.isVoided }
        val payments = _customerPayments.value.filter { it.customerId == customerId }
        val totalCredit = creditSales.sumOf { it.totalAmount }
        val totalPaid = payments.sumOf { it.amount }
        val debt = maxOf(0.0, totalCredit - totalPaid)

        _customers.update { currentCustomers ->
            currentCustomers.map { customer ->
                if (customer.id == customerId) {
                    customer.copy(currentDebt = debt)
                } else {
                    customer
                }
            }
        }
    }

    private fun calculateDynamicStocks(products: List<Product>): List<Product> {
        val stockMap = products.associate { it.id to it.stock }.toMutableMap()

        repeat(5) {
            val newMap = stockMap.toMutableMap()
            for (p in products) {
                if (p.isBundle || p.isService) {
                    val calculated = if (p.bundleItems.isEmpty()) {
                        if (p.isService) 9999 else 0
                    } else {
                        p.bundleItems.minOfOrNull { item ->
                            val compStock = stockMap[item.productId] ?: 0
                            val reqQty = item.quantity.coerceAtLeast(1)
                            compStock / reqQty
                        } ?: 0
                    }
                    newMap[p.id] = calculated
                } else {
                    newMap[p.id] = p.stock
                }
            }
            stockMap.clear()
            stockMap.putAll(newMap)
        }

        return products.map { p ->
            p.copy(stock = stockMap[p.id] ?: p.stock)
        }
    }
}
