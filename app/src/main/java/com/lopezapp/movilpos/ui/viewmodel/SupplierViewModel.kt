package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SupplierViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allSuppliers: StateFlow<List<Supplier>> = repository.suppliers

    val suppliers: StateFlow<List<Supplier>> = combine(
        repository.suppliers,
        _searchQuery
    ) { supplierList, query ->
        if (query.isBlank()) {
            supplierList
        } else {
            supplierList.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getSupplierById(id: String?): Supplier? {
        if (id == null) return null
        return repository.suppliers.value.find { it.id == id }
    }

    fun addSupplier(
        name: String,
        logoUri: String? = null,
        address: String? = null,
        email: String? = null,
        phone: String? = null
    ) {
        viewModelScope.launch {
            val newSupplier = Supplier(
                name = name,
                logoUri = logoUri,
                address = address,
                email = email,
                phone = phone
            )
            repository.addSupplier(newSupplier)
        }
    }

    fun addSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.addSupplier(supplier)
        }
    }

    fun updateSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.updateSupplier(supplier)
        }
    }

    fun deleteSupplier(supplierId: String) {
        viewModelScope.launch {
            repository.deleteSupplier(supplierId)
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SupplierViewModel::class.java)) {
                return SupplierViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
