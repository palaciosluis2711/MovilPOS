package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.CartItem
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.util.roundToTwoDecimals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

data class POSState(
    val products: List<Product> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val total: Double = 0.0
)

class POSViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    
    val uiState: StateFlow<POSState> = combine(
        repository.products,
        _cartItems
    ) { products, cartItems ->
        val total = cartItems.sumOf { it.subtotal }.roundToTwoDecimals()
        POSState(
            products = products,
            cartItems = cartItems,
            total = total
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = POSState()
    )

    fun addToCart(product: Product) {
        if (product.isService) {
            if (product.bundleItems.isNotEmpty()) {
                val currentProducts = repository.products.value
                val itemsWithProducts = product.bundleItems.mapNotNull { bundleItem ->
                    val itemProduct = currentProducts.find { it.id == bundleItem.productId }
                    if (itemProduct != null) bundleItem to itemProduct else null
                }
                val includedProductsTotal = itemsWithProducts.sumOf { (bundleItem, itemProduct) ->
                    itemProduct.price * bundleItem.quantity
                }
                val serviceRemainderPrice = (maxOf(0.0, product.price - includedProductsTotal)).roundToTwoDecimals()

                _cartItems.update { currentCart ->
                    var updatedCart = currentCart
                    for ((bundleItem, itemProduct) in itemsWithProducts) {
                        updatedCart = addCartItemToCart(
                            cart = updatedCart,
                            product = itemProduct,
                            quantityToAdd = bundleItem.quantity,
                            unitPriceOverride = null,
                            isBundleDiscounted = false
                        )
                    }
                    updatedCart = addCartItemToCart(
                        cart = updatedCart,
                        product = product,
                        quantityToAdd = 1,
                        unitPriceOverride = serviceRemainderPrice,
                        isBundleDiscounted = false
                    )
                    updatedCart
                }
            } else {
                _cartItems.update { currentCart ->
                    addCartItemToCart(
                        cart = currentCart,
                        product = product,
                        quantityToAdd = 1,
                        unitPriceOverride = null,
                        isBundleDiscounted = false
                    )
                }
            }
        } else if (product.isBundle) {
            val currentProducts = repository.products.value
            val itemsWithProducts = product.bundleItems.mapNotNull { bundleItem ->
                val itemProduct = currentProducts.find { it.id == bundleItem.productId }
                if (itemProduct != null) bundleItem to itemProduct else null
            }
            val originalTotal = itemsWithProducts.sumOf { (bundleItem, itemProduct) ->
                itemProduct.price * bundleItem.quantity
            }

            if (product.price < originalTotal && originalTotal > 0) {
                val ratio = product.price / originalTotal
                _cartItems.update { currentCart ->
                    var updatedCart = currentCart
                    for ((bundleItem, itemProduct) in itemsWithProducts) {
                        updatedCart = addCartItemToCart(
                            cart = updatedCart,
                            product = itemProduct,
                            quantityToAdd = bundleItem.quantity,
                            unitPriceOverride = (itemProduct.price * ratio).roundToTwoDecimals(),
                            isBundleDiscounted = true
                        )
                    }
                    updatedCart
                }
            } else {
                _cartItems.update { currentCart ->
                    var updatedCart = currentCart
                    for ((bundleItem, itemProduct) in itemsWithProducts) {
                        updatedCart = addCartItemToCart(
                            cart = updatedCart,
                            product = itemProduct,
                            quantityToAdd = bundleItem.quantity,
                            unitPriceOverride = null,
                            isBundleDiscounted = false
                        )
                    }
                    updatedCart
                }
            }
        } else {
            _cartItems.update { currentCart ->
                addCartItemToCart(
                    cart = currentCart,
                    product = product,
                    quantityToAdd = 1,
                    unitPriceOverride = null,
                    isBundleDiscounted = false
                )
            }
        }
    }

    fun addToCart(cartItem: CartItem) {
        _cartItems.update { currentCart ->
            val index = currentCart.indexOfFirst {
                it.product.id == cartItem.product.id &&
                it.isBundleDiscounted == cartItem.isBundleDiscounted &&
                arePricesEqual(it.unitPriceOverride, cartItem.unitPriceOverride)
            }
            if (index != -1) {
                currentCart.mapIndexed { i, item ->
                    if (i == index) item.copy(quantity = item.quantity + 1) else item
                }
            } else {
                addCartItemToCart(
                    cart = currentCart,
                    product = cartItem.product,
                    quantityToAdd = 1,
                    unitPriceOverride = cartItem.unitPriceOverride,
                    isBundleDiscounted = cartItem.isBundleDiscounted
                )
            }
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        _cartItems.update { currentCart ->
            val index = currentCart.indexOfFirst {
                it.product.id == cartItem.product.id &&
                it.isBundleDiscounted == cartItem.isBundleDiscounted &&
                arePricesEqual(it.unitPriceOverride, cartItem.unitPriceOverride)
            }
            if (index != -1) {
                val item = currentCart[index]
                if (item.quantity > 1) {
                    currentCart.mapIndexed { i, current ->
                        if (i == index) current.copy(quantity = current.quantity - 1) else current
                    }
                } else {
                    currentCart.filterIndexed { i, _ -> i != index }
                }
            } else {
                currentCart
            }
        }
    }

    fun removeFromCart(product: Product) {
        _cartItems.update { currentCart ->
            val existingItem = currentCart.find { it.product.id == product.id }
            if (existingItem != null) {
                if (existingItem.quantity > 1) {
                    currentCart.map {
                        if (it.product.id == product.id) {
                            it.copy(quantity = it.quantity - 1)
                        } else {
                            it
                        }
                    }
                } else {
                    currentCart.filter { it.product.id != product.id }
                }
            } else {
                currentCart
            }
        }
    }

    private fun addCartItemToCart(
        cart: List<CartItem>,
        product: Product,
        quantityToAdd: Int,
        unitPriceOverride: Double?,
        isBundleDiscounted: Boolean
    ): List<CartItem> {
        val existingIndex = cart.indexOfFirst { item ->
            item.product.id == product.id &&
            item.isBundleDiscounted == isBundleDiscounted &&
            arePricesEqual(item.unitPriceOverride, unitPriceOverride)
        }

        return if (existingIndex != -1) {
            cart.mapIndexed { index, item ->
                if (index == existingIndex) {
                    item.copy(quantity = item.quantity + quantityToAdd)
                } else {
                    item
                }
            }
        } else {
            cart + CartItem(
                product = product,
                quantity = quantityToAdd,
                unitPriceOverride = unitPriceOverride,
                isBundleDiscounted = isBundleDiscounted
            )
        }
    }

    private fun arePricesEqual(p1: Double?, p2: Double?): Boolean {
        if (p1 == null && p2 == null) return true
        if (p1 == null || p2 == null) return false
        return abs(p1 - p2) < 1e-6
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun checkout() {
        viewModelScope.launch {
            // Update inventory based on cart
            val currentCart = _cartItems.value
            val currentProducts = repository.products.value
            
            for (item in currentCart) {
                val productInRepo = currentProducts.find { it.id == item.product.id }
                if (productInRepo != null) {
                    val updatedStock = (productInRepo.stock - item.quantity).coerceAtLeast(0)
                    repository.updateProduct(productInRepo.copy(stock = updatedStock))
                }
            }
            clearCart()
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(POSViewModel::class.java)) {
                return POSViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
