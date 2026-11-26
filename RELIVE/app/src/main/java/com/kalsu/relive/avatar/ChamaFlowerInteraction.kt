package com.kalsu.relive.avatar

import kotlin.math.cos
import kotlin.math.sin

/** Current in-room location of the small rose bouquet. */
enum class ChamaFlowerLocation {
    VASE,
    IN_HAND,
    COFFEE_TABLE
}

/** World-space target used by the room renderer to animate the bouquet. */
data class ChamaFlowerPose(
    val x: Float,
    val y: Float,
    val z: Float
) {
    val isFinite: Boolean get() = x.isFinite() && y.isFinite() && z.isFinite()
}

/**
 * Local, deterministic flower interaction.
 *
 * Chama can pick the bouquet only after reaching the validated flower approach.
 * The renderer interpolates between these poses. This is an in-room visual
 * interaction, not physics, object grabbing through real-world sensors, or
 * an action outside the virtual salon.
 */
object ChamaFlowerInteraction {
    val VASE_POSE = ChamaFlowerPose(1.72f, 0.84f, -0.78f)
    val TABLE_POSE = ChamaFlowerPose(0.05f, 0.35f, 0.10f)

    fun poseAt(
        location: ChamaFlowerLocation,
        bodyPosition: ChamaRoomPoint,
        headingDegrees: Float
    ): ChamaFlowerPose = when (location) {
        ChamaFlowerLocation.VASE -> VASE_POSE
        ChamaFlowerLocation.COFFEE_TABLE -> TABLE_POSE
        ChamaFlowerLocation.IN_HAND -> handPose(bodyPosition, headingDegrees)
    }

    /**
     * Mirrors the exact reaching-arm lengths and angles used by ChamaBody3D,
     * then transforms the local hand point by the avatar's world heading.
     */
    fun handPose(
        bodyPosition: ChamaRoomPoint,
        headingDegrees: Float
    ): ChamaFlowerPose {
        require(bodyPosition.x.isFinite() && bodyPosition.z.isFinite())
        require(headingDegrees.isFinite())
        val hand = ChamaBodyPoseMath.reachingArm(
            shoulder = ChamaLimbPoint3D(0.31f, 1.30f, 0f),
            upperArmLength = 0.42f,
            forearmLength = 0.36f,
            lateralAngleDegrees = -35f,
            upperForwardTiltDegrees = 55f,
            forearmForwardTiltDegrees = 60f
        ).hand
        val heading = Math.toRadians(headingDegrees.toDouble())
        val cosHeading = cos(heading).toFloat()
        val sinHeading = sin(heading).toFloat()
        return ChamaFlowerPose(
            x = bodyPosition.x + cosHeading * hand.x + sinHeading * hand.z,
            y = hand.y,
            z = bodyPosition.z - sinHeading * hand.x + cosHeading * hand.z
        )
    }
}

/**
 * Small state machine that prevents impossible object actions:
 * a bouquet must be picked up before it can be placed on the table.
 */
class ChamaFlowerInteractionController(
    initialLocation: ChamaFlowerLocation = ChamaFlowerLocation.VASE
) {
    @Volatile
    private var currentLocation = initialLocation

    val location: ChamaFlowerLocation
        @Synchronized get() = currentLocation

    @Synchronized
    fun pickUp(): Boolean {
        if (currentLocation != ChamaFlowerLocation.VASE) return false
        currentLocation = ChamaFlowerLocation.IN_HAND
        return true
    }

    @Synchronized
    fun placeOnTable(): Boolean {
        if (currentLocation != ChamaFlowerLocation.IN_HAND) return false
        currentLocation = ChamaFlowerLocation.COFFEE_TABLE
        return true
    }

    @Synchronized
    fun dropOnTableIfHeld(): Boolean = placeOnTable()

    @Synchronized
    fun returnToVase(): Boolean {
        if (currentLocation == ChamaFlowerLocation.IN_HAND) return false
        if (currentLocation == ChamaFlowerLocation.VASE) return false
        currentLocation = ChamaFlowerLocation.VASE
        return true
    }
}
