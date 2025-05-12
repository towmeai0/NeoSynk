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
    var loc by mutableStateOf("")
    var gender by mutableStateOf("")

    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("NeoSynk").child("user")

    fun saveUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userProfile = mapOf(
            "id" to userId,
            "name" to name,
            "location" to loc,
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

    fun fetchUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userRef.child(userId).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    name = dataSnapshot.child("name").getValue(String::class.java) ?: ""
                    loc = dataSnapshot.child("location").getValue(String::class.java) ?: ""
                    gender = dataSnapshot.child("gender").getValue(String::class.java) ?: ""
                }
            }
            .addOnFailureListener {
                // Handle fetch error
            }
    }
}

