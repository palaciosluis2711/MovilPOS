package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.InventoryOutflow
import com.lopezapp.movilpos.data.model.OutflowItem
import com.lopezapp.movilpos.data.model.OutflowReason
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryOutflowViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedReasonFilter = MutableStateFlow<OutflowReason?>(null)
    val selectedReasonFilter: StateFlow<OutflowReason?> = _selectedReasonFilter.asStateFlow()

    val allProducts: StateFlow<List<Product>> = repository.products

    val outflows: StateFlow<List<InventoryOutflow>> = combine(
        repository.inventoryOutflows,
        _searchQuery,
        _selectedReasonFilter
    ) { outflowList, query, reasonFilter ->
        outflowList.filter { outflow ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                outflow.notes.contains(query, ignoreCase = true) ||
                        outflow.registeredByUserName.contains(query, ignoreCase = true) ||
                        outflow.id.contains(query, ignoreCase = true) ||
                        outflow.items.any { it.productName.contains(query, ignoreCase = true) }
            }

            val matchesReason = reasonFilter == null || outflow.reason == reasonFilter

            matchesQuery && matchesReason
        }.sortedByDescending { it.dateMillis }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Draft / New Outflow Registration Form State
    private val _draftReason = MutableStateFlow(OutflowReason.CONSUMO_INTERNO)
    val draftReason: StateFlow<OutflowReason> = _draftReason.asStateFlow()

    private val _draftNotes = MutableStateFlow("")
    val draftNotes: StateFlow<String> = _draftNotes.asStateFlow()

    private val _draftItems = MutableStateFlow<List<OutflowItem>>(emptyList())
    val draftItems: StateFlow<List<OutflowItem>> = _draftItems.asStateFlow()

    val totalOutflowCost: StateFlow<Double> = _draftItems.map { items ->
        items.sumOf { it.quantity * it.unitCost }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0.0
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setReasonFilter(reason: OutflowReason?) {
        _selectedReasonFilter.value = reason
    }

    fun setDraftReason(reason: OutflowReason) {
        _draftReason.value = reason
    }

    fun setDraftNotes(notes: String) {
        _draftNotes.value = notes
    }

    fun addDraftItem(product: Product, quantity: Int = 1) {
        if (quantity <= 0) return
        val currentItems = _draftItems.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.productId == product.id }
        if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            val newQty = existing.quantity + quantity
            currentItems[existingIndex] = existing.copy(
                quantity = newQty,
                totalCost = newQty * existing.unitCost
            )
        } else {
            currentItems.add(
                OutflowItem(
                    productId = product.id,
                    productName = product.name,
                    quantity = quantity,
                    unitCost = product.cost,
                    totalCost = quantity * product.cost
                )
            )
        }
        _draftItems.value = currentItems
    }

    fun removeDraftItem(productId: String) {
        _draftItems.value = _draftItems.value.filter { it.productId != productId }
    }

    fun updateDraftItemQuantity(productId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeDraftItem(productId)
            return
        }
        _draftItems.value = _draftItems.value.map { item ->
            if (item.productId == productId) {
                item.copy(
                    quantity = newQuantity,
                    totalCost = newQuantity * item.unitCost
                )
            } else {
                item
            }
        }
    }

    fun registerOutflow(): Boolean {
        val notesText = _draftNotes.value.trim()
        val items = _draftItems.value
        if (notesText.isBlank() || items.isEmpty()) {
            return false
        }

        val currentShift = repository.activeShift.value
        val userId = currentShift?.cashierId.orEmpty()
        val userName = currentShift?.cashierName.orEmpty().ifBlank { "Administrador" }

        val outflow = InventoryOutflow(
            reason = _draftReason.value,
            registeredByUserId = userId,
            registeredByUserName = userName,
            notes = notesText,
            items = items,
            totalCost = items.sumOf { it.quantity * it.unitCost },
            dateMillis = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.addInventoryOutflow(outflow)
        }
        resetForm()
        return true
    }

    fun resetForm() {
        _draftReason.value = OutflowReason.CONSUMO_INTERNO
        _draftNotes.value = ""
        _draftItems.value = emptyList()
    }

    fun getOutflowById(id: String?): InventoryOutflow? {
        if (id == null) return null
        return repository.inventoryOutflows.value.find { it.id == id }
    }

    fun deleteOutflow(outflowId: String) {
        viewModelScope.launch {
            repository.deleteInventoryOutflow(outflowId)
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventoryOutflowViewModel::class.java)) {
                return InventoryOutflowViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
