package com.ayudevices.neosynkparent.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayudevices.neosynkparent.data.repository.ParentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentViewModel @Inject constructor(
    private val parentRepository: ParentRepository
) : ViewModel() {

    fun registerParent() {
        viewModelScope.launch {
            val success = parentRepository.sendParentInfoToServer()
            if (success) {
                // You can update UI state via LiveData or StateFlow
                // Example: _uiState.value = UiState.Success
            } else {
                // Example: _uiState.value = UiState.Error("Failed to register")
            }
        }
    }
}
