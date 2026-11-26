package com.kalsu.relive.avatar

import kotlin.math.sqrt

/**
 * Walkable waypoint graph for the current RELIVE salon blockout.
 *
 * Coordinates use the room's X/Z floor plane in metres. The graph deliberately
 * routes around the sofa, coffee table and side console rather than drawing
 * straight lines through furniture. Waypoints include a conservative horizontal
 * clearance for the current blockout; a live renderer must still validate
 * avatar radius, swept movement and dynamic furniture before applying motion.
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
        ChamaRoomWaypoint.ROOM_CENTER to ChamaRoomPoint(0.0f, 0.85f),
        // Keep a body-width margin from the sofa's left arm and the left wall.
        ChamaRoomWaypoint.LEFT_AISLE to ChamaRoomPoint(-1.75f, 0.85f),
        ChamaRoomWaypoint.WINDOW_STOP to ChamaRoomPoint(-1.75f, -1.72f),
        ChamaRoomWaypoint.SOFA_STOP to ChamaRoomPoint(-1.65f, -0.20f),
        // The right aisle stays in front of the coffee table and away from the console.
        ChamaRoomWaypoint.RIGHT_AISLE to ChamaRoomPoint(1.55f, 0.85f),
        ChamaRoomWaypoint.FLOWERS_STOP to ChamaRoomPoint(1.20f, 0.15f)
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

    private fun distance(a: ChamaRoomPoint, b: ChamaRoomPoint): Float {
        val dx = a.x - b.x
        val dz = a.z - b.z
        return sqrt(dx * dx + dz * dz)
    }
}
