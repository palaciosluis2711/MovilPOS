package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.ArithmeticOperator
import com.lopezapp.movilpos.data.model.BaseVariable
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.TaxValueType
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.model.AnimationType
import com.lopezapp.movilpos.ui.navigation.buildNavTransition
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = SettingsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaultSettings_hasCorrectDefaults() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(400, state.animationDurationMs)
        assertEquals(AnimationType.SLIDE_AND_FADE, state.animationType)
        assertEquals("$", state.currencySymbol)
        assertEquals(2, state.defaultDecimalPlaces)
        assertTrue(state.allowExtraDecimals)
        assertEquals(4, state.categories.size)
        assertEquals(4, state.brands.size)
        assertEquals(5, state.unitsOfMeasure.size)
        assertEquals(1, state.taxes.size)
        assertEquals("IVA", state.taxes[0].name)
        assertEquals(16.0, state.taxes[0].value, 0.001)
        assertEquals(TaxValueType.PERCENTAGE, state.taxes[0].valueType)
    }

    @Test
    fun updateAnimationDuration_updatesAndClampsValue() = runTest {
        viewModel.updateAnimationDuration(600)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(600, viewModel.uiState.value.animationDurationMs)

        // Test lower bound clamping
        viewModel.updateAnimationDuration(50)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(100, viewModel.uiState.value.animationDurationMs)

        // Test upper bound clamping
        viewModel.updateAnimationDuration(1500)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1000, viewModel.uiState.value.animationDurationMs)
    }

    @Test
    fun updateAnimationType_updatesValueCorrectly() = runTest {
        AnimationType.entries.forEach { type ->
            viewModel.updateAnimationType(type)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(type, viewModel.uiState.value.animationType)
        }
    }

    @Test
    fun updateCurrencySymbol_updatesValueCorrectly() = runTest {
        viewModel.updateCurrencySymbol("€")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("€", viewModel.uiState.value.currencySymbol)

        viewModel.updateCurrencySymbol("MXN$")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("MXN$", viewModel.uiState.value.currencySymbol)
    }

    @Test
    fun updateDefaultDecimalPlaces_updatesAndClampsValue() = runTest {
        viewModel.updateDefaultDecimalPlaces(3)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.defaultDecimalPlaces)

        viewModel.updateDefaultDecimalPlaces(4)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(4, viewModel.uiState.value.defaultDecimalPlaces)

        // Test clamping below 2
        viewModel.updateDefaultDecimalPlaces(1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.defaultDecimalPlaces)

        // Test clamping above 4
        viewModel.updateDefaultDecimalPlaces(5)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(4, viewModel.uiState.value.defaultDecimalPlaces)
    }

    @Test
    fun updateAllowExtraDecimals_updatesValueCorrectly() = runTest {
        viewModel.updateAllowExtraDecimals(false)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.allowExtraDecimals)

        viewModel.updateAllowExtraDecimals(true)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.allowExtraDecimals)
    }

    @Test
    fun addCategory_addsNewCategoryToState() = runTest {
        viewModel.addCategory("Electrónica", "Aparatos y accesorios")
        testDispatcher.scheduler.advanceUntilIdle()

        val categories = repository.categories.value
        val addedCategory = categories.find { it.name == "Electrónica" }

        assertNotNull(addedCategory)
        assertEquals("Aparatos y accesorios", addedCategory?.description)
    }

    @Test
    fun updateCategory_updatesCategoryInState() = runTest {
        val initialCategory = repository.categories.value.first()
        viewModel.updateCategory(initialCategory.id, "Bebidas Refrescantes", "Nuevas bebidas")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedCategory = repository.categories.value.find { it.id == initialCategory.id }
        assertEquals("Bebidas Refrescantes", updatedCategory?.name)
        assertEquals("Nuevas bebidas", updatedCategory?.description)
    }

    @Test
    fun deleteCategory_removesCategoryFromState() = runTest {
        val initialCategory = repository.categories.value.first()
        viewModel.deleteCategory(initialCategory.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val deletedCategory = repository.categories.value.find { it.id == initialCategory.id }
        assertNull(deletedCategory)
    }

    @Test
    fun addBrand_addsNewBrandToState() = runTest {
        viewModel.addBrand("Pepsi", "Bebidas gaseosas", "content://logo/pepsi")
        testDispatcher.scheduler.advanceUntilIdle()

        val brands = repository.brands.value
        val addedBrand = brands.find { it.name == "Pepsi" }

        assertNotNull(addedBrand)
        assertEquals("Bebidas gaseosas", addedBrand?.description)
        assertEquals("content://logo/pepsi", addedBrand?.logoUri)
    }

    @Test
    fun updateBrand_updatesBrandInState() = runTest {
        val initialBrand = repository.brands.value.first()
        viewModel.updateBrand(initialBrand.id, "Sin marca actualizada", "Sin marca desc", "content://logo/updated")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedBrand = repository.brands.value.find { it.id == initialBrand.id }
        assertEquals("Sin marca actualizada", updatedBrand?.name)
        assertEquals("Sin marca desc", updatedBrand?.description)
        assertEquals("content://logo/updated", updatedBrand?.logoUri)
    }

    @Test
    fun deleteBrand_removesBrandFromState() = runTest {
        val initialBrand = repository.brands.value.first()
        viewModel.deleteBrand(initialBrand.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val deletedBrand = repository.brands.value.find { it.id == initialBrand.id }
        assertNull(deletedBrand)
    }

    @Test
    fun addUnitOfMeasure_addsNewUnitToState() = runTest {
        viewModel.addUnitOfMeasure("Litro", "l", false)
        testDispatcher.scheduler.advanceUntilIdle()

        val units = repository.unitsOfMeasure.value
        val addedUnit = units.find { it.name == "Litro" }

        assertNotNull(addedUnit)
        assertEquals("l", addedUnit?.abbreviation)
        assertEquals(false, addedUnit?.isPackageOrBox)
    }

    @Test
    fun updateUnitOfMeasure_updatesUnitInState() = runTest {
        val initialUnit = repository.unitsOfMeasure.value.first()
        viewModel.updateUnitOfMeasure(initialUnit.id, "Unidad Especial", "ue", true)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedUnit = repository.unitsOfMeasure.value.find { it.id == initialUnit.id }
        assertEquals("Unidad Especial", updatedUnit?.name)
        assertEquals("ue", updatedUnit?.abbreviation)
        assertEquals(true, updatedUnit?.isPackageOrBox)
    }

    @Test
    fun deleteUnitOfMeasure_removesUnitFromState() = runTest {
        val initialUnit = repository.unitsOfMeasure.value.first()
        viewModel.deleteUnitOfMeasure(initialUnit.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val deletedUnit = repository.unitsOfMeasure.value.find { it.id == initialUnit.id }
        assertNull(deletedUnit)
    }

    @Test
    fun addTax_addsNewTaxToState() = runTest {
        viewModel.addTax("Propina", "Propina voluntaria", TaxValueType.FIXED_AMOUNT, 2.0)
        testDispatcher.scheduler.advanceUntilIdle()

        val taxes = repository.taxes.value
        val addedTax = taxes.find { it.name == "Propina" }

        assertNotNull(addedTax)
        assertEquals("Propina voluntaria", addedTax?.description)
        assertEquals(TaxValueType.FIXED_AMOUNT, addedTax?.valueType)
        assertEquals(2.0, addedTax?.value ?: 0.0, 0.001)
    }

    @Test
    fun updateTax_updatesTaxInState() = runTest {
        val initialTax = repository.taxes.value.first()
        viewModel.updateTax(initialTax.id, "IVA Reducido", "Impuesto reducido", TaxValueType.PERCENTAGE, 8.0)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedTax = repository.taxes.value.find { it.id == initialTax.id }
        assertEquals("IVA Reducido", updatedTax?.name)
        assertEquals("Impuesto reducido", updatedTax?.description)
        assertEquals(TaxValueType.PERCENTAGE, updatedTax?.valueType)
        assertEquals(8.0, updatedTax?.value ?: 0.0, 0.001)
    }

    @Test
    fun deleteTax_removesTaxFromState() = runTest {
        val initialTax = repository.taxes.value.first()
        viewModel.deleteTax(initialTax.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val deletedTax = repository.taxes.value.find { it.id == initialTax.id }
        assertNull(deletedTax)
    }

    @Test
    fun priceRule_calculatePrice_calculatesCorrectly() {
        val costRule = PriceRule(
            name = "Mayoreo",
            baseVariable = BaseVariable.COST,
            operator = ArithmeticOperator.DIVIDE,
            value = 0.80
        )
        // cost = 10.0, price = 20.0 -> base = cost = 10.0 -> 10.0 / 0.80 = 12.5
        assertEquals(12.5, costRule.calculatePrice(basePrice = 20.0, cost = 10.0), 0.001)

        val priceRule = PriceRule(
            name = "Descuento 20%",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.80
        )
        // cost = 10.0, price = 20.0 -> base = price = 20.0 -> 20.0 * 0.80 = 16.0
        assertEquals(16.0, priceRule.calculatePrice(basePrice = 20.0, cost = 10.0), 0.001)

        val addRule = PriceRule(
            name = "Cargo Fijo",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.ADD,
            value = 5.0
        )
        assertEquals(25.0, addRule.calculatePrice(basePrice = 20.0, cost = 10.0), 0.001)

        val subRule = PriceRule(
            name = "Descuento Fijo",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.SUBTRACT,
            value = 3.0
        )
        assertEquals(17.0, subRule.calculatePrice(basePrice = 20.0, cost = 10.0), 0.001)
    }

    @Test
    fun priceRule_formulaRepresentation_formatsCorrectly() {
        val rule1 = PriceRule(
            name = "Regla 1",
            baseVariable = BaseVariable.COST,
            operator = ArithmeticOperator.DIVIDE,
            value = 0.8
        )
        assertEquals("[Costo] ÷ 0.8", rule1.formulaRepresentation())

        val rule2 = PriceRule(
            name = "Regla 2",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.ADD,
            value = 5.0
        )
        assertEquals("[Precio] + 5", rule2.formulaRepresentation())
    }

    @Test
    fun addPriceRule_addsNewRuleToViewModelAndRepository() = runTest {
        val newRule = PriceRule(
            name = "Precio VIP",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.90,
            applyToAllCategories = false,
            categoryNames = listOf("Bebidas"),
            applyToAllCustomers = true
        )
        viewModel.addPriceRule(newRule)
        testDispatcher.scheduler.advanceUntilIdle()

        val rules = viewModel.uiState.value.priceRules
        val addedRule = rules.find { it.name == "Precio VIP" }

        assertNotNull(addedRule)
        assertEquals(BaseVariable.PRICE, addedRule?.baseVariable)
        assertEquals(ArithmeticOperator.MULTIPLY, addedRule?.operator)
        assertEquals(0.90, addedRule?.value ?: 0.0, 0.001)
        assertFalse(addedRule?.applyToAllCategories ?: true)
        assertEquals(listOf("Bebidas"), addedRule?.categoryNames)
    }

    @Test
    fun updatePriceRule_updatesRuleInViewModelAndRepository() = runTest {
        val newRule = PriceRule(
            name = "Regla Inicial",
            baseVariable = BaseVariable.COST,
            operator = ArithmeticOperator.ADD,
            value = 2.0
        )
        viewModel.addPriceRule(newRule)
        testDispatcher.scheduler.advanceUntilIdle()

        val createdRule = viewModel.uiState.value.priceRules.find { it.name == "Regla Inicial" }!!
        val updatedRule = createdRule.copy(name = "Regla Modificada", value = 3.5, isActive = false)

        viewModel.updatePriceRule(updatedRule)
        testDispatcher.scheduler.advanceUntilIdle()

        val finalRule = viewModel.uiState.value.priceRules.find { it.id == createdRule.id }
        assertNotNull(finalRule)
        assertEquals("Regla Modificada", finalRule?.name)
        assertEquals(3.5, finalRule?.value ?: 0.0, 0.001)
        assertFalse(finalRule?.isActive ?: true)
    }

    @Test
    fun deletePriceRule_removesRuleFromViewModelAndRepository() = runTest {
        val newRule = PriceRule(
            name = "Regla a Borrar",
            baseVariable = BaseVariable.COST,
            operator = ArithmeticOperator.ADD,
            value = 1.0
        )
        viewModel.addPriceRule(newRule)
        testDispatcher.scheduler.advanceUntilIdle()

        val createdRule = viewModel.uiState.value.priceRules.find { it.name == "Regla a Borrar" }!!
        viewModel.deletePriceRule(createdRule.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val deletedRule = viewModel.uiState.value.priceRules.find { it.id == createdRule.id }
        assertNull(deletedRule)
    }

    @Test
    fun buildNavTransition_returnsValidContentTransformForAllTypes() {
        AnimationType.entries.forEach { type ->
            val pushTransition = buildNavTransition(type, durationMs = 400, isPop = false)
            assertNotNull(pushTransition)

            val popTransition = buildNavTransition(type, durationMs = 400, isPop = true)
            assertNotNull(popTransition)
        }
    }
}
