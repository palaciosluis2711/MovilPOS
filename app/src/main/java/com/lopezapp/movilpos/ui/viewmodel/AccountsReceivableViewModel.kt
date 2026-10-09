package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.CustomerPayment
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountsReceivableViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val totalAccountsReceivable: StateFlow<Double> = repository.customers
        .map { customerList ->
            customerList.sumOf { maxOf(0.0, it.currentDebt) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0.0
        )

    val debtorCustomers: StateFlow<List<Customer>> = combine(
        repository.customers,
        _searchQuery
    ) { customerList, query ->
        val debtors = customerList.filter { it.currentDebt > 0.0 }
        if (query.isBlank()) {
            debtors
        } else {
            debtors.filter { customer ->
                customer.name.contains(query, ignoreCase = true) ||
                        customer.documentNumber.contains(query, ignoreCase = true) ||
                        (customer.phone != null && customer.phone.contains(query, ignoreCase = true))
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val sales: StateFlow<List<Sale>> = repository.sales
    val customerPayments: StateFlow<List<CustomerPayment>> = repository.customerPayments

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getCustomerById(customerId: String?): Customer? {
        if (customerId == null) return null
        return repository.customers.value.find { it.id == customerId }
    }

    fun getPendingCreditSalesForCustomer(customerId: String): List<Sale> {
        return repository.sales.value
            .filter { it.customerId == customerId && it.isCredit && !it.isVoided && it.remainingBalance > 0.0 }
            .sortedByDescending { it.dateMillis }
    }

    fun getPaymentHistoryForCustomer(customerId: String): List<CustomerPayment> {
        return repository.customerPayments.value
            .filter { it.customerId == customerId }
            .sortedByDescending { it.dateMillis }
    }

    fun registerPayment(
        customerId: String,
        saleId: String?,
        amount: Double,
        paymentMethodName: String,
        notes: String?
    ) {
        if (amount <= 0.0) return
        val customer = repository.customers.value.find { it.id == customerId } ?: return

        val cleanSaleId = if (saleId.isNullOrBlank()) null else saleId

        val payment = CustomerPayment(
            customerId = customerId,
            customerName = customer.name,
            saleId = cleanSaleId,
            amount = amount,
            paymentMethodName = paymentMethodName,
            notes = notes?.ifBlank { null }
        )

        viewModelScope.launch {
            repository.addCustomerPayment(payment)
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountsReceivableViewModel::class.java)) {
                return AccountsReceivableViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
