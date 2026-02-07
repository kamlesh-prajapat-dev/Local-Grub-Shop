package com.example.localgrubshop.utils

import android.util.Log
import com.example.localgrubshop.BuildConfig

object AppLogger {

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String, message: String, t: Throwable? = null) {
        Log.e(tag, message, t)
    }
}