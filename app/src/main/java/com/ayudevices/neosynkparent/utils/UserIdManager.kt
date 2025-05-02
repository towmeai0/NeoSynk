package com.ayudevices.neosynkparent.utils

/*
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.UUID
import androidx.core.content.edit

object UserIdManager {

    private const val PREF_NAME = "app_prefs"
    private const val KEY_USER_ID = "unique_user_id"

    fun getUserId(context: Context): String {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val existingId = prefs.getString(KEY_USER_ID, null)
        return existingId ?: UUID.randomUUID().toString().also { newId ->
            prefs.edit() { putString(KEY_USER_ID, newId) }
        }
    }
}

 */
