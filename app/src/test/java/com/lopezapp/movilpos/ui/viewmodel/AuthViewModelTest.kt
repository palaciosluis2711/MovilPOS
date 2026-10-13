package com.lopezapp.movilpos.ui.viewmodel

import com.lopezapp.movilpos.data.model.Role
import com.lopezapp.movilpos.data.model.User
import com.lopezapp.movilpos.data.repository.AppRepository
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
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: AppRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = AppRepository()
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasNoAuthSessionAndEmptyFields() {
        assertNull(viewModel.authSession.value)
        assertEquals("", viewModel.loginUsernameStr.value)
        assertEquals("", viewModel.loginPasswordStr.value)
        assertNull(viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun inputChanges_updatesUsernameAndPasswordStr() {
        viewModel.onLoginUsernameChanged("admin")
        viewModel.onLoginPasswordChanged("Admin123!")

        assertEquals("admin", viewModel.loginUsernameStr.value)
        assertEquals("Admin123!", viewModel.loginPasswordStr.value)

        viewModel.setLoginUsername("User2")
        viewModel.setLoginPassword("Pass5678")

        assertEquals("User2", viewModel.loginUsernameStr.value)
        assertEquals("Pass5678", viewModel.loginPasswordStr.value)
    }

    @Test
    fun login_withValidCredentials_updatesAuthSessionAndClearsFields() = runTest {
        viewModel.onLoginUsernameChanged("admin")
        viewModel.onLoginPasswordChanged("Admin123!")

        val result = viewModel.login()

        assertTrue(result.isSuccess)
        assertNotNull(viewModel.authSession.value)
        assertEquals("Administrador", viewModel.authSession.value?.user?.name)
        assertTrue(viewModel.authSession.value?.isLoggedIn == true)
        assertEquals("", viewModel.loginUsernameStr.value)
        assertEquals("", viewModel.loginPasswordStr.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun login_withInvalidCredentials_setsErrorMessage() = runTest {
        viewModel.onLoginUsernameChanged("UsuarioInvalido")
        viewModel.onLoginPasswordChanged("0000")

        val result = viewModel.login()

        assertTrue(result.isFailure)
        assertNull(viewModel.authSession.value)
        assertEquals("Usuario o contraseña incorrectos", viewModel.errorMessage.value)
    }

    @Test
    fun login_withParameters_succeeds() = runTest {
        val user = User(id = "c1", name = "Juan", username = "juan", pin = "9999", passwordHash = "Pass999!", role = Role.CASHIER)
        repository.addUser(user)

        val result = viewModel.login("juan", "Pass999!")

        assertTrue(result.isSuccess)
        assertEquals("Juan", viewModel.authSession.value?.user?.name)
    }

    @Test
    fun logout_clearsAuthSession() = runTest {
        viewModel.login("admin", "Admin123!")
        testScheduler.advanceUntilIdle()
        assertNotNull(viewModel.authSession.value)

        viewModel.logout()
        var timeout = 0
        while(viewModel.authSession.value != null && timeout < 200) {
            kotlinx.coroutines.delay(10)
            timeout++
        }

        assertNull(viewModel.authSession.value)
        assertEquals("", viewModel.loginUsernameStr.value)
        assertEquals("", viewModel.loginPasswordStr.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun clearError_removesErrorMessage() = runTest {
        viewModel.login("wrong", "wrong")
        assertNotNull(viewModel.errorMessage.value)

        viewModel.clearError()

        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun rememberMe_canBeToggled() {
        assertTrue(viewModel.rememberMe.value)

        viewModel.onRememberMeChanged(false)
        assertFalse(viewModel.rememberMe.value)

        viewModel.setRememberMe(true)
        assertTrue(viewModel.rememberMe.value)
    }

    @Test
    fun login_withEmail_succeeds() = runTest {
        val result = viewModel.login("admin@minegocio.com", "Admin123!")

        assertTrue(result.isSuccess)
        assertNotNull(viewModel.authSession.value)
        assertEquals("admin_1", viewModel.authSession.value?.user?.id)
    }

    @Test
    fun factory_createsViewModelCorrectly() {
        val factory = AuthViewModel.Factory(repository)
        val createdViewModel = factory.create(AuthViewModel::class.java)
        assertNotNull(createdViewModel)
    }
}
