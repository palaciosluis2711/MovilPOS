package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.ProductReturn
import com.lopezapp.movilpos.data.model.ReturnItem
import com.lopezapp.movilpos.data.model.ReturnReason
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.toSpanishLabel
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductReturnViewModel(
    private val repository: AppRepository,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedReasonFilter = MutableStateFlow<ReturnReason?>(null)
    val selectedReasonFilter: StateFlow<ReturnReason?> = _selectedReasonFilter.asStateFlow()

    val allSales: StateFlow<List<Sale>> = repository.sales

    val productReturns: StateFlow<List<ProductReturn>> = combine(
        repository.productReturns,
        _searchQuery,
        _selectedReasonFilter
    ) { returnsList, query, reasonFilter ->
        returnsList.filter { productReturn ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                productReturn.customerName.contains(query, ignoreCase = true) ||
                        productReturn.originalSaleId.contains(query, ignoreCase = true) ||
                        productReturn.id.contains(query, ignoreCase = true) ||
                        productReturn.notes.contains(query, ignoreCase = true) ||
                        productReturn.items.any { it.productName.contains(query, ignoreCase = true) }
            }

            val matchesReason = reasonFilter == null || productReturn.reason == reasonFilter

            matchesQuery && matchesReason
        }.sortedByDescending { it.dateMillis }
    }
    .flowOn(defaultDispatcher)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Draft / New Return Wizard State
    private val _salesSearchQuery = MutableStateFlow("")
    val salesSearchQuery: StateFlow<String> = _salesSearchQuery.asStateFlow()

    private val _selectedSale = MutableStateFlow<Sale?>(null)
    val selectedSale: StateFlow<Sale?> = _selectedSale.asStateFlow()

    // Map of productId to return quantity
    private val _returnQuantities = MutableStateFlow<Map<String, Int>>(emptyMap())
    val returnQuantities: StateFlow<Map<String, Int>> = _returnQuantities.asStateFlow()

    private val _draftReason = MutableStateFlow(ReturnReason.DEFECTIVE)
    val draftReason: StateFlow<ReturnReason> = _draftReason.asStateFlow()

    private val _draftRefundMethod = MutableStateFlow("Efectivo")
    val draftRefundMethod: StateFlow<String> = _draftRefundMethod.asStateFlow()

    private val _draftNotes = MutableStateFlow("")
    val draftNotes: StateFlow<String> = _draftNotes.asStateFlow()

    val totalRefundAmount: StateFlow<Double> = combine(
        _selectedSale,
        _returnQuantities
    ) { sale, quantities ->
        if (sale == null) return@combine 0.0
        sale.items.sumOf { item ->
            val qty = quantities[item.productId] ?: 0
            qty * item.unitPrice
        }
    }
    .flowOn(defaultDispatcher)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setReasonFilter(reason: ReturnReason?) {
        _selectedReasonFilter.value = reason
    }

    fun onSalesSearchQueryChanged(query: String) {
        _salesSearchQuery.value = query
    }

    fun selectSale(sale: Sale?) {
        _selectedSale.value = sale
        // Reset quantities map with 0 for each item in the new sale
        if (sale != null) {
            val initialMap = sale.items.associate { it.productId to 0 }
            _returnQuantities.value = initialMap
        } else {
            _returnQuantities.value = emptyMap()
        }
    }

    fun updateReturnItemQuantity(productId: String, maxQuantity: Int, newQuantity: Int) {
        val clampedQty = newQuantity.coerceIn(0, maxQuantity)
        val currentMap = _returnQuantities.value.toMutableMap()
        currentMap[productId] = clampedQty
        _returnQuantities.value = currentMap
    }

    fun setDraftReason(reason: ReturnReason) {
        _draftReason.value = reason
    }

    fun setDraftRefundMethod(method: String) {
        _draftRefundMethod.value = method
    }

    fun setDraftNotes(notes: String) {
        _draftNotes.value = notes
    }

    fun processReturn(): Boolean {
        val sale = _selectedSale.value ?: return false
        val quantities = _returnQuantities.value
        val itemsToReturn = sale.items.mapNotNull { item ->
            val qty = quantities[item.productId] ?: 0
            if (qty > 0) {
                ReturnItem(
                    productId = item.productId,
                    productName = item.productName,
                    quantity = qty,
                    unitPrice = item.unitPrice,
                    refundSubtotal = qty * item.unitPrice
                )
            } else {
                null
            }
        }

        if (itemsToReturn.isEmpty()) return false

        val totalRefund = itemsToReturn.sumOf { it.refundSubtotal }
        val currentShift = repository.activeShift.value
        val cashierId = currentShift?.cashierId.orEmpty()
        val cashierName = currentShift?.cashierName.orEmpty().ifBlank { "Administrador" }

        val productReturn = ProductReturn(
            originalSaleId = sale.id,
            customerId = sale.customerId,
            customerName = sale.customerName,
            reason = _draftReason.value,
            notes = _draftNotes.value.trim(),
            items = itemsToReturn,
            totalRefundAmount = totalRefund,
            refundMethod = _draftRefundMethod.value,
            cashierId = cashierId,
            cashierName = cashierName,
            dateMillis = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.processProductReturn(productReturn)
        }
        resetForm()
        return true
    }

    fun resetForm() {
        _selectedSale.value = null
        _returnQuantities.value = emptyMap()
        _draftReason.value = ReturnReason.DEFECTIVE
        _draftRefundMethod.value = "Efectivo"
        _draftNotes.value = ""
        _salesSearchQuery.value = ""
    }

    fun getReturnById(id: String?): ProductReturn? {
        if (id == null) return null
        return repository.productReturns.value.find { it.id == id }
    }

    fun deleteReturn(returnId: String) {
        viewModelScope.launch {
            repository.deleteProductReturn(returnId)
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductReturnViewModel::class.java)) {
                return ProductReturnViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
