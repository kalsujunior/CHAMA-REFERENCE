package com.kalsu.relive.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ReliveMemory(
    val id: Long,
    val text: String,
    val createdAt: Long,
    val category: String = MemoryCategories.GENERAL
)

object MemoryCategories {
    const val IDENTITY = "identity"
    const val PREFERENCES = "preferences"
    const val PROJECTS = "projects"
    const val IMPORTANT = "important"
    const val GENERAL = "general"

    val all = setOf(IDENTITY, PREFERENCES, PROJECTS, IMPORTANT, GENERAL)

    fun normalize(value: String): String = value.takeIf { it in all } ?: GENERAL
}

class MemoryStore(context: Context) {
    private val prefs = SecurePreferences.open(context, "relive_memories")

    @Synchronized
    fun load(): List<ReliveMemory> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(ReliveMemory(
                        id = item.optLong("id"),
                        text = item.optString("text"),
                        createdAt = item.optLong("createdAt"),
                        category = MemoryCategories.normalize(item.optString("category", MemoryCategories.GENERAL))
                    ))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun add(text: String, category: String = MemoryCategories.GENERAL): ReliveMemory {
        require(text.isNotBlank()) { "Le souvenir ne peut pas être vide." }
        val existing = load()
        val now = System.currentTimeMillis()
        val item = ReliveMemory(
            id = maxOf(now, (existing.lastOrNull()?.id ?: 0L) + 1L),
            text = text.trim(),
            createdAt = now,
            category = MemoryCategories.normalize(category)
        )
        val next = (existing + item).takeLast(MAX_ITEMS)
        val array = JSONArray()
        next.forEach { memory ->
            array.put(JSONObject().put("id", memory.id).put("text", memory.text).put("createdAt", memory.createdAt)
                .put("category", memory.category))
        }
        prefs.edit().putString(KEY, array.toString()).apply()
        return item
    }

    @Synchronized
    fun delete(id: Long) {
        val array = JSONArray()
        load().filterNot { it.id == id }.forEach { memory ->
            array.put(JSONObject().put("id", memory.id).put("text", memory.text).put("createdAt", memory.createdAt))
        }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    @Synchronized
    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        private const val KEY = "saved_memories"
        private const val MAX_ITEMS = 500
    }
}
