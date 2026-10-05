package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.ArithmeticOperator
import com.lopezapp.movilpos.data.model.BaseVariable
import com.lopezapp.movilpos.data.model.BusinessInfo
import com.lopezapp.movilpos.data.model.DteEnvironment
import com.lopezapp.movilpos.data.model.ElectronicBillingConfig
import com.lopezapp.movilpos.data.model.PriceRule
import com.lopezapp.movilpos.data.model.Role
import com.lopezapp.movilpos.data.model.TaxValueType
import com.lopezapp.movilpos.data.model.TicketConfig
import com.lopezapp.movilpos.data.model.TicketPaperSize
import com.lopezapp.movilpos.data.model.User
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
        assertEquals(4, state.paymentMethods.size)
        assertEquals("Efectivo", state.paymentMethods[0].name)
        assertTrue(state.paymentMethods[0].isDefault)
        assertEquals("Tarjeta de Crédito / Débito", state.paymentMethods[1].name)
        assertEquals("Transferencia Bancaria", state.paymentMethods[2].name)
        assertEquals("Cheque", state.paymentMethods[3].name)
        assertEquals("Mi Negocio", state.businessInfo.name)
        assertEquals("", state.businessInfo.nit)
        assertEquals("", state.businessInfo.nrc)
        assertEquals("", state.businessInfo.address)
        assertEquals("", state.businessInfo.phone)
        assertEquals("", state.businessInfo.socialMedia)
        assertNull(state.businessInfo.logoUri)
        assertTrue(state.ticketConfig.showBusinessName)
        assertTrue(state.ticketConfig.showNit)
        assertTrue(state.ticketConfig.showNrc)
        assertTrue(state.ticketConfig.showAddress)
        assertTrue(state.ticketConfig.showPhone)
        assertTrue(state.ticketConfig.showSocialMedia)
        assertTrue(state.ticketConfig.showLogo)
        assertEquals("¡Gracias por su compra! Vuelva pronto.", state.ticketConfig.footerMessage)
        assertEquals(TicketPaperSize.SIZE_80MM, state.ticketConfig.paperSize)
    }

    @Test
    fun usersAndShiftHistory_exposedInStateAndProperties() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(1, state.users.size)
        assertEquals("admin_1", state.users.first().id)
        assertEquals(1, viewModel.users.value.size)

        assertTrue(state.shiftHistory.isEmpty())
        assertTrue(viewModel.shiftHistory.value.isEmpty())
    }

    @Test
    fun userCrudAndValidateAdminPin_worksCorrectly() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.validateAdminPin("1234"))
        assertFalse(viewModel.validateAdminPin("9999"))

        val newUser = User(id = "u_test", name = "Test User", pin = "8888", role = Role.SUPERVISOR)
        viewModel.addUser(newUser)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.users.value.size)
        assertTrue(viewModel.validateAdminPin("8888"))

        val updatedUser = newUser.copy(name = "Updated User")
        viewModel.updateUser(updatedUser)
        testDispatcher.scheduler.advanceUntilIdle()

        val fetched = viewModel.users.value.find { it.id == "u_test" }
        assertEquals("Updated User", fetched?.name)

        viewModel.deleteUser("u_test")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.users.value.size)
        assertFalse(viewModel.validateAdminPin("8888"))
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
    fun addPaymentMethod_addsNewMethodToViewModelAndRepository() = runTest {
        viewModel.addPaymentMethod("Criptomonedas", isDefault = false)
        testDispatcher.scheduler.advanceUntilIdle()

        val methods = viewModel.uiState.value.paymentMethods
        val addedMethod = methods.find { it.name == "Criptomonedas" }

        assertNotNull(addedMethod)
        assertFalse(addedMethod?.isDefault ?: true)
    }

    @Test
    fun updatePaymentMethod_updatesMethodInViewModelAndRepository() = runTest {
        val initialMethod = viewModel.uiState.value.paymentMethods.first()
        viewModel.updatePaymentMethod(initialMethod.id, "Efectivo USD", isDefault = true)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedMethod = viewModel.uiState.value.paymentMethods.find { it.id == initialMethod.id }
        assertNotNull(updatedMethod)
        assertEquals("Efectivo USD", updatedMethod?.name)
        assertTrue(updatedMethod?.isDefault ?: false)
    }

    @Test
    fun deletePaymentMethod_removesMethodFromViewModelAndRepository() = runTest {
        val initialMethod = viewModel.uiState.value.paymentMethods.first()
        viewModel.deletePaymentMethod(initialMethod.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val deletedMethod = viewModel.uiState.value.paymentMethods.find { it.id == initialMethod.id }
        assertNull(deletedMethod)
    }

    @Test
    fun updateBusinessInfo_updatesBusinessInfoInViewModelAndRepository() = runTest {
        val newBusinessInfo = BusinessInfo(
            name = "Tienda El Sol",
            nit = "06141508901011",
            nrc = "1234567",
            address = "Calle Principal #123, San Salvador",
            phone = "22223333",
            socialMedia = "@tiendaelsol",
            logoUri = "content://media/external/images/media/1"
        )
        viewModel.updateBusinessInfo(newBusinessInfo)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedStateInfo = viewModel.uiState.value.businessInfo
        assertEquals("Tienda El Sol", updatedStateInfo.name)
        assertEquals("06141508901011", updatedStateInfo.nit)
        assertEquals("1234567", updatedStateInfo.nrc)
        assertEquals("Calle Principal #123, San Salvador", updatedStateInfo.address)
        assertEquals("22223333", updatedStateInfo.phone)
        assertEquals("@tiendaelsol", updatedStateInfo.socialMedia)
        assertEquals("content://media/external/images/media/1", updatedStateInfo.logoUri)

        val updatedRepoInfo = repository.businessInfo.value
        assertEquals("Tienda El Sol", updatedRepoInfo.name)
        assertEquals("06141508901011", updatedRepoInfo.nit)
    }

    @Test
    fun updateTicketConfig_updatesTicketConfigInViewModelAndRepository() = runTest {
        val newTicketConfig = TicketConfig(
            showBusinessName = true,
            showNit = false,
            showNrc = false,
            showAddress = true,
            showPhone = true,
            showSocialMedia = false,
            showLogo = true,
            footerMessage = "Gracias por preferirnos",
            paperSize = TicketPaperSize.SIZE_57MM
        )
        viewModel.updateTicketConfig(newTicketConfig)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedStateConfig = viewModel.uiState.value.ticketConfig
        assertTrue(updatedStateConfig.showBusinessName)
        assertFalse(updatedStateConfig.showNit)
        assertFalse(updatedStateConfig.showNrc)
        assertTrue(updatedStateConfig.showAddress)
        assertTrue(updatedStateConfig.showPhone)
        assertFalse(updatedStateConfig.showSocialMedia)
        assertTrue(updatedStateConfig.showLogo)
        assertEquals("Gracias por preferirnos", updatedStateConfig.footerMessage)
        assertEquals(TicketPaperSize.SIZE_57MM, updatedStateConfig.paperSize)

        val updatedRepoConfig = repository.ticketConfig.value
        assertEquals(TicketPaperSize.SIZE_57MM, updatedRepoConfig.paperSize)
        assertEquals("Gracias por preferirnos", updatedRepoConfig.footerMessage)
    }

    @Test
    fun updateElectronicBillingConfig_updatesConfigInViewModelAndRepository() = runTest {
        val newConfig = ElectronicBillingConfig(
            isEnabled = true,
            environment = DteEnvironment.PRODUCTION,
            nit = "0614-010190-101-2",
            apiToken = "secret_token_123",
            establishmentCode = "0002",
            posCode = "0003",
            economicActivity = "Servicios de restaurantes, cafeterías y servicios móviles de comidas",
            certificatePassword = "cert_password"
        )
        viewModel.updateElectronicBillingConfig(newConfig)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.uiState.value.electronicBillingConfig
        assertTrue(updatedState.isEnabled)
        assertEquals(DteEnvironment.PRODUCTION, updatedState.environment)
        assertEquals("0614-010190-101-2", updatedState.nit)
        assertEquals("secret_token_123", updatedState.apiToken)
        assertEquals("0002", updatedState.establishmentCode)
        assertEquals("0003", updatedState.posCode)
        assertEquals("Servicios de restaurantes, cafeterías y servicios móviles de comidas", updatedState.economicActivity)
        assertEquals("cert_password", updatedState.certificatePassword)

        val updatedRepo = repository.electronicBillingConfig.value
        assertTrue(updatedRepo.isEnabled)
        assertEquals(DteEnvironment.PRODUCTION, updatedRepo.environment)
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
