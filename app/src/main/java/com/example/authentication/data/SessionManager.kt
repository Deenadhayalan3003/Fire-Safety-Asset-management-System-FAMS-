package com.example.authentication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.models.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fams_session_prefs")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val userAdapter = moshi.adapter(User::class.java)

    companion object {
        private val KEY_USER_PROFILE = stringPreferencesKey("user_profile")
    }

    val userProfileFlow: Flow<User?> = context.dataStore.data.map { preferences ->
        val json = preferences[KEY_USER_PROFILE]
        if (!json.isNullOrEmpty()) {
            try {
                userAdapter.fromJson(json)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    suspend fun saveSession(user: User) {
        val json = userAdapter.toJson(user)
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_PROFILE] = json
        }
    }

    suspend fun getSession(): User? {
        return userProfileFlow.first()
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_USER_PROFILE)
        }
    }
}
