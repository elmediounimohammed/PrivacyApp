package com.example.privacyapp.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.privacyapp.data.model.AppInfo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.example.privacyapp.data.manager.AppListManager
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    appList: List<AppInfo>,
    onAppSelected: (AppInfo) -> Unit,
    onSaveSelection: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var appList by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current

    // Add LaunchedEffect here, at the top level of the composable
    LaunchedEffect(Unit) {
        Log.d("AppSelection", "Starting to fetch apps")
        withContext(Dispatchers.IO) {
            try {
                val manager = AppListManager(context)
                val apps = manager.getInstalledApps()
                Log.d("AppSelection", "Fetched ${apps.size} apps")

                withContext(Dispatchers.Main) {
                    appList = apps
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e("AppSelection", "Error fetching apps", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Bar with Title and Save Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Apps",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(
                onClick = onSaveSelection,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save")
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search apps") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )



        // Category Filter
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            listOf("All", "Social", "Messaging", "Browser").forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        // App List
        if (appList.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        } else {
            LazyColumn {
                items(
                    appList.filter { app ->
                        val matchesCategory = selectedCategory == "All" ||
                                getAppCategory(app.packageName) == selectedCategory
                        val matchesSearch = app.appName.contains(searchQuery, ignoreCase = true)
                        matchesCategory && matchesSearch
                    }
                ) { app ->
                    AppItem(app = app, onAppSelected = onAppSelected)
                }
            }
        }
    }
}

@Composable
fun AppItem(
    app: AppInfo,
    onAppSelected: (AppInfo) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAppSelected(app) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberDrawablePainter(drawable = app.icon),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .padding(end = 16.dp)
        )
        Text(
            text = app.appName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = app.isSelected,
            onCheckedChange = { onAppSelected(app) }
        )
    }


}
// Add this function in the same file as your AppSelectionScreen
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