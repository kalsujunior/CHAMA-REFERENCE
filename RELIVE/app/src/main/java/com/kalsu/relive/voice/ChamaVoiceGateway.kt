package com.kalsu.relive.voice

import android.content.Context
import android.media.MediaPlayer
import com.kalsu.relive.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Calls the authenticated RELIVE Worker. The ElevenLabs key never leaves Cloudflare. */
object ChamaVoiceGateway {
    suspend fun generate(text: String, accessToken: String, languageTag: String = "fr-FR"): ByteArray? = withContext(Dispatchers.IO) {
        val baseUrl = BuildConfig.RELIVE_API_BASE_URL.trim().trimEnd('/')
        if (!baseUrl.startsWith("https://", ignoreCase = true) ||
            accessToken.isBlank() || text.isBlank() || text.length > 3000
        ) return@withContext null

        var connection: HttpURLConnection? = null
        try {
            connection = (URL("$baseUrl/speech").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12_000
                readTimeout = 45_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "audio/mpeg")
                setRequestProperty("X-RELIVE-ACCESS", accessToken)
            }
            val payload = JSONObject().put("text", text).put("language", languageTag).toString()
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            if (connection.responseCode !in 200..299) return@withContext null
            if (!connection.contentType.orEmpty().contains("audio", ignoreCase = true)) return@withContext null
            connection.inputStream.use { input ->
                val bytes = input.readBytes()
                bytes.takeIf { it.isNotEmpty() && it.size <= 8 * 1024 * 1024 }
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}

/** Plays generated MP3 without adding a third-party Android dependency. */
class ChamaVoicePlayer(context: Context) {
    private val appContext = context.applicationContext
    private var player: MediaPlayer? = null
    private var currentFile: File? = null

    fun play(audio: ByteArray) {
        stop()
        if (audio.isEmpty()) return
        try {
            val file = File.createTempFile("relive-chama-", ".mp3", appContext.cacheDir)
            file.writeBytes(audio)
            currentFile = file
            val next = MediaPlayer()
            player = next
            next.setOnPreparedListener { it.start() }
            next.setOnCompletionListener { releasePlayer(deleteFile = true) }
            next.setOnErrorListener { _, _, _ ->
                releasePlayer(deleteFile = true)
                true
            }
            next.setDataSource(file.absolutePath)
            next.prepareAsync()
        } catch (_: Exception) {
            releasePlayer(deleteFile = true)
        }
    }

    fun stop() = releasePlayer(deleteFile = true)

    private fun releasePlayer(deleteFile: Boolean) {
        try { player?.reset() } catch (_: Exception) { }
        try { player?.release() } catch (_: Exception) { }
        player = null
        if (deleteFile) {
            try { currentFile?.delete() } catch (_: Exception) { }
            currentFile = null
        }
    }
}
