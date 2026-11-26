package com.kalsu.relive.avatar

/**
 * Decision layer for Chama's future autonomous virtual-space behaviour.
 *
 * This planner is deliberately independent from rendering, networking and Android UI.
 * It may choose an intention, but it never moves the avatar, speaks, accesses the
 * microphone, sends data, or performs actions outside the virtual room.
 *
 * ChamaAutonomyRuntime translates [ChamaAutonomyTarget] into a validated in-room
 * route and the shared SceneView applies the resulting locomotion frames.
 */
enum class ChamaAutonomyActivity {
    WALK_TO_WINDOW,
    WALK_TO_FLOWERS,
    WALK_TO_SOFA,
    LOOK_AROUND,
    DANCE_IN_PLACE,
    REST
}

/** Stable semantic destinations; the 3D scene owns their actual coordinates. */
enum class ChamaAutonomyTarget {
    WINDOW,
    FLOWERS,
    SOFA,
    ROOM_CENTER
}

enum class ChamaAutonomyPauseReason {
    DISABLED,
    SPACE_NOT_VISIBLE,
    USER_INTERACTION,
    LOW_POWER
}

data class ChamaAutonomyInput(
    val enabled: Boolean = false,
    val virtualSpaceVisible: Boolean = false,
    val userInteracting: Boolean = false,
    val lowPowerMode: Boolean = false,
    val energy: Float = 0.7f,
    val curiosity: Float = 0.5f,
    /** Monotonic time, such as SystemClock.elapsedRealtime(). */
    val nowElapsedRealtimeMs: Long
)

data class ChamaAutonomyDecision(
    val activity: ChamaAutonomyActivity?,
    val target: ChamaAutonomyTarget?,
    val bodyGesture: ChamaBodyGesture = ChamaBodyGesture.IDLE,
    val durationMs: Long = 0L,
    val pauseReason: ChamaAutonomyPauseReason? = null,
    val statusFr: String
) {
    val isPaused: Boolean get() = pauseReason != null
}

/**
 * Small deterministic planner. Decisions are stable for their duration and are
 * interrupted immediately when the user interacts, disables autonomy, leaves
 * the virtual space, or enables a low-power condition.
 */
class ChamaAutonomyPlanner(
    private val decisionDurationMs: Long = 18_000L,
    private val minimumRestMs: Long = 12_000L
) {
    private var currentDecision: ChamaAutonomyDecision? = null
    private var nextDecisionAtMs: Long = Long.MIN_VALUE
    private var sequence = 0

    init {
        require(decisionDurationMs in 5_000L..120_000L) {
            "decisionDurationMs must be between 5 and 120 seconds"
        }
        require(minimumRestMs in 5_000L..120_000L) {
            "minimumRestMs must be between 5 and 120 seconds"
        }
    }

    @Synchronized
    fun decide(input: ChamaAutonomyInput): ChamaAutonomyDecision {
        val pause = when {
            !input.enabled -> ChamaAutonomyPauseReason.DISABLED
            !input.virtualSpaceVisible -> ChamaAutonomyPauseReason.SPACE_NOT_VISIBLE
            input.userInteracting -> ChamaAutonomyPauseReason.USER_INTERACTION
            input.lowPowerMode -> ChamaAutonomyPauseReason.LOW_POWER
            else -> null
        }

        if (pause != null) {
            currentDecision = null
            nextDecisionAtMs = Long.MIN_VALUE
            return pausedDecision(pause)
        }

        val now = input.nowElapsedRealtimeMs.coerceAtLeast(0L)
        currentDecision?.let { active ->
            if (now < nextDecisionAtMs) return active
        }

        val energy = input.energy.coerceIn(0f, 1f)
        val curiosity = input.curiosity.coerceIn(0f, 1f)
        val selected = chooseActivity(energy, curiosity)
        val duration = if (selected.activity == ChamaAutonomyActivity.REST) {
            minimumRestMs
        } else {
            decisionDurationMs
        }
        val decision = ChamaAutonomyDecision(
            activity = selected.activity,
            target = selected.target,
            bodyGesture = selected.gesture,
            durationMs = duration,
            statusFr = selected.status
        )
        currentDecision = decision
        nextDecisionAtMs = now + duration
        sequence += 1
        return decision
    }

    @Synchronized
    fun reset() {
        currentDecision = null
        nextDecisionAtMs = Long.MIN_VALUE
        sequence = 0
    }

    private data class Candidate(
        val activity: ChamaAutonomyActivity,
        val target: ChamaAutonomyTarget?,
        val gesture: ChamaBodyGesture,
        val status: String
    )

    private fun chooseActivity(energy: Float, curiosity: Float): Candidate {
        if (energy < 0.22f) {
            return Candidate(
                ChamaAutonomyActivity.REST, ChamaAutonomyTarget.SOFA,
                ChamaBodyGesture.IDLE, "Je vais prendre un petit moment de repos."
            )
        }

        val curiousRoute = listOf(
            Candidate(ChamaAutonomyActivity.WALK_TO_WINDOW, ChamaAutonomyTarget.WINDOW,
                ChamaBodyGesture.IDLE, "Je vais regarder un peu par la fenêtre."),
            Candidate(ChamaAutonomyActivity.WALK_TO_FLOWERS, ChamaAutonomyTarget.FLOWERS,
                ChamaBodyGesture.IDLE, "Je vais aller admirer les fleurs."),
            Candidate(ChamaAutonomyActivity.WALK_TO_SOFA, ChamaAutonomyTarget.SOFA,
                ChamaBodyGesture.IDLE, "Je vais me rapprocher du canapé."),
            Candidate(ChamaAutonomyActivity.LOOK_AROUND, ChamaAutonomyTarget.ROOM_CENTER,
                ChamaBodyGesture.LISTEN, "Je prends un instant pour observer mon espace.")
        )

        if (energy > 0.78f && curiosity > 0.72f && sequence % 4 == 3) {
            return Candidate(
                ChamaAutonomyActivity.DANCE_IN_PLACE, ChamaAutonomyTarget.ROOM_CENTER,
                ChamaBodyGesture.DANCE, "J'ai envie de danser un petit peu !"
            )
        }
        val offset = if (curiosity < 0.25f) 2 else 0
        return curiousRoute[(sequence + offset) % curiousRoute.size]
    }

    private fun pausedDecision(reason: ChamaAutonomyPauseReason): ChamaAutonomyDecision {
        val line = when (reason) {
            ChamaAutonomyPauseReason.DISABLED -> "Mon autonomie est désactivée."
            ChamaAutonomyPauseReason.SPACE_NOT_VISIBLE -> "Je reste en pause hors de mon espace virtuel."
            ChamaAutonomyPauseReason.USER_INTERACTION -> "Je m'arrête pour être attentive à toi."
            ChamaAutonomyPauseReason.LOW_POWER -> "Je me repose pour économiser la batterie."
        }
        return ChamaAutonomyDecision(
            activity = null,
            target = null,
            pauseReason = reason,
            statusFr = line
        )
    }
}
