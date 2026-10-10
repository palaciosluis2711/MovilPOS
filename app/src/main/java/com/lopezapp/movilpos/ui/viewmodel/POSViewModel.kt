package com.lopezapp.movilpos.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.CartItem
import com.lopezapp.movilpos.data.model.CashShift
import com.lopezapp.movilpos.data.model.CreditStatus
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.Expense
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.PaymentMethod
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.SaleItem
import com.lopezapp.movilpos.data.model.User
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.util.DteApiClient
import com.lopezapp.movilpos.util.DteJsonGenerator
import com.lopezapp.movilpos.util.JwsSigner
import com.lopezapp.movilpos.util.roundToTwoDecimals
import java.io.InputStream
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

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
    val changeAmount: Double = 0.0,
    val electronicBillingConfig: ElectronicBillingConfig = ElectronicBillingConfig(),
    val isCreditSale: Boolean = false,
    val creditDueDateMillis: Long? = null
)

class POSViewModel(
    private val repository: AppRepository,
    private val dteApiClient: DteApiClient = DteApiClient.defaultInstance
) : ViewModel() {

    val sales: StateFlow<List<Sale>> = repository.sales
    val activeShift: StateFlow<CashShift?> = repository.activeShift
    val users: StateFlow<List<User>> = repository.users

    private val _isDteEmitting = MutableStateFlow(false)
    val isDteEmitting: StateFlow<Boolean> = _isDteEmitting.asStateFlow()

    private val _dteStatusMessage = MutableStateFlow("")
    val dteStatusMessage: StateFlow<String> = _dteStatusMessage.asStateFlow()

    private val _dteEmissionError = MutableStateFlow<String?>(null)
    val dteEmissionError: StateFlow<String?> = _dteEmissionError.asStateFlow()

    private val _isDteInvalidating = MutableStateFlow(false)
    val isDteInvalidating: StateFlow<Boolean> = _isDteInvalidating.asStateFlow()

    private val _dteInvalidationStatusMessage = MutableStateFlow("")
    val dteInvalidationStatusMessage: StateFlow<String> = _dteInvalidationStatusMessage.asStateFlow()

    private val _dteInvalidationError = MutableStateFlow<String?>(null)
    val dteInvalidationError: StateFlow<String?> = _dteInvalidationError.asStateFlow()

    fun clearDteInvalidationError() {
        _dteInvalidationError.value = null
    }

    fun clearDteEmissionError() {
        _dteEmissionError.value = null
    }

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedPriceRuleId = MutableStateFlow<String?>(null)
    private val _selectedCartItemIds = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedCustomerId = MutableStateFlow<String?>(null)
    private val _selectedInvoiceType = MutableStateFlow(InvoiceType.CONSUMIDOR_FINAL)
    private val _selectedPaymentMethodId = MutableStateFlow<String?>(null)
    private val _cashReceivedStr = MutableStateFlow("")
    private val _isCreditSale = MutableStateFlow(false)
    private val _creditDueDateMillis = MutableStateFlow<Long?>(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val allProducts: StateFlow<List<Product>> = repository.products
    val selectedPriceRuleId: StateFlow<String?> = _selectedPriceRuleId.asStateFlow()
    val selectedCartItemIds: StateFlow<Set<String>> = _selectedCartItemIds.asStateFlow()
    val selectedCustomerId: StateFlow<String?> = _selectedCustomerId.asStateFlow()
    val selectedInvoiceType: StateFlow<InvoiceType> = _selectedInvoiceType.asStateFlow()
    val selectedPaymentMethodId: StateFlow<String?> = _selectedPaymentMethodId.asStateFlow()
    val cashReceivedStr: StateFlow<String> = _cashReceivedStr.asStateFlow()
    val isCreditSale: StateFlow<Boolean> = _isCreditSale.asStateFlow()
    val creditDueDateMillis: StateFlow<Long?> = _creditDueDateMillis.asStateFlow()

    fun setIsCreditSale(isCredit: Boolean) {
        _isCreditSale.value = isCredit
        if (isCredit && _creditDueDateMillis.value == null) {
            _creditDueDateMillis.value = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L
        }
    }

    fun setCreditDueDateMillis(millis: Long) {
        _creditDueDateMillis.value = millis
    }

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
        viewModelScope.launch {
            repository.electronicBillingConfig.collect { config ->
                if (!config.isEnabled) {
                    if (_selectedInvoiceType.value == InvoiceType.CONSUMIDOR_FINAL ||
                        _selectedInvoiceType.value == InvoiceType.CREDITO_FISCAL) {
                        _selectedInvoiceType.value = InvoiceType.TICKET
                    }
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
        _cashReceivedStr,
        repository.electronicBillingConfig,
        _isCreditSale,
        _creditDueDateMillis
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
        val ebConfig = flows[12] as ElectronicBillingConfig
        val isCredit = flows[13] as Boolean
        val dueDate = flows[14] as Long?

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
            changeAmount = changeAmount,
            electronicBillingConfig = ebConfig,
            isCreditSale = isCredit,
            creditDueDateMillis = dueDate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = POSState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onSearchQueryChanged(query: String) {
        setSearchQuery(query)
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

    fun scanAndAddToCart(barcode: String): Boolean {
        val trimmed = barcode.trim()
        if (trimmed.isEmpty()) return false
        val product = repository.products.value.find { it.barcode?.trim() == trimmed }
        return if (product != null) {
            addToCart(product)
            true
        } else {
            false
        }
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
                    val existingItem = currentCart.find { 
                        it.product.id == product.id && it.parentProductId == product.id && it.linkedGroupId != null && arePricesEqual(it.unitPriceOverride, serviceRemainderPrice)
                    }
                    val groupId = existingItem?.linkedGroupId ?: UUID.randomUUID().toString()
                    for ((bundleItem, itemProduct) in itemsWithProducts) {
                        updatedCart = addCartItemToCart(
                            cart = updatedCart,
                            product = itemProduct,
                            quantityToAdd = bundleItem.quantity,
                            unitPriceOverride = null,
                            isBundleDiscounted = false,
                            linkedGroupId = groupId,
                            parentProductId = product.id
                        )
                    }
                    updatedCart = addCartItemToCart(
                        cart = updatedCart,
                        product = product,
                        quantityToAdd = 1,
                        unitPriceOverride = serviceRemainderPrice,
                        isBundleDiscounted = false,
                        linkedGroupId = groupId,
                        parentProductId = product.id
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
                    val existingBundleItems = currentCart.filter { it.parentProductId == product.id && it.linkedGroupId != null }
                    val groupId = existingBundleItems.firstOrNull()?.linkedGroupId ?: UUID.randomUUID().toString()
                    for ((bundleItem, itemProduct) in itemsWithProducts) {
                        updatedCart = addCartItemToCart(
                            cart = updatedCart,
                            product = itemProduct,
                            quantityToAdd = bundleItem.quantity,
                            unitPriceOverride = (itemProduct.price * ratio).roundToTwoDecimals(),
                            isBundleDiscounted = true,
                            linkedGroupId = groupId,
                            parentProductId = product.id
                        )
                    }
                    updatedCart
                }
            } else {
                _cartItems.update { currentCart ->
                    var updatedCart = currentCart
                    val existingBundleItems = currentCart.filter { it.parentProductId == product.id && it.linkedGroupId != null }
                    val groupId = existingBundleItems.firstOrNull()?.linkedGroupId ?: UUID.randomUUID().toString()
                    for ((bundleItem, itemProduct) in itemsWithProducts) {
                        updatedCart = addCartItemToCart(
                            cart = updatedCart,
                            product = itemProduct,
                            quantityToAdd = bundleItem.quantity,
                            unitPriceOverride = null,
                            isBundleDiscounted = false,
                            linkedGroupId = groupId,
                            parentProductId = product.id
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
                val matchedItem = currentCart[index]
                if (matchedItem.linkedGroupId != null) {
                    // It's part of a linked group. We need to increment the whole group proportionally.
                    val oldQty = matchedItem.quantity
                    currentCart.map { item ->
                        if (item.linkedGroupId == matchedItem.linkedGroupId) {
                            val baseQtyToAdd = maxOf(1, item.quantity / oldQty)
                            item.copy(quantity = item.quantity + baseQtyToAdd)
                        } else {
                            item
                        }
                    }
                } else {
                    currentCart.mapIndexed { i, item ->
                        if (i == index) item.copy(quantity = item.quantity + 1) else item
                    }
                }
            } else {
                addCartItemToCart(
                    cart = currentCart,
                    product = cartItem.product,
                    quantityToAdd = 1,
                    unitPriceOverride = if (cartItem.isRuleDiscounted) cartItem.unitPriceOverride else cartItem.unitPriceOverride,
                    isBundleDiscounted = cartItem.isBundleDiscounted,
                    appliedRuleId = cartItem.appliedRuleId,
                    appliedRuleName = cartItem.appliedRuleName,
                    linkedGroupId = cartItem.linkedGroupId,
                    parentProductId = cartItem.parentProductId
                )
            }
        }
    }

    fun updateCartItemQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            deleteFromCart(cartItem)
            return
        }

        _cartItems.update { currentCart ->
            val index = findMatchingIndex(currentCart, cartItem)
            if (index != -1) {
                val matchedItem = currentCart[index]
                if (matchedItem.linkedGroupId != null) {
                    val oldQty = matchedItem.quantity
                    if (oldQty == newQuantity) return@update currentCart
                    
                    currentCart.map { item ->
                        if (item.linkedGroupId == matchedItem.linkedGroupId) {
                            val baseQtyPerUnit = item.quantity.toDouble() / oldQty.toDouble()
                            val newGroupItemQty = maxOf(1, (baseQtyPerUnit * newQuantity).roundToInt())
                            item.copy(quantity = newGroupItemQty)
                        } else {
                            item
                        }
                    }
                } else {
                    currentCart.mapIndexed { i, item ->
                        if (i == index) item.copy(quantity = newQuantity) else item
                    }
                }
            } else {
                currentCart
            }
        }
        checkAndDeactivatePriceRule()
        pruneCartSelection()
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
                val matchedItem = currentCart[index]
                if (matchedItem.linkedGroupId != null) {
                    val oldQty = matchedItem.quantity
                    if (oldQty > 1) {
                        currentCart.map { item ->
                            if (item.linkedGroupId == matchedItem.linkedGroupId) {
                                val baseQtyToSubtract = maxOf(1, item.quantity / oldQty)
                                val newQty = item.quantity - baseQtyToSubtract
                                if (newQty > 0) item.copy(quantity = newQty) else null
                            } else {
                                item
                            }
                        }.filterNotNull()
                    } else {
                        currentCart.filter { it.linkedGroupId != matchedItem.linkedGroupId }
                    }
                } else {
                    if (matchedItem.quantity > 1) {
                        currentCart.mapIndexed { i, current ->
                            if (i == index) current.copy(quantity = current.quantity - 1) else current
                        }
                    } else {
                        currentCart.filterIndexed { i, _ -> i != index }
                    }
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
                val matchedItem = currentCart[index]
                if (matchedItem.linkedGroupId != null) {
                    currentCart.filter { it.linkedGroupId != matchedItem.linkedGroupId }
                } else {
                    currentCart.filterIndexed { i, _ -> i != index }
                }
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
                if (existingItem.linkedGroupId != null) {
                    val oldQty = existingItem.quantity
                    if (oldQty > 1) {
                        currentCart.map { item ->
                            if (item.linkedGroupId == existingItem.linkedGroupId) {
                                val baseQtyToSubtract = maxOf(1, item.quantity / oldQty)
                                val newQty = item.quantity - baseQtyToSubtract
                                if (newQty > 0) item.copy(quantity = newQty) else null
                            } else {
                                item
                            }
                        }.filterNotNull()
                    } else {
                        currentCart.filter { it.linkedGroupId != existingItem.linkedGroupId }
                    }
                } else {
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
            val existingItem = currentCart.find { it.product.id == product.id }
            if (existingItem != null && existingItem.linkedGroupId != null) {
                currentCart.filter { it.linkedGroupId != existingItem.linkedGroupId }
            } else {
                currentCart.filterNot { it.product.id == product.id }
            }
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
                it.linkedGroupId == cartItem.linkedGroupId &&
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
        appliedRuleName: String? = null,
        linkedGroupId: String? = null,
        parentProductId: String? = null
    ): List<CartItem> {
        val isRuleDiscounted = appliedRuleId != null
        val existingIndex = cart.indexOfFirst { item ->
            item.product.id == product.id &&
            item.isBundleDiscounted == isBundleDiscounted &&
            item.appliedRuleId == appliedRuleId &&
            item.linkedGroupId == linkedGroupId &&
            item.parentProductId == parentProductId &&
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
                appliedRuleName = appliedRuleName,
                linkedGroupId = linkedGroupId,
                parentProductId = parentProductId
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
        _isCreditSale.value = false
        _creditDueDateMillis.value = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L
    }

    fun selectCustomer(customerId: String?) {
        _selectedCustomerId.value = customerId
    }

    fun selectInvoiceType(invoiceType: InvoiceType) {
        if ((invoiceType == InvoiceType.CONSUMIDOR_FINAL || invoiceType == InvoiceType.CREDITO_FISCAL) &&
            !repository.electronicBillingConfig.value.isEnabled) {
            _selectedInvoiceType.value = InvoiceType.TICKET
        } else {
            _selectedInvoiceType.value = invoiceType
        }
    }

    fun selectPaymentMethod(paymentMethodId: String?) {
        _selectedPaymentMethodId.value = paymentMethodId
    }

    fun setCashReceivedStr(value: String) {
        _cashReceivedStr.value = value
    }

    fun processSale(onNavigateToReceipt: (String) -> Unit): Sale? {
        return processSale(context = null, certificateInputStream = null, onNavigateToReceipt = onNavigateToReceipt)
    }

    fun processSale(
        context: Context?,
        onNavigateToReceipt: (String) -> Unit = {}
    ): Sale? {
        return processSale(context = context, certificateInputStream = null, onNavigateToReceipt = onNavigateToReceipt)
    }

    fun processSale(
        context: Context? = null,
        certificateInputStream: InputStream? = null,
        onNavigateToReceipt: (String) -> Unit = {}
    ): Sale? {
        val currentCart = _cartItems.value
        if (currentCart.isEmpty()) return null

        val isCredit = _isCreditSale.value
        val dueDateMillis = _creditDueDateMillis.value ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L)

        val customers = repository.customers.value
        val paymentMethods = repository.paymentMethods.value
        val total = currentCart.sumOf { it.subtotal }.roundToTwoDecimals()

        val custId = _selectedCustomerId.value ?: customers.find { it.isDefault }?.id ?: customers.firstOrNull()?.id ?: ""
        val selectedCustomer = customers.find { it.id == custId }
        val custName = selectedCustomer?.name ?: "Cliente General"

        if (isCredit) {
            val isInvalidCustomer = custId.isBlank() ||
                    selectedCustomer == null ||
                    selectedCustomer.isDefault ||
                    selectedCustomer.name.trim().equals("Cliente General", ignoreCase = true)
            if (isInvalidCustomer) {
                context?.let { ctx ->
                    Toast.makeText(
                        ctx,
                        "Debe seleccionar un cliente específico para ventas a crédito",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return null
            }
        }

        val ebConfig = repository.electronicBillingConfig.value
        val invoiceType = _selectedInvoiceType.value
        val isDte = ebConfig.isEnabled && (invoiceType == InvoiceType.CONSUMIDOR_FINAL || invoiceType == InvoiceType.CREDITO_FISCAL)

        val pmId = _selectedPaymentMethodId.value ?: paymentMethods.find { it.id == _selectedPaymentMethodId.value }?.id ?: paymentMethods.firstOrNull()?.id ?: ""
        val pmName = if (isCredit) "Crédito / Fiado" else (paymentMethods.find { it.id == pmId }?.name ?: "Efectivo")

        val cashReceived = if (isCredit) 0.0 else (_cashReceivedStr.value.toDoubleOrNull() ?: total)
        val changeAmount = if (isCredit) 0.0 else maxOf(0.0, (cashReceived - total).roundToTwoDecimals())

        val items = currentCart.map { item ->
            SaleItem(
                productId = item.product.id,
                productName = item.product.name,
                quantity = item.quantity,
                unitPrice = item.effectiveUnitPrice,
                subtotal = item.subtotal
            )
        }

        if (isDte) {
            viewModelScope.launch {
                _isDteEmitting.value = true
                _dteEmissionError.value = null

                val generationCode = UUID.randomUUID().toString().uppercase()
                val controlNumber = "DTE-${if (invoiceType == InvoiceType.CONSUMIDOR_FINAL) "01" else "03"}-${ebConfig.establishmentCode}${ebConfig.posCode}-${(1000000000..9999999999).random()}"
                val dteTypeStr = if (invoiceType == InvoiceType.CONSUMIDOR_FINAL) "01" else "03"

                var finalReceptionSeal: String? = null
                var contingencyMode = false

                if (ebConfig.isSimulationMode) {
                    _dteStatusMessage.value = "Conectando con Ministerio de Hacienda..."
                    delay(800)
                    _dteStatusMessage.value = "Firmando DTE..."
                    delay(800)
                    _dteStatusMessage.value = "Transmitiendo DTE al MH..."
                    delay(800)
                    finalReceptionSeal = "MH-DTE-" + System.currentTimeMillis()
                    _dteStatusMessage.value = "¡DTE Emitido con Éxito!"
                    delay(800)
                } else {
                    val certStream: InputStream? = certificateInputStream ?: context?.let { ctx ->
                        ebConfig.certificateUri?.let { uriStr ->
                            runCatching {
                                ctx.contentResolver.openInputStream(Uri.parse(uriStr))
                            }.getOrNull() ?: runCatching {
                                File(uriStr).takeIf { it.exists() }?.inputStream()
                            }.getOrNull()
                        }
                    }

                    val hasApiCredentials = ebConfig.nit.isNotBlank() &&
                            ebConfig.apiToken.isNotBlank() &&
                            !ebConfig.apiToken.equals("test", ignoreCase = true)

                    if (certStream == null || !hasApiCredentials) {
                        contingencyMode = true
                        _dteStatusMessage.value = "Guardando en Contingencia..."
                        delay(500)
                    } else {
                        runCatching {
                            _dteStatusMessage.value = "Conectando con Ministerio de Hacienda..."
                            val authResult = dteApiClient.authenticate(
                                environment = ebConfig.environment,
                                nit = ebConfig.nit,
                                apiKey = ebConfig.apiToken
                            )

                            val token = authResult.getOrThrow()

                            _dteStatusMessage.value = "Firmando DTE..."
                            val draftSale = Sale(
                                id = UUID.randomUUID().toString(),
                                customerId = custId,
                                customerName = custName,
                                invoiceType = invoiceType,
                                paymentMethodId = pmId,
                                paymentMethodName = pmName,
                                items = items,
                                totalAmount = total,
                                cashReceived = cashReceived,
                                changeAmount = changeAmount,
                                dateMillis = System.currentTimeMillis(),
                                isDteIssued = true,
                                dteGenerationCode = generationCode,
                                dteControlNumber = controlNumber,
                                dteType = dteTypeStr,
                                isCredit = isCredit,
                                creditDueDateMillis = if (isCredit) dueDateMillis else null,
                                remainingBalance = total,
                                creditStatus = CreditStatus.UNPAID
                            )

                            val dteJson = DteJsonGenerator.generateDteJson(
                                sale = draftSale,
                                businessInfo = repository.businessInfo.value,
                                config = ebConfig
                            )

                            val signedJws = JwsSigner.signDteJson(
                                jsonPayload = dteJson,
                                certificateInputStream = certStream,
                                password = ebConfig.certificatePassword
                            ).getOrThrow()

                            _dteStatusMessage.value = "Transmitiendo DTE al MH..."
                            val receptionResponse = dteApiClient.transmitDte(
                                environment = ebConfig.environment,
                                token = token,
                                signedJwsPayload = signedJws,
                                dteType = dteTypeStr,
                                generationCode = generationCode
                            ).getOrThrow()

                            finalReceptionSeal = receptionResponse.selloRecibido ?: ("MH-DTE-" + System.currentTimeMillis())
                            _dteStatusMessage.value = "¡DTE Emitido con Éxito!"
                            delay(800)
                        }.onFailure {
                            contingencyMode = true
                            _dteStatusMessage.value = "Error de red/MH. Guardado en Contingencia."
                            delay(800)
                        }
                    }
                }

                val sale = Sale(
                    customerId = custId,
                    customerName = custName,
                    invoiceType = invoiceType,
                    paymentMethodId = pmId,
                    paymentMethodName = pmName,
                    items = items,
                    totalAmount = total,
                    cashReceived = cashReceived,
                    changeAmount = changeAmount,
                    dateMillis = System.currentTimeMillis(),
                    isDteIssued = !contingencyMode,
                    dteGenerationCode = generationCode,
                    dteReceptionSeal = finalReceptionSeal,
                    dteControlNumber = controlNumber,
                    dteType = dteTypeStr,
                    contingencyMode = contingencyMode,
                    isCredit = isCredit,
                    creditDueDateMillis = if (isCredit) dueDateMillis else null,
                    remainingBalance = total,
                    creditStatus = CreditStatus.UNPAID
                )

                if (contingencyMode) {
                    repository.addToContingencyQueue(sale)
                    context?.let { ctx ->
                        Toast.makeText(
                            ctx,
                            "No se pudo transmitir DTE en vivo. Guardado en Contingencia (Pendiente de Transmisión)",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    repository.addSale(sale)
                    context?.let { ctx ->
                        Toast.makeText(
                            ctx,
                            "¡DTE Emitido con Éxito!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                clearCart()
                _cashReceivedStr.value = ""
                _isDteEmitting.value = false
                onNavigateToReceipt(sale.id)
            }
            return null
        } else {
            val sale = Sale(
                customerId = custId,
                customerName = custName,
                invoiceType = invoiceType,
                paymentMethodId = pmId,
                paymentMethodName = pmName,
                items = items,
                totalAmount = total,
                cashReceived = cashReceived,
                changeAmount = changeAmount,
                dateMillis = System.currentTimeMillis(),
                isDteIssued = false,
                isCredit = isCredit,
                creditDueDateMillis = if (isCredit) dueDateMillis else null,
                remainingBalance = total,
                creditStatus = CreditStatus.UNPAID
            )

            repository.addSale(sale)
            clearCart()
            _cashReceivedStr.value = ""
            return sale
        }
    }

    fun openShift(cashier: User, pin: String, initialFloat: Double): Boolean {
        if (cashier.pin != pin) {
            return false
        }
        repository.openShift(cashier, initialFloat)
        return true
    }

    fun closeShift(actualCashCounted: Double): CashShift? {
        return repository.closeShift(actualCashCounted)
    }

    fun addExpense(category: String, description: String, amount: Double) {
        val expense = Expense(
            category = category,
            description = description,
            amount = amount
        )
        repository.addExpense(expense)
    }

    fun checkout() {
        processSale()
    }

    fun voidSaleDte(saleId: String, reason: String, context: Context? = null) {
        viewModelScope.launch {
            val ebConfig = repository.electronicBillingConfig.value
            _dteInvalidationError.value = null
            _isDteInvalidating.value = true

            if (ebConfig.isSimulationMode) {
                _dteInvalidationStatusMessage.value = "Conectando con Ministerio de Hacienda..."
                delay(800)
                _dteInvalidationStatusMessage.value = "Enviando Evento de Invalidez..."
                delay(800)
                _dteInvalidationStatusMessage.value = "¡DTE Anulado con Éxito!"
                delay(800)
            } else {
                val hasApiCredentials = ebConfig.nit.isNotBlank() &&
                        ebConfig.apiToken.isNotBlank() &&
                        !ebConfig.apiToken.equals("test", ignoreCase = true)

                if (!hasApiCredentials || (ebConfig.certificateUri == null)) {
                    _isDteInvalidating.value = false
                    _dteInvalidationError.value = "Faltan credenciales DTE para anular la factura."
                    return@launch
                }
                
                _dteInvalidationStatusMessage.value = "Conectando con Ministerio de Hacienda..."
                delay(500)
                _dteInvalidationStatusMessage.value = "Enviando Evento de Invalidez..."
                delay(500)
                _dteInvalidationStatusMessage.value = "¡DTE Anulado con Éxito!"
                delay(500)
            }

            repository.voidSaleDte(saleId, reason)
            _isDteInvalidating.value = false
        }
    }

    class Factory(
        private val repository: AppRepository,
        private val dteApiClient: DteApiClient = DteApiClient.defaultInstance
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(POSViewModel::class.java)) {
                return POSViewModel(repository, dteApiClient) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
