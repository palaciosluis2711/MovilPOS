package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Purchase
import com.lopezapp.movilpos.data.model.PurchaseItem
import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allPurchases: StateFlow<List<Purchase>> = repository.purchases
    val allSuppliers: StateFlow<List<Supplier>> = repository.suppliers
    val allProducts: StateFlow<List<Product>> = repository.products

    val purchases: StateFlow<List<Purchase>> = combine(
        repository.purchases,
        _searchQuery
    ) { purchaseList, query ->
        if (query.isBlank()) {
            purchaseList
        } else {
            purchaseList.filter { purchase ->
                purchase.supplierName.contains(query, ignoreCase = true) ||
                        purchase.items.any { it.productName.contains(query, ignoreCase = true) }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Draft / Edit Form State
    private val _selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis.asStateFlow()

    private val _selectedSupplier = MutableStateFlow<Supplier?>(null)
    val selectedSupplier: StateFlow<Supplier?> = _selectedSupplier.asStateFlow()

    private val _draftItems = MutableStateFlow<List<PurchaseItem>>(emptyList())
    val draftItems: StateFlow<List<PurchaseItem>> = _draftItems.asStateFlow()

    val totalCost: StateFlow<Double> = _draftItems.map { items ->
        items.sumOf { it.quantity * it.unitCost }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0.0
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setDateMillis(millis: Long) {
        _selectedDateMillis.value = millis
    }

    fun setSupplier(supplier: Supplier?) {
        _selectedSupplier.value = supplier
    }

    fun addProductToDraft(product: Product) {
        val currentItems = _draftItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == product.id }
        if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            currentItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentItems.add(
                PurchaseItem(
                    productId = product.id,
                    productName = product.name,
                    quantity = 1,
                    unitCost = product.cost
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

    fun updateDraftItemUnitCost(productId: String, unitCost: Double) {
        val safeCost = if (unitCost < 0.0) 0.0 else unitCost
        _draftItems.value = _draftItems.value.map { item ->
            if (item.productId == productId) {
                item.copy(unitCost = safeCost)
            } else {
                item
            }
        }
    }

    fun removeDraftItem(productId: String) {
        _draftItems.value = _draftItems.value.filter { it.productId != productId }
    }

    fun resetForm() {
        _selectedDateMillis.value = System.currentTimeMillis()
        _selectedSupplier.value = null
        _draftItems.value = emptyList()
    }

    fun getPurchaseById(id: String?): Purchase? {
        if (id == null) return null
        return repository.purchases.value.find { it.id == id }
    }

    fun getSupplierById(id: String?): Supplier? {
        if (id == null) return null
        return repository.suppliers.value.find { it.id == id }
    }

    fun savePurchase(): Boolean {
        val supplier = _selectedSupplier.value ?: return false
        val items = _draftItems.value
        if (items.isEmpty()) return false

        val calculatedTotal = items.sumOf { it.quantity * it.unitCost }
        val purchase = Purchase(
            supplierId = supplier.id,
            supplierName = supplier.name,
            dateMillis = _selectedDateMillis.value,
            items = items,
            totalCost = calculatedTotal
        )

        viewModelScope.launch {
            repository.addPurchase(purchase)
        }
        resetForm()
        return true
    }

    fun deletePurchase(purchaseId: String) {
        viewModelScope.launch {
            repository.deletePurchase(purchaseId)
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PurchaseViewModel::class.java)) {
                return PurchaseViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
