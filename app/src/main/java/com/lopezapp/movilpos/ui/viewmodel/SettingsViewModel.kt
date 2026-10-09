package com.lopezapp.movilpos.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.BaseVariable
import com.lopezapp.movilpos.data.model.BluetoothPrinterConfig
import com.lopezapp.movilpos.data.model.Brand
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.CashShift
import com.lopezapp.movilpos.data.model.Category
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.PaymentMethod
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Sale
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TaxValueType
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.TicketPaperSize
import com.lopezapp.movilpos.data.model.UnitOfMeasure
import com.lopezapp.movilpos.data.model.User
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.model.AnimationType
import com.lopezapp.movilpos.util.EscPosPrinter
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
    val electronicBillingConfig: ElectronicBillingConfig = ElectronicBillingConfig(),
    val bluetoothPrinterConfig: BluetoothPrinterConfig = BluetoothPrinterConfig(),
    val users: List<User> = emptyList(),
    val shiftHistory: List<CashShift> = emptyList()
)

class SettingsViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {
    val users: StateFlow<List<User>> = repository.users
    val shiftHistory: StateFlow<List<CashShift>> = repository.shiftHistory
    val contingencyDtes: StateFlow<List<Sale>> = repository.contingencyDtes
    val bluetoothPrinterConfig: StateFlow<BluetoothPrinterConfig> = repository.bluetoothPrinterConfig

    fun retryContingencyTransmissions(context: Context? = null): Result<Int> {
        return repository.retryContingencyTransmissions(context = context)
    }

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

    private data class ConfigData(
        val businessInfo: BusinessInfo,
        val ticketConfig: TicketConfig,
        val electronicBillingConfig: ElectronicBillingConfig,
        val bluetoothPrinterConfig: BluetoothPrinterConfig
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
        combine(repository.businessInfo, repository.ticketConfig, repository.electronicBillingConfig, repository.bluetoothPrinterConfig) { businessInfo, ticketConfig, electronicBillingConfig, bluetoothPrinterConfig ->
            ConfigData(businessInfo, ticketConfig, electronicBillingConfig, bluetoothPrinterConfig)
        },
        combine(repository.users, repository.shiftHistory) { users, shiftHistory ->
            Pair(users, shiftHistory)
        }
    ) { baseState, catalog, extra, config, userShiftData ->
        baseState.copy(
            categories = catalog.categories,
            brands = catalog.brands,
            unitsOfMeasure = catalog.unitsOfMeasure,
            taxes = catalog.taxes,
            customers = extra.first,
            priceRules = extra.second,
            paymentMethods = extra.third,
            businessInfo = config.businessInfo,
            ticketConfig = config.ticketConfig,
            electronicBillingConfig = config.electronicBillingConfig,
            bluetoothPrinterConfig = config.bluetoothPrinterConfig,
            users = userShiftData.first,
            shiftHistory = userShiftData.second
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
            electronicBillingConfig = repository.electronicBillingConfig.value,
            bluetoothPrinterConfig = repository.bluetoothPrinterConfig.value,
            users = repository.users.value,
            shiftHistory = repository.shiftHistory.value
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

    fun updateBluetoothPrinterConfig(config: BluetoothPrinterConfig) {
        viewModelScope.launch {
            repository.updateBluetoothPrinterConfig(config)
        }
    }

    fun sendTestPrint(macAddress: String, paperSize: TicketPaperSize): Result<Unit> {
        val businessInfo = repository.businessInfo.value
        val bytes = EscPosPrinter.formatTestTicket(paperSize, businessInfo)
        return EscPosPrinter.printBytesViaBluetooth(macAddress, bytes)
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            repository.addUser(user)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
        }
    }

    fun validateAdminPin(pin: String): Boolean {
        return repository.validateAdminPin(pin)
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
