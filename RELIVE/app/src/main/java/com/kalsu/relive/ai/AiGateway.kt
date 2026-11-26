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
    suspend fun reply(message: String, languageTag: String, memories: List<String>, history: List<Pair<String, String>>, accessToken: String): String? =
        withContext(Dispatchers.IO) {
            val baseUrl = BuildConfig.RELIVE_API_BASE_URL.trim().trimEnd('/')
            if (!baseUrl.startsWith("https://", ignoreCase = true) || accessToken.isBlank() || message.isBlank()) return@withContext null

            var connection: HttpURLConnection? = null
            try {
                connection = (URL("$baseUrl/chat").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 8_000
                    readTimeout = 20_000
                    useCaches = false
                    setRequestProperty("Connection", "close")
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("X-RELIVE-ACCESS", accessToken)
                }
                val memoryArray = JSONArray()
                memories.takeLast(20).forEach { memoryArray.put(it) }
                val historyArray = JSONArray()
                history.takeLast(20).forEach { (author, text) ->
                    historyArray.put(JSONObject().put("author", author).put("text", text))
                }
                val payload = JSONObject()
                    .put("message", message.take(6000))
                    .put("language", languageTag.take(12))
                    .put("memories", memoryArray)
                    .put("history", historyArray)
                connection.outputStream.use { output ->
                    output.write(payload.toString().toByteArray(Charsets.UTF_8))
                }
                if (connection.responseCode !in 200..299) return@withContext null
                val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                    val result = StringBuilder()
                    val buffer = CharArray(2048)
                    while (result.length <= 64_000) {
                        val count = reader.read(buffer)
                        if (count < 0) break
                        result.append(buffer, 0, count)
                    }
                    result.toString()
                }
                if (response.length > 64_000) return@withContext null
                JSONObject(response).optString("reply").take(6000).takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            } finally {
                connection?.disconnect()
            }
        }
}
