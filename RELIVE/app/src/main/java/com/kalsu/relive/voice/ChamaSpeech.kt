package com.kalsu.relive.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/** Local Android text-to-speech. Speech never drives or modifies the portrait. */
class ChamaSpeech(context: Context) : TextToSpeech.OnInitListener {
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var onFinished: (() -> Unit)? = null
    private var activeUtteranceId: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            engine?.language = Locale.FRENCH
            engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit

                override fun onDone(utteranceId: String?) {
                    if (utteranceId != activeUtteranceId) return
                    mainHandler.post { finishUtterance(utteranceId) }
                }

                @Deprecated("Deprecated by Android")
                override fun onError(utteranceId: String?) {
                    if (utteranceId != activeUtteranceId) return
                    mainHandler.post { finishUtterance(utteranceId) }
                }
            })
        }
    }

    fun speak(
        text: String,
        languageTag: String = "fr-FR",
        onDone: (() -> Unit)? = null
    ) {
        if (!ready || text.isBlank()) {
            onDone?.invoke()
            return
        }
        onFinished = onDone
        val utteranceId = "relive-chama-${System.currentTimeMillis()}"
        activeUtteranceId = utteranceId
        engine?.language = Locale.forLanguageTag(languageTag)
        val result = engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) finishUtterance(utteranceId)
    }

    private fun finishUtterance(utteranceId: String?) {
        if (utteranceId != activeUtteranceId) return
        val callback = onFinished
        onFinished = null
        activeUtteranceId = null
        callback?.invoke()
    }

    fun stop() {
        engine?.stop()
        finishUtterance(activeUtteranceId)
    }

    fun release() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
        onFinished = null
        activeUtteranceId = null
    }
}
