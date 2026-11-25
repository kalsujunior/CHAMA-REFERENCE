package com.kalsu.relive.avatar

import java.text.Normalizer
import java.util.Locale

/**
 * Semantic mouth poses for the future layered Chama avatar.
 * These values are animation instructions; they never alter the original photo.
 */
enum class ChamaViseme(val label: String, val mouthHint: String) {
    REST("Repos", "closed"),
    A("A", "open-wide"),
    E("E", "open-mid"),
    I("I", "spread"),
    O("O", "rounded"),
    U("OU", "pursed"),
    MBP("M/B/P", "closed-pressed"),
    FV("F/V", "lower-lip-teeth"),
    L("L", "tongue-tip"),
    WQ("W/Q", "rounded-small")
}

/** Approximation from Android TTS word-range callbacks, not a phoneme-level alignment. */
object ChamaVisemeMapper {
    fun fromTextRange(text: String, start: Int, end: Int): ChamaViseme {
        if (text.isEmpty() || start !in text.indices) return ChamaViseme.REST
        val safeEnd = end.coerceIn(start + 1, text.length)
        val token = normalize(text.substring(start, safeEnd))
        if (token.isEmpty()) return ChamaViseme.REST

        val first = token.first()
        return when {
            first in "bmp" -> ChamaViseme.MBP
            first in "fv" -> ChamaViseme.FV
            first in "wq" -> ChamaViseme.WQ
            first == 'l' -> ChamaViseme.L
            'a' in token -> ChamaViseme.A
            'o' in token -> ChamaViseme.O
            'u' in token -> ChamaViseme.U
            'i' in token || 'y' in token -> ChamaViseme.I
            'e' in token -> ChamaViseme.E
            else -> ChamaViseme.REST
        }
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .filter(Char::isLetter)
}

/** Named rig anchors reserved for a future layered/mesh avatar renderer. */
enum class ChamaRigJoint {
    ROOT, TORSO, NECK, HEAD,
    EYE_LEFT, EYE_RIGHT, BROW_LEFT, BROW_RIGHT,
    JAW, MOUTH,
    SHOULDER_LEFT, ELBOW_LEFT, WRIST_LEFT, HAND_LEFT,
    SHOULDER_RIGHT, ELBOW_RIGHT, WRIST_RIGHT, HAND_RIGHT,
    BRAID_LEFT, BRAID_RIGHT
}

enum class ChamaGaze { CENTER, LEFT, RIGHT, UP, DOWN }
enum class ChamaExpression { NEUTRAL, HAPPY, SURPRISED, LISTENING, TALKING }
enum class ChamaGesture { IDLE, WAVE, LISTEN, DANCE }

data class ChamaAvatarPose(
    val viseme: ChamaViseme = ChamaViseme.REST,
    val gaze: ChamaGaze = ChamaGaze.CENTER,
    val expression: ChamaExpression = ChamaExpression.NEUTRAL,
    val gesture: ChamaGesture = ChamaGesture.IDLE,
    val blink: Boolean = false
)
