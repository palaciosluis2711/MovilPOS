package com.lopezapp.movilpos.data.model

import com.lopezapp.movilpos.data.repository.AppRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InventoryOutflowTest {

    private lateinit var repository: AppRepository

    @Before
    fun setUp() {
        repository = AppRepository()
    }

    // ------------------------------------------------------------------
    // 1. OutflowReason Tests
    // ------------------------------------------------------------------

    @Test
    fun outflowReason_toSpanishLabel_returnsCorrectLabels() {
        assertEquals("Consumo Propio / Uso Interno", OutflowReason.CONSUMO_INTERNO.toSpanishLabel())
        assertEquals("Merma / Producto Dañado", OutflowReason.MERMA_DANADO.toSpanishLabel())
        assertEquals("Producto Vencido", OutflowReason.VENCIDO.toSpanishLabel())
        assertEquals("Muestra Gratis / Promoción", OutflowReason.MUESTRA_PROMOCION.toSpanishLabel())
        assertEquals("Ajuste de Conteo", OutflowReason.AJUSTE_INVENTARIO.toSpanishLabel())
    }

    // ------------------------------------------------------------------
    // 2. OutflowItem Instantiation, Defaults, Properties, and Costs
    // ------------------------------------------------------------------

    @Test
    fun outflowItem_defaultParameters_setsDefaultsAndZeroCost() {
        val item = OutflowItem(
            productId = "p0",
            productName = "Item Defecto",
        )

        assertEquals("p0", item.productId)
        assertEquals("Item Defecto", item.productName)
        assertEquals(1, item.quantity)
        assertEquals(0.0, item.unitCost, 0.001)
        assertEquals(0.0, item.totalCost, 0.001)
    }

    @Test
    fun outflowItem_defaultValues_calculatesTotalCostCorrectly() {
        val item = OutflowItem(
            productId = "p1",
            productName = "Producto Test",
            quantity = 3,
            unitCost = 2.50,
        )

        assertEquals("p1", item.productId)
        assertEquals("Producto Test", item.productName)
        assertEquals(3, item.quantity)
        assertEquals(2.50, item.unitCost, 0.001)
        assertEquals(7.50, item.totalCost, 0.001)
    }

    @Test
    fun outflowItem_customTotalCost_overridesCalculation() {
        val item = OutflowItem(
            productId = "p2",
            productName = "Producto Custom Total",
            quantity = 2,
            unitCost = 5.00,
            totalCost = 12.00,
        )

        assertEquals(12.00, item.totalCost, 0.001)
    }

    // ------------------------------------------------------------------
    // 3. InventoryOutflow Instantiation, Defaults, Properties, and Costs
    // ------------------------------------------------------------------

    @Test
    fun inventoryOutflow_defaultValues_areSetCorrectly() {
        val beforeCreation = System.currentTimeMillis()
        val outflow = InventoryOutflow(
            notes = "Dañado durante transporte",
        )
        val afterCreation = System.currentTimeMillis()

        assertNotNull(outflow.id)
        assertTrue(outflow.id.isNotBlank())
        assertEquals(OutflowReason.CONSUMO_INTERNO, outflow.reason)
        assertEquals("", outflow.registeredByUserId)
        assertEquals("", outflow.registeredByUserName)
        assertEquals("Dañado durante transporte", outflow.notes)
        assertTrue(outflow.items.isEmpty())
        assertEquals(0.0, outflow.totalCost, 0.001)
        assertTrue((outflow.dateMillis in beforeCreation..afterCreation))
    }

    @Test
    fun inventoryOutflow_customInstantiation_setsAllPropertiesCorrectly() {
        val items = listOf(
            OutflowItem(productId = "p1", productName = "Item 1", quantity = 2, unitCost = 5.0),
        )
        val customDate = 1700000000000L

        val outflow = InventoryOutflow(
            id = "out_custom_1",
            reason = OutflowReason.VENCIDO,
            registeredByUserId = "u1",
            registeredByUserName = "Ana Lopez",
            notes = "Vencido en estante",
            items = items,
            totalCost = 10.0,
            dateMillis = customDate,
        )

        assertEquals("out_custom_1", outflow.id)
        assertEquals(OutflowReason.VENCIDO, outflow.reason)
        assertEquals("u1", outflow.registeredByUserId)
        assertEquals("Ana Lopez", outflow.registeredByUserName)
        assertEquals("Vencido en estante", outflow.notes)
        assertEquals(1, outflow.items.size)
        assertEquals(10.0, outflow.totalCost, 0.001)
        assertEquals(customDate, outflow.dateMillis)
    }

    @Test
    fun inventoryOutflow_calculatesTotalCostFromItemsCorrectly() {
        val items = listOf(
            OutflowItem(productId = "p1", productName = "Item 1", quantity = 2, unitCost = 5.0),
            OutflowItem(productId = "p2", productName = "Item 2", quantity = 4, unitCost = 2.5),
        )

        val outflow = InventoryOutflow(
            reason = OutflowReason.MERMA_DANADO,
            registeredByUserId = "u101",
            registeredByUserName = "Juan Perez",
            notes = "Productos rotos en bodega",
            items = items,
        )

        assertEquals("u101", outflow.registeredByUserId)
        assertEquals("Juan Perez", outflow.registeredByUserName)
        assertEquals(OutflowReason.MERMA_DANADO, outflow.reason)
        assertEquals(2, outflow.items.size)
        // 2 * 5.0 + 4 * 2.5 = 10.0 + 10.0 = 20.0
        assertEquals(20.0, outflow.totalCost, 0.001)
    }

    // ------------------------------------------------------------------
    // 4. AppRepository.addInventoryOutflow Tests
    // ------------------------------------------------------------------

    @Test
    fun addInventoryOutflow_savesOutflowInRepository() {
        val outflow = InventoryOutflow(
            id = "out_repo_1",
            notes = "Salida de prueba",
        )

        repository.addInventoryOutflow(outflow)

        val outflows = repository.inventoryOutflows.value
        assertEquals(1, outflows.size)
        assertEquals("out_repo_1", outflows.first().id)
    }

    @Test
    fun addInventoryOutflow_reducesProductStock() {
        val product = Product(id = "p_stock_1", name = "Jugo de Naranja", price = 2.0, stock = 20)
        repository.addProduct(product)

        val item = OutflowItem(productId = "p_stock_1", productName = "Jugo de Naranja", quantity = 5, unitCost = 1.0)
        val outflow = InventoryOutflow(
            id = "out_stock_1",
            reason = OutflowReason.MERMA_DANADO,
            items = listOf(item),
        )

        repository.addInventoryOutflow(outflow)

        val updatedProduct = repository.products.value.find { it.id == "p_stock_1" }
        assertNotNull(updatedProduct)
        assertEquals(15, updatedProduct?.stock)
    }

    @Test
    fun addInventoryOutflow_stockDoesNotDropBelowZero() {
        val product = Product(id = "p_stock_2", name = "Leche", price = 1.5, stock = 3)
        repository.addProduct(product)

        val item = OutflowItem(productId = "p_stock_2", productName = "Leche", quantity = 10, unitCost = 1.0)
        val outflow = InventoryOutflow(
            id = "out_stock_2",
            reason = OutflowReason.VENCIDO,
            items = listOf(item),
        )

        repository.addInventoryOutflow(outflow)

        val updatedProduct = repository.products.value.find { it.id == "p_stock_2" }
        assertNotNull(updatedProduct)
        assertEquals(0, updatedProduct?.stock)
    }

    @Test
    fun addInventoryOutflow_multipleItemsSameProduct_deductsSummedQuantity() {
        val product = Product(id = "p_stock_3", name = "Pan", price = 0.5, stock = 20)
        repository.addProduct(product)

        val item1 = OutflowItem(productId = "p_stock_3", productName = "Pan", quantity = 3, unitCost = 0.3)
        val item2 = OutflowItem(productId = "p_stock_3", productName = "Pan", quantity = 4, unitCost = 0.3)
        val outflow = InventoryOutflow(
            id = "out_stock_3",
            reason = OutflowReason.MERMA_DANADO,
            items = listOf(item1, item2),
        )

        repository.addInventoryOutflow(outflow)

        val updatedProduct = repository.products.value.find { it.id == "p_stock_3" }
        assertNotNull(updatedProduct)
        assertEquals(13, updatedProduct?.stock)
    }

    @Test
    fun addInventoryOutflow_doesNotRegisterExpense_forConsumoInterno() {
        val item = OutflowItem(productId = "p1", productName = "Café", quantity = 2, unitCost = 3.0)
        val outflow = InventoryOutflow(
            id = "out_exp_1",
            reason = OutflowReason.CONSUMO_INTERNO,
            registeredByUserId = "u_cashier",
            registeredByUserName = "Pedro",
            notes = "Consumo reunión de personal",
            items = listOf(item),
        )

        val initialExpenseCount = repository.expenses.value.size

        repository.addInventoryOutflow(outflow)

        val expenses = repository.expenses.value
        assertEquals(initialExpenseCount, expenses.size)
        val expense = expenses.find { it.description.contains("Consumo reunión de personal") }
        assertNull(expense)
    }

    @Test
    fun addInventoryOutflow_doesNotRegisterExpense_forBajasYMermas() {
        val item = OutflowItem(productId = "p2", productName = "Yogurt", quantity = 5, unitCost = 1.2)
        val outflow = InventoryOutflow(
            id = "out_exp_2",
            reason = OutflowReason.VENCIDO,
            registeredByUserId = "u_mgr",
            registeredByUserName = "Maria",
            notes = "Lote vencido",
            items = listOf(item),
        )

        val initialExpenseCount = repository.expenses.value.size

        repository.addInventoryOutflow(outflow)

        val expenses = repository.expenses.value
        assertEquals(initialExpenseCount, expenses.size)
        val expense = expenses.find { it.description.contains("Lote vencido") }
        assertNull(expense)
    }

    // ------------------------------------------------------------------
    // 5. AppRepository.deleteInventoryOutflow Tests
    // ------------------------------------------------------------------

    @Test
    fun deleteInventoryOutflow_removesOutflowFromRepository() {
        val outflow1 = InventoryOutflow(id = "out_del_1", notes = "Outflow 1")
        val outflow2 = InventoryOutflow(id = "out_del_2", notes = "Outflow 2")

        repository.addInventoryOutflow(outflow1)
        repository.addInventoryOutflow(outflow2)

        assertEquals(2, repository.inventoryOutflows.value.size)

        repository.deleteInventoryOutflow("out_del_1")

        val remaining = repository.inventoryOutflows.value
        assertEquals(1, remaining.size)
        assertEquals("out_del_2", remaining.first().id)
    }

    @Test
    fun deleteInventoryOutflow_nonExistentId_doesNotModifyOutflows() {
        val outflow = InventoryOutflow(id = "out_del_3", notes = "Outflow 3")
        repository.addInventoryOutflow(outflow)

        assertEquals(1, repository.inventoryOutflows.value.size)

        repository.deleteInventoryOutflow("invalid_id")

        assertEquals(1, repository.inventoryOutflows.value.size)
        assertEquals("out_del_3", repository.inventoryOutflows.value.first().id)
    }
}
