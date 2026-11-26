package com.kalsu.relive.avatar

/**
 * Small local needs model used to vary Chama's autonomous routine.
 *
 * Energy is spent while walking/dancing and recovered during an actual rest
 * at the sofa. Curiosity slowly recovers during exploration and rest. This is
 * a virtual-character simulation only; it does not read device/user health.
 */
data class ChamaAutonomyNeedsSnapshot(
    val energy: Float,
    val curiosity: Float
)

class ChamaAutonomyNeeds(
    initialEnergy: Float = 0.82f,
    initialCuriosity: Float = 0.88f
) {
    private var energy = initialEnergy.coerceIn(0f, 1f)
    private var curiosity = initialCuriosity.coerceIn(0f, 1f)
    private var lastUpdateMs: Long? = null

    init {
        require(initialEnergy.isFinite())
        require(initialCuriosity.isFinite())
    }

    @Synchronized
    fun snapshot() = ChamaAutonomyNeedsSnapshot(energy, curiosity)

    /**
     * Advances needs using monotonic time. Delta is capped at 250 ms so
     * background stalls cannot create sudden exhaustion or instant recovery.
     */
    @Synchronized
    fun advance(
        nowElapsedRealtimeMs: Long,
        activity: ChamaAutonomyActivity?,
        locomotionStatus: ChamaLocomotionStatus
    ): ChamaAutonomyNeedsSnapshot {
        val now = nowElapsedRealtimeMs.coerceAtLeast(0L)
        val previous = lastUpdateMs
        lastUpdateMs = now
        if (previous == null) return snapshot()

        val deltaSeconds = ((now - previous).coerceAtLeast(0L) / 1000f).coerceAtMost(0.25f)
        if (deltaSeconds <= 0f) return snapshot()

        val energyRate = when {
            activity == ChamaAutonomyActivity.REST &&
                locomotionStatus == ChamaLocomotionStatus.ARRIVED -> 0.045f
            activity == ChamaAutonomyActivity.DANCE_IN_PLACE -> -0.022f
            locomotionStatus == ChamaLocomotionStatus.WALKING -> -0.0065f
            locomotionStatus == ChamaLocomotionStatus.TURNING -> -0.0025f
            else -> 0.0008f
        }
        energy = (energy + energyRate * deltaSeconds).coerceIn(0f, 1f)

        val curiosityRate = when (activity) {
            ChamaAutonomyActivity.WALK_TO_WINDOW,
            ChamaAutonomyActivity.WALK_TO_FLOWERS,
            ChamaAutonomyActivity.WALK_TO_SOFA -> 0.0012f
            ChamaAutonomyActivity.REST -> 0.003f
            ChamaAutonomyActivity.LOOK_AROUND -> 0.0018f
            ChamaAutonomyActivity.DANCE_IN_PLACE -> -0.0005f
            null -> 0f
        }
        curiosity = (curiosity + curiosityRate * deltaSeconds).coerceIn(0f, 1f)
        return snapshot()
    }

    @Synchronized
    fun resetClock() {
        lastUpdateMs = null
    }
}
