package com.kalsu.relive.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/** Local text-to-speech output. No network or API key is required. */
class ChamaSpeech(context: Context) : TextToSpeech.OnInitListener {
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var onFinished: (() -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            engine?.language = Locale.FRENCH
            engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) {
                    mainHandler.post {
                        val callback = onFinished
                        onFinished = null
                        callback?.invoke()
                    }
                }
                @Deprecated("Deprecated by Android")
                override fun onError(utteranceId: String?) {
                    mainHandler.post {
                        val callback = onFinished
                        onFinished = null
                        callback?.invoke()
                    }
                }
            })
        }
    }

    fun speak(text: String, languageTag: String = "fr-FR", onDone: (() -> Unit)? = null) {
        if (!ready || text.isBlank()) {
            onDone?.invoke()
            return
        }
        onFinished = onDone
        val locale = Locale.forLanguageTag(languageTag)
        engine?.language = locale
        val result = engine?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "relive-chama-${System.currentTimeMillis()}"
        )
        if (result == TextToSpeech.ERROR) {
            val callback = onFinished
            onFinished = null
            callback?.invoke()
        }
    }

    fun stop() {
        engine?.stop()
        val callback = onFinished
        onFinished = null
        callback?.invoke()
    }

    fun release() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
        onFinished = null
    }
}
