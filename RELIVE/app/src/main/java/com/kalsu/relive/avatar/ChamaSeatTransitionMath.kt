package com.kalsu.relive.avatar

import kotlin.math.max

/**
 * Visual transfer between the collision-safe sofa approach and the cushion.
 *
 * The navigation runtime remains at the safe floor waypoint while this
 * presentation-only arc places the body on the cushion. The seated body is
 * therefore not misrepresented as a floor-navigation point.
 */
data class ChamaSeatPose(
    val position: ChamaRoomPoint,
    val rootHeight: Float
)

object ChamaSeatTransitionMath {
    val SOFA_CUSHION_CENTER = ChamaRoomPoint(-0.48f, -0.92f)

    /**
     * progress=0 is the validated approach; progress=1 is the cushion.
     * Smoothstep avoids abrupt starts/stops and a small arc clears the sofa edge.
     */
    fun pose(
        approach: ChamaRoomPoint,
        cushion: ChamaRoomPoint = SOFA_CUSHION_CENTER,
        progress: Float
    ): ChamaSeatPose {
        val t = progress.coerceIn(0f, 1f)
        if (t <= 0f) return ChamaSeatPose(approach, 0f)
        if (t >= 1f) return ChamaSeatPose(cushion, 0f)
        val eased = t * t * (3f - 2f * t)
        val arc = 4f * eased * (1f - eased)
        return ChamaSeatPose(
            position = ChamaRoomPoint(
                approach.x + (cushion.x - approach.x) * eased,
                approach.z + (cushion.z - approach.z) * eased
            ),
            rootHeight = max(0f, 0.12f * arc)
        )
    }
}
