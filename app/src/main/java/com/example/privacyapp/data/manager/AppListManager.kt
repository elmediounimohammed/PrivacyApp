package com.example.privacyapp.data.manager

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.example.privacyapp.data.model.AppInfo

class AppListManager(private val context: Context) {
    fun getInstalledApps(): List<AppInfo> {
        Log.d("AppListManager", "Fetching installed apps...")
        val packageManager = context.packageManager
        val installedApps = mutableListOf<AppInfo>()

        try {
            // Get ALL apps (including non-launcher apps)
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            for (appInfo in apps) {
                try {
                    // Skip system apps
                    if (!isSystemApp(appInfo)) {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val icon = packageManager.getApplicationIcon(appInfo.packageName)
                        val category = getAppCategory(appInfo.packageName)

                        val app = AppInfo(
                            packageName = appInfo.packageName,
                            appName = appName,
                            icon = icon,
                            isSelected = false,
                            category = category
                        )

                        installedApps.add(app)
                        Log.d("AppListManager", "Added app: ${app.appName}")
                    }
                } catch (e: Exception) {
                    Log.e("AppListManager", "Error processing app ${appInfo.packageName}: ${e.message}")
                    // Continue with next app instead of breaking the loop
                    continue
                }
            }

            Log.d("AppListManager", "Successfully fetched ${installedApps.size} apps")
            return installedApps.sortedBy { it.appName }

        } catch (e: Exception) {
            Log.e("AppListManager", "Critical error fetching apps: ${e.stackTraceToString()}")
            return emptyList()
        }
    }

    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }

    private fun getAppCategory(packageName: String): String {
        return when {
            isSocialApp(packageName) -> "Social"
            isMessagingApp(packageName) -> "Messaging"
            isBrowserApp(packageName) -> "Browser"
            else -> "All"
        }
    }

    private fun isSocialApp(packageName: String): Boolean {
        val socialApps = listOf(
            "com.facebook.katana",
            "com.instagram.android",
            "com.twitter.android",
            "com.linkedin.android",
            "com.snapchat.android",
            "com.pinterest"
        )
        return socialApps.any { packageName.contains(it) }
    }

    private fun isMessagingApp(packageName: String): Boolean {
        val messagingApps = listOf(
            "com.whatsapp",
            "com.facebook.messenger",
            "org.telegram.messenger",
            "com.google.android.apps.messaging",
            "com.viber.voip"
        )
        return messagingApps.any { packageName.contains(it) }
    }

    private fun isBrowserApp(packageName: String): Boolean {
        val browserApps = listOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.opera.browser",
            "com.microsoft.emmx",
            "com.brave.browser"
        )
        return browserApps.any { packageName.contains(it) }
    }
}