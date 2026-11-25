package com.kalsu.relive.data

import android.content.Context
import android.net.Uri

/** Stores user-selected original photo URIs and their persisted read permissions. */
class PhotoStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("relive_photos", Context.MODE_PRIVATE)

    fun get(slot: Int): Uri? = prefs.getString(key(slot), null)?.let(Uri::parse)

    fun set(slot: Int, uri: Uri) {
        prefs.edit().putString(key(slot), uri.toString()).apply()
    }

    fun clear(slot: Int) {
        prefs.edit().remove(key(slot)).apply()
    }

    private fun key(slot: Int): String = "photo_slot_$slot"
}
