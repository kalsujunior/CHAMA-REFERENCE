package com.kalsu.relive.avatar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChamaAutonomyTest {

    @Test
    fun everyWaypointCanReachEverySemanticTarget() {
        ChamaRoomWaypoint.values().forEach { start ->
            ChamaAutonomyTarget.values().forEach { target ->
                val route = ChamaRoomNavigation.findRoute(start, target)
                assertNotNull("Missing route from $start to $target", route)
                assertTrue(
                    "Unsafe route from $start to $target",
                    ChamaRoomCollisionMap.validateRoute(route!!)
                )
            }
        }
    }

    @Test
    fun sofaSeatIsReachableAndOutsideExpandedFurnitureFootprint() {
        val seat = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.SOFA_SEAT)
        val approach = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.SOFA_STOP)
        assertTrue(ChamaRoomCollisionMap.isPointWalkable(seat))
        assertTrue(ChamaRoomCollisionMap.isSegmentWalkable(approach, seat))
        val route = ChamaRoomNavigation.findRoute(
            ChamaRoomWaypoint.ROOM_CENTER,
            ChamaAutonomyTarget.SOFA
        )
        assertNotNull(route)
        assertEquals(ChamaRoomWaypoint.SOFA_SEAT, route!!.waypoints.last())
        assertTrue(ChamaRoomCollisionMap.validateRoute(route))
    }

    @Test
    fun autonomousRestArrivesAtSofaSeatAndUsesSitGesture() {
        val runtime = ChamaAutonomyRuntime()
        var frame = runtime.tick(
            ChamaAutonomyRuntimeInput(
                enabled = true,
                virtualSpaceVisible = true,
                userInteracting = false,
                lowPowerMode = false,
                energy = 0.1f,
                curiosity = 0.5f,
                nowElapsedRealtimeMs = 0L
            )
        )
        for (time in 50L..30_000L step 50L) {
            frame = runtime.tick(
                ChamaAutonomyRuntimeInput(
                    enabled = true,
                    virtualSpaceVisible = true,
                    userInteracting = false,
                    lowPowerMode = false,
                    energy = 0.1f,
                    curiosity = 0.5f,
                    nowElapsedRealtimeMs = time
                )
            )
            if (frame.locomotion.status == ChamaLocomotionStatus.ARRIVED) break
        }
        assertEquals(ChamaRoomWaypoint.SOFA_SEAT, frame.currentWaypoint)
        assertEquals(ChamaLocomotionStatus.ARRIVED, frame.locomotion.status)
        assertEquals(ChamaBodyGesture.SIT, frame.bodyGesture)
    }

    @Test
    fun plannerUsesReachGestureAtFlowersAndSitGestureForRest() {
        val planner = ChamaAutonomyPlanner()
        fun input(now: Long, energy: Float) = ChamaAutonomyInput(
            enabled = true,
            virtualSpaceVisible = true,
            userInteracting = false,
            lowPowerMode = false,
            energy = energy,
            curiosity = 0.9f,
            nowElapsedRealtimeMs = now
        )
        planner.decide(input(0L, 0.8f))
        val flowers = planner.decide(input(18_000L, 0.8f))
        assertEquals(ChamaAutonomyActivity.WALK_TO_FLOWERS, flowers.activity)
        assertEquals(ChamaBodyGesture.REACH, flowers.bodyGesture)

        val tired = ChamaAutonomyPlanner().decide(input(0L, 0.1f))
        assertEquals(ChamaAutonomyActivity.REST, tired.activity)
        assertEquals(ChamaBodyGesture.SIT, tired.bodyGesture)
        assertEquals(ChamaAutonomyTarget.SOFA, tired.target)
    }

    @Test
    fun routePlannerRejectsStraightPathThroughFurniture() {
        val start = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.ROOM_CENTER)
        val farSide = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.WINDOW_STOP)
        assertFalse(ChamaRoomCollisionMap.isSegmentWalkable(start, farSide))
    }

    @Test
    fun nearestWaypointMustHaveClearConnector() {
        val point = ChamaRoomPoint(-0.72f, 1.10f)
        val nearest = ChamaRoomNavigation.nearestReachableWaypoint(point)
        assertNotNull(nearest)
        assertTrue(
            ChamaRoomCollisionMap.isSegmentWalkable(
                point,
                ChamaRoomNavigation.pointOf(nearest!!)
            )
        )
    }

    @Test
    fun locomotionReachesWindowWithoutTeleporting() {
        val route = ChamaRoomNavigation.findRoute(
            ChamaRoomWaypoint.ROOM_CENTER,
            ChamaAutonomyTarget.WINDOW
        )!!
        val controller = ChamaLocomotionController()
        var frame = controller.begin(
            route = route,
            currentPosition = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.ROOM_CENTER),
            currentHeadingDegrees = 0f,
            nowElapsedRealtimeMs = 0L
        )

        var previousDistance = Float.POSITIVE_INFINITY
        for (time in 50L..30_000L step 50L) {
            frame = controller.update(
                nowElapsedRealtimeMs = time,
                enabled = true,
                userInteracting = false,
                lowPowerMode = false
            )
            assertTrue("A single frame must not teleport", frame.speedMetersPerSecond <= 0.73f)
            if (frame.status != ChamaLocomotionStatus.ARRIVED) {
                val target = route.points.last()
                val dx = target.x - frame.position.x
                val dz = target.z - frame.position.z
                val distance = kotlin.math.sqrt(dx * dx + dz * dz)
                assertTrue("Controller moved away from destination", distance <= previousDistance + 0.02f)
                previousDistance = distance
            }
            if (frame.status == ChamaLocomotionStatus.ARRIVED) break
        }

        assertEquals(ChamaLocomotionStatus.ARRIVED, frame.status)
        assertEquals(route.points.last().x, frame.position.x, 0.001f)
        assertEquals(route.points.last().z, frame.position.z, 0.001f)
    }

    @Test
    fun userInteractionImmediatelyCancelsActiveLocomotion() {
        val route = ChamaRoomNavigation.findRoute(
            ChamaRoomWaypoint.ROOM_CENTER,
            ChamaAutonomyTarget.WINDOW
        )!!
        val controller = ChamaLocomotionController()
        controller.begin(
            route,
            ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.ROOM_CENTER),
            0f,
            0L
        )
        controller.update(500L, enabled = true, userInteracting = false, lowPowerMode = false)

        val paused = controller.update(
            nowElapsedRealtimeMs = 510L,
            enabled = true,
            userInteracting = true,
            lowPowerMode = false
        )
        assertEquals(ChamaLocomotionStatus.PAUSED, paused.status)
        assertEquals(0f, paused.speedMetersPerSecond, 0.0001f)
        assertEquals(ChamaLocomotionPauseReason.USER_INTERACTION, paused.pauseReason)
    }

    @Test
    fun autonomyRuntimeCanReplanAfterInterruption() {
        val runtime = ChamaAutonomyRuntime()
        var frame = runtime.tick(input(now = 0L))
        for (time in 50L..1_000L step 50L) {
            frame = runtime.tick(input(now = time))
        }
        assertTrue(
            frame.locomotion.status == ChamaLocomotionStatus.WALKING ||
                frame.locomotion.status == ChamaLocomotionStatus.TURNING
        )

        val interrupted = runtime.tick(input(now = 1_050L, userInteracting = true))
        assertEquals(ChamaAutonomyPauseReason.USER_INTERACTION, interrupted.decision.pauseReason)
        assertEquals(ChamaLocomotionStatus.PAUSED, interrupted.locomotion.status)

        val resumed = runtime.tick(input(now = 1_100L))
        assertNull(resumed.routeError)
        assertTrue(
            resumed.locomotion.status == ChamaLocomotionStatus.WALKING ||
                resumed.locomotion.status == ChamaLocomotionStatus.TURNING ||
                resumed.locomotion.status == ChamaLocomotionStatus.ARRIVED
        )
    }

    @Test
    fun runtimePausesWhenSpaceIsHiddenAndReplansWhenVisible() {
        val runtime = ChamaAutonomyRuntime()
        runtime.tick(input(now = 0L))
        for (time in 50L..500L step 50L) {
            runtime.tick(input(now = time))
        }

        val hidden = runtime.tick(input(now = 550L).copy(virtualSpaceVisible = false))
        assertEquals(ChamaAutonomyPauseReason.SPACE_NOT_VISIBLE, hidden.decision.pauseReason)
        assertEquals(ChamaLocomotionStatus.PAUSED, hidden.locomotion.status)

        val visible = runtime.tick(input(now = 600L))
        assertNull(visible.routeError)
        assertTrue(
            visible.locomotion.status == ChamaLocomotionStatus.WALKING ||
                visible.locomotion.status == ChamaLocomotionStatus.TURNING ||
                visible.locomotion.status == ChamaLocomotionStatus.ARRIVED
        )

        val powerSave = runtime.tick(input(now = 650L).copy(lowPowerMode = true))
        assertEquals(ChamaAutonomyPauseReason.LOW_POWER, powerSave.decision.pauseReason)
        assertEquals(ChamaLocomotionStatus.PAUSED, powerSave.locomotion.status)
    }

    @Test
    fun failedCollisionGuardCanRestoreLastSafePose() {
        val safePosition = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.ROOM_CENTER)
        val controller = ChamaLocomotionController()
        val route = ChamaRoomNavigation.findRoute(
            ChamaRoomWaypoint.ROOM_CENTER,
            ChamaAutonomyTarget.WINDOW
        )!!
        controller.begin(route, safePosition, 0f, 0L)
        controller.update(500L, enabled = true, userInteracting = false, lowPowerMode = false)

        val held = controller.holdAt(safePosition, 0f)
        assertEquals(safePosition, held.position)
        assertEquals(0f, held.speedMetersPerSecond, 0.0001f)
        assertEquals(ChamaLocomotionStatus.IDLE, held.status)
    }

    @Test
    fun arrivedCharacterCanFaceTheSemanticDestination() {
        val point = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.FLOWERS_STOP)
        val flowerHeading = ChamaRoomNavigation.facingDegreesAt(
            ChamaAutonomyTarget.FLOWERS,
            point
        )
        assertTrue("Chama should face toward the flower vase", flowerHeading in 90f..180f)
        assertEquals(
            0f,
            ChamaRoomNavigation.facingDegreesAt(
                ChamaAutonomyTarget.ROOM_CENTER,
                ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.ROOM_CENTER)
            ),
            0.001f
        )

        val route = ChamaRoomNavigation.findRoute(
            ChamaRoomWaypoint.ROOM_CENTER,
            ChamaAutonomyTarget.ROOM_CENTER
        )!!
        val controller = ChamaLocomotionController()
        controller.begin(
            route,
            ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.ROOM_CENTER),
            0f,
            0L
        )
        val oriented = controller.orientAtArrival(90f)
        assertEquals(90f, oriented.headingDegrees, 0.001f)
        assertEquals(ChamaLocomotionStatus.ARRIVED, oriented.status)
    }

    @Test
    fun plannerSchedulesRestAndOccasionalDance() {
        val planner = ChamaAutonomyPlanner()
        fun plannerInput(now: Long, energy: Float) = ChamaAutonomyInput(
            enabled = true,
            virtualSpaceVisible = true,
            userInteracting = false,
            lowPowerMode = false,
            energy = energy,
            curiosity = 0.90f,
            nowElapsedRealtimeMs = now
        )

        assertEquals(
            ChamaAutonomyActivity.WALK_TO_WINDOW,
            planner.decide(plannerInput(0L, 0.75f)).activity
        )
        planner.decide(plannerInput(18_000L, 0.75f))
        planner.decide(plannerInput(36_000L, 0.75f))
        assertEquals(
            ChamaAutonomyActivity.DANCE_IN_PLACE,
            planner.decide(plannerInput(54_000L, 0.75f)).activity
        )

        val tiredPlanner = ChamaAutonomyPlanner()
        assertEquals(
            ChamaAutonomyActivity.REST,
            tiredPlanner.decide(plannerInput(0L, 0.12f)).activity
        )
    }

    @Test
    fun energyDecreasesDuringWalkingAndRecoversDuringRest() {
        val needs = ChamaAutonomyNeeds()
        needs.advance(0L, null, ChamaLocomotionStatus.IDLE)
        val initial = needs.snapshot()

        for (tick in 1L..20L) {
            needs.advance(
                tick * 50L,
                ChamaAutonomyActivity.WALK_TO_WINDOW,
                ChamaLocomotionStatus.WALKING
            )
        }
        val afterWalking = needs.snapshot()
        assertTrue(afterWalking.energy < initial.energy)
        assertTrue(afterWalking.curiosity > initial.curiosity)

        for (tick in 21L..40L) {
            needs.advance(
                tick * 50L,
                ChamaAutonomyActivity.REST,
                ChamaLocomotionStatus.ARRIVED
            )
        }
        assertTrue(needs.snapshot().energy > afterWalking.energy)
    }

    private fun input(
        now: Long,
        userInteracting: Boolean = false
    ) = ChamaAutonomyRuntimeInput(
        enabled = true,
        virtualSpaceVisible = true,
        userInteracting = userInteracting,
        lowPowerMode = false,
        energy = 0.82f,
        curiosity = 0.88f,
        nowElapsedRealtimeMs = now
    )
}
