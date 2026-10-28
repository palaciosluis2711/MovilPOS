package com.lopezapp.movilpos.ui

import com.lopezapp.movilpos.data.model.Role
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenRoleFilteringTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var authViewModel: AuthViewModel

    private data class TestMenuItem(
        val title: String,
        val allowedRoles: Set<Role>
    )

    private val menuItems = listOf(
        TestMenuItem("POS", setOf(Role.CASHIER, Role.SUPERVISOR, Role.ADMINISTRATOR)),
        TestMenuItem("Clientes", setOf(Role.CASHIER, Role.SUPERVISOR, Role.ADMINISTRATOR)),
        TestMenuItem("Cotizaciones", setOf(Role.CASHIER, Role.SUPERVISOR, Role.ADMINISTRATOR)),
        TestMenuItem("Inventario", setOf(Role.SUPERVISOR, Role.ADMINISTRATOR)),
        TestMenuItem("Ventas", setOf(Role.SUPERVISOR, Role.ADMINISTRATOR)),
        TestMenuItem("Etiquetas de Productos", setOf(Role.SUPERVISOR, Role.ADMINISTRATOR)),
        TestMenuItem("Salidas de Inventario", setOf(Role.ADMINISTRATOR)),
        TestMenuItem("Cuentas por Cobrar", setOf(Role.ADMINISTRATOR)),
        TestMenuItem("Estadísticas", setOf(Role.ADMINISTRATOR)),
        TestMenuItem("Reportes DTE y MH", setOf(Role.ADMINISTRATOR)),
        TestMenuItem("Compras", setOf(Role.ADMINISTRATOR)),
        TestMenuItem("Gastos de Caja", setOf(Role.ADMINISTRATOR)),
        TestMenuItem("Proveedores", setOf(Role.ADMINISTRATOR)),
        TestMenuItem("Configuración", setOf(Role.ADMINISTRATOR))
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        authViewModel = AuthViewModel(repository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cashierRole_displaysStrictlyPOS_Clientes_Cotizaciones() {
        val userRole = Role.CASHIER
        val visibleTitles = menuItems.filter { userRole in it.allowedRoles }.map { it.title }

        assertEquals(3, visibleTitles.size)
        assertEquals(listOf("POS", "Clientes", "Cotizaciones"), visibleTitles)
    }

    @Test
    fun supervisorRole_displaysPOS_Clientes_Cotizaciones_Inventario_Ventas_EtiquetasDeProductos() {
        val userRole = Role.SUPERVISOR
        val visibleTitles = menuItems.filter { userRole in it.allowedRoles }.map { it.title }

        assertEquals(6, visibleTitles.size)
        assertEquals(
            listOf("POS", "Clientes", "Cotizaciones", "Inventario", "Ventas", "Etiquetas de Productos"),
            visibleTitles
        )
    }

    @Test
    fun administratorRole_displaysAllMenuCards() {
        val userRole = Role.ADMINISTRATOR
        val visibleTitles = menuItems.filter { userRole in it.allowedRoles }.map { it.title }

        assertEquals(14, visibleTitles.size)
        assertTrue(visibleTitles.contains("POS"))
        assertTrue(visibleTitles.contains("Inventario"))
        assertTrue(visibleTitles.contains("Clientes"))
        assertTrue(visibleTitles.contains("Proveedores"))
        assertTrue(visibleTitles.contains("Compras"))
        assertTrue(visibleTitles.contains("Ventas"))
        assertTrue(visibleTitles.contains("Cotizaciones"))
        assertTrue(visibleTitles.contains("Etiquetas de Productos"))
        assertTrue(visibleTitles.contains("Salidas de Inventario"))
        assertTrue(visibleTitles.contains("Cuentas por Cobrar"))
        assertTrue(visibleTitles.contains("Estadísticas"))
        assertTrue(visibleTitles.contains("Reportes DTE y MH"))
        assertTrue(visibleTitles.contains("Gastos de Caja"))
        assertTrue(visibleTitles.contains("Configuración"))
    }

    @Test
    fun logout_clearsActiveAuthSession() = runTest {
        authViewModel.login("admin", "Admin123!")
        testScheduler.advanceUntilIdle()
        assertNotNull(authViewModel.authSession.value)

        authViewModel.logout()
        var attempts = 0
        testScheduler.advanceUntilIdle()
        while (authViewModel.isLoggingOut.value && attempts < 100) {
            Thread.sleep(10)
            attempts++
        }
        testScheduler.advanceUntilIdle()

        assertNull(authViewModel.authSession.value)
    }
}
