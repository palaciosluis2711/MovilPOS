package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lopezapp.movilpos.data.model.AuthSession
import com.lopezapp.movilpos.data.model.User
import com.lopezapp.movilpos.data.repository.AppRepository
import com.lopezapp.movilpos.util.FirebaseSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class AuthViewModel(
    private val repository: AppRepository
) : ViewModel() {

    val authSession: StateFlow<AuthSession?> = repository.authSession

    private val _loginUsernameStr = MutableStateFlow("")
    val loginUsernameStr: StateFlow<String> = _loginUsernameStr.asStateFlow()

    private val _loginPasswordStr = MutableStateFlow("")
    val loginPasswordStr: StateFlow<String> = _loginPasswordStr.asStateFlow()

    val rememberMe: StateFlow<Boolean> = repository.rememberMe

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    private val _loadingMessage = MutableStateFlow("Iniciando sesión...")
    val loadingMessage: StateFlow<String> = _loadingMessage.asStateFlow()

    fun onLoginUsernameChanged(username: String) {
        _loginUsernameStr.value = username
    }

    fun onLoginPasswordChanged(password: String) {
        _loginPasswordStr.value = password
    }

    fun setLoginUsername(username: String) {
        _loginUsernameStr.value = username
    }

    fun setLoginPassword(password: String) {
        _loginPasswordStr.value = password
    }

    fun onRememberMeChanged(remember: Boolean) {
        repository.setRememberMe(remember)
    }

    fun setRememberMe(remember: Boolean) {
        repository.setRememberMe(remember)
    }

    suspend fun login(
        usernameOrEmail: String = _loginUsernameStr.value,
        password: String = _loginPasswordStr.value,
        remember: Boolean = rememberMe.value
    ): Result<User> {
        val validationResult = repository.validateCredentials(usernameOrEmail, password)
        if (validationResult.isFailure) {
            _errorMessage.value = validationResult.exceptionOrNull()?.message ?: "Error de autenticación"
            return validationResult
        }

        _isLoading.value = true
        _errorMessage.value = null
        _loadingMessage.value = "Verificando y descargando datos de la nube..."

        try {
            withContext(Dispatchers.IO) {
                FirebaseSyncService.performInitialSync(repository)
            }
        } catch (_: Exception) {
            // Ignore or handle sync exception during login
        }

        _loadingMessage.value = "Iniciando sesión..."

        val result = withContext(Dispatchers.IO) {
            repository.login(usernameOrEmail, password, remember)
        }
            
        if (result.isSuccess) {
            _loginUsernameStr.value = ""
            _loginPasswordStr.value = ""
            _errorMessage.value = null
            _isLoading.value = false
        } else {
            _errorMessage.value = result.exceptionOrNull()?.message ?: "Error de autenticación"
            _isLoading.value = false
        }
        
        return result
    }

    fun logout() {
        viewModelScope.launch {
            _isLoggingOut.value = true
            withContext(Dispatchers.IO) {
                repository.logout()
            }
            _loginUsernameStr.value = ""
            _loginPasswordStr.value = ""
            _errorMessage.value = null
            _isLoggingOut.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
