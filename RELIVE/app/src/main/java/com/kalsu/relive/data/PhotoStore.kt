package com.kalsu.relive.data

import android.content.Context
import android.net.Uri

/**
 * Stores only the two optional gallery photos.
 * Slot 0 is reserved for the bundled, immutable Chama portrait.
 */
class PhotoStore(context: Context) {
    private val prefs = SecurePreferences.open(context, "relive_photos")

    fun get(slot: Int): Uri? {
        require(slot in 0..2) { "Emplacement photo invalide." }
        if (slot == 0) return null
        return prefs.getString(key(slot), null)?.let(Uri::parse)
    }

    fun set(slot: Int, uri: Uri) {
        require(slot in 1..2) { "Le portrait principal intégré ne peut pas être remplacé." }
        prefs.edit().putString(key(slot), uri.toString()).apply()
    }

    fun clear(slot: Int) {
        require(slot in 1..2) { "Le portrait principal intégré ne peut pas être retiré." }
        prefs.edit().remove(key(slot)).apply()
    }

    private fun key(slot: Int): String = "photo_slot_$slot"
}
