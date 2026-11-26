package com.kalsu.relive.avatar

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** A point in Chama's body-local 3D coordinate system (metres). */
data class ChamaLimbPoint3D(val x: Float, val y: Float, val z: Float)

data class ChamaArmReachPose(
    val elbow: ChamaLimbPoint3D,
    val wrist: ChamaLimbPoint3D,
    val hand: ChamaLimbPoint3D
)

data class ChamaLegChainPose(
    val knee: ChamaLimbPoint3D,
    val ankle: ChamaLimbPoint3D
)

data class ChamaSegmentRotation(
    val xDegrees: Float,
    val zDegrees: Float
)

/**
 * Renderer-independent limb geometry. Keeping these calculations in pure
 * Kotlin makes the seated and reaching poses testable without starting Filament.
 */
object ChamaBodyPoseMath {
    private val seatedThighCos = cos(Math.toRadians(55.0)).toFloat()
    private val seatedCalfCos = cos(Math.toRadians(45.0)).toFloat()

    /**
     * Euler angles that align a vertical capsule's local +Y axis with a
     * start-to-end vector using SceneView's X-then-Z Euler convention.
     * The returned angles are derived from the actual joint coordinates,
     * avoiding visual gaps between the capsules and their joints.
     */
    fun segmentRotation(
        start: ChamaLimbPoint3D,
        end: ChamaLimbPoint3D
    ): ChamaSegmentRotation {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val dz = end.z - start.z
        require(dx.isFinite() && dy.isFinite() && dz.isFinite())
        val radial = sqrt(dy * dy + dz * dz)
        require(dx * dx + radial * radial > 1e-10f) { "A segment must have non-zero length" }
        val verticalSign = if (dy < 0f) -1f else 1f
        val x = atan2(dz * verticalSign, kotlin.math.abs(dy))
        val z = atan2(-dx, verticalSign * radial)
        return ChamaSegmentRotation(
            xDegrees = Math.toDegrees(x.toDouble()).toFloat(),
            zDegrees = Math.toDegrees(z.toDouble()).toFloat()
        )
    }

    fun reachingArm(
        shoulder: ChamaLimbPoint3D,
        upperArmLength: Float = 0.42f,
        forearmLength: Float = 0.36f,
        lateralAngleDegrees: Float = 6f,
        upperForwardTiltDegrees: Float = 25f,
        forearmForwardTiltDegrees: Float = 35f,
        handLength: Float = 0.09f
    ): ChamaArmReachPose {
        require(upperArmLength.isFinite() && upperArmLength >= 0f)
        require(forearmLength.isFinite() && forearmLength >= 0f)
        require(handLength.isFinite() && handLength >= 0f)
        require(lateralAngleDegrees.isFinite())
        require(upperForwardTiltDegrees.isFinite())
        require(forearmForwardTiltDegrees.isFinite())

        fun endpoint(start: ChamaLimbPoint3D, length: Float, forwardTilt: Float): ChamaLimbPoint3D {
            val lateral = Math.toRadians(lateralAngleDegrees.toDouble())
            val forward = Math.toRadians(forwardTilt.toDouble())
            val horizontal = cos(forward).toFloat()
            return ChamaLimbPoint3D(
                x = start.x + sin(lateral).toFloat() * horizontal * length,
                y = start.y - cos(lateral).toFloat() * horizontal * length,
                z = start.z + sin(forward).toFloat() * length
            )
        }

        val elbow = endpoint(shoulder, upperArmLength, upperForwardTiltDegrees)
        val wrist = endpoint(elbow, forearmLength, forearmForwardTiltDegrees)
        val hand = ChamaLimbPoint3D(
            x = wrist.x,
            y = wrist.y - handLength * cos(Math.toRadians(forearmForwardTiltDegrees.toDouble())).toFloat(),
            z = wrist.z + handLength * sin(Math.toRadians(forearmForwardTiltDegrees.toDouble())).toFloat()
        )
        return ChamaArmReachPose(elbow, wrist, hand)
    }

    /**
     * Blends a standing two-link leg into a seated pose. The seated knee and
     * ankle travel forward in local +Z while the feet remain close to floor
     * height once the body root is lowered to the sofa cushion.
     */
    fun blendLegToSeat(
        hipX: Float,
        hipY: Float,
        standingKnee: ChamaLimbPoint3D,
        standingAnkle: ChamaLimbPoint3D,
        blend: Float,
        thighLength: Float = 0.36f,
        calfLength: Float = 0.34f,
        seatedThighAngleDegrees: Float = 55f,
        seatedCalfAngleDegrees: Float = 45f,
        seatedKneeForwardMeters: Float = 0.30f,
        seatedFootForwardMeters: Float = 0.54f
    ): ChamaLegChainPose {
        require(blend.isFinite())
        require(thighLength > 0f && calfLength > 0f)
        val amount = blend.coerceIn(0f, 1f)
        val thighCos = if (seatedThighAngleDegrees == 55f) seatedThighCos
            else cos(Math.toRadians(seatedThighAngleDegrees.toDouble())).toFloat()
        val calfCos = if (seatedCalfAngleDegrees == 45f) seatedCalfCos
            else cos(Math.toRadians(seatedCalfAngleDegrees.toDouble())).toFloat()
        val seatedKneeY = hipY - thighCos * thighLength
        val seatedAnkleY = seatedKneeY - calfCos * calfLength
        fun mix(a: Float, b: Float) = a + (b - a) * amount
        return ChamaLegChainPose(
            knee = ChamaLimbPoint3D(
                mix(standingKnee.x, hipX),
                mix(standingKnee.y, seatedKneeY),
                mix(standingKnee.z, seatedKneeForwardMeters)
            ),
            ankle = ChamaLimbPoint3D(
                mix(standingAnkle.x, hipX),
                mix(standingAnkle.y, seatedAnkleY),
                mix(standingAnkle.z, seatedFootForwardMeters)
            )
        )
    }
}
