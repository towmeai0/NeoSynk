package com.ayudevices.neosynkparent.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    var email = mutableStateOf("")
    var name = mutableStateOf("")
    var location = mutableStateOf("")
    var gender = mutableStateOf("")
    var loading = mutableStateOf(true)

    private val databaseRef = FirebaseDatabase.getInstance().getReference("NeoSynk").child("user")

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Log.e("ProfileViewModel", "User not logged in.")
            loading.value = false
            return
        }

        Log.d("ProfileViewModel", "Current UID: $currentUserId")

        databaseRef.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                name.value = snapshot.child("name").getValue(String::class.java) ?: ""
                location.value = snapshot.child("location").getValue(String::class.java) ?: ""
                gender.value = snapshot.child("gender").getValue(String::class.java) ?: ""
                loading.value = false

                Log.d("ProfileViewModel", "Fetched data - Name: ${name.value}, Location: ${location.value}, Gender: ${gender.value}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileViewModel", "Firebase error: ${error.message}")
                loading.value = false
            }
        })
    }

    fun updateProfile(email: String?, name: String, location: String, gender: String) {
        this.email.value = email.toString()
        this.name.value = name
        this.location.value = location
        this.gender.value = gender
    }

    fun saveUserProfile() {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId == null) {
                Log.e("ProfileViewModel", "Cannot save - user not logged in.")
                return@launch
            }

            val profileData = mapOf(
                "email" to email.value,
                "name" to name.value,
                "location" to location.value,
                "gender" to gender.value
            )

            databaseRef.child(currentUserId).setValue(profileData)
                .addOnSuccessListener {
                    Log.d("ProfileViewModel", "Profile saved successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileViewModel", "Failed to save profile", e)
                }
        }
    }
}
