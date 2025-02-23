//package com.example.privacyapp.data.apps.model
package com.example.privacyapp.data.model
import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    var isSelected: Boolean = false,
    val category: String


)