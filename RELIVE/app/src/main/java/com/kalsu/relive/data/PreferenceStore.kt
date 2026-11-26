package com.kalsu.relive.data

import android.content.Context

class PreferenceStore(context: Context) {
    private val prefs = SecurePreferences.open(context, "relive_preferences")

    var displayName: String
        get() = prefs.getString("display_name", "Chama") ?: "Chama"
        set(value) { prefs.edit().putString("display_name", value.trim().take(40)).apply() }

    /** "outfit" follows Chama's configured outfit palette; "manual" locks a chosen preset. */
    var themeMode: String
        get() = prefs.getString("theme_mode", "outfit") ?: "outfit"
        set(value) { prefs.edit().putString("theme_mode", if (value == "manual") "manual" else "outfit").apply() }

    /** Supported presets: chama, rose, midnight. */
    var themePreset: String
        get() = prefs.getString("theme_preset", "chama")?.takeIf {
            it in setOf("chama", "rose", "midnight")
        } ?: "chama"
        set(value) {
            val safeValue = value.takeIf { it in setOf("chama", "rose", "midnight") } ?: "chama"
            prefs.edit().putString("theme_preset", safeValue).apply()
        }

    var languageTag: String
        get() = prefs.getString("language_tag", "fr-FR") ?: "fr-FR"
        set(value) { prefs.edit().putString("language_tag", value.takeIf { it == "fr-FR" || it == "ht-HT" } ?: "fr-FR").apply() }

    var voiceEnabled: Boolean
        get() = prefs.getBoolean("voice_enabled", true)
        set(value) { prefs.edit().putBoolean("voice_enabled", value).apply() }

    var remoteAiEnabled: Boolean
        get() = prefs.getBoolean("remote_ai_enabled", false)
        set(value) { prefs.edit().putBoolean("remote_ai_enabled", value).apply() }

    var remoteAccessToken: String
        get() = prefs.getString("remote_access_token", "") ?: ""
        set(value) { prefs.edit().putString("remote_access_token", value.trim().take(512)).apply() }
}
