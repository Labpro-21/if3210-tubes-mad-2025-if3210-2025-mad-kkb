package com.kkb.purrytify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkb.purrytify.data.model.LoginRequest
import com.kkb.purrytify.data.model.LoginResponse
import com.kkb.purrytify.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val loginResponse: LoginResponse? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private var loginJob: Job? = null

    fun login(email: String, password: String) {
        if (_uiState.value.isLoading) return // Prevent multiple logins
        _uiState.value = LoginUiState(isLoading = true)
        loginJob?.cancel()
        loginJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        _uiState.value = LoginUiState(
                            isLoading = false,
                            success = true,
                            loginResponse = response.body()
                        )
                    } else {
                        _uiState.value = LoginUiState(
                            isLoading = false,
                            error = response.errorBody()?.string() ?: "Login failed"
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}