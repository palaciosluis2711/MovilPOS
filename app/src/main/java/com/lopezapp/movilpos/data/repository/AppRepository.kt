package com.lopezapp.movilpos.data.repository

import com.lopezapp.movilpos.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppRepository {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    init {
        // Load initial dummy data
        _products.value = listOf(
            Product(name = "Coffee", price = 2.5, stock = 100),
            Product(name = "Tea", price = 1.5, stock = 50),
            Product(name = "Sandwich", price = 4.0, stock = 20)
        )
    }

    fun addProduct(product: Product) {
        _products.update { currentList ->
            currentList + product
        }
    }

    fun updateProduct(product: Product) {
        _products.update { currentList ->
            currentList.map { if (it.id == product.id) product else it }
        }
    }

    fun deleteProduct(productId: String) {
        _products.update { currentList ->
            currentList.filter { it.id != productId }
        }
    }
}
