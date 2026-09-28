package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.Brand
import com.lopezapp.movilpos.data.model.Category
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TaxValueType
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
    val taxes: List<Tax> = emptyList()
)

class SettingsViewModel(
    private val repository: AppRepository = AppRepository()
) : ViewModel() {
    private val _animationDurationMs = MutableStateFlow(400)
    private val _animationType = MutableStateFlow(AnimationType.SLIDE_AND_FADE)
    private val _currencySymbol = MutableStateFlow("$")
    private val _defaultDecimalPlaces = MutableStateFlow(2)
    private val _allowExtraDecimals = MutableStateFlow(true)

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
        repository.categories,
        repository.brands,
        repository.unitsOfMeasure,
        repository.taxes
    ) { baseState, categories, brands, unitsOfMeasure, taxes ->
        baseState.copy(
            categories = categories,
            brands = brands,
            unitsOfMeasure = unitsOfMeasure,
            taxes = taxes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(
            categories = repository.categories.value,
            brands = repository.brands.value,
            unitsOfMeasure = repository.unitsOfMeasure.value,
            taxes = repository.taxes.value
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
