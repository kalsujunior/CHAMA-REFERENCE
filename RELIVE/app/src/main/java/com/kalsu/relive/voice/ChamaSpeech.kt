package com.kalsu.relive.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.kalsu.relive.avatar.ChamaViseme
import com.kalsu.relive.avatar.ChamaVisemeMapper
import java.util.Locale

/** Local Android TTS with optional word-range viseme hints when the engine supports them. */
class ChamaSpeech(context: Context) : TextToSpeech.OnInitListener {
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var onFinished: (() -> Unit)? = null
    private var onVisemeChanged: ((ChamaViseme) -> Unit)? = null
    private var activeText: String = ""
    private var activeUtteranceId: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            engine?.language = Locale.FRENCH
            engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit

                // Range callbacks are word-level hints, not phoneme-accurate lip sync.
                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    if (utteranceId != activeUtteranceId) return
                    val viseme = ChamaVisemeMapper.fromTextRange(activeText, start, end)
                    mainHandler.post { onVisemeChanged?.invoke(viseme) }
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId != activeUtteranceId) return
                    mainHandler.post { finishUtterance() }
                }

                @Deprecated("Deprecated by Android")
                override fun onError(utteranceId: String?) {
                    if (utteranceId != activeUtteranceId) return
                    mainHandler.post { finishUtterance() }
                }
            })
        }
    }

    fun speak(
        text: String,
        languageTag: String = "fr-FR",
        onDone: (() -> Unit)? = null,
        onViseme: ((ChamaViseme) -> Unit)? = null
    ) {
        if (!ready || text.isBlank()) {
            onDone?.invoke()
            onViseme?.invoke(ChamaViseme.REST)
            return
        }
        onFinished = onDone
        onVisemeChanged = onViseme
        activeText = text
        val utteranceId = "relive-chama-${System.currentTimeMillis()}"
        activeUtteranceId = utteranceId
        engine?.language = Locale.forLanguageTag(languageTag)
        val result = engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) finishUtterance()
    }

    private fun finishUtterance() {
        onVisemeChanged?.invoke(ChamaViseme.REST)
        val callback = onFinished
        onFinished = null
        onVisemeChanged = null
        activeText = ""
        activeUtteranceId = null
        callback?.invoke()
    }

    fun stop() {
        engine?.stop()
        finishUtterance()
    }

    fun release() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
        onFinished = null
        onVisemeChanged = null
        activeText = ""
        activeUtteranceId = null
    }
}
