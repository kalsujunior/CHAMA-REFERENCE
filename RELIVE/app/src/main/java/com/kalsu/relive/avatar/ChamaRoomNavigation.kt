package com.kalsu.relive.avatar

import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Walkable waypoint graph for the current RELIVE salon blockout.
 *
 * Coordinates use the room's X/Z floor plane in metres. The graph deliberately
 * routes around the sofa, coffee table and side console rather than drawing
 * straight lines through furniture. Waypoints include conservative clearance
 * for the current blockout; ChamaAutonomyRuntime rechecks every motion segment.
 * Dynamic furniture and the full articulated avatar volume still need device tests.
 */
enum class ChamaRoomWaypoint {
    ROOM_CENTER,
    LEFT_AISLE,
    WINDOW_STOP,
    SOFA_STOP,
    RIGHT_AISLE,
    FLOWERS_STOP
}

data class ChamaRoomPoint(val x: Float, val z: Float)

data class ChamaRoomRoute(
    val waypoints: List<ChamaRoomWaypoint>,
    val points: List<ChamaRoomPoint>,
    val lengthMeters: Float
) {
    init {
        require(waypoints.isNotEmpty())
        require(waypoints.size == points.size)
        require(lengthMeters >= 0f)
    }
}

object ChamaRoomNavigation {
    private val positions = mapOf(
        ChamaRoomWaypoint.ROOM_CENTER to ChamaRoomPoint(0.0f, 1.10f),
        // Keep a body-width margin from the sofa's left arm and the left wall.
        ChamaRoomWaypoint.LEFT_AISLE to ChamaRoomPoint(-1.95f, 1.10f),
        ChamaRoomWaypoint.WINDOW_STOP to ChamaRoomPoint(-1.95f, -1.50f),
        ChamaRoomWaypoint.SOFA_STOP to ChamaRoomPoint(-1.95f, -0.15f),
        // The right aisle stays in front of the coffee table and away from the console.
        ChamaRoomWaypoint.RIGHT_AISLE to ChamaRoomPoint(1.55f, 1.10f),
        ChamaRoomWaypoint.FLOWERS_STOP to ChamaRoomPoint(1.30f, 0.15f)
    )

    private val edges = listOf(
        ChamaRoomWaypoint.ROOM_CENTER to ChamaRoomWaypoint.LEFT_AISLE,
        ChamaRoomWaypoint.LEFT_AISLE to ChamaRoomWaypoint.WINDOW_STOP,
        ChamaRoomWaypoint.LEFT_AISLE to ChamaRoomWaypoint.SOFA_STOP,
        ChamaRoomWaypoint.ROOM_CENTER to ChamaRoomWaypoint.RIGHT_AISLE,
        ChamaRoomWaypoint.RIGHT_AISLE to ChamaRoomWaypoint.FLOWERS_STOP
    )

    private val targetWaypoints = mapOf(
        ChamaAutonomyTarget.ROOM_CENTER to ChamaRoomWaypoint.ROOM_CENTER,
        ChamaAutonomyTarget.WINDOW to ChamaRoomWaypoint.WINDOW_STOP,
        ChamaAutonomyTarget.SOFA to ChamaRoomWaypoint.SOFA_STOP,
        ChamaAutonomyTarget.FLOWERS to ChamaRoomWaypoint.FLOWERS_STOP
    )

    /** Returns a shortest waypoint route, or null if a target is not mapped. */
    fun findRoute(
        start: ChamaRoomWaypoint,
        target: ChamaAutonomyTarget
    ): ChamaRoomRoute? {
        val destination = targetWaypoints[target] ?: return null
        val startPoint = positions.getValue(start)
        val destinationPoint = positions.getValue(destination)
        if (!ChamaRoomCollisionMap.isPointWalkable(startPoint) ||
            !ChamaRoomCollisionMap.isPointWalkable(destinationPoint)
        ) return null
        if (start == destination) {
            return ChamaRoomRoute(listOf(start), listOf(startPoint), 0f)
        }

        val distances = ChamaRoomWaypoint.values().associateWith { Float.POSITIVE_INFINITY }.toMutableMap()
        val previous = mutableMapOf<ChamaRoomWaypoint, ChamaRoomWaypoint>()
        val unvisited = ChamaRoomWaypoint.values().toMutableSet()
        distances[start] = 0f

        while (unvisited.isNotEmpty()) {
            val current = unvisited.minByOrNull { distances.getValue(it) } ?: break
            val currentDistance = distances.getValue(current)
            if (!currentDistance.isFinite()) break
            unvisited.remove(current)
            if (current == destination) break

            edges.forEach { (a, b) ->
                val neighbour = when (current) {
                    a -> b
                    b -> a
                    else -> null
                } ?: return@forEach
                if (neighbour !in unvisited) return@forEach
                val currentPoint = positions.getValue(current)
                val neighbourPoint = positions.getValue(neighbour)
                if (!ChamaRoomCollisionMap.isSegmentWalkable(currentPoint, neighbourPoint)) {
                    return@forEach
                }
                val candidate = currentDistance + distance(currentPoint, neighbourPoint)
                if (candidate < distances.getValue(neighbour)) {
                    distances[neighbour] = candidate
                    previous[neighbour] = current
                }
            }
        }

        if (!distances.getValue(destination).isFinite()) return null
        val reversed = mutableListOf(destination)
        var cursor = destination
        while (cursor != start) {
            cursor = previous[cursor] ?: return null
            reversed += cursor
        }
        val route = reversed.asReversed()
        return ChamaRoomRoute(
            waypoints = route,
            points = route.map { positions.getValue(it) },
            lengthMeters = distances.getValue(destination)
        )
    }

    fun pointOf(waypoint: ChamaRoomWaypoint): ChamaRoomPoint =
        positions.getValue(waypoint)

    fun waypointOf(target: ChamaAutonomyTarget): ChamaRoomWaypoint =
        targetWaypoints.getValue(target)

    /**
     * Finds the closest waypoint connected to the live position by a clear
     * segment. Used to safely re-anchor the planner after an interrupted walk.
     */
    fun nearestReachableWaypoint(point: ChamaRoomPoint): ChamaRoomWaypoint? =
        positions.entries
            .filter { (_, waypointPoint) ->
                waypointPoint.x.isFinite() && waypointPoint.z.isFinite() &&
                    ChamaRoomCollisionMap.isSegmentWalkable(point, waypointPoint)
            }
            .minByOrNull { (_, waypointPoint) -> distance(point, waypointPoint) }
            ?.key

    /** Heading for the body to face the semantic object after arriving. */
    fun facingDegreesAt(target: ChamaAutonomyTarget, from: ChamaRoomPoint): Float {
        val focus = when (target) {
            ChamaAutonomyTarget.WINDOW -> ChamaRoomPoint(-1.15f, -2.29f)
            ChamaAutonomyTarget.FLOWERS -> ChamaRoomPoint(1.72f, -0.78f)
            ChamaAutonomyTarget.SOFA -> ChamaRoomPoint(0.05f, -0.92f)
            ChamaAutonomyTarget.ROOM_CENTER -> ChamaRoomPoint(0f, 0f)
        }
        val dx = focus.x - from.x
        val dz = focus.z - from.z
        return Math.toDegrees(atan2(dx.toDouble(), dz.toDouble())).toFloat()
    }

    private fun distance(a: ChamaRoomPoint, b: ChamaRoomPoint): Float {
        val dx = a.x - b.x
        val dz = a.z - b.z
        return sqrt(dx * dx + dz * dz)
    }
}
