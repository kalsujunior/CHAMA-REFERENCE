package com.kalsu.relive.avatar


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
    const val DEFAULT_AVATAR_RADIUS_METERS = 0.38f
    const val DEFAULT_SAFETY_MARGIN_METERS = 0.07f

    private const val MIN_X = -2.55f
    private const val MAX_X = 2.60f
    private const val MIN_Z = -2.35f
    private const val MAX_Z = 2.60f

    private val obstacles = listOf(
        // Sofa body plus both armrests.
        ChamaRoomObstacle(-1.22f, 1.32f, -1.36f, -0.53f),
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
        if (!avatarRadiusMeters.isFinite() || !safetyMarginMeters.isFinite() ||
            avatarRadiusMeters < 0f || safetyMarginMeters < 0f
        ) return false
        val clearance = avatarRadiusMeters + safetyMarginMeters
        if (point.x < MIN_X + clearance || point.x > MAX_X - clearance ||
            point.z < MIN_Z + clearance || point.z > MAX_Z - clearance
        ) return false
        return obstacles.none { it.contains(point, clearance) }
    }

    /**
     * Exact swept-disc pre-check in the floor plane.
     *
     * The avatar disc is conservatively represented by expanding each furniture
     * AABB by its radius and safety margin, then testing the complete movement
     * segment against each expanded rectangle with a slab intersection test.
     * Unlike fixed-step sampling, this cannot skip a thin obstacle between samples.
     */
    fun isSegmentWalkable(
        start: ChamaRoomPoint,
        end: ChamaRoomPoint,
        avatarRadiusMeters: Float = DEFAULT_AVATAR_RADIUS_METERS,
        safetyMarginMeters: Float = DEFAULT_SAFETY_MARGIN_METERS,
        sampleSpacingMeters: Float = 0.05f
    ): Boolean {
        // Retained for source compatibility; exact segment intersection no longer
        // depends on a sampling interval.
        if (!sampleSpacingMeters.isFinite() || sampleSpacingMeters <= 0f) return false
        if (!avatarRadiusMeters.isFinite() || !safetyMarginMeters.isFinite() ||
            avatarRadiusMeters < 0f || safetyMarginMeters < 0f
        ) return false
        if (!isPointWalkable(start, avatarRadiusMeters, safetyMarginMeters) ||
            !isPointWalkable(end, avatarRadiusMeters, safetyMarginMeters)
        ) return false

        val clearance = avatarRadiusMeters + safetyMarginMeters
        return obstacles.none { obstacle ->
            segmentIntersectsExpandedObstacle(start, end, obstacle, clearance)
        }
    }

    private fun segmentIntersectsExpandedObstacle(
        start: ChamaRoomPoint,
        end: ChamaRoomPoint,
        obstacle: ChamaRoomObstacle,
        clearance: Float
    ): Boolean {
        val minX = obstacle.minX - clearance
        val maxX = obstacle.maxX + clearance
        val minZ = obstacle.minZ - clearance
        val maxZ = obstacle.maxZ + clearance
        val dx = end.x - start.x
        val dz = end.z - start.z
        var tMin = 0f
        var tMax = 1f

        fun clip(origin: Float, delta: Float, minimum: Float, maximum: Float): Boolean {
            if (kotlin.math.abs(delta) < 1e-7f) {
                return origin >= minimum && origin <= maximum
            }
            var near = (minimum - origin) / delta
            var far = (maximum - origin) / delta
            if (near > far) {
                val swap = near
                near = far
                far = swap
            }
            tMin = maxOf(tMin, near)
            tMax = minOf(tMax, far)
            return tMin <= tMax
        }

        if (!clip(start.x, dx, minX, maxX)) return false
        if (!clip(start.z, dz, minZ, maxZ)) return false
        return tMax >= 0f && tMin <= 1f
    }

    fun validateRoute(route: ChamaRoomRoute): Boolean {
        if (route.points.isEmpty()) return false
        if (route.points.size == 1) return isPointWalkable(route.points.single())
        return route.points.zipWithNext().all { (start, end) ->
            isSegmentWalkable(start, end)
        }
    }
}
