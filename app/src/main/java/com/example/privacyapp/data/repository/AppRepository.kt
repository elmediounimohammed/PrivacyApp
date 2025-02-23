package com.example.privacyapp.data.repository

import com.example.privacyapp.data.PreferencesManager

import com.example.privacyapp.data.manager.AppListManager
import com.example.privacyapp.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



class AppRepository(
    private val appListManager: AppListManager,
    private val preferencesManager: PreferencesManager
) {
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val selectedApps = preferencesManager.getSelectedApps()
        appListManager.getInstalledApps().map { app ->
            app.copy(isSelected = selectedApps.contains(app.packageName))
        }
    }

    fun saveSelectedApps(selectedApps: List<AppInfo>) {
        preferencesManager.saveSelectedApps(
            selectedApps.filter { it.isSelected }
                .map { it.packageName }
                .toSet()
        )
    }

    fun getSelectedAppsCount(): Int {
        return preferencesManager.getSelectedApps().size
    }
}