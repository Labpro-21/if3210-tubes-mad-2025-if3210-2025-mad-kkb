package com.kkb.purrytify.viewmodel

import android.content.Context
import android.net.Uri
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val profile: ProfileResponse? = null,
    val error: String? = null,
    val isUpdateSuccessful: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState

    fun loadProfile(token: String) {
        if (_uiState.value.isLoading) return

        _uiState.value = EditProfileUiState(isLoading = true)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = apiService.getProfile("Bearer $token")
                withContext(Dispatchers.Main) {
                    _uiState.value = EditProfileUiState(profile = profile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = EditProfileUiState(error = e.message ?: "Failed to load profile")
                }
            }
        }
    }

    fun updateProfile(
        token: String,
        location: String?,
        profilePhotoUri: Uri?,
        context: Context
    ) {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Prepare multipart request
                val parts = mutableListOf<MultipartBody.Part>()

                // Add location if provided
                location?.let { loc ->
                    if (loc.isNotEmpty()) {
                        val locationBody = loc.toRequestBody("text/plain".toMediaTypeOrNull())
                        parts.add(MultipartBody.Part.createFormData("location", null, locationBody))
                    }
                }

                // Add profile photo if provided
                profilePhotoUri?.let { uri ->
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        inputStream?.let { stream ->
                            // Create a temporary file
                            val tempFile = File(context.cacheDir, "temp_profile_photo.jpg")
                            val outputStream = FileOutputStream(tempFile)

                            stream.copyTo(outputStream)
                            stream.close()
                            outputStream.close()

                            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                            parts.add(
                                MultipartBody.Part.createFormData(
                                    "profilePhoto",
                                    tempFile.name,
                                    requestFile
                                )
                            )
                        }
                    } catch (e: Exception) {
                        throw Exception("Failed to process image: ${e.message}")
                    }
                }

                // Make API call
                val updatedProfile = apiService.updateProfile("Bearer $token", parts)

                withContext(Dispatchers.Main) {
                    _uiState.value = EditProfileUiState(
                        profile = updatedProfile,
                        isUpdateSuccessful = true
                    )
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = when {
                            e.message?.contains("400") == true -> "Invalid data provided. Please check your inputs."
                            e.message?.contains("401") == true -> "Authentication failed. Please login again."
                            e.message?.contains("413") == true -> "Image file is too large. Please choose a smaller image."
                            e.message?.contains("422") == true -> "Invalid country code. Please use 2-letter ISO country codes (e.g., US, ID, UK)."
                            e.message?.contains("network") == true -> "Network error. Please check your connection."
                            else -> e.message ?: "Failed to update profile. Please try again."
                        }
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}