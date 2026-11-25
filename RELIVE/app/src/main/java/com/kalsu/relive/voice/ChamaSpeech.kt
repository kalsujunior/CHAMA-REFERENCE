package com.kalsu.relive.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Local text-to-speech output. No network or API key is required. */
class ChamaSpeech(context: Context) : TextToSpeech.OnInitListener {
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) engine?.language = Locale.FRENCH
    }

    fun speak(text: String, languageTag: String = "fr-FR") {
        if (!ready || text.isBlank()) return
        val locale = Locale.forLanguageTag(languageTag)
        engine?.language = locale
        engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "relive-chama-${System.currentTimeMillis()}")
    }

    fun stop() {
        engine?.stop()
    }

    fun release() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
    }
}
