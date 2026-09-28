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

class InventoryViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val categories: StateFlow<List<Category>> = repository.categories
    val brands: StateFlow<List<Brand>> = repository.brands
    val unitsOfMeasure: StateFlow<List<UnitOfMeasure>> = repository.unitsOfMeasure
    val taxes: StateFlow<List<Tax>> = repository.taxes
    val allProducts: StateFlow<List<Product>> = repository.products

    val inventoryState: StateFlow<List<Product>> = combine(
        repository.products,
        _searchQuery
    ) { products, query ->
        if (query.isBlank()) {
            products
        } else {
            products.filter { it.name.contains(query, ignoreCase = true) || it.barcode?.contains(query) == true }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
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
