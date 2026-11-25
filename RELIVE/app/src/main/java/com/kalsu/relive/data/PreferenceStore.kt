package com.kalsu.relive.data

import android.content.Context

class PreferenceStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("relive_preferences", Context.MODE_PRIVATE)

    var displayName: String
        get() = prefs.getString("display_name", "Chama") ?: "Chama"
        set(value) { prefs.edit().putString("display_name", value.trim()).apply() }

    var languageTag: String
        get() = prefs.getString("language_tag", "fr-FR") ?: "fr-FR"
        set(value) { prefs.edit().putString("language_tag", value).apply() }

    var voiceEnabled: Boolean
        get() = prefs.getBoolean("voice_enabled", true)
        set(value) { prefs.edit().putBoolean("voice_enabled", value).apply() }
}
