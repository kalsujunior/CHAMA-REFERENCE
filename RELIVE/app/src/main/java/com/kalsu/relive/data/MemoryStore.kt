package com.kalsu.relive.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ReliveMemory(val id: Long, val text: String, val createdAt: Long)

class MemoryStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("relive_memories", Context.MODE_PRIVATE)

    @Synchronized
    fun load(): List<ReliveMemory> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(ReliveMemory(item.optLong("id"), item.optString("text"), item.optLong("createdAt")))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun add(text: String): ReliveMemory {
        require(text.isNotBlank()) { "Le souvenir ne peut pas être vide." }
        val item = ReliveMemory(System.currentTimeMillis(), text.trim(), System.currentTimeMillis())
        val next = (load() + item).takeLast(MAX_ITEMS)
        val array = JSONArray()
        next.forEach { memory ->
            array.put(JSONObject().put("id", memory.id).put("text", memory.text).put("createdAt", memory.createdAt))
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
