package com.kkb.purrytify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkb.purrytify.data.model.ProfileResponse
import com.kkb.purrytify.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: ProfileResponse? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private var cachedProfile: ProfileResponse? = null

    fun fetchProfile(token: String, forceRefresh: Boolean = false) {
        if (_uiState.value.isLoading) return
        if (cachedProfile != null && !forceRefresh) {
            _uiState.value = ProfileUiState(profile = cachedProfile)
            return
        }
        _uiState.value = ProfileUiState(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = apiService.getProfile("Bearer $token")
                cachedProfile = profile
                withContext(Dispatchers.Main) {
                    _uiState.value = ProfileUiState(profile = profile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = ProfileUiState(error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}