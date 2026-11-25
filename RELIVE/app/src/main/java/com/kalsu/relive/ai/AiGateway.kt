package com.kalsu.relive.ai

import com.kalsu.relive.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Optional secure-backend client. The API key belongs on the server, never in this APK.
 * Expected POST endpoint: {baseUrl}/chat with {message, language, memories}.
 * Expected success response: {reply: "..."}.
 */
object AiGateway {
    suspend fun reply(message: String, languageTag: String, memories: List<String>, history: List<Pair<String, String>>): String? =
        withContext(Dispatchers.IO) {
            val baseUrl = BuildConfig.RELIVE_API_BASE_URL.trim().trimEnd('/')
            if (!baseUrl.startsWith("https://", ignoreCase = true)) return@withContext null

            var connection: HttpURLConnection? = null
            try {
                connection = (URL("$baseUrl/chat").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 10_000
                    readTimeout = 25_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                }
                val memoryArray = JSONArray()
                memories.takeLast(20).forEach { memoryArray.put(it) }
                val historyArray = JSONArray()
                history.takeLast(20).forEach { (author, text) ->
                    historyArray.put(JSONObject().put("author", author).put("text", text))
                }
                val payload = JSONObject()
                    .put("message", message)
                    .put("language", languageTag)
                    .put("memories", memoryArray)
                    .put("history", historyArray)
                connection.outputStream.use { output ->
                    output.write(payload.toString().toByteArray(Charsets.UTF_8))
                }
                if (connection.responseCode !in 200..299) return@withContext null
                val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                JSONObject(response).optString("reply").takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            } finally {
                connection?.disconnect()
            }
        }
}
