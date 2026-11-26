package com.kalsu.relive.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** AES-256 encrypted local preferences backed by Android Keystore. */
internal object SecurePreferences {
    fun open(context: Context, fileName: String): SharedPreferences {
        val appContext = context.applicationContext
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val encrypted = EncryptedSharedPreferences.create(
            appContext, fileName + "_secure", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        migrateLegacy(appContext, fileName, encrypted)
        return encrypted
    }

    private fun migrateLegacy(context: Context, fileName: String, encrypted: SharedPreferences) {
        val legacy = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val existing = legacy.all
        if (existing.isEmpty()) return
        val editor = encrypted.edit()
        existing.forEach { (key, value) ->
            if (!encrypted.contains(key)) {
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
                }
            }
        }
        check(editor.commit()) { "Impossible de chiffrer les données RELIVE existantes." }
        check(legacy.edit().clear().commit()) { "Impossible de terminer la migration sécurisée RELIVE." }
    }
}
