package com.kalsu.relive.data

import android.content.Context
import android.net.Uri
import android.content.Intent

/**
 * Stores only the two optional gallery photos.
 * Slot 0 is reserved for the bundled, immutable Chama portrait.
 */
class PhotoStore(private val context: Context) {
    private val prefs = SecurePreferences.open(context, "relive_photos")

    fun get(slot: Int): Uri? {
        require(slot in 0..2) { "Emplacement photo invalide." }
        if (slot == 0) return null
        return prefs.getString(key(slot), null)?.let(Uri::parse)
    }

    fun set(slot: Int, uri: Uri) {
        require(slot in 1..2) { "Le portrait principal intégré ne peut pas être remplacé." }
        val previous = get(slot)
        if (previous != null && previous != uri) releaseReadPermission(previous)
        prefs.edit().putString(key(slot), uri.toString()).apply()
    }

    fun clear(slot: Int) {
        require(slot in 1..2) { "Le portrait principal intégré ne peut pas être retiré." }
        get(slot)?.let(::releaseReadPermission)
        prefs.edit().remove(key(slot)).apply()
    }

    private fun releaseReadPermission(uri: Uri) {
        try {
            context.contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            // The provider may already have revoked the grant.
        } catch (_: IllegalArgumentException) {
            // Some document providers do not support persistable grants.
        }
    }

    private fun key(slot: Int): String = "photo_slot_$slot"
}
