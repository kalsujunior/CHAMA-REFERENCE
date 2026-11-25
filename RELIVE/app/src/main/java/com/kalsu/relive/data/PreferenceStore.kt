package com.kalsu.relive.data

import android.content.Context

class PreferenceStore(context: Context) {
    private val prefs = SecurePreferences.open(context, "relive_preferences")

    var displayName: String
        get() = prefs.getString("display_name", "Chama") ?: "Chama"
        set(value) { prefs.edit().putString("display_name", value.trim()).apply() }

    var languageTag: String
        get() = prefs.getString("language_tag", "fr-FR") ?: "fr-FR"
        set(value) { prefs.edit().putString("language_tag", value).apply() }

    var voiceEnabled: Boolean
        get() = prefs.getBoolean("voice_enabled", true)
        set(value) { prefs.edit().putBoolean("voice_enabled", value).apply() }

    var remoteAiEnabled: Boolean
        get() = prefs.getBoolean("remote_ai_enabled", false)
        set(value) { prefs.edit().putBoolean("remote_ai_enabled", value).apply() }

    var remoteAccessToken: String
        get() = prefs.getString("remote_access_token", "") ?: ""
        set(value) { prefs.edit().putString("remote_access_token", value.trim()).apply() }
}
