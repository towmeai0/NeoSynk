package com.ayudevices.neosynkparent.viewmodel


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    var name by mutableStateOf("")
    var Loc by mutableStateOf("")
    var gender by mutableStateOf("")

    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("NeoSynk")

    fun saveUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userProfile = mapOf(
            "name" to name,
            "location" to Loc,
            "gender" to gender
        )

        userRef.child(userId).setValue(userProfile)
            .addOnSuccessListener {
                // Profile saved successfully
            }
            .addOnFailureListener {
                // Handle error
            }
    }
}
