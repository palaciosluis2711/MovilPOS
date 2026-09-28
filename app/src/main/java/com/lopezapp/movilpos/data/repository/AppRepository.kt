package com.lopezapp.movilpos.data.repository

import com.lopezapp.movilpos.data.model.Brand
import com.lopezapp.movilpos.data.model.Category
import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.DocumentType
import com.lopezapp.movilpos.data.model.Product
import com.lopezapp.movilpos.data.model.Supplier
import com.lopezapp.movilpos.data.model.Tax
import com.lopezapp.movilpos.data.model.TaxValueType
import com.lopezapp.movilpos.data.model.UnitOfMeasure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppRepository {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _brands = MutableStateFlow<List<Brand>>(emptyList())
    val brands: StateFlow<List<Brand>> = _brands.asStateFlow()

    private val _unitsOfMeasure = MutableStateFlow<List<UnitOfMeasure>>(emptyList())
    val unitsOfMeasure: StateFlow<List<UnitOfMeasure>> = _unitsOfMeasure.asStateFlow()

    private val _taxes = MutableStateFlow<List<Tax>>(emptyList())
    val taxes: StateFlow<List<Tax>> = _taxes.asStateFlow()

    private val _suppliers = MutableStateFlow<List<Supplier>>(emptyList())
    val suppliers: StateFlow<List<Supplier>> = _suppliers.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    init {
        // Load initial dummy data
        _products.value = listOf(
            Product(name = "Coffee", price = 2.5, stock = 100, category = "Bebidas", brand = "Nestlé"),
            Product(name = "Tea", price = 1.5, stock = 50, category = "Bebidas", brand = "General"),
            Product(name = "Sandwich", price = 4.0, stock = 20, category = "Alimentos", brand = "Sin marca")
        )
        _categories.value = listOf(
            Category(name = "Bebidas", description = "Bebidas calientes y frías"),
            Category(name = "Alimentos", description = "Comidas y snacks"),
            Category(name = "Limpieza", description = "Artículos de limpieza"),
            Category(name = "General", description = "Categoría por defecto")
        )
        _brands.value = listOf(
            Brand(name = "Sin marca", description = "Sin marca específica"),
            Brand(name = "Coca-Cola", description = "Bebidas gaseosas y refrescos"),
            Brand(name = "Nestlé", description = "Alimentos y bebidas"),
            Brand(name = "General", description = "Marca general")
        )
        _unitsOfMeasure.value = listOf(
            UnitOfMeasure(name = "Unidad", abbreviation = "ud", isPackageOrBox = false),
            UnitOfMeasure(name = "Kilogramo", abbreviation = "kg", isPackageOrBox = false),
            UnitOfMeasure(name = "Libra", abbreviation = "lb", isPackageOrBox = false),
            UnitOfMeasure(name = "Caja", abbreviation = "cj", isPackageOrBox = true),
            UnitOfMeasure(name = "Paquete", abbreviation = "paq", isPackageOrBox = true)
        )
        _taxes.value = listOf(
            Tax(name = "IVA", description = "Impuesto al Valor Agregado", valueType = TaxValueType.PERCENTAGE, value = 16.0)
        )
        _suppliers.value = listOf(
            Supplier(
                name = "Distribuidora Central S.A.",
                address = "Av. Principal 123, Ciudad",
                email = "contacto@distribuidoracentral.com",
                phone = "+1 800 555 0199"
            ),
            Supplier(
                name = "Comercializadora del Norte",
                address = "Calle Industrial 45",
                email = "ventas@comercializadoranorte.com",
                phone = "+1 800 555 0200"
            )
        )
        _customers.value = listOf(
            Customer(
                name = "Cliente General",
                documentType = DocumentType.DUI,
                documentNumber = "00000000-0",
                country = "El Salvador",
                department = "San Salvador",
                municipality = "San Salvador Centro",
                district = "San Salvador",
                isDefault = true
            )
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

    fun addCategory(category: Category) {
        _categories.update { currentList ->
            currentList + category
        }
    }

    fun updateCategory(category: Category) {
        _categories.update { currentList ->
            currentList.map { if (it.id == category.id) category else it }
        }
    }

    fun deleteCategory(categoryId: String) {
        _categories.update { currentList ->
            currentList.filter { it.id != categoryId }
        }
    }

    fun addBrand(brand: Brand) {
        _brands.update { currentList ->
            currentList + brand
        }
    }

    fun updateBrand(brand: Brand) {
        _brands.update { currentList ->
            currentList.map { if (it.id == brand.id) brand else it }
        }
    }

    fun deleteBrand(brandId: String) {
        _brands.update { currentList ->
            currentList.filter { it.id != brandId }
        }
    }

    fun addUnitOfMeasure(unitOfMeasure: UnitOfMeasure) {
        _unitsOfMeasure.update { currentList ->
            currentList + unitOfMeasure
        }
    }

    fun updateUnitOfMeasure(unitOfMeasure: UnitOfMeasure) {
        _unitsOfMeasure.update { currentList ->
            currentList.map { if (it.id == unitOfMeasure.id) unitOfMeasure else it }
        }
    }

    fun deleteUnitOfMeasure(unitId: String) {
        _unitsOfMeasure.update { currentList ->
            currentList.filter { it.id != unitId }
        }
    }

    fun addTax(tax: Tax) {
        _taxes.update { currentList ->
            currentList + tax
        }
    }

    fun updateTax(tax: Tax) {
        _taxes.update { currentList ->
            currentList.map { if (it.id == tax.id) tax else it }
        }
    }

    fun deleteTax(taxId: String) {
        _taxes.update { currentList ->
            currentList.filter { it.id != taxId }
        }
    }

    fun addSupplier(supplier: Supplier) {
        _suppliers.update { currentList ->
            currentList + supplier
        }
    }

    fun updateSupplier(supplier: Supplier) {
        _suppliers.update { currentList ->
            currentList.map { if (it.id == supplier.id) supplier else it }
        }
    }

    fun deleteSupplier(supplierId: String) {
        _suppliers.update { currentList ->
            currentList.filter { it.id != supplierId }
        }
    }

    fun addCustomer(customer: Customer) {
        _customers.update { currentList ->
            val updatedList = if (customer.isDefault) {
                currentList.map { it.copy(isDefault = false) }
            } else {
                currentList
            }
            updatedList + customer
        }
    }

    fun updateCustomer(customer: Customer) {
        _customers.update { currentList ->
            val updatedList = if (customer.isDefault) {
                currentList.map { if (it.id == customer.id) customer else it.copy(isDefault = false) }
            } else {
                currentList.map { if (it.id == customer.id) customer else it }
            }
            updatedList
        }
    }

    fun deleteCustomer(customerId: String) {
        _customers.update { currentList ->
            currentList.filter { it.id != customerId }
        }
    }

    fun setDefaultCustomer(customerId: String) {
        _customers.update { currentList ->
            currentList.map { it.copy(isDefault = (it.id == customerId)) }
        }
    }
}
