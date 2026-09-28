package com.lopezapp.movilpos

import com.lopezapp.movilpos.data.model.Customer
import com.lopezapp.movilpos.data.model.DocumentType
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.CustomerViewModel
import com.lopezapp.movilpos.util.ElSalvadorGeography
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
class CustomerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: CustomerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = CustomerViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun customers_initialDataHasDefaultCustomer() = runTest {
        val initialCustomers = viewModel.allCustomers.value
        assertEquals(1, initialCustomers.size)
        val defaultCustomer = initialCustomers[0]
        assertEquals("Cliente General", defaultCustomer.name)
        assertTrue(defaultCustomer.isDefault)
        assertEquals(DocumentType.DUI, defaultCustomer.documentType)
        assertEquals("00000000-0", defaultCustomer.documentNumber)
        assertEquals("San Salvador", defaultCustomer.department)
    }

    @Test
    fun addCustomer_nonDefault_addsCustomerToRepository() = runTest {
        val newCustomer = Customer(
            name = "Juan Pérez",
            documentType = DocumentType.DUI,
            documentNumber = "12345678-9",
            department = "La Libertad",
            municipality = "La Libertad Sur",
            district = "Santa Tecla",
            isDefault = false
        )

        viewModel.addCustomer(newCustomer)
        testDispatcher.scheduler.advanceUntilIdle()

        val customers = viewModel.allCustomers.value
        assertEquals(2, customers.size)
        val fetched = viewModel.getCustomerById(newCustomer.id)
        assertNotNull(fetched)
        assertEquals("Juan Pérez", fetched?.name)

        // Initial customer should still be default
        val generalClient = customers.find { it.name == "Cliente General" }
        assertTrue(generalClient?.isDefault == true)
    }

    @Test
    fun addCustomer_withIsDefault_unmarksPreviousDefault() = runTest {
        val newDefaultCustomer = Customer(
            name = "Empresa ABC S.A. de C.V.",
            documentType = DocumentType.NIT,
            documentNumber = "0614-150820-101-2",
            nrc = "123456-7",
            department = "San Salvador",
            municipality = "San Salvador Centro",
            district = "San Salvador",
            isLargeContributor = true,
            commercialActivity = "Servicios Tecnológicos",
            isDefault = true
        )

        viewModel.addCustomer(newDefaultCustomer)
        testDispatcher.scheduler.advanceUntilIdle()

        val customers = viewModel.allCustomers.value
        assertEquals(2, customers.size)

        val newCust = customers.find { it.id == newDefaultCustomer.id }
        assertTrue(newCust?.isDefault == true)

        val oldCust = customers.find { it.name == "Cliente General" }
        assertFalse(oldCust?.isDefault == true)
    }

    @Test
    fun updateCustomer_updatesDetails() = runTest {
        val initialCustomer = viewModel.allCustomers.value.first()
        val updatedCustomer = initialCustomer.copy(
            phone = "7777-8888",
            address = "Colonia Escalón #123"
        )

        viewModel.updateCustomer(updatedCustomer)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.getCustomerById(initialCustomer.id)
        assertNotNull(result)
        assertEquals("7777-8888", result?.phone)
        assertEquals("Colonia Escalón #123", result?.address)
    }

    @Test
    fun updateCustomer_settingDefaultUnmarksOthers() = runTest {
        val customer2 = Customer(
            name = "María López",
            documentType = DocumentType.DUI,
            documentNumber = "87654321-0",
            department = "Santa Ana",
            municipality = "Santa Ana Centro",
            district = "Santa Ana",
            isDefault = false
        )
        viewModel.addCustomer(customer2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Set customer2 as default
        val updatedCustomer2 = customer2.copy(isDefault = true)
        viewModel.updateCustomer(updatedCustomer2)
        testDispatcher.scheduler.advanceUntilIdle()

        val customers = viewModel.allCustomers.value
        val updatedC2 = customers.find { it.id == customer2.id }
        assertTrue(updatedC2?.isDefault == true)

        val generalClient = customers.find { it.name == "Cliente General" }
        assertFalse(generalClient?.isDefault == true)
    }

    @Test
    fun deleteCustomer_removesFromRepository() = runTest {
        val newCustomer = Customer(
            name = "Carlos Ruiz",
            documentType = DocumentType.DUI,
            documentNumber = "11223344-5",
            department = "Sonsonate",
            municipality = "Sonsonate Centro",
            district = "Sonsonate",
            isDefault = false
        )
        viewModel.addCustomer(newCustomer)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteCustomer(newCustomer.id)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.getCustomerById(newCustomer.id))
    }

    @Test
    fun setDefaultCustomer_changesDefault() = runTest {
        val newCustomer = Customer(
            name = "Ana Gómez",
            documentType = DocumentType.DUI,
            documentNumber = "99887766-5",
            department = "San Miguel",
            municipality = "San Miguel Centro",
            district = "San Miguel",
            isDefault = false
        )
        viewModel.addCustomer(newCustomer)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setDefaultCustomer(newCustomer.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val customers = viewModel.allCustomers.value
        val ana = customers.find { it.id == newCustomer.id }
        assertTrue(ana?.isDefault == true)

        val general = customers.find { it.name == "Cliente General" }
        assertFalse(general?.isDefault == true)
    }

    @Test
    fun searchQuery_updatesSearchState() = runTest {
        viewModel.onSearchQueryChanged("Cliente")
        assertEquals("Cliente", viewModel.searchQuery.value)
    }

    @Test
    fun elSalvadorGeography_has14DepartmentsAndValidData() {
        val departments = ElSalvadorGeography.departments
        assertEquals(14, departments.size)
        assertTrue(departments.contains("San Salvador"))
        assertTrue(departments.contains("La Libertad"))
        assertTrue(departments.contains("Santa Ana"))
        assertTrue(departments.contains("San Miguel"))
        assertTrue(departments.contains("Sonsonate"))

        val sanSalvadorMunicipalities = ElSalvadorGeography.getMunicipalities("San Salvador")
        assertTrue(sanSalvadorMunicipalities.isNotEmpty())
        assertTrue(sanSalvadorMunicipalities.contains("San Salvador Centro"))

        val districts = ElSalvadorGeography.getDistricts("San Salvador", "San Salvador Centro")
        assertTrue(districts.isNotEmpty())
        assertTrue(districts.contains("San Salvador"))
        assertTrue(districts.contains("Mejicanos"))
    }
}
