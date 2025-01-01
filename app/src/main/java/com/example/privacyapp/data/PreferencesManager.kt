package com.example.privacyapp.data

import android.content.Context

class PreferencesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("privacy_app_prefs", Context.MODE_PRIVATE)

    fun savePin(pin: String) {
        sharedPreferences.edit()
            .putString("app_pin", pin)
            .apply()
    }

    fun getPin(): String {
        return sharedPreferences.getString("app_pin", "1234") ?: "1234"
    }
}