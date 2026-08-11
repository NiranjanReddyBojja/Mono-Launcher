package com.focushome.launcher.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focushome.launcher.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppInfo(
    val label: String,
    val packageName: String,
    val isPinned: Boolean = false
)

class AppListViewModel(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val isProUser: StateFlow<Boolean> = preferencesManager.isProUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    
    val filteredApps: StateFlow<List<AppInfo>> = combine(_allApps, _searchQuery, preferencesManager.pinnedApps) { apps, query, pinned ->
        apps.map { it.copy(isPinned = pinned.contains(it.packageName)) }
            .filter { it.label.contains(query, ignoreCase = true) }
            .sortedBy { it.label }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedApps: StateFlow<List<AppInfo>> = combine(_allApps, preferencesManager.pinnedApps) { apps, pinned ->
        apps.filter { pinned.contains(it.packageName) }
            .take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                pm.queryIntentActivities(mainIntent, 0).map { resolveInfo ->
                    AppInfo(
                        label = resolveInfo.loadLabel(pm).toString(),
                        packageName = resolveInfo.activityInfo.packageName
                    )
                }
            }
            _allApps.value = apps
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun togglePinnedApp(app: AppInfo) {
        viewModelScope.launch {
            preferencesManager.togglePinnedApp(app.packageName)
        }
    }

    fun launchApp(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        }
    }
}
