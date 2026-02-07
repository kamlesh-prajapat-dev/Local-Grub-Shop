package com.example.localgrubshop.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.localgrubshop.data.models.AdminUser
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDatabase @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {
    companion object {
        private const val USER = "user"
    }

    fun setUser(user: AdminUser) {
        val json = gson.toJson(user)
        sharedPreferences.edit { putString(USER, json) }
    }

    fun getUser(): AdminUser? {
        val json = sharedPreferences.getString(USER, null) ?: return null
        return gson.fromJson(json, AdminUser::class.java)
    }
}