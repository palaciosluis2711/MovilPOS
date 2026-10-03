package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.InvoiceType
import com.lopezapp.movilpos.data.model.PaymentMethod
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class SalesViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCustomerIdFilter = MutableStateFlow<String?>(null)
    val selectedCustomerIdFilter: StateFlow<String?> = _selectedCustomerIdFilter.asStateFlow()

    private val _selectedInvoiceTypeFilter = MutableStateFlow<InvoiceType?>(null)
    val selectedInvoiceTypeFilter: StateFlow<InvoiceType?> = _selectedInvoiceTypeFilter.asStateFlow()

    private val _selectedPaymentMethodIdFilter = MutableStateFlow<String?>(null)
    val selectedPaymentMethodIdFilter: StateFlow<String?> = _selectedPaymentMethodIdFilter.asStateFlow()

    private val _selectedDateFilterMillis = MutableStateFlow<Long?>(null)
    val selectedDateFilterMillis: StateFlow<Long?> = _selectedDateFilterMillis.asStateFlow()

    val allCustomers: StateFlow<List<Customer>> = repository.customers
    val allPaymentMethods: StateFlow<List<PaymentMethod>> = repository.paymentMethods
    val allSales: StateFlow<List<Sale>> = repository.sales

    val filteredSales: StateFlow<List<Sale>> = combine(
        repository.sales,
        _searchQuery,
        _selectedCustomerIdFilter,
        _selectedInvoiceTypeFilter,
        _selectedPaymentMethodIdFilter,
        _selectedDateFilterMillis
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val salesList = flows[0] as List<Sale>
        val query = flows[1] as String
        val customerId = flows[2] as String?
        val invoiceType = flows[3] as InvoiceType?
        val paymentMethodId = flows[4] as String?
        val dateMillis = flows[5] as Long?

        salesList.filter { sale ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                sale.customerName.contains(query, ignoreCase = true) ||
                        sale.id.contains(query, ignoreCase = true) ||
                        sale.items.any { it.productName.contains(query, ignoreCase = true) }
            }

            val matchesCustomer = customerId == null || sale.customerId == customerId
            val matchesInvoiceType = invoiceType == null || sale.invoiceType == invoiceType
            val matchesPaymentMethod = paymentMethodId == null || sale.paymentMethodId == paymentMethodId
            val matchesDate = dateMillis == null || isSameDay(sale.dateMillis, dateMillis)

            matchesQuery && matchesCustomer && matchesInvoiceType && matchesPaymentMethod && matchesDate
        }.sortedByDescending { it.dateMillis }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCustomerIdFilter(customerId: String?) {
        _selectedCustomerIdFilter.value = customerId
    }

    fun setSelectedInvoiceTypeFilter(invoiceType: InvoiceType?) {
        _selectedInvoiceTypeFilter.value = invoiceType
    }

    fun setSelectedPaymentMethodIdFilter(paymentMethodId: String?) {
        _selectedPaymentMethodIdFilter.value = paymentMethodId
    }

    fun setSelectedDateFilterMillis(dateMillis: Long?) {
        _selectedDateFilterMillis.value = dateMillis
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCustomerIdFilter.value = null
        _selectedInvoiceTypeFilter.value = null
        _selectedPaymentMethodIdFilter.value = null
        _selectedDateFilterMillis.value = null
    }

    fun getSaleById(saleId: String?): Sale? {
        if (saleId == null) return null
        return repository.sales.value.find { it.id == saleId }
    }

    fun deleteSale(saleId: String) {
        viewModelScope.launch {
            repository.deleteSale(saleId)
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
            if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
                return SalesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
