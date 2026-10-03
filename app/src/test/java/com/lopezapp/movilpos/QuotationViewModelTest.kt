package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.BaseVariable
import com.lopezapp.movilpos.data.model.ArithmeticOperator
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.QuotationViewModel
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
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class QuotationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: QuotationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = QuotationViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun quotations_initialDataIsLoaded() = runTest {
        val quotations = viewModel.allQuotations.value
        assertEquals(1, quotations.size)
        assertEquals("Cliente General", quotations[0].customerName)
    }

    @Test
    fun selectCustomer_setsCustomerNameAndEmailInputs() = runTest {
        val customer = repository.customers.value.first()
        viewModel.selectCustomer(customer)

        assertEquals("Cliente General", viewModel.customerNameInput.value)
    }

    @Test
    fun addProductToDraft_updatesDraftItemsAndTotalAmount() = runTest {
        val product = repository.products.value.first()
        viewModel.addProductToDraft(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val draftItems = viewModel.draftItems.value
        assertEquals(1, draftItems.size)
        assertEquals(product.id, draftItems[0].productId)
        assertEquals(1, draftItems[0].quantity)
        assertEquals(product.price, draftItems[0].unitPrice, 0.001)

        // Add same product again -> quantity increments
        viewModel.addProductToDraft(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedDraftItems = viewModel.draftItems.value
        assertEquals(1, updatedDraftItems.size)
        assertEquals(2, updatedDraftItems[0].quantity)
    }

    @Test
    fun updateDraftItemQuantityAndUnitPrice_calculatesTotalAmountCorrectly() = runTest {
        val product = repository.products.value.first()
        viewModel.addProductToDraft(product)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateDraftItemQuantity(product.id, 4)
        viewModel.updateDraftItemUnitPrice(product.id, 3.00)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(12.00, viewModel.totalAmount.value, 0.001)
    }

    @Test
    fun applyPriceRuleToDraftItem_updatesUnitPriceAndRuleFlag() = runTest {
        val product = repository.products.value.first()
        viewModel.addProductToDraft(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val priceRule = PriceRule(
            name = "Descuento 10%",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.9
        )

        viewModel.applyPriceRuleToDraftItem(product.id, priceRule)
        testDispatcher.scheduler.advanceUntilIdle()

        val draftItem = viewModel.draftItems.value.first()
        assertTrue(draftItem.isDiscounted)
        assertEquals("Descuento 10%", draftItem.appliedRuleName)
        assertEquals(product.price * 0.9, draftItem.unitPrice, 0.001)

        // Remove rule
        viewModel.removePriceRuleFromDraftItem(product.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val resetItem = viewModel.draftItems.value.first()
        assertFalse(resetItem.isDiscounted)
        assertNull(resetItem.appliedRuleName)
        assertEquals(product.price, resetItem.unitPrice, 0.001)
    }

    @Test
    fun applyPriceRuleToAllDraftItems_appliesRuleToAllItems() = runTest {
        val products = repository.products.value
        viewModel.addProductToDraft(products[0])
        viewModel.addProductToDraft(products[1])
        testDispatcher.scheduler.advanceUntilIdle()

        val priceRule = PriceRule(
            name = "Oferta Especial",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.SUBTRACT,
            value = 0.5
        )

        viewModel.applyPriceRuleToAllDraftItems(priceRule)
        testDispatcher.scheduler.advanceUntilIdle()

        val draftItems = viewModel.draftItems.value
        assertEquals(2, draftItems.size)
        assertTrue(draftItems[0].isDiscounted)
        assertTrue(draftItems[1].isDiscounted)
        assertEquals("Oferta Especial", draftItems[0].appliedRuleName)
        assertEquals("Oferta Especial", draftItems[1].appliedRuleName)
    }

    @Test
    fun saveQuotation_savesRecordAndResetsForm() = runTest {
        val defaultCustomer = repository.customers.value.find { it.isDefault } ?: repository.customers.value.first()
        val product = repository.products.value.first()
        viewModel.setCustomerName("Carlos Martinez")
        viewModel.setCustomerEmail("carlos@empresa.com")
        viewModel.addProductToDraft(product)
        testDispatcher.scheduler.advanceUntilIdle()

        val savedQuotation = viewModel.saveQuotation()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(savedQuotation)
        assertEquals("Carlos Martinez", savedQuotation?.customerName)
        assertEquals("carlos@empresa.com", savedQuotation?.customerEmail)

        // Form should be reset to default customer
        assertEquals(defaultCustomer.name, viewModel.customerNameInput.value)
        assertEquals("", viewModel.customerEmailInput.value)
        assertTrue(viewModel.draftItems.value.isEmpty())

        // Repository should contain new quotation
        val allQuotations = viewModel.allQuotations.value
        assertEquals(2, allQuotations.size)
    }

    @Test
    fun saveQuotation_failsIfNoCustomerNameOrItems() = runTest {
        viewModel.setCustomerName("")
        assertNull(viewModel.saveQuotation())

        viewModel.setCustomerName("Cliente Valido")
        // No items in draft
        assertNull(viewModel.saveQuotation())
    }

    @Test
    fun searchQuery_filtersQuotationsByCustomerOrProduct() = runTest {
        viewModel.onSearchQueryChanged("Cliente General")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredQuotations.value.size)

        viewModel.onSearchQueryChanged("Inexistente")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, viewModel.filteredQuotations.value.size)
    }

    @Test
    fun deleteQuotation_removesQuotationFromRepository() = runTest {
        val existing = viewModel.allQuotations.value.first()
        viewModel.deleteQuotation(existing.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val remaining = viewModel.allQuotations.value
        assertEquals(0, remaining.size)
        assertNull(viewModel.getQuotationById(existing.id))
    }

    @Test
    fun loadQuotationForEdit_populatesFormFields() = runTest {
        val existing = viewModel.allQuotations.value.first()
        viewModel.loadQuotationForEdit(existing)

        assertEquals(existing.id, viewModel.editingQuotationId.value)
        assertEquals(existing.customerName, viewModel.customerNameInput.value)
        assertEquals(existing.customerEmail ?: "", viewModel.customerEmailInput.value)
        assertEquals(existing.items.size, viewModel.draftItems.value.size)
    }

    @Test
    fun setExpirationDaysFromToday_updatesExpirationDateMillis() = runTest {
        viewModel.setExpirationDaysFromToday(7)
        testDispatcher.scheduler.advanceUntilIdle()

        val cal = Calendar.getInstance().apply {
            timeInMillis = viewModel.expirationDateMillis.value
        }
        val expectedTarget = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
        }

        assertEquals(expectedTarget.get(Calendar.YEAR), cal.get(Calendar.YEAR))
        assertEquals(expectedTarget.get(Calendar.DAY_OF_YEAR), cal.get(Calendar.DAY_OF_YEAR))
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, cal.get(Calendar.MINUTE))
    }

    @Test
    fun itemSelection_toggleAndSelectAllAndClear() = runTest {
        val products = repository.products.value
        viewModel.addProductToDraft(products[0])
        viewModel.addProductToDraft(products[1])
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSelectQuotationItem(products[0].id)
        assertEquals(setOf(products[0].id), viewModel.selectedQuotationItemIds.value)

        viewModel.selectAllQuotationItems()
        assertEquals(setOf(products[0].id, products[1].id), viewModel.selectedQuotationItemIds.value)

        viewModel.clearQuotationItemSelection()
        assertTrue(viewModel.selectedQuotationItemIds.value.isEmpty())
    }

    @Test
    fun togglePriceRule_appliesToSelectedItemsOnlyWhenSelected() = runTest {
        val products = repository.products.value
        viewModel.addProductToDraft(products[0])
        viewModel.addProductToDraft(products[1])
        testDispatcher.scheduler.advanceUntilIdle()

        val rule = PriceRule(
            id = "rule-1",
            name = "Rule 10% Off",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.9
        )

        viewModel.toggleSelectQuotationItem(products[0].id)
        viewModel.togglePriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()

        val items = viewModel.draftItems.value
        assertTrue(items.find { it.productId == products[0].id }?.isDiscounted == true)
        assertFalse(items.find { it.productId == products[1].id }?.isDiscounted == true)
        assertEquals("rule-1", viewModel.selectedPriceRuleId.value)
    }

    @Test
    fun togglePriceRule_appliesToAllItemsAndTogglesOff() = runTest {
        val products = repository.products.value
        viewModel.addProductToDraft(products[0])
        viewModel.addProductToDraft(products[1])
        testDispatcher.scheduler.advanceUntilIdle()

        val rule = PriceRule(
            id = "rule-1",
            name = "Rule 10% Off",
            baseVariable = BaseVariable.PRICE,
            operator = ArithmeticOperator.MULTIPLY,
            value = 0.9
        )

        // No selection -> applies to all
        viewModel.togglePriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()

        var items = viewModel.draftItems.value
        assertTrue(items[0].isDiscounted)
        assertTrue(items[1].isDiscounted)

        // Toggle rule again -> removes rule from all
        viewModel.togglePriceRule(rule)
        testDispatcher.scheduler.advanceUntilIdle()

        items = viewModel.draftItems.value
        assertFalse(items[0].isDiscounted)
        assertFalse(items[1].isDiscounted)
        assertNull(viewModel.selectedPriceRuleId.value)
    }

    @Test
    fun handleCustomerFallback_whenInputIsBlank_selectsDefaultCustomer() = runTest {
        val defaultCustomer = repository.customers.value.find { it.isDefault } ?: repository.customers.value.first()
        viewModel.setCustomerName("")
        viewModel.handleCustomerFallback()

        assertEquals(defaultCustomer.name, viewModel.customerNameInput.value)
        assertEquals(defaultCustomer.id, viewModel.selectedCustomer.value?.id)
    }

    @Test
    fun handleCustomerFallback_whenNoCustomerMatches_selectsDefaultCustomer() = runTest {
        val defaultCustomer = repository.customers.value.find { it.isDefault } ?: repository.customers.value.first()
        viewModel.setCustomerName("Inexistente 123")
        viewModel.handleCustomerFallback()

        assertEquals(defaultCustomer.name, viewModel.customerNameInput.value)
        assertEquals(defaultCustomer.id, viewModel.selectedCustomer.value?.id)
    }

    @Test
    fun handleCustomerFallback_whenCustomerMatches_selectsMatchingCustomer() = runTest {
        val matchingCustomer = repository.customers.value.first()
        viewModel.setCustomerName(matchingCustomer.name)
        viewModel.handleCustomerFallback()

        assertEquals(matchingCustomer.name, viewModel.customerNameInput.value)
        assertEquals(matchingCustomer.id, viewModel.selectedCustomer.value?.id)
    }

    @Test
    fun taxBreakdown_calculatesTaxesAndSavesCorrectly() = runTest {
        val product = repository.products.value.first() // Price = 2.5
        viewModel.addProductToDraft(product)
        viewModel.setCustomerName("Test Tax Customer")
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially showTaxBreakdown is false
        assertFalse(viewModel.showTaxBreakdown.value)
        assertEquals(0.0, viewModel.calculatedTaxAmount.value, 0.001)
        assertEquals(2.5, viewModel.totalAmount.value, 0.001)

        // Enable showTaxBreakdown -> active tax in repository is IVA 16%
        viewModel.setShowTaxBreakdown(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.showTaxBreakdown.value)
        val expectedTax = 2.5 - (2.5 / 1.16)
        assertEquals(expectedTax, viewModel.calculatedTaxAmount.value, 0.001)
        assertEquals(2.5, viewModel.totalAmount.value, 0.001)

        // Save quotation with showTaxBreakdown = true
        val savedQuotation = viewModel.saveQuotation()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(savedQuotation)
        assertTrue(savedQuotation!!.showTaxBreakdown)
        assertEquals(expectedTax, savedQuotation.taxAmount, 0.001)
        assertEquals(2.5, savedQuotation.totalAmount, 0.001)

        // Loading for edit restores showTaxBreakdown
        viewModel.loadQuotationForEdit(savedQuotation)
        assertTrue(viewModel.showTaxBreakdown.value)

        // Reset form resets showTaxBreakdown
        viewModel.resetForm()
        assertFalse(viewModel.showTaxBreakdown.value)
    }

    @Test
    fun activeTaxLabel_formatsTaxNameAndValueCorrectly() = runTest {
        assertEquals("IVA (16%)", viewModel.activeTaxLabel.value)
    }

    @Test
    fun printOptions_canBeToggledAndSavedAndLoaded() = runTest {
        val product = repository.products.value.first()
        viewModel.addProductToDraft(product)
        viewModel.setCustomerName("Print Options Customer")
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially false
        assertFalse(viewModel.showSignatureBlock.value)
        assertFalse(viewModel.showStampBlock.value)
        assertFalse(viewModel.showContactBlock.value)

        // Toggle
        viewModel.setShowSignatureBlock(true)
        viewModel.setShowStampBlock(true)
        viewModel.setShowContactBlock(true)
        
        assertTrue(viewModel.showSignatureBlock.value)
        assertTrue(viewModel.showStampBlock.value)
        assertTrue(viewModel.showContactBlock.value)

        val savedQuotation = viewModel.saveQuotation()
        assertNotNull(savedQuotation)
        assertTrue(savedQuotation!!.showSignatureBlock)
        assertTrue(savedQuotation.showStampBlock)
        assertTrue(savedQuotation.showContactBlock)

        // Reset
        assertFalse(viewModel.showSignatureBlock.value)

        // Load restores values
        viewModel.loadQuotationForEdit(savedQuotation)
        assertTrue(viewModel.showSignatureBlock.value)
        assertTrue(viewModel.showStampBlock.value)
        assertTrue(viewModel.showContactBlock.value)
    }
}
