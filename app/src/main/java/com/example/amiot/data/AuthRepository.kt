package com.example.amiot.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthRepository(private val context: Context) {
    private val emailKey = stringPreferencesKey("user_email")
    private val passwordKey = stringPreferencesKey("user_password")
    private val isLoggedInKey = stringPreferencesKey("is_logged_in")

    suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[emailKey] = email
                preferences[passwordKey] = password
                preferences[isLoggedInKey] = "false"
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            val preferences = context.dataStore.data
            val storedEmail = preferences.first()[emailKey] ?: ""
            val storedPassword = preferences.first()[passwordKey] ?: ""
            
            if (storedEmail == email && storedPassword == password) {
                context.dataStore.edit { prefs ->
                    prefs[isLoggedInKey] = "true"
                }
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences[isLoggedInKey] = "false"
        }
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[isLoggedInKey] == "true"
    }

    val userEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[emailKey] ?: ""
    }

    suspend fun resetPassword(email: String): Result<Boolean> {
        return try {
            val preferences = context.dataStore.data.first()
            val storedEmail = preferences[emailKey] ?: ""
            
            if (storedEmail == email) {
                // En una app real, aquí enviarías un email de recuperación
                // Por ahora, solo verificamos que el email existe
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

