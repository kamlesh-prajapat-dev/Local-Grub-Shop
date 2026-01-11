package com.example.localgrubshop.utils

import android.util.Log
import com.google.android.datatransport.BuildConfig

object AppLogger {
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
}