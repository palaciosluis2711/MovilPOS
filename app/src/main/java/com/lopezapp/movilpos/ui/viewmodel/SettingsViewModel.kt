package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.BaseVariable
import com.lopezapp.movilpos.data.model.Brand
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.Category
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.PaymentMethod
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TaxValueType
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.UnitOfMeasure
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.model.AnimationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val animationDurationMs: Int = 400,
    val animationType: AnimationType = AnimationType.SLIDE_AND_FADE,
    val currencySymbol: String = "$",
    val defaultDecimalPlaces: Int = 2,
    val allowExtraDecimals: Boolean = true,
    val categories: List<Category> = emptyList(),
    val brands: List<Brand> = emptyList(),
    val unitsOfMeasure: List<UnitOfMeasure> = emptyList(),
    val taxes: List<Tax> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val priceRules: List<PriceRule> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val businessInfo: BusinessInfo = BusinessInfo(),
    val ticketConfig: TicketConfig = TicketConfig(),
    val electronicBillingConfig: ElectronicBillingConfig = ElectronicBillingConfig()
)

class SettingsViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {
    private val _animationDurationMs = MutableStateFlow(400)
    private val _animationType = MutableStateFlow(AnimationType.SLIDE_AND_FADE)
    private val _currencySymbol = MutableStateFlow("$")
    private val _defaultDecimalPlaces = MutableStateFlow(2)
    private val _allowExtraDecimals = MutableStateFlow(true)

    private data class CatalogData(
        val categories: List<Category>,
        val brands: List<Brand>,
        val unitsOfMeasure: List<UnitOfMeasure>,
        val taxes: List<Tax>
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(_animationDurationMs, _animationType, _currencySymbol, _defaultDecimalPlaces, _allowExtraDecimals) { duration, type, symbol, decimals, extra ->
            SettingsUiState(
                animationDurationMs = duration,
                animationType = type,
                currencySymbol = symbol,
                defaultDecimalPlaces = decimals,
                allowExtraDecimals = extra
            )
        },
        combine(repository.categories, repository.brands, repository.unitsOfMeasure, repository.taxes) { categories, brands, unitsOfMeasure, taxes ->
            CatalogData(categories, brands, unitsOfMeasure, taxes)
        },
        combine(repository.customers, repository.priceRules, repository.paymentMethods) { customers, priceRules, paymentMethods ->
            Triple(customers, priceRules, paymentMethods)
        },
        combine(repository.businessInfo, repository.ticketConfig, repository.electronicBillingConfig) { businessInfo, ticketConfig, electronicBillingConfig ->
            Triple(businessInfo, ticketConfig, electronicBillingConfig)
        }
    ) { baseState, catalog, extra, billing ->
        baseState.copy(
            categories = catalog.categories,
            brands = catalog.brands,
            unitsOfMeasure = catalog.unitsOfMeasure,
            taxes = catalog.taxes,
            customers = extra.first,
            priceRules = extra.second,
            paymentMethods = extra.third,
            businessInfo = billing.first,
            ticketConfig = billing.second,
            electronicBillingConfig = billing.third
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(
            categories = repository.categories.value,
            brands = repository.brands.value,
            unitsOfMeasure = repository.unitsOfMeasure.value,
            taxes = repository.taxes.value,
            customers = repository.customers.value,
            priceRules = repository.priceRules.value,
            paymentMethods = repository.paymentMethods.value,
            businessInfo = repository.businessInfo.value,
            ticketConfig = repository.ticketConfig.value,
            electronicBillingConfig = repository.electronicBillingConfig.value
        )
    )

    fun updateAnimationDuration(durationMs: Int) {
        _animationDurationMs.value = durationMs.coerceIn(100, 1000)
    }

    fun updateAnimationType(type: AnimationType) {
        _animationType.value = type
    }

    fun updateCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol
    }

    fun updateDefaultDecimalPlaces(places: Int) {
        _defaultDecimalPlaces.value = places.coerceIn(2, 4)
    }

    fun updateAllowExtraDecimals(allow: Boolean) {
        _allowExtraDecimals.value = allow
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            repository.addCategory(category)
        }
    }

    fun addCategory(name: String, description: String? = null) {
        if (name.isBlank()) return
        val category = Category(name = name.trim(), description = description?.trim()?.ifBlank { null })
        addCategory(category)
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun updateCategory(id: String, name: String, description: String? = null) {
        if (name.isBlank()) return
        val category = Category(id = id, name = name.trim(), description = description?.trim()?.ifBlank { null })
        updateCategory(category)
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
        }
    }

    fun addBrand(brand: Brand) {
        viewModelScope.launch {
            repository.addBrand(brand)
        }
    }

    fun addBrand(name: String, description: String? = null, logoUri: String? = null) {
        if (name.isBlank()) return
        val brand = Brand(
            name = name.trim(),
            description = description?.trim()?.ifBlank { null },
            logoUri = logoUri?.trim()?.ifBlank { null }
        )
        addBrand(brand)
    }

    fun updateBrand(brand: Brand) {
        viewModelScope.launch {
            repository.updateBrand(brand)
        }
    }

    fun updateBrand(id: String, name: String, description: String? = null, logoUri: String? = null) {
        if (name.isBlank()) return
        val brand = Brand(
            id = id,
            name = name.trim(),
            description = description?.trim()?.ifBlank { null },
            logoUri = logoUri?.trim()?.ifBlank { null }
        )
        updateBrand(brand)
    }

    fun deleteBrand(brandId: String) {
        viewModelScope.launch {
            repository.deleteBrand(brandId)
        }
    }

    fun addUnitOfMeasure(unitOfMeasure: UnitOfMeasure) {
        viewModelScope.launch {
            repository.addUnitOfMeasure(unitOfMeasure)
        }
    }

    fun addUnitOfMeasure(name: String, abbreviation: String? = null, isPackageOrBox: Boolean = false) {
        if (name.isBlank()) return
        val unit = UnitOfMeasure(
            name = name.trim(),
            abbreviation = abbreviation?.trim()?.ifBlank { null },
            isPackageOrBox = isPackageOrBox
        )
        addUnitOfMeasure(unit)
    }

    fun updateUnitOfMeasure(unitOfMeasure: UnitOfMeasure) {
        viewModelScope.launch {
            repository.updateUnitOfMeasure(unitOfMeasure)
        }
    }

    fun updateUnitOfMeasure(id: String, name: String, abbreviation: String? = null, isPackageOrBox: Boolean = false) {
        if (name.isBlank()) return
        val unit = UnitOfMeasure(
            id = id,
            name = name.trim(),
            abbreviation = abbreviation?.trim()?.ifBlank { null },
            isPackageOrBox = isPackageOrBox
        )
        updateUnitOfMeasure(unit)
    }

    fun deleteUnitOfMeasure(unitId: String) {
        viewModelScope.launch {
            repository.deleteUnitOfMeasure(unitId)
        }
    }

    fun addTax(tax: Tax) {
        viewModelScope.launch {
            repository.addTax(tax)
        }
    }

    fun addTax(name: String, description: String? = null, valueType: TaxValueType, value: Double) {
        if (name.isBlank()) return
        val tax = Tax(
            name = name.trim(),
            description = description?.trim()?.ifBlank { null },
            valueType = valueType,
            value = value
        )
        addTax(tax)
    }

    fun updateTax(tax: Tax) {
        viewModelScope.launch {
            repository.updateTax(tax)
        }
    }

    fun updateTax(id: String, name: String, description: String? = null, valueType: TaxValueType, value: Double) {
        if (name.isBlank()) return
        val tax = Tax(
            id = id,
            name = name.trim(),
            description = description?.trim()?.ifBlank { null },
            valueType = valueType,
            value = value
        )
        updateTax(tax)
    }

    fun deleteTax(taxId: String) {
        viewModelScope.launch {
            repository.deleteTax(taxId)
        }
    }

    fun addPriceRule(priceRule: PriceRule) {
        viewModelScope.launch {
            repository.addPriceRule(priceRule)
        }
    }

    fun updatePriceRule(priceRule: PriceRule) {
        viewModelScope.launch {
            repository.updatePriceRule(priceRule)
        }
    }

    fun deletePriceRule(ruleId: String) {
        viewModelScope.launch {
            repository.deletePriceRule(ruleId)
        }
    }

    fun addPaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            repository.addPaymentMethod(paymentMethod)
        }
    }

    fun addPaymentMethod(name: String, isDefault: Boolean = false) {
        if (name.isBlank()) return
        val paymentMethod = PaymentMethod(name = name.trim(), isDefault = isDefault)
        addPaymentMethod(paymentMethod)
    }

    fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            repository.updatePaymentMethod(paymentMethod)
        }
    }

    fun updatePaymentMethod(id: String, name: String, isDefault: Boolean = false) {
        if (name.isBlank()) return
        val paymentMethod = PaymentMethod(id = id, name = name.trim(), isDefault = isDefault)
        updatePaymentMethod(paymentMethod)
    }

    fun deletePaymentMethod(paymentMethodId: String) {
        viewModelScope.launch {
            repository.deletePaymentMethod(paymentMethodId)
        }
    }

    fun updateBusinessInfo(businessInfo: BusinessInfo) {
        viewModelScope.launch {
            repository.updateBusinessInfo(businessInfo)
        }
    }

    fun updateTicketConfig(config: TicketConfig) {
        viewModelScope.launch {
            repository.updateTicketConfig(config)
        }
    }

    fun updateElectronicBillingConfig(config: ElectronicBillingConfig) {
        viewModelScope.launch {
            repository.updateElectronicBillingConfig(config)
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
