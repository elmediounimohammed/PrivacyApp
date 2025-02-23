package com.example.privacyapp.data

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log

class PreferencesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("privacy_app_prefs", Context.MODE_PRIVATE)

    // PIN Management
    fun savePin(pin: String) {
        sharedPreferences.edit()
            .putString("app_pin", pin)
            .apply()
    }

    fun getPin(): String {
        return sharedPreferences.getString("app_pin", "1234") ?: "1234"
    }

    // Face Image Management
    fun setReferenceImageUri(uri: String) {
        try {
            sharedPreferences.edit()
                .putString("reference_image_uri", uri)
                .apply()
        } catch (e: Exception) {
            Log.e("PreferencesManager", "Error saving image URI", e)
        }
    }

    fun getReferenceImageUri(): String? {
        return sharedPreferences.getString("reference_image_uri", null)
    }

    fun clearReferenceImage() {
        sharedPreferences.edit()
            .remove("reference_image_uri")
            .apply()
    }

    fun hasReferenceImage(): Boolean {
        return sharedPreferences.contains("reference_image_uri")
    }

    // Settings Management
    fun setSensitivity(value: Float) {
        sharedPreferences.edit()
            .putFloat("sensitivity", value)
            .apply()
    }

    fun getSensitivity(): Float {
        return sharedPreferences.getFloat("sensitivity", 0.7f)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean("notifications_enabled", enabled)
            .apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("notifications_enabled", true)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean("vibration_enabled", enabled)
            .apply()
    }

    fun isVibrationEnabled(): Boolean {
        return sharedPreferences.getBoolean("vibration_enabled", true)
    }

    fun setAutoStartEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean("auto_start", enabled)
            .apply()
    }

    fun isAutoStartEnabled(): Boolean {
        return sharedPreferences.getBoolean("auto_start", false)
    }

    // First Time Setup Management
//    fun isFirstTime(): Boolean {
//        return !sharedPreferences.contains("setup_complete")
//    }
//
//    fun setFirstTimeDone() {
//        sharedPreferences.edit()
//            .putBoolean("setup_complete", true)
//            .apply()
//    }

    // Face Data Storage (for actual face recognition data)
    fun saveFaceData(data: ByteArray) {
        val encodedData = Base64.encodeToString(data, Base64.DEFAULT)
        sharedPreferences.edit()
            .putString("face_data", encodedData)
            .apply()
    }

    fun getFaceData(): ByteArray? {
        val encodedData = sharedPreferences.getString("face_data", null)
        return encodedData?.let { Base64.decode(it, Base64.DEFAULT) }
    }

    fun hasFaceData(): Boolean {
        return sharedPreferences.contains("face_data")
    }
    // Add these functions to your existing PreferencesManager class
    fun saveSelectedApps(selectedApps: Set<String>) {
        sharedPreferences.edit()
            .putStringSet("selected_apps", selectedApps)
            .apply()
    }

    fun getSelectedApps(): Set<String> {
        return sharedPreferences.getStringSet("selected_apps", emptySet()) ?: emptySet()
    }
}