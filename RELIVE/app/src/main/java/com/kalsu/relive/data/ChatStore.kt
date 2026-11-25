package com.kalsu.relive.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ChatMessage(
    val id: Long,
    val author: String,
    val text: String,
    val timestamp: Long
)

/** Persistent local conversation store. Messages remain available after app restarts. */
class ChatStore(context: Context) {
    private val prefs = SecurePreferences.open(context, "relive_chat")

    @Synchronized
    fun load(): List<ChatMessage> {
        val raw = prefs.getString(KEY_MESSAGES, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(ChatMessage(
                        id = item.optLong("id"),
                        author = item.optString("author"),
                        text = item.optString("text"),
                        timestamp = item.optLong("timestamp")
                    ))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun append(author: String, text: String): ChatMessage {
        val message = ChatMessage(
            id = System.currentTimeMillis(),
            author = author,
            text = text.trim(),
            timestamp = System.currentTimeMillis()
        )
        val messages = (load() + message).takeLast(MAX_MESSAGES)
        val array = JSONArray()
        messages.forEach { item ->
            array.put(JSONObject()
                .put("id", item.id)
                .put("author", item.author)
                .put("text", item.text)
                .put("timestamp", item.timestamp))
        }
        prefs.edit().putString(KEY_MESSAGES, array.toString()).apply()
        return message
    }

    @Synchronized
    fun clear() {
        prefs.edit().remove(KEY_MESSAGES).apply()
    }

    companion object {
        private const val KEY_MESSAGES = "messages_json"
        private const val MAX_MESSAGES = 1000
    }
}
