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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProductTypeFilter {
    ALL,
    NORMAL,
    BUNDLE,
    SERVICE
}

enum class ProductSortOption {
    NEWEST,       // 🕒 Más recientes
    NAME_ASC,     // 🔤 A - Z
    NAME_DESC,    // 🔠 Z - A
    OLDEST,       // 📜 Más antiguos
    BEST_SELLERS  // 🔥 Más vendidos
}

private data class FilterParams(
    val query: String,
    val categoryFilter: String?,
    val brandFilter: String?,
    val typeFilter: ProductTypeFilter?,
    val lowStockFilter: Boolean
)

class InventoryViewModel(
    private val repository: AppRepository,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
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

    private val _selectedSortOption = MutableStateFlow<ProductSortOption>(ProductSortOption.NEWEST)
    val selectedSortOption: StateFlow<ProductSortOption> = _selectedSortOption.asStateFlow()

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
    }.flowOn(defaultDispatcher)

    val filteredProducts: StateFlow<List<Product>> = combine(
        repository.products,
        _filterParams,
        _selectedSortOption
    ) { products, params, sortOption ->
        val filtered = products.filter { product ->
            val matchesQuery = if (params.query.isBlank()) {
                true
            } else {
                product.name.contains(params.query, ignoreCase = true) ||
                        (product.barcode?.contains(params.query, ignoreCase = true) == true) ||
                        (product.differentiator?.contains(params.query, ignoreCase = true) == true) ||
                        product.tags.any { it.contains(params.query, ignoreCase = true) }
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

        when (sortOption) {
            ProductSortOption.NEWEST -> filtered
            ProductSortOption.NAME_ASC -> filtered.sortedBy { it.displayName.lowercase() }
            ProductSortOption.NAME_DESC -> filtered.sortedByDescending { it.displayName.lowercase() }
            ProductSortOption.OLDEST -> filtered.reversed()
            ProductSortOption.BEST_SELLERS -> filtered.sortedWith(
                compareByDescending<Product> { it.salesCount }.thenBy { it.displayName.lowercase() }
            )
        }
    }
    .flowOn(defaultDispatcher)
    .stateIn(
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

    fun setSelectedSortOption(option: ProductSortOption) {
        _selectedSortOption.value = option
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategoryFilter.value = null
        _selectedBrandFilter.value = null
        _selectedTypeFilter.value = null
        _lowStockOnlyFilter.value = false
        _selectedSortOption.value = ProductSortOption.NEWEST
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
        hasIva: Boolean = appliedTaxIds.isNotEmpty(),
        priceIncludesTax: Boolean = isTaxIncludedInPrice,
        isBundle: Boolean = false,
        bundleItems: List<BundleItem> = emptyList(),
        isService: Boolean = false,
        tags: List<String> = emptyList(),
        differentiator: String? = null
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
                isTaxIncludedInPrice = priceIncludesTax || isTaxIncludedInPrice,
                hasIva = hasIva || appliedTaxIds.isNotEmpty(),
                priceIncludesTax = priceIncludesTax || isTaxIncludedInPrice,
                isBundle = isBundle,
                bundleItems = bundleItems,
                isService = isService,
                tags = tags,
                differentiator = differentiator
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

    fun removeProducts(productIds: Collection<String>) {
        viewModelScope.launch {
            repository.deleteProducts(productIds)
        }
    }

    fun preloadSingleProduct(product: Product) {
        repository.preloadSingleProduct(product)
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
