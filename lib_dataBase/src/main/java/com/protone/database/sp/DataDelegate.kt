package com.protone.database.sp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DataDelegate(private val dataStore: DataStore<Preferences>) : IGetNSet,
    CoroutineScope by CoroutineScope(Dispatchers.IO) {

    override fun setInt(key: String, value: Int) {
        launch {
            dataStore.edit {
                it[intPreferencesKey(key)] = value
            }
        }
    }

    override fun getInt(key: String, defValue: Int): Int {
        return runBlocking(coroutineContext) {
            dataStore.data.catch {
                emit(emptyPreferences())
            }.map {
                it[intPreferencesKey(key)]
            }.first() ?: defValue
        }
    }

    override fun setLong(key: String, value: Long) {
        launch {
            dataStore.edit {
                it[longPreferencesKey(key)] = value
            }
        }
    }

    override fun getLong(key: String, defValue: Long): Long {
        return runBlocking(coroutineContext) {
            dataStore.data.catch {
                emit(emptyPreferences())
            }.map {
                it[longPreferencesKey(key)]
            }.first() ?: defValue
        }
    }

    override fun setString(key: String, value: String) {
        launch {
            dataStore.edit {
                it[stringPreferencesKey(key)] = value
            }
        }
    }

    override fun getString(key: String, defValue: String): String {
        return runBlocking(coroutineContext) {
            dataStore.data.catch {
                emit(emptyPreferences())
            }.map {
                it[stringPreferencesKey(key)]
            }.first() ?: defValue
        }
    }

    override fun setBoolean(key: String, value: Boolean) {
        launch {
            dataStore.edit {
                it[booleanPreferencesKey(key)] = value
            }
        }
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return runBlocking(coroutineContext) {
            dataStore.data.catch {
                emit(emptyPreferences())
            }.map {
                it[booleanPreferencesKey(key)]
            }.first() ?: defValue
        }
    }

}