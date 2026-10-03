package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Quotation
import com.lopezapp.movilpos.data.model.QuotationItem
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TaxValueType
import com.lopezapp.movilpos.data.model.calculatePriceWithoutTax
import com.lopezapp.movilpos.data.model.getFormattedTaxLabel
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class QuotationViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCustomerIdFilter = MutableStateFlow<String?>(null)
    val selectedCustomerIdFilter: StateFlow<String?> = _selectedCustomerIdFilter.asStateFlow()

    private val _selectedDateFilterMillis = MutableStateFlow<Long?>(null)
    val selectedDateFilterMillis: StateFlow<Long?> = _selectedDateFilterMillis.asStateFlow()

    val allQuotations: StateFlow<List<Quotation>> = repository.quotations
    val allCustomers: StateFlow<List<Customer>> = repository.customers
    val allProducts: StateFlow<List<Product>> = repository.products
    val allPriceRules: StateFlow<List<PriceRule>> = repository.priceRules
    val businessInfo: StateFlow<BusinessInfo> = repository.businessInfo

    val filteredQuotations: StateFlow<List<Quotation>> = combine(
        repository.quotations,
        _searchQuery,
        _selectedCustomerIdFilter,
        _selectedDateFilterMillis
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val quotationList = flows[0] as List<Quotation>
        val query = flows[1] as String
        val customerId = flows[2] as String?
        val dateMillis = flows[3] as Long?

        quotationList.filter { quotation ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                quotation.customerName.contains(query, ignoreCase = true) ||
                        (quotation.customerEmail?.contains(query, ignoreCase = true) == true) ||
                        quotation.id.contains(query, ignoreCase = true) ||
                        quotation.items.any { it.productName.contains(query, ignoreCase = true) }
            }

            val matchesCustomer = customerId == null || quotation.customerName.contains(customerId, ignoreCase = true)
            val matchesDate = dateMillis == null || isSameDay(quotation.dateMillis, dateMillis)

            matchesQuery && matchesCustomer && matchesDate
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Form / Draft State
    private val _editingQuotationId = MutableStateFlow<String?>(null)
    val editingQuotationId: StateFlow<String?> = _editingQuotationId.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _customerNameInput = MutableStateFlow("")
    val customerNameInput: StateFlow<String> = _customerNameInput.asStateFlow()

    private val _customerEmailInput = MutableStateFlow("")
    val customerEmailInput: StateFlow<String> = _customerEmailInput.asStateFlow()

    private val _dateMillis = MutableStateFlow(System.currentTimeMillis())
    val dateMillis: StateFlow<Long> = _dateMillis.asStateFlow()

    // Default expiration date is +15 days from now
    private val _expirationDateMillis = MutableStateFlow(
        System.currentTimeMillis() + (15L * 24 * 3600 * 1000)
    )
    val expirationDateMillis: StateFlow<Long> = _expirationDateMillis.asStateFlow()

    private val _draftItems = MutableStateFlow<List<QuotationItem>>(emptyList())
    val draftItems: StateFlow<List<QuotationItem>> = _draftItems.asStateFlow()

    val taxes: StateFlow<List<Tax>> = repository.taxes

    val activeTaxLabel: StateFlow<String> = repository.taxes.map { taxes ->
        taxes.getFormattedTaxLabel()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = repository.taxes.value.getFormattedTaxLabel()
    )

    private val _showTaxBreakdown = MutableStateFlow(false)
    val showTaxBreakdown: StateFlow<Boolean> = _showTaxBreakdown.asStateFlow()

    private val _showSignatureBlock = MutableStateFlow(false)
    val showSignatureBlock: StateFlow<Boolean> = _showSignatureBlock.asStateFlow()

    private val _showStampBlock = MutableStateFlow(false)
    val showStampBlock: StateFlow<Boolean> = _showStampBlock.asStateFlow()

    private val _showContactBlock = MutableStateFlow(false)
    val showContactBlock: StateFlow<Boolean> = _showContactBlock.asStateFlow()

    private val _selectedQuotationItemIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedQuotationItemIds: StateFlow<Set<String>> = _selectedQuotationItemIds.asStateFlow()

    private val _selectedPriceRuleId = MutableStateFlow<String?>(null)
    val selectedPriceRuleId: StateFlow<String?> = _selectedPriceRuleId.asStateFlow()

    fun setShowTaxBreakdown(show: Boolean) {
        _showTaxBreakdown.value = show
    }

    fun setShowSignatureBlock(show: Boolean) {
        _showSignatureBlock.value = show
    }

    fun setShowStampBlock(show: Boolean) {
        _showStampBlock.value = show
    }

    fun setShowContactBlock(show: Boolean) {
        _showContactBlock.value = show
    }

    val subtotalAmount: StateFlow<Double> = _draftItems.map { items ->
        items.sumOf { it.quantity * it.unitPrice }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0.0
    )

    val calculatedTaxAmount: StateFlow<Double> = combine(
        _draftItems,
        _showTaxBreakdown,
        repository.products,
        repository.taxes
    ) { items, showTax, products, taxes ->
        if (!showTax || items.isEmpty()) {
            0.0
        } else {
            calculateTaxAmountInternal(items, products, taxes)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0.0
    )

    val totalAmount: StateFlow<Double> = _draftItems.map { items ->
        if (items.isEmpty()) 0.0 else items.sumOf { it.quantity * it.unitPrice }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0.0
    )

    private fun calculateTaxAmountInternal(
        items: List<QuotationItem>,
        products: List<Product>,
        taxes: List<Tax>
    ): Double {
        if (taxes.isEmpty() || items.isEmpty()) return 0.0
        val productMap = products.associateBy { it.id }
        
        var totalWithoutTax = 0.0
        var totalWithTax = 0.0

        for (item in items) {
            val product = productMap[item.productId]
            val itemTotal = item.quantity * item.unitPrice
            totalWithTax += itemTotal
            
            val applicableTaxes = if (product != null && product.appliedTaxIds.isNotEmpty()) {
                taxes.filter { tax -> product.appliedTaxIds.contains(tax.id) }
            } else {
                taxes
            }
            
            val itemTotalWithoutTax = calculatePriceWithoutTax(itemTotal, applicableTaxes)
            totalWithoutTax += itemTotalWithoutTax
        }
        
        return totalWithTax - totalWithoutTax
    }



    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCustomerIdFilter(customerId: String?) {
        _selectedCustomerIdFilter.value = customerId
    }

    fun setSelectedDateFilterMillis(dateMillis: Long?) {
        _selectedDateFilterMillis.value = dateMillis
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCustomerIdFilter.value = null
        _selectedDateFilterMillis.value = null
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
        if (customer != null) {
            _customerNameInput.value = customer.name
            _customerEmailInput.value = customer.email ?: ""
        }
    }

    fun handleCustomerFallback() {
        val customers = allCustomers.value
        val defaultCustomer = customers.find { it.isDefault } ?: customers.firstOrNull()
        val query = _customerNameInput.value.trim()
        val matchingCustomer = if (query.isNotEmpty()) {
            customers.find { customer ->
                customer.name.equals(query, ignoreCase = true) ||
                        (customer.email?.equals(query, ignoreCase = true) == true)
            }
        } else null

        if (query.isEmpty() || matchingCustomer == null) {
            if (defaultCustomer != null) {
                selectCustomer(defaultCustomer)
            }
        } else {
            selectCustomer(matchingCustomer)
        }
    }

    fun setCustomerName(name: String) {
        _customerNameInput.value = name
        if (_selectedCustomer.value?.name != name) {
            _selectedCustomer.value = null
        }
    }

    fun setCustomerEmail(email: String) {
        _customerEmailInput.value = email
    }

    fun setDateMillis(millis: Long) {
        _dateMillis.value = millis
    }

    fun setExpirationDateMillis(millis: Long) {
        _expirationDateMillis.value = millis
    }

    fun setExpirationDaysFromToday(days: Int) {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, days)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        _expirationDateMillis.value = calendar.timeInMillis
    }

    fun addProductToDraft(product: Product) {
        val currentItems = _draftItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == product.id }
        if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            currentItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentItems.add(
                QuotationItem(
                    productId = product.id,
                    productName = product.name,
                    quantity = 1,
                    unitPrice = product.price,
                    isDiscounted = false,
                    appliedRuleName = null
                )
            )
        }
        _draftItems.value = currentItems
    }

    fun updateDraftItemQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeDraftItem(productId)
            return
        }
        _draftItems.value = _draftItems.value.map { item ->
            if (item.productId == productId) {
                item.copy(quantity = quantity)
            } else {
                item
            }
        }
    }

    fun updateDraftItemUnitPrice(productId: String, unitPrice: Double) {
        val safePrice = if (unitPrice < 0.0) 0.0 else unitPrice
        _draftItems.value = _draftItems.value.map { item ->
            if (item.productId == productId) {
                item.copy(
                    unitPrice = safePrice,
                    isDiscounted = false,
                    appliedRuleName = null
                )
            } else {
                item
            }
        }
    }

    fun toggleSelectQuotationItem(productId: String) {
        _selectedQuotationItemIds.update { set ->
            if (set.contains(productId)) set - productId else set + productId
        }
    }

    fun selectAllQuotationItems() {
        _selectedQuotationItemIds.value = _draftItems.value.map { it.productId }.toSet()
    }

    fun clearQuotationItemSelection() {
        _selectedQuotationItemIds.value = emptySet()
    }

    fun selectPriceRule(ruleId: String?) {
        if (ruleId == null) {
            val currentRuleId = _selectedPriceRuleId.value
            if (currentRuleId != null) {
                val rule = repository.priceRules.value.find { it.id == currentRuleId }
                if (rule != null) {
                    togglePriceRule(rule)
                    return
                }
            }
            val selectedIds = _selectedQuotationItemIds.value
            val productMap = repository.products.value.associateBy { it.id }
            _draftItems.update { items ->
                items.map { item ->
                    val isTarget = selectedIds.isEmpty() || item.productId in selectedIds
                    if (isTarget && item.isDiscounted) {
                        val product = productMap[item.productId]
                        item.copy(
                            unitPrice = product?.price ?: item.unitPrice,
                            isDiscounted = false,
                            appliedRuleName = null
                        )
                    } else {
                        item
                    }
                }
            }
            _selectedPriceRuleId.value = null
        } else {
            val rule = repository.priceRules.value.find { it.id == ruleId }
            if (rule != null) {
                togglePriceRule(rule)
            } else {
                _selectedPriceRuleId.value = ruleId
            }
        }
    }

    fun togglePriceRule(rule: PriceRule) {
        val currentItems = _draftItems.value
        if (currentItems.isEmpty()) return

        val selectedIds = _selectedQuotationItemIds.value
        val isSelectionNotEmpty = selectedIds.isNotEmpty()

        val targetItems = if (isSelectionNotEmpty) {
            currentItems.filter { it.productId in selectedIds }
        } else {
            currentItems
        }

        if (targetItems.isEmpty()) return

        val allTargetItemsHaveRule = targetItems.all { it.appliedRuleName == rule.name }
        val productMap = repository.products.value.associateBy { it.id }

        _draftItems.update { items ->
            items.map { item ->
                val isTarget = if (isSelectionNotEmpty) item.productId in selectedIds else true
                if (!isTarget) {
                    item
                } else if (allTargetItemsHaveRule) {
                    val product = productMap[item.productId]
                    item.copy(
                        unitPrice = product?.price ?: item.unitPrice,
                        isDiscounted = false,
                        appliedRuleName = null
                    )
                } else {
                    val product = productMap[item.productId]
                    if (product != null) {
                        val calculatedPrice = rule.calculatePrice(basePrice = product.price, cost = product.cost)
                        val safePrice = if (calculatedPrice < 0.0) 0.0 else calculatedPrice
                        item.copy(
                            unitPrice = safePrice,
                            isDiscounted = true,
                            appliedRuleName = rule.name
                        )
                    } else {
                        item
                    }
                }
            }
        }

        if (allTargetItemsHaveRule) {
            _selectedPriceRuleId.value = null
        } else {
            _selectedPriceRuleId.value = rule.id
        }
    }

    fun applyPriceRuleToDraftItem(productId: String, rule: PriceRule) {
        val product = repository.products.value.find { it.id == productId } ?: return
        val calculatedPrice = rule.calculatePrice(basePrice = product.price, cost = product.cost)
        val safePrice = if (calculatedPrice < 0.0) 0.0 else calculatedPrice

        _draftItems.value = _draftItems.value.map { item ->
            if (item.productId == productId) {
                item.copy(
                    unitPrice = safePrice,
                    isDiscounted = true,
                    appliedRuleName = rule.name
                )
            } else {
                item
            }
        }
    }

    fun applyPriceRuleToAllDraftItems(rule: PriceRule) {
        val productMap = repository.products.value.associateBy { it.id }
        _draftItems.value = _draftItems.value.map { item ->
            val product = productMap[item.productId]
            if (product != null) {
                val calculatedPrice = rule.calculatePrice(basePrice = product.price, cost = product.cost)
                val safePrice = if (calculatedPrice < 0.0) 0.0 else calculatedPrice
                item.copy(
                    unitPrice = safePrice,
                    isDiscounted = true,
                    appliedRuleName = rule.name
                )
            } else {
                item
            }
        }
    }

    fun removePriceRuleFromDraftItem(productId: String) {
        val product = repository.products.value.find { it.id == productId } ?: return
        _draftItems.value = _draftItems.value.map { item ->
            if (item.productId == productId) {
                item.copy(
                    unitPrice = product.price,
                    isDiscounted = false,
                    appliedRuleName = null
                )
            } else {
                item
            }
        }
    }

    fun removeDraftItem(productId: String) {
        _draftItems.value = _draftItems.value.filter { it.productId != productId }
        _selectedQuotationItemIds.update { it - productId }
    }

    fun loadQuotationForEdit(quotation: Quotation) {
        _editingQuotationId.value = quotation.id
        _customerNameInput.value = quotation.customerName
        _customerEmailInput.value = quotation.customerEmail ?: ""
        _selectedCustomer.value = repository.customers.value.find { it.name.equals(quotation.customerName, ignoreCase = true) }
        _dateMillis.value = quotation.dateMillis
        _expirationDateMillis.value = quotation.expirationDateMillis
        _draftItems.value = quotation.items
        _selectedQuotationItemIds.value = emptySet()
        _selectedPriceRuleId.value = null
        _showTaxBreakdown.value = quotation.showTaxBreakdown
        _showSignatureBlock.value = quotation.showSignatureBlock
        _showStampBlock.value = quotation.showStampBlock
        _showContactBlock.value = quotation.showContactBlock
    }

    fun resetForm() {
        _editingQuotationId.value = null
        val defaultCustomer = repository.customers.value.find { it.isDefault } ?: repository.customers.value.firstOrNull()
        _selectedCustomer.value = defaultCustomer
        _customerNameInput.value = defaultCustomer?.name ?: ""
        _customerEmailInput.value = defaultCustomer?.email ?: ""
        _dateMillis.value = System.currentTimeMillis()
        _expirationDateMillis.value = System.currentTimeMillis() + (15L * 24 * 3600 * 1000)
        _draftItems.value = emptyList()
        _selectedQuotationItemIds.value = emptySet()
        _selectedPriceRuleId.value = null
        _showTaxBreakdown.value = false
        _showSignatureBlock.value = false
        _showStampBlock.value = false
        _showContactBlock.value = false
    }

    fun getQuotationById(id: String?): Quotation? {
        if (id == null) return null
        return repository.quotations.value.find { it.id == id }
    }

    fun saveQuotation(): Quotation? {
        val name = _customerNameInput.value.trim()
        if (name.isBlank()) return null
        val items = _draftItems.value
        if (items.isEmpty()) return null

        val email = _customerEmailInput.value.trim().takeIf { it.isNotBlank() }
        val showTax = _showTaxBreakdown.value
        val rawSubtotal = items.sumOf { it.quantity * it.unitPrice }
        val products = repository.products.value
        val activeTaxes = repository.taxes.value

        val calculatedTax = if (showTax) calculateTaxAmountInternal(items, products, activeTaxes) else 0.0
        val finalTotal = rawSubtotal

        val existingId = _editingQuotationId.value
        val quotation = if (existingId != null) {
            Quotation(
                id = existingId,
                customerName = name,
                customerEmail = email,
                dateMillis = _dateMillis.value,
                expirationDateMillis = _expirationDateMillis.value,
                items = items,
                totalAmount = finalTotal,
                showTaxBreakdown = showTax,
                taxAmount = calculatedTax,
                showSignatureBlock = _showSignatureBlock.value,
                showStampBlock = _showStampBlock.value,
                showContactBlock = _showContactBlock.value
            )
        } else {
            Quotation(
                customerName = name,
                customerEmail = email,
                dateMillis = _dateMillis.value,
                expirationDateMillis = _expirationDateMillis.value,
                items = items,
                totalAmount = finalTotal,
                showTaxBreakdown = showTax,
                taxAmount = calculatedTax,
                showSignatureBlock = _showSignatureBlock.value,
                showStampBlock = _showStampBlock.value,
                showContactBlock = _showContactBlock.value
            )
        }

        viewModelScope.launch {
            if (existingId != null) {
                repository.updateQuotation(quotation)
            } else {
                repository.addQuotation(quotation)
            }
        }
        resetForm()
        return quotation
    }

    fun deleteQuotation(quotationId: String) {
        viewModelScope.launch {
            repository.deleteQuotation(quotationId)
        }
    }

    private fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuotationViewModel::class.java)) {
                return QuotationViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
