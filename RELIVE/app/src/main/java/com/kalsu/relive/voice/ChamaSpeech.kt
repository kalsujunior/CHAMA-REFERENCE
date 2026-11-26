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
    private var pendingText: String? = null
    private var pendingLanguageTag: String = "fr-FR"
    private var pendingCallback: (() -> Unit)? = null
    private var released = false
    private var utteranceSequence = 0L
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onInit(status: Int) {
        if (released) {
            engine?.shutdown()
            engine = null
            return
        }
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
            val queuedText = pendingText
            if (queuedText != null) {
                val queuedLanguage = pendingLanguageTag
                val queuedCallback = pendingCallback
                pendingText = null
                pendingCallback = null
                startSpeaking(queuedText, queuedLanguage, queuedCallback)
            } else {
                pendingCallback?.invoke()
                pendingCallback = null
            }
        } else {
            pendingText = null
            val callback = pendingCallback
            pendingCallback = null
            callback?.invoke()
        }
    }

    fun speak(
        text: String,
        languageTag: String = "fr-FR",
        onDone: (() -> Unit)? = null
    ) {
        if (text.isBlank() || released) {
            onDone?.invoke()
            return
        }
        if (!ready) {
            // Keep the latest requested line until Android finishes TTS initialization.
            pendingText = text
            pendingLanguageTag = languageTag
            pendingCallback = onDone
            return
        }
        startSpeaking(text, languageTag, onDone)
    }

    private fun startSpeaking(text: String, languageTag: String, onDone: (() -> Unit)?) {
        onFinished?.invoke()
        onFinished = onDone
        utteranceSequence += 1
        val utteranceId = "relive-chama-$utteranceSequence"
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
        pendingText = null
        val queuedCallback = pendingCallback
        pendingCallback = null
        queuedCallback?.invoke()
        engine?.stop()
        finishUtterance(activeUtteranceId)
    }

    fun release() {
        released = true
        pendingText = null
        pendingCallback = null
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
        onFinished = null
        activeUtteranceId = null
    }
}
