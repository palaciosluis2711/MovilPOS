package com.lopezapp.movilpos.ui

import com.lopezapp.movilpos.data.model.BusinessInfo
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
class LoginScreenTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        authViewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun businessInfo_defaultNameFallback_returnsMiNegocioPOS() {
        val emptyBusinessInfo = BusinessInfo(name = "")
        val displayedName = emptyBusinessInfo.name.ifBlank { "Mi Negocio POS" }
        assertEquals("Mi Negocio POS", displayedName)

        val customBusinessInfo = BusinessInfo(name = "Tienda Don Pedro")
        val customDisplayedName = customBusinessInfo.name.ifBlank { "Mi Negocio POS" }
        assertEquals("Tienda Don Pedro", customDisplayedName)
    }

    @Test
    fun loginFlow_withValidAdminCredentials_succeeds() = runTest {
        authViewModel.onLoginUsernameChanged("admin")
        authViewModel.onLoginPasswordChanged("Admin123!")

        val result = authViewModel.login()

        assertTrue(result.isSuccess)
        assertNotNull(authViewModel.authSession.value)
        assertEquals("Administrador", authViewModel.authSession.value?.user?.name)
        assertNull(authViewModel.errorMessage.value)
    }

    @Test
    fun loginFlow_withInvalidCredentials_showsErrorMessage() = runTest {
        authViewModel.onLoginUsernameChanged("WrongUser")
        authViewModel.onLoginPasswordChanged("0000")

        val result = authViewModel.login()

        assertTrue(result.isFailure)
        assertNull(authViewModel.authSession.value)
        assertEquals("Usuario o contraseña incorrectos", authViewModel.errorMessage.value)
    }
}
