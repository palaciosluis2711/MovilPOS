package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DteReportsViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String>("ALL") // "ALL", "APROBADO"
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    val businessInfo: StateFlow<BusinessInfo> = repository.businessInfo
    val electronicBillingConfig: StateFlow<ElectronicBillingConfig> = repository.electronicBillingConfig
    val customers: StateFlow<List<Customer>> = repository.customers

    val issuedDtes: StateFlow<List<Sale>> = combine(
        repository.sales,
        _searchQuery,
        _statusFilter
    ) { sales, query, status ->
        sales.filter { sale ->
            val isIssued = sale.isDteIssued
            if (!isIssued) return@filter false

            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                sale.customerName.contains(query, ignoreCase = true) ||
                        (sale.dteGenerationCode?.contains(query, ignoreCase = true) == true) ||
                        (sale.dteControlNumber?.contains(query, ignoreCase = true) == true) ||
                        sale.id.contains(query, ignoreCase = true)
            }

            val matchesStatus = when (status) {
                "APROBADO" -> sale.dteReceptionSeal != null
                else -> true
            }

            matchesQuery && matchesStatus
        }.sortedByDescending { it.dateMillis }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterChanged(status: String) {
        _statusFilter.value = status
    }

    fun getSaleById(saleId: String): Sale? {
        return repository.sales.value.find { it.id == saleId }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DteReportsViewModel::class.java)) {
                return DteReportsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
