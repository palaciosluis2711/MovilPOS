package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.Brand
import com.lopezapp.movilpos.data.model.BundleItem
import com.lopezapp.movilpos.data.model.Category
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.UnitOfMeasure
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProductTypeFilter {
    ALL,
    NORMAL,
    BUNDLE,
    SERVICE
}

private data class FilterParams(
    val query: String,
    val categoryFilter: String?,
    val brandFilter: String?,
    val typeFilter: ProductTypeFilter?,
    val lowStockFilter: Boolean
)

class InventoryViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter

    private val _selectedBrandFilter = MutableStateFlow<String?>(null)
    val selectedBrandFilter: StateFlow<String?> = _selectedBrandFilter

    private val _selectedTypeFilter = MutableStateFlow<ProductTypeFilter?>(null)
    val selectedTypeFilter: StateFlow<ProductTypeFilter?> = _selectedTypeFilter

    private val _lowStockOnlyFilter = MutableStateFlow<Boolean>(false)
    val lowStockOnlyFilter: StateFlow<Boolean> = _lowStockOnlyFilter

    val categories: StateFlow<List<Category>> = repository.categories
    val brands: StateFlow<List<Brand>> = repository.brands
    val unitsOfMeasure: StateFlow<List<UnitOfMeasure>> = repository.unitsOfMeasure
    val taxes: StateFlow<List<Tax>> = repository.taxes
    val allProducts: StateFlow<List<Product>> = repository.products

    private val _filterParams = combine(
        _searchQuery,
        _selectedCategoryFilter,
        _selectedBrandFilter,
        _selectedTypeFilter,
        _lowStockOnlyFilter
    ) { query, categoryFilter, brandFilter, typeFilter, lowStockFilter ->
        FilterParams(query, categoryFilter, brandFilter, typeFilter, lowStockFilter)
    }

    val filteredProducts: StateFlow<List<Product>> = combine(
        repository.products,
        _filterParams
    ) { products, params ->
        products.filter { product ->
            val matchesQuery = if (params.query.isBlank()) {
                true
            } else {
                product.name.contains(params.query, ignoreCase = true) ||
                        product.barcode?.contains(params.query, ignoreCase = true) == true
            }

            val matchesCategory = if (params.categoryFilter == null) {
                true
            } else {
                product.category.equals(params.categoryFilter, ignoreCase = true)
            }

            val matchesBrand = if (params.brandFilter == null) {
                true
            } else {
                product.brand.equals(params.brandFilter, ignoreCase = true)
            }

            val matchesType = when (params.typeFilter) {
                null, ProductTypeFilter.ALL -> true
                ProductTypeFilter.NORMAL -> !product.isBundle && !product.isService
                ProductTypeFilter.BUNDLE -> product.isBundle
                ProductTypeFilter.SERVICE -> product.isService
            }

            val matchesLowStock = if (params.lowStockFilter) {
                !product.isService && product.stock <= product.alertQuantity
            } else {
                true
            }

            matchesQuery && matchesCategory && matchesBrand && matchesType && matchesLowStock
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val inventoryState: StateFlow<List<Product>> = filteredProducts

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setSelectedBrandFilter(brand: String?) {
        _selectedBrandFilter.value = brand
    }

    fun setSelectedTypeFilter(type: ProductTypeFilter?) {
        _selectedTypeFilter.value = type
    }

    fun setLowStockOnlyFilter(lowStockOnly: Boolean) {
        _lowStockOnlyFilter.value = lowStockOnly
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategoryFilter.value = null
        _selectedBrandFilter.value = null
        _selectedTypeFilter.value = null
        _lowStockOnlyFilter.value = false
    }

    fun addProduct(
        name: String, 
        barcode: String? = null, 
        category: String = "General",
        brand: String = "",
        unitOfMeasure: String = "unidad",
        cost: Double = 0.0,
        price: Double, 
        alertQuantity: Int = 0,
        stock: Int = 0,
        imageUri: String? = null,
        appliedTaxIds: List<String> = emptyList(),
        isTaxIncludedInPrice: Boolean = false,
        isBundle: Boolean = false,
        bundleItems: List<BundleItem> = emptyList(),
        isService: Boolean = false
    ) {
        viewModelScope.launch {
            val newProduct = Product(
                name = name,
                barcode = barcode,
                category = category,
                brand = brand,
                unitOfMeasure = unitOfMeasure,
                cost = cost,
                price = price,
                alertQuantity = alertQuantity,
                stock = stock,
                imageUri = imageUri,
                appliedTaxIds = appliedTaxIds,
                isTaxIncludedInPrice = isTaxIncludedInPrice,
                isBundle = isBundle,
                bundleItems = bundleItems,
                isService = isService
            )
            repository.addProduct(newProduct)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun removeProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
                return InventoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
