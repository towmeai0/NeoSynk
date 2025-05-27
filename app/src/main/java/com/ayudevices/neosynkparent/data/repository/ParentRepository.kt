package com.ayudevices.neosynkparent.data.repository


import android.util.Log
import com.ayudevices.neosynkparent.data.model.ParentInfoRequest
import com.ayudevices.neosynkparent.data.network.ApiService
import javax.inject.Inject

class ParentRepository @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) {
    suspend fun sendParentInfoToServer(): Boolean {
        val uid = authRepository.getCurrentUserId()
        val email = authRepository.getCurrentUser()?.email

        if (uid != null && email != null) {
            val request = ParentInfoRequest(
                parent_id = uid,
                email = email
            )
            return try {
                val response = apiService.sendParentInfo(request)
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("ParentRepository", "Error: ${e.message}", e)
                false
            }
        } else {
            Log.e("ParentRepository", "Firebase UID or Email is null")
            return false
        }
    }
}
