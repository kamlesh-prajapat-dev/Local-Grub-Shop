package com.example.localgrubshop.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalHelper @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val TOKEN = "token"
    }

    fun setToken(token: String) {
        sharedPreferences.edit { putString(TOKEN, token) }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(TOKEN, null)
    }
}