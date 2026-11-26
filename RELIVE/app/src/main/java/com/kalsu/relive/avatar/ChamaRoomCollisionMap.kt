package com.kalsu.relive.avatar

import kotlin.math.ceil

/** Axis-aligned furniture footprint on the salon floor plane. */
data class ChamaRoomObstacle(
    val minX: Float,
    val maxX: Float,
    val minZ: Float,
    val maxZ: Float
) {
    init {
        require(minX <= maxX && minZ <= maxZ)
    }

    fun contains(point: ChamaRoomPoint, clearance: Float): Boolean =
        point.x >= minX - clearance && point.x <= maxX + clearance &&
            point.z >= minZ - clearance && point.z <= maxZ + clearance
}

/**
 * Conservative static collision map for the current room blockout.
 *
 * Furniture footprints are expanded by the avatar radius plus a safety margin.
 * This is a geometric pre-check, not a substitute for live SceneView collision
 * detection or continuous collision detection in the final renderer.
 */
object ChamaRoomCollisionMap {
    const val DEFAULT_AVATAR_RADIUS_METERS = 0.30f
    const val DEFAULT_SAFETY_MARGIN_METERS = 0.05f

    private const val MIN_X = -2.20f
    private const val MAX_X = 2.20f
    private const val MIN_Z = -2.00f
    private const val MAX_Z = 2.20f

    private val obstacles = listOf(
        // Sofa body plus both armrests.
        ChamaRoomObstacle(-1.22f, 1.32f, -1.31f, -0.53f),
        // Coffee table top and its supporting footprint.
        ChamaRoomObstacle(-0.65f, 0.75f, -0.235f, 0.435f),
        // Side console and lamp base.
        ChamaRoomObstacle(1.48f, 1.96f, -0.99f, -0.57f),
        // Decorative plant pot on the right side.
        ChamaRoomObstacle(1.84f, 2.20f, -1.83f, -1.47f)
    )

    fun isPointWalkable(
        point: ChamaRoomPoint,
        avatarRadiusMeters: Float = DEFAULT_AVATAR_RADIUS_METERS,
        safetyMarginMeters: Float = DEFAULT_SAFETY_MARGIN_METERS
    ): Boolean {
        if (!point.x.isFinite() || !point.z.isFinite()) return false
        if (avatarRadiusMeters < 0f || safetyMarginMeters < 0f) return false
        val clearance = avatarRadiusMeters + safetyMarginMeters
        if (point.x < MIN_X + clearance || point.x > MAX_X - clearance ||
            point.z < MIN_Z + clearance || point.z > MAX_Z - clearance
        ) return false
        return obstacles.none { it.contains(point, clearance) }
    }

    /**
     * Samples a segment at no more than [sampleSpacingMeters] intervals.
     * Final runtime movement must still use swept-volume collision checks.
     */
    fun isSegmentWalkable(
        start: ChamaRoomPoint,
        end: ChamaRoomPoint,
        avatarRadiusMeters: Float = DEFAULT_AVATAR_RADIUS_METERS,
        safetyMarginMeters: Float = DEFAULT_SAFETY_MARGIN_METERS,
        sampleSpacingMeters: Float = 0.05f
    ): Boolean {
        if (sampleSpacingMeters <= 0f) return false
        if (!isPointWalkable(start, avatarRadiusMeters, safetyMarginMeters) ||
            !isPointWalkable(end, avatarRadiusMeters, safetyMarginMeters)
        ) return false

        val dx = end.x - start.x
        val dz = end.z - start.z
        val length = kotlin.math.sqrt(dx * dx + dz * dz)
        val steps = ceil(length / sampleSpacingMeters).toInt().coerceAtLeast(1)
        for (step in 1 until steps) {
            val fraction = step.toFloat() / steps.toFloat()
            val sample = ChamaRoomPoint(start.x + dx * fraction, start.z + dz * fraction)
            if (!isPointWalkable(sample, avatarRadiusMeters, safetyMarginMeters)) return false
        }
        return true
    }

    fun validateRoute(route: ChamaRoomRoute): Boolean {
        if (route.points.isEmpty()) return false
        if (route.points.size == 1) return isPointWalkable(route.points.single())
        return route.points.zipWithNext().all { (start, end) ->
            isSegmentWalkable(start, end)
        }
    }
}
