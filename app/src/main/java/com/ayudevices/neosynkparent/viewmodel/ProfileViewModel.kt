package com.ayudevices.neosynkparent.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    // Inject your repository or use case here if needed
) : ViewModel() {

    // Simple properties for storing profile data
    private var userName: String = ""
    private var userLocation: String = ""
    private var userGender: String = ""

    fun updateProfile(name: String, location: String, gender: String) {
        userName = name
        userLocation = location
        userGender = gender
        Log.d("ProfileViewModel", "Profile updated - Name: $name, Location: $location, Gender: $gender")
    }

    fun saveUserProfile() {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Saving profile: $userName, $userLocation, $userGender")
                // Implement your profile saving logic here
                // For example, call repository to save user profile
                val profileData = UserProfile(
                    name = userName,
                    location = userLocation,
                    gender = userGender
                )
                // repository.saveProfile(profileData)
                Log.d("ProfileViewModel", "Profile saved successfully")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving profile", e)
            }
        }
    }

    // Getters if needed
    fun getName() = userName
    fun getLocation() = userLocation
    fun getGender() = userGender
}

// Data class for user profile
data class UserProfile(
    val name: String,
    val location: String,
    val gender: String
)