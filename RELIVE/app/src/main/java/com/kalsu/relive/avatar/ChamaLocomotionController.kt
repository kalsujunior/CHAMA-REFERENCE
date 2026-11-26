package com.kalsu.relive.avatar

import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Time-based, renderer-independent locomotion simulation for Chama.
 *
 * This class only produces a proposed root position and heading in the salon's
 * X/Z plane. A SceneView adapter must apply the frame, validate collisions and
 * keep the body-only rig aligned. It does not move SceneView nodes itself.
 */
enum class ChamaLocomotionStatus {
    IDLE,
    WALKING,
    TURNING,
    ARRIVED,
    PAUSED
}

enum class ChamaLocomotionPauseReason {
    DISABLED,
    SPACE_NOT_VISIBLE,
    USER_INTERACTION,
    LOW_POWER
}

data class ChamaLocomotionFrame(
    val position: ChamaRoomPoint,
    /** Degrees around the room's Y axis; zero faces toward positive Z. */
    val headingDegrees: Float,
    val speedMetersPerSecond: Float,
    /** Normalized 0..1 gait phase, advanced by travelled distance. */
    val gaitPhase: Float,
    val status: ChamaLocomotionStatus,
    val pauseReason: ChamaLocomotionPauseReason? = null
)

class ChamaLocomotionController(
    private val maxSpeedMetersPerSecond: Float = 0.72f,
    private val accelerationMetersPerSecondSquared: Float = 1.25f,
    private val turnRateDegreesPerSecond: Float = 150f,
    private val stopRadiusMeters: Float = 0.09f,
    private val strideLengthMeters: Float = 0.68f
) {
    private var routePoints: List<ChamaRoomPoint> = emptyList()
    private var nextPointIndex = 0
    private var position = ChamaRoomPoint(0f, 0f)
    private var heading = 0f
    private var speed = 0f
    private var gaitDistance = 0f
    private var lastUpdateMs: Long? = null
    private var status = ChamaLocomotionStatus.IDLE

    init {
        require(maxSpeedMetersPerSecond > 0f)
        require(accelerationMetersPerSecondSquared > 0f)
        require(turnRateDegreesPerSecond > 0f)
        require(stopRadiusMeters >= 0f)
        require(strideLengthMeters > 0f)
    }

    /** Starts a validated route. The first route point is the expected start. */
    @Synchronized
    fun begin(
        route: ChamaRoomRoute,
        currentPosition: ChamaRoomPoint,
        currentHeadingDegrees: Float,
        nowElapsedRealtimeMs: Long
    ): ChamaLocomotionFrame {
        require(route.points.isNotEmpty()) { "A route must contain at least one point" }
        require(route.points.all { it.x.isFinite() && it.z.isFinite() })
        require(currentPosition.x.isFinite() && currentPosition.z.isFinite())
        require(currentHeadingDegrees.isFinite())
        position = currentPosition
        heading = normalizeAngle(currentHeadingDegrees)
        speed = 0f
        gaitDistance = 0f
        lastUpdateMs = nowElapsedRealtimeMs.coerceAtLeast(0L)
        val first = route.points.first()
        val dx = first.x - currentPosition.x
        val dz = first.z - currentPosition.z
        val startsAtWaypoint = sqrt(dx * dx + dz * dz) <= stopRadiusMeters
        routePoints = if (startsAtWaypoint) route.points else listOf(currentPosition) + route.points
        nextPointIndex = if (routePoints.size > 1) 1 else routePoints.size
        status = if (routePoints.size > 1) ChamaLocomotionStatus.TURNING
            else ChamaLocomotionStatus.ARRIVED
        return frame()
    }

    /**
     * Advances at most 100 ms per call, preventing a delayed frame from causing
     * a large teleport. Any safety interrupt cancels the route immediately.
     */
    @Synchronized
    fun update(
        nowElapsedRealtimeMs: Long,
        enabled: Boolean,
        virtualSpaceVisible: Boolean = true,
        userInteracting: Boolean,
        lowPowerMode: Boolean
    ): ChamaLocomotionFrame {
        val pause = when {
            !enabled -> ChamaLocomotionPauseReason.DISABLED
            !virtualSpaceVisible -> ChamaLocomotionPauseReason.SPACE_NOT_VISIBLE
            userInteracting -> ChamaLocomotionPauseReason.USER_INTERACTION
            lowPowerMode -> ChamaLocomotionPauseReason.LOW_POWER
            else -> null
        }
        if (pause != null) {
            routePoints = emptyList()
            nextPointIndex = 0
            speed = 0f
            lastUpdateMs = nowElapsedRealtimeMs.coerceAtLeast(0L)
            status = ChamaLocomotionStatus.PAUSED
            return frame(pause)
        }
        if (status == ChamaLocomotionStatus.PAUSED) {
            status = ChamaLocomotionStatus.IDLE
            lastUpdateMs = nowElapsedRealtimeMs.coerceAtLeast(0L)
            return frame()
        }
        if (status == ChamaLocomotionStatus.ARRIVED || routePoints.isEmpty()) {
            speed = 0f
            return frame()
        }

        val now = nowElapsedRealtimeMs.coerceAtLeast(0L)
        val previous = lastUpdateMs ?: now
        val deltaSeconds = ((now - previous).coerceAtLeast(0L) / 1000f).coerceAtMost(0.1f)
        lastUpdateMs = now
        if (deltaSeconds <= 0f) return frame()

        var remainingTime = deltaSeconds
        var travelledThisFrame = 0f
        while (remainingTime > 0f && nextPointIndex < routePoints.size) {
            val target = routePoints[nextPointIndex]
            val dx = target.x - position.x
            val dz = target.z - position.z
            val distance = sqrt(dx * dx + dz * dz)
            if (distance <= stopRadiusMeters) {
                position = target
                nextPointIndex++
                continue
            }

            val targetHeading = normalizeAngle(Math.toDegrees(atan2(dx.toDouble(), dz.toDouble())).toFloat())
            val angleDelta = shortestAngleDelta(heading, targetHeading)
            val maxTurn = turnRateDegreesPerSecond * remainingTime
            if (kotlin.math.abs(angleDelta) > 1.5f) {
                val turn = angleDelta.coerceIn(-maxTurn, maxTurn)
                heading = normalizeAngle(heading + turn)
                status = ChamaLocomotionStatus.TURNING
                if (kotlin.math.abs(angleDelta) > maxTurn + 1.5f) {
                    speed = 0f
                    break
                }
            }

            val stoppingSpeed = sqrt((2f * accelerationMetersPerSecondSquared * distance).coerceAtLeast(0f))
            val desiredSpeed = min(maxSpeedMetersPerSecond, stoppingSpeed)
            speed = approach(speed, desiredSpeed, accelerationMetersPerSecondSquared * remainingTime)
            val travel = min(distance, speed * remainingTime)
            if (travel > 0f) {
                val ratio = travel / distance
                position = ChamaRoomPoint(
                    position.x + dx * ratio,
                    position.z + dz * ratio
                )
                travelledThisFrame += travel
                remainingTime = (remainingTime - travel / speed.coerceAtLeast(0.001f)).coerceAtLeast(0f)
                status = ChamaLocomotionStatus.WALKING
            } else {
                break
            }
            if (travel >= distance) {
                position = target
                nextPointIndex++
            }
        }

        gaitDistance += travelledThisFrame
        if (nextPointIndex >= routePoints.size) {
            speed = 0f
            status = ChamaLocomotionStatus.ARRIVED
            routePoints = emptyList()
        }
        return frame()
    }

    @Synchronized
    fun cancel(): ChamaLocomotionFrame {
        routePoints = emptyList()
        nextPointIndex = 0
        speed = 0f
        status = ChamaLocomotionStatus.IDLE
        lastUpdateMs = null
        return frame()
    }

    /** Rolls back to the last known safe pose after a failed runtime guard. */
    @Synchronized
    fun holdAt(safePosition: ChamaRoomPoint, safeHeadingDegrees: Float): ChamaLocomotionFrame {
        require(safePosition.x.isFinite() && safePosition.z.isFinite())
        require(safeHeadingDegrees.isFinite())
        routePoints = emptyList()
        nextPointIndex = 0
        position = safePosition
        heading = normalizeAngle(safeHeadingDegrees)
        speed = 0f
        status = ChamaLocomotionStatus.IDLE
        lastUpdateMs = null
        return frame()
    }

    private fun frame(pause: ChamaLocomotionPauseReason? = null) = ChamaLocomotionFrame(
        position = position,
        headingDegrees = heading,
        speedMetersPerSecond = speed,
        gaitPhase = ((gaitDistance / strideLengthMeters) % 1f).coerceIn(0f, 1f),
        status = status,
        pauseReason = pause
    )

    private fun approach(current: Float, target: Float, maxDelta: Float): Float =
        when {
            current < target -> min(target, current + maxDelta)
            else -> kotlin.math.max(target, current - maxDelta)
        }

    private fun normalizeAngle(value: Float): Float {
        var angle = value % 360f
        if (angle > 180f) angle -= 360f
        if (angle < -180f) angle += 360f
        return angle
    }

    private fun shortestAngleDelta(from: Float, to: Float): Float =
        normalizeAngle(to - from)
}
