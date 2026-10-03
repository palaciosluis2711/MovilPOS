package com.lopezapp.movilpos.data.repository

import com.lopezapp.movilpos.data.model.BaseVariable
import com.lopezapp.movilpos.data.model.Brand
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.Category
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.DocumentType
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.PaymentMethod
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Purchase
import com.lopezapp.movilpos.data.model.PurchaseItem
import com.lopezapp.movilpos.data.model.Quotation
import com.lopezapp.movilpos.data.model.QuotationItem
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TaxValueType
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.UnitOfMeasure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppRepository {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

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
        _products.update { currentProducts ->
            currentProducts.map { product ->
                val matchingItem = purchase.items.find { it.productId == product.id }
                if (matchingItem != null) {
                    product.copy(
                        stock = product.stock + matchingItem.quantity,
                        cost = matchingItem.unitCost
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

    fun addSale(sale: Sale) {
        _sales.update { currentList ->
            currentList + sale
        }
        val quantitySoldMap = sale.items.groupBy { it.productId }
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
    }

    fun deleteSale(saleId: String) {
        _sales.update { currentList ->
            currentList.filter { it.id != saleId }
        }
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
}
