package com.lopezapp.movilpos.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.BarcodeLabelConfig
import com.lopezapp.movilpos.data.model.BatchLabelItem
import com.lopezapp.movilpos.data.model.LabelSize
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.util.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

class BarcodeLabelViewModel(
    private val repository: AppRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.products
    val labelSizes: StateFlow<List<LabelSize>> = repository.labelSizes
    val config: StateFlow<BarcodeLabelConfig> = repository.barcodeLabelConfig
    val businessInfo = repository.businessInfo

    val batchItems: StateFlow<List<BatchLabelItem>> = repository.batchItems

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    fun preloadSingleProduct(product: Product) {
        repository.preloadSingleProduct(product)
    }

    fun addProductToBatch(product: Product, quantity: Int = 1) {
        repository.addProductToBatch(product, quantity)
    }

    fun updateItemQuantity(productId: String, quantity: Int) {
        repository.updateItemQuantity(productId, quantity)
    }

    fun removeItem(productId: String) {
        repository.removeItem(productId)
    }

    fun clearBatch() {
        repository.clearBatch()
    }

    fun updateConfig(newConfig: BarcodeLabelConfig) {
        repository.updateBarcodeLabelConfig(newConfig)
    }

    fun selectSize(sizeId: String) {
        val currentConfig = config.value
        repository.updateBarcodeLabelConfig(currentConfig.copy(selectedSizeId = sizeId))
    }

    fun setCustomDimensions(widthMm: Int, heightMm: Int) {
        val currentConfig = config.value
        repository.updateBarcodeLabelConfig(currentConfig.copy(customWidthMm = widthMm, customHeightMm = heightMm))
    }

    fun addFavoriteSize(name: String, widthMm: Int, heightMm: Int) {
        repository.addFavoriteSize(name, widthMm.toDouble(), heightMm.toDouble())
    }

    fun removeFavoriteSize(sizeId: String) {
        repository.removeFavoriteSize(sizeId)
    }

    suspend fun generatePdf(context: Context): File? {
        val items = batchItems.value
        if (items.isEmpty()) return null
        _isGeneratingPdf.value = true
        return try {
            withContext(Dispatchers.IO) {
                PdfReportGenerator.generateBarcodeLabelsPdf(
                    context = context,
                    items = items,
                    config = config.value,
                    labelSizes = labelSizes.value,
                    businessInfo = businessInfo.value
                )
            }
        } finally {
            _isGeneratingPdf.value = false
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BarcodeLabelViewModel::class.java)) {
                return BarcodeLabelViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
