package com.example.messagingapp.data

import android.content.Context
import android.util.Log
import androidx.datastore.createDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

data class UserPreferences(val currentUserID: String, val token: String, val phoneNum: String)

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private object PreferencesKeys {
        val currentUserID = stringPreferencesKey("currentUserID")
        val token = stringPreferencesKey("token")
        val phoneNum = stringPreferencesKey("phoneNum")
    }

    private val dataStore = context.createDataStore("user_preferences")

    suspend fun updateCurrentUserID(currentUserID: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.currentUserID] = currentUserID
        }
    }

    suspend fun updateToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.token] = token
        }
    }

    suspend fun updatePhoneNum(phoneNum: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.phoneNum] = phoneNum
        }
    }

    val preferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.d(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val currentUserID = preferences[PreferencesKeys.currentUserID] ?: "no_user"
            val token = preferences[PreferencesKeys.token] ?: "no_token"
            val phoneNum = preferences[PreferencesKeys.phoneNum] ?: "no_phoneNum"

            UserPreferences(currentUserID, token, phoneNum)
        }


}