package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.CartItem
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.PaymentMethod
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.util.roundToTwoDecimals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

data class POSState(
    val products: List<Product> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val priceRules: List<PriceRule> = emptyList(),
    val selectedPriceRuleId: String? = null,
    val searchQuery: String = "",
    val total: Double = 0.0,
    val selectedCartItemIds: Set<String> = emptySet(),
    val customers: List<Customer> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedCustomerId: String? = null,
    val selectedInvoiceType: InvoiceType = InvoiceType.CONSUMIDOR_FINAL,
    val selectedPaymentMethodId: String? = null,
    val cashReceivedStr: String = "",
    val changeAmount: Double = 0.0
)

class POSViewModel(
    private val repository: AppRepository
) : ViewModel() {

    val sales: StateFlow<List<Sale>> = repository.sales

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedPriceRuleId = MutableStateFlow<String?>(null)
    private val _selectedCartItemIds = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedCustomerId = MutableStateFlow<String?>(null)
    private val _selectedInvoiceType = MutableStateFlow(InvoiceType.CONSUMIDOR_FINAL)
    private val _selectedPaymentMethodId = MutableStateFlow<String?>(null)
    private val _cashReceivedStr = MutableStateFlow("")

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedPriceRuleId: StateFlow<String?> = _selectedPriceRuleId.asStateFlow()
    val selectedCartItemIds: StateFlow<Set<String>> = _selectedCartItemIds.asStateFlow()
    val selectedCustomerId: StateFlow<String?> = _selectedCustomerId.asStateFlow()
    val selectedInvoiceType: StateFlow<InvoiceType> = _selectedInvoiceType.asStateFlow()
    val selectedPaymentMethodId: StateFlow<String?> = _selectedPaymentMethodId.asStateFlow()
    val cashReceivedStr: StateFlow<String> = _cashReceivedStr.asStateFlow()

    init {
        viewModelScope.launch {
            repository.customers.collect { customers ->
                if (_selectedCustomerId.value == null || customers.none { it.id == _selectedCustomerId.value }) {
                    _selectedCustomerId.value = customers.find { it.isDefault }?.id ?: customers.firstOrNull()?.id
                }
            }
        }
        viewModelScope.launch {
            repository.paymentMethods.collect { pms ->
                if (_selectedPaymentMethodId.value == null || pms.none { it.id == _selectedPaymentMethodId.value }) {
                    _selectedPaymentMethodId.value = pms.find { it.isDefault }?.id ?: pms.firstOrNull()?.id
                }
            }
        }
    }

    val uiState: StateFlow<POSState> = combine(
        repository.products,
        _cartItems,
        _searchQuery,
        _selectedPriceRuleId,
        _selectedCartItemIds,
        repository.priceRules,
        repository.customers,
        repository.paymentMethods,
        _selectedCustomerId,
        _selectedInvoiceType,
        _selectedPaymentMethodId,
        _cashReceivedStr
    ) { flows: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val products = flows[0] as List<Product>
        @Suppress("UNCHECKED_CAST")
        val rawCartItems = flows[1] as List<CartItem>
        val query = flows[2] as String
        val ruleId = flows[3] as String?
        @Suppress("UNCHECKED_CAST")
        val selectedIds = flows[4] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val allRules = flows[5] as List<PriceRule>
        @Suppress("UNCHECKED_CAST")
        val customers = flows[6] as List<Customer>
        @Suppress("UNCHECKED_CAST")
        val paymentMethods = flows[7] as List<PaymentMethod>
        val custId = flows[8] as String?
        val invoiceType = flows[9] as InvoiceType
        val pmId = flows[10] as String?
        val cashStr = flows[11] as String

        val activeRules = allRules.filter { it.isActive }

        val filteredProducts = if (query.isBlank()) {
            products
        } else {
            products.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                (product.barcode?.contains(query, ignoreCase = true) == true)
            }
        }

        val total = rawCartItems.sumOf { it.subtotal }.roundToTwoDecimals()

        val validItemIds = rawCartItems.map { it.id }.toSet()
        val sanitizedSelectedIds = selectedIds.filter { it in validItemIds }.toSet()

        val effectiveCustId = custId ?: customers.find { it.isDefault }?.id ?: customers.firstOrNull()?.id
        val effectivePmId = pmId ?: paymentMethods.find { it.isDefault }?.id ?: paymentMethods.firstOrNull()?.id

        val cashReceived = cashStr.toDoubleOrNull() ?: 0.0
        val changeAmount = maxOf(0.0, (cashReceived - total).roundToTwoDecimals())

        POSState(
            products = filteredProducts,
            cartItems = rawCartItems,
            priceRules = activeRules,
            selectedPriceRuleId = ruleId,
            searchQuery = query,
            total = total,
            selectedCartItemIds = sanitizedSelectedIds,
            customers = customers,
            paymentMethods = paymentMethods,
            selectedCustomerId = effectiveCustId,
            selectedInvoiceType = invoiceType,
            selectedPaymentMethodId = effectivePmId,
            cashReceivedStr = cashStr,
            changeAmount = changeAmount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = POSState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectPriceRule(ruleId: String?) {
        if (ruleId == null) {
            val currentRuleId = _selectedPriceRuleId.value
            if (currentRuleId != null) {
                val activeRules = repository.priceRules.value
                val currentRule = activeRules.find { it.id == currentRuleId }
                if (currentRule != null) {
                    togglePriceRule(currentRule)
                    return
                }
            }
            val selectedIds = _selectedCartItemIds.value
            _cartItems.update { cart ->
                cart.map { item ->
                    val isTarget = selectedIds.isEmpty() || item.id in selectedIds
                    if (isTarget && item.isRuleDiscounted) {
                        item.copy(
                            unitPriceOverride = null,
                            isRuleDiscounted = false,
                            appliedRuleId = null,
                            appliedRuleName = null
                        )
                    } else {
                        item
                    }
                }
            }
            _selectedPriceRuleId.value = null
        } else {
            val activeRules = repository.priceRules.value
            val rule = activeRules.find { it.id == ruleId }
            if (rule != null) {
                togglePriceRule(rule)
            } else {
                _selectedPriceRuleId.value = ruleId
            }
        }
    }

    fun togglePriceRule(rule: PriceRule) {
        val currentCart = _cartItems.value
        if (currentCart.isEmpty()) return

        val selectedIds = _selectedCartItemIds.value
        val isSelectionNotEmpty = selectedIds.isNotEmpty()

        // Target items: If selectedCartItemIds is NOT empty, targets are the selected cart items.
        // If NO items are selected (selectedCartItemIds is empty), targets are ALL cart items.
        val targetItems = if (isSelectionNotEmpty) {
            currentCart.filter { it.id in selectedIds }
        } else {
            currentCart
        }

        if (targetItems.isEmpty()) return

        // Toggle OFF condition: If all target items already have appliedRuleId == rule.id
        val allTargetItemsHaveRule = targetItems.all { it.appliedRuleId == rule.id }

        _cartItems.update { cart ->
            cart.map { item ->
                val isTarget = if (isSelectionNotEmpty) item.id in selectedIds else true
                if (!isTarget) {
                    item
                } else if (allTargetItemsHaveRule) {
                    // Remove rule from target items
                    item.copy(
                        unitPriceOverride = if (item.isRuleDiscounted) null else item.unitPriceOverride,
                        isRuleDiscounted = false,
                        appliedRuleId = null,
                        appliedRuleName = null
                    )
                } else {
                    // Apply price rule formula to target item
                    if (!rule.isActive) {
                        item.copy(
                            unitPriceOverride = if (item.isRuleDiscounted) null else item.unitPriceOverride,
                            isRuleDiscounted = false,
                            appliedRuleId = null,
                            appliedRuleName = null
                        )
                    } else {
                        val categoryMatch = rule.applyToAllCategories || rule.categoryNames.contains(item.product.category)
                        val bundleMatch = rule.applyToBundles || (!item.product.isBundle && !item.isBundleDiscounted)
                        val serviceMatch = rule.applyToServices || !item.product.isService
                        val discountMatch = rule.applyToAlreadyDiscounted || (!item.isBundleDiscounted && (item.unitPriceOverride == null || item.isRuleDiscounted))

                        if (categoryMatch && bundleMatch && serviceMatch && discountMatch) {
                            val basePrice = item.product.price
                            val ruleCalculatedPrice = rule.calculatePrice(basePrice, item.product.cost)
                                .coerceAtLeast(0.0)
                                .roundToTwoDecimals()

                            if (ruleCalculatedPrice < basePrice) {
                                item.copy(
                                    unitPriceOverride = ruleCalculatedPrice,
                                    isRuleDiscounted = true,
                                    appliedRuleId = rule.id,
                                    appliedRuleName = rule.name
                                )
                            } else {
                                if (item.isRuleDiscounted) {
                                    item.copy(
                                        unitPriceOverride = null,
                                        isRuleDiscounted = false,
                                        appliedRuleId = null,
                                        appliedRuleName = null
                                    )
                                } else {
                                    item
                                }
                            }
                        } else {
                            if (item.isRuleDiscounted) {
                                item.copy(
                                    unitPriceOverride = null,
                                    isRuleDiscounted = false,
                                    appliedRuleId = null,
                                    appliedRuleName = null
                                )
                            } else {
                                item
                            }
                        }
                    }
                }
            }
        }

        // Update selectedPriceRuleId state based on result
        val updatedCart = _cartItems.value
        val updatedTargets = if (isSelectionNotEmpty) {
            updatedCart.filter { it.id in selectedIds }
        } else {
            updatedCart
        }

        if (allTargetItemsHaveRule) {
            _selectedPriceRuleId.value = null
        } else if (updatedTargets.any { it.appliedRuleId == rule.id }) {
            _selectedPriceRuleId.value = rule.id
        } else {
            _selectedPriceRuleId.value = null
        }
    }

    fun toggleItemSelection(itemId: String) {
        _selectedCartItemIds.update { set ->
            if (set.contains(itemId)) set - itemId else set + itemId
        }
    }

    fun toggleItemSelection(item: CartItem) {
        toggleItemSelection(item.id)
    }

    fun selectAll() {
        _selectedCartItemIds.value = _cartItems.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedCartItemIds.value = emptySet()
    }

    private data class ActiveRuleResult(
        val rulePrice: Double? = null,
        val ruleId: String? = null,
        val ruleName: String? = null
    )

    private fun getActiveRuleForProduct(product: Product): ActiveRuleResult {
        val activeRuleId = _selectedPriceRuleId.value
        if (activeRuleId == null || _selectedCartItemIds.value.isNotEmpty()) return ActiveRuleResult()
        val rule = repository.priceRules.value.find { it.id == activeRuleId && it.isActive } ?: return ActiveRuleResult()

        val categoryMatch = rule.applyToAllCategories || rule.categoryNames.contains(product.category)
        val bundleMatch = rule.applyToBundles || !product.isBundle
        val serviceMatch = rule.applyToServices || !product.isService
        val discountMatch = rule.applyToAlreadyDiscounted

        if (categoryMatch && bundleMatch && serviceMatch && discountMatch) {
            val basePrice = product.price
            val ruleCalculatedPrice = rule.calculatePrice(basePrice, product.cost)
                .coerceAtLeast(0.0)
                .roundToTwoDecimals()

            if (ruleCalculatedPrice < basePrice) {
                return ActiveRuleResult(ruleCalculatedPrice, rule.id, rule.name)
            }
        }
        return ActiveRuleResult()
    }

    fun addToCart(product: Product) {
        val (rulePrice, ruleId, ruleName) = getActiveRuleForProduct(product)
        if (product.isService) {
            if (product.bundleItems.isNotEmpty()) {
                val currentProducts = repository.products.value
                val itemsWithProducts = product.bundleItems.mapNotNull { bundleItem ->
                    val itemProduct = currentProducts.find { it.id == bundleItem.productId }
                    if (itemProduct != null) bundleItem to itemProduct else null
                }
                val includedProductsTotal = itemsWithProducts.sumOf { (bundleItem, itemProduct) ->
                    itemProduct.price * bundleItem.quantity
                }
                val serviceRemainderPrice = (maxOf(0.0, product.price - includedProductsTotal)).roundToTwoDecimals()

                _cartItems.update { currentCart ->
                    var updatedCart = currentCart
                    for ((bundleItem, itemProduct) in itemsWithProducts) {
                        updatedCart = addCartItemToCart(
                            cart = updatedCart,
                            product = itemProduct,
                            quantityToAdd = bundleItem.quantity,
                            unitPriceOverride = null,
                            isBundleDiscounted = false
                        )
                    }
                    updatedCart = addCartItemToCart(
                        cart = updatedCart,
                        product = product,
                        quantityToAdd = 1,
                        unitPriceOverride = serviceRemainderPrice,
                        isBundleDiscounted = false
                    )
                    updatedCart
                }
            } else {
                _cartItems.update { currentCart ->
                    addCartItemToCart(
                        cart = currentCart,
                        product = product,
                        quantityToAdd = 1,
                        unitPriceOverride = rulePrice,
                        isBundleDiscounted = false,
                        appliedRuleId = ruleId,
                        appliedRuleName = ruleName
                    )
                }
            }
        } else if (product.isBundle) {
            val currentProducts = repository.products.value
            val itemsWithProducts = product.bundleItems.mapNotNull { bundleItem ->
                val itemProduct = currentProducts.find { it.id == bundleItem.productId }
                if (itemProduct != null) bundleItem to itemProduct else null
            }
            val originalTotal = itemsWithProducts.sumOf { (bundleItem, itemProduct) ->
                itemProduct.price * bundleItem.quantity
            }

            if (product.price < originalTotal && originalTotal > 0) {
                val ratio = product.price / originalTotal
                _cartItems.update { currentCart ->
                    var updatedCart = currentCart
                    for ((bundleItem, itemProduct) in itemsWithProducts) {
                        updatedCart = addCartItemToCart(
                            cart = updatedCart,
                            product = itemProduct,
                            quantityToAdd = bundleItem.quantity,
                            unitPriceOverride = (itemProduct.price * ratio).roundToTwoDecimals(),
                            isBundleDiscounted = true
                        )
                    }
                    updatedCart
                }
            } else {
                _cartItems.update { currentCart ->
                    var updatedCart = currentCart
                    for ((bundleItem, itemProduct) in itemsWithProducts) {
                        updatedCart = addCartItemToCart(
                            cart = updatedCart,
                            product = itemProduct,
                            quantityToAdd = bundleItem.quantity,
                            unitPriceOverride = null,
                            isBundleDiscounted = false
                        )
                    }
                    updatedCart
                }
            }
        } else {
            _cartItems.update { currentCart ->
                addCartItemToCart(
                    cart = currentCart,
                    product = product,
                    quantityToAdd = 1,
                    unitPriceOverride = rulePrice,
                    isBundleDiscounted = false,
                    appliedRuleId = ruleId,
                    appliedRuleName = ruleName
                )
            }
        }
    }

    fun addToCart(cartItem: CartItem) {
        _cartItems.update { currentCart ->
            val index = findMatchingIndex(currentCart, cartItem)
            if (index != -1) {
                currentCart.mapIndexed { i, item ->
                    if (i == index) item.copy(quantity = item.quantity + 1) else item
                }
            } else {
                addCartItemToCart(
                    cart = currentCart,
                    product = cartItem.product,
                    quantityToAdd = 1,
                    unitPriceOverride = if (cartItem.isRuleDiscounted) cartItem.unitPriceOverride else cartItem.unitPriceOverride,
                    isBundleDiscounted = cartItem.isBundleDiscounted,
                    appliedRuleId = cartItem.appliedRuleId,
                    appliedRuleName = cartItem.appliedRuleName
                )
            }
        }
    }

    fun pruneCartSelection() {
        val validIds = _cartItems.value.map { it.id }.toSet()
        _selectedCartItemIds.update { selectedIds ->
            selectedIds.intersect(validIds)
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        _cartItems.update { currentCart ->
            val index = findMatchingIndex(currentCart, cartItem)
            if (index != -1) {
                val item = currentCart[index]
                if (item.quantity > 1) {
                    currentCart.mapIndexed { i, current ->
                        if (i == index) current.copy(quantity = current.quantity - 1) else current
                    }
                } else {
                    currentCart.filterIndexed { i, _ -> i != index }
                }
            } else {
                currentCart
            }
        }
        checkAndDeactivatePriceRule()
        pruneCartSelection()
    }

    fun deleteFromCart(cartItem: CartItem) {
        _cartItems.update { currentCart ->
            val index = findMatchingIndex(currentCart, cartItem)
            if (index != -1) {
                currentCart.filterIndexed { i, _ -> i != index }
            } else {
                currentCart.filterNot { it.product.id == cartItem.product.id }
            }
        }
        checkAndDeactivatePriceRule()
        pruneCartSelection()
    }

    fun removeFromCart(product: Product) {
        _cartItems.update { currentCart ->
            val existingItem = currentCart.find { it.product.id == product.id }
            if (existingItem != null) {
                if (existingItem.quantity > 1) {
                    currentCart.map {
                        if (it.product.id == product.id) {
                            it.copy(quantity = it.quantity - 1)
                        } else {
                            it
                        }
                    }
                } else {
                    currentCart.filter { it.product.id != product.id }
                }
            } else {
                currentCart
            }
        }
        checkAndDeactivatePriceRule()
        pruneCartSelection()
    }

    fun deleteFromCart(product: Product) {
        _cartItems.update { currentCart ->
            currentCart.filterNot { it.product.id == product.id }
        }
        checkAndDeactivatePriceRule()
        pruneCartSelection()
    }

    private fun checkAndDeactivatePriceRule() {
        val currentRuleId = _selectedPriceRuleId.value ?: return
        if (!_cartItems.value.any { it.appliedRuleId == currentRuleId }) {
            _selectedPriceRuleId.value = null
        }
    }

    private fun findMatchingIndex(cart: List<CartItem>, cartItem: CartItem): Int {
        return cart.indexOfFirst {
            it.id == cartItem.id || (
                it.product.id == cartItem.product.id &&
                it.isBundleDiscounted == cartItem.isBundleDiscounted &&
                it.appliedRuleId == cartItem.appliedRuleId &&
                (cartItem.isRuleDiscounted || arePricesEqual(it.unitPriceOverride, cartItem.unitPriceOverride))
            )
        }
    }

    private fun addCartItemToCart(
        cart: List<CartItem>,
        product: Product,
        quantityToAdd: Int,
        unitPriceOverride: Double?,
        isBundleDiscounted: Boolean,
        appliedRuleId: String? = null,
        appliedRuleName: String? = null
    ): List<CartItem> {
        val isRuleDiscounted = appliedRuleId != null
        val existingIndex = cart.indexOfFirst { item ->
            item.product.id == product.id &&
            item.isBundleDiscounted == isBundleDiscounted &&
            item.appliedRuleId == appliedRuleId &&
            arePricesEqual(item.unitPriceOverride, unitPriceOverride)
        }

        return if (existingIndex != -1) {
            cart.mapIndexed { index, item ->
                if (index == existingIndex) {
                    item.copy(
                        quantity = item.quantity + quantityToAdd,
                        appliedRuleName = appliedRuleName ?: item.appliedRuleName
                    )
                } else {
                    item
                }
            }
        } else {
            cart + CartItem(
                product = product,
                quantity = quantityToAdd,
                unitPriceOverride = unitPriceOverride,
                isBundleDiscounted = isBundleDiscounted,
                isRuleDiscounted = isRuleDiscounted,
                appliedRuleId = appliedRuleId,
                appliedRuleName = appliedRuleName
            )
        }
    }

    private fun arePricesEqual(p1: Double?, p2: Double?): Boolean {
        if (p1 == null && p2 == null) return true
        if (p1 == null || p2 == null) return false
        return abs(p1 - p2) < 1e-6
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        pruneCartSelection()
        _selectedPriceRuleId.value = null
    }

    fun selectCustomer(customerId: String?) {
        _selectedCustomerId.value = customerId
    }

    fun selectInvoiceType(invoiceType: InvoiceType) {
        _selectedInvoiceType.value = invoiceType
    }

    fun selectPaymentMethod(paymentMethodId: String?) {
        _selectedPaymentMethodId.value = paymentMethodId
    }

    fun setCashReceivedStr(value: String) {
        _cashReceivedStr.value = value
    }

    fun processSale(): Sale? {
        val currentCart = _cartItems.value
        if (currentCart.isEmpty()) return null

        val customers = repository.customers.value
        val paymentMethods = repository.paymentMethods.value
        val total = currentCart.sumOf { it.subtotal }.roundToTwoDecimals()

        val custId = _selectedCustomerId.value ?: customers.find { it.isDefault }?.id ?: customers.firstOrNull()?.id ?: ""
        val custName = customers.find { it.id == custId }?.name ?: "Cliente General"

        val pmId = _selectedPaymentMethodId.value ?: paymentMethods.find { it.isDefault }?.id ?: paymentMethods.firstOrNull()?.id ?: ""
        val pmName = paymentMethods.find { it.id == pmId }?.name ?: "Efectivo"

        val cashReceived = _cashReceivedStr.value.toDoubleOrNull() ?: total
        val changeAmount = maxOf(0.0, (cashReceived - total).roundToTwoDecimals())

        val sale = Sale(
            customerId = custId,
            customerName = custName,
            invoiceType = _selectedInvoiceType.value,
            paymentMethodId = pmId,
            paymentMethodName = pmName,
            items = currentCart.map { item ->
                SaleItem(
                    productId = item.product.id,
                    productName = item.product.name,
                    quantity = item.quantity,
                    unitPrice = item.effectiveUnitPrice,
                    subtotal = item.subtotal
                )
            },
            totalAmount = total,
            cashReceived = cashReceived,
            changeAmount = changeAmount,
            dateMillis = System.currentTimeMillis()
        )

        repository.addSale(sale)
        clearCart()
        _cashReceivedStr.value = ""
        return sale
    }

    fun checkout() {
        processSale()
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(POSViewModel::class.java)) {
                return POSViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
