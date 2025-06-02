package com.ayudevices.neosynkparent.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneResponseEntity
import com.ayudevices.neosynkparent.data.model.MileStoneDataResponse
import com.ayudevices.neosynkparent.data.repository.MilestoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.forEach
import javax.inject.Inject


@HiltViewModel
class MilestoneViewModel @Inject constructor(
    private val repository: MilestoneRepository
) : ViewModel() {

    // Progress tracking
    private val _overallProgress = MutableStateFlow(0.0)
    val overallProgress: StateFlow<Double> = _overallProgress

    private val _motorProgress = MutableStateFlow(0.0)
    val motorProgress: StateFlow<Double> = _motorProgress

    private val _sensoryProgress = MutableStateFlow(0.0)
    val sensoryProgress: StateFlow<Double> = _sensoryProgress

    private val _cognitiveProgress = MutableStateFlow(0.0)
    val cognitiveProgress: StateFlow<Double> = _cognitiveProgress

    private val _feedingProgress = MutableStateFlow(0.0)
    val feedingProgress: StateFlow<Double> = _feedingProgress

    private val _milestoneReport = MutableStateFlow("")
    val milestoneReport: StateFlow<String> = _milestoneReport

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ADD THIS - Complete milestone data
    private val _milestoneData = MutableStateFlow<MileStoneDataResponse?>(null)
    val milestoneData: StateFlow<MileStoneDataResponse?> = _milestoneData

    fun fetchMilestoneData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.fetchMilestoneData(userId)
                .onSuccess { milestoneDataResponse ->
                    _milestoneData.value = milestoneDataResponse // Store complete data
                    updateProgressData(milestoneDataResponse)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Unknown error occurred"
                }

            _isLoading.value = false
        }
    }

    private fun updateProgressData(data: MileStoneDataResponse) {
        val results = data.milestone_results

        _motorProgress.value = results.Motor.percentage
        _sensoryProgress.value = results.Sensory.percentage
        _cognitiveProgress.value = results.Cognitive.percentage
        _feedingProgress.value = results.Feeding.percentage
        _milestoneReport.value = data.milestone_report

        // Calculate overall progress as average
        _overallProgress.value = (
                results.Motor.percentage +
                        results.Sensory.percentage +
                        results.Cognitive.percentage +
                        results.Feeding.percentage
                ) / 4.0
    }

    fun clearError() {
        _error.value = null
    }
}