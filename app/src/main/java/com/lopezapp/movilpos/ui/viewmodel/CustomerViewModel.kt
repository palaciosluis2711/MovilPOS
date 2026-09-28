package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allCustomers: StateFlow<List<Customer>> = repository.customers

    val customers: StateFlow<List<Customer>> = combine(
        repository.customers,
        _searchQuery
    ) { customerList, query ->
        if (query.isBlank()) {
            customerList
        } else {
            customerList.filter { customer ->
                customer.name.contains(query, ignoreCase = true) ||
                        customer.documentNumber.contains(query, ignoreCase = true) ||
                        (customer.phone != null && customer.phone.contains(query, ignoreCase = true)) ||
                        (customer.email != null && customer.email.contains(query, ignoreCase = true))
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getCustomerById(id: String?): Customer? {
        if (id == null) return null
        return repository.customers.value.find { it.id == id }
    }

    fun addCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.addCustomer(customer)
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
        }
    }

    fun setDefaultCustomer(customerId: String) {
        viewModelScope.launch {
            repository.setDefaultCustomer(customerId)
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomerViewModel::class.java)) {
                return CustomerViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
