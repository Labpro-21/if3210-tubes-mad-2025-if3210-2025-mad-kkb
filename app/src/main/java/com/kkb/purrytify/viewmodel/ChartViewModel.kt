package com.kkb.purrytify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkb.purrytify.data.model.ChartResponse
import com.kkb.purrytify.data.model.ChartSong
import com.kkb.purrytify.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _chartSongs = MutableStateFlow<List<ChartSong>>(emptyList())
    val chartSongs: StateFlow<List<ChartSong>> = _chartSongs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchGlobalChart() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val response = apiService.getTopGlobal()
                if (response.isSuccessful) {
                    _chartSongs.value = response.body() ?: emptyList()
                } else {
                    _error.value = response.message()
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}