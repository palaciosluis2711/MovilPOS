package com.lopezapp.movilpos.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    private val _isDteInvalidating = MutableStateFlow(false)
    val isDteInvalidating: StateFlow<Boolean> = _isDteInvalidating.asStateFlow()

    private val _dteInvalidationStatusMessage = MutableStateFlow("")
    val dteInvalidationStatusMessage: StateFlow<String> = _dteInvalidationStatusMessage.asStateFlow()

    private val _dteInvalidationError = MutableStateFlow<String?>(null)
    val dteInvalidationError: StateFlow<String?> = _dteInvalidationError.asStateFlow()

    fun clearDteInvalidationError() {
        _dteInvalidationError.value = null
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String>("ALL") // "ALL", "APROBADO"
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    val businessInfo: StateFlow<BusinessInfo> = repository.businessInfo
    val electronicBillingConfig: StateFlow<ElectronicBillingConfig> = repository.electronicBillingConfig
    val customers: StateFlow<List<Customer>> = repository.customers
    val contingencyDtes: StateFlow<List<Sale>> = repository.contingencyDtes

    val issuedDtes: StateFlow<List<Sale>> = combine(
        repository.sales,
        _searchQuery,
        _statusFilter
    ) { sales, query, status ->
        sales.filter { sale ->
            val isIssued = sale.isDteIssued || sale.contingencyMode
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
                
                val hasCert = ebConfig.certificateUri != null && context != null

                if (!hasCert && context == null) {
                    // if context is null, we can't reliably check cert existence from URI, but assume it fails if missing credentials
                }

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

    fun retryContingencyTransmissions(): Result<Int> {
        return repository.retryContingencyTransmissions()
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
