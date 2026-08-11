package com.focushome.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val IS_PRO_USER = booleanPreferencesKey("is_pro_user")
        private val PINNED_APPS = stringSetPreferencesKey("pinned_apps")
    }

    val isProUser: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_PRO_USER] ?: false
        }

    val pinnedApps: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PINNED_APPS] ?: emptySet()
        }

    suspend fun setProUser(isPro: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_PRO_USER] = isPro
        }
    }

    suspend fun togglePinnedApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PINNED_APPS] ?: emptySet()
            if (current.contains(packageName)) {
                preferences[PINNED_APPS] = current - packageName
            } else {
                if (current.size < 5) {
                    preferences[PINNED_APPS] = current + packageName
                }
            }
        }
    }
}
