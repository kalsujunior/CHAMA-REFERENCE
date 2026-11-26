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
    fun bouquetStateMachineRequiresPickupBeforePlacement() {
        val flowers = ChamaFlowerInteractionController()
        assertEquals(ChamaFlowerLocation.VASE, flowers.location)
        assertFalse(flowers.placeOnTable())
        assertTrue(flowers.pickUp())
        assertEquals(ChamaFlowerLocation.IN_HAND, flowers.location)
        assertFalse(flowers.pickUp())
        assertTrue(flowers.placeOnTable())
        assertEquals(ChamaFlowerLocation.COFFEE_TABLE, flowers.location)
        assertFalse(flowers.placeOnTable())
        assertTrue(flowers.returnToVase())
        assertEquals(ChamaFlowerLocation.VASE, flowers.location)
    }

    @Test
    fun interruptedFlowerPickupDropsBouquetSafelyOnCoffeeTable() {
        val flowers = ChamaFlowerInteractionController()
        assertTrue(flowers.pickUp())
        assertTrue(flowers.dropOnTableIfHeld())
        assertEquals(ChamaFlowerLocation.COFFEE_TABLE, flowers.location)
        assertFalse(flowers.dropOnTableIfHeld())
    }

    @Test
    fun flowerPickupPosePlacesBouquetAtTheLoweredReachingHand() {
        val approach = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.FLOWERS_STOP)
        val heading = ChamaRoomNavigation.facingDegreesAt(ChamaAutonomyTarget.FLOWERS, approach)
        val hand = ChamaFlowerInteraction.handPose(approach, heading)
        assertTrue(hand.isFinite)
        assertEquals(0.9102f, hand.y, 0.002f)
        // The hand is aimed into the console/vase area, not across the room.
        assertTrue(kotlin.math.abs(hand.x - ChamaFlowerInteraction.VASE_POSE.x) < 0.15f)
        assertTrue(kotlin.math.abs(hand.z - ChamaFlowerInteraction.VASE_POSE.z) < 0.15f)
        assertTrue(kotlin.math.abs(hand.y - ChamaFlowerInteraction.VASE_POSE.y) < 0.10f)
        val dx = hand.x - ChamaFlowerInteraction.VASE_POSE.x
        val dy = hand.y - ChamaFlowerInteraction.VASE_POSE.y
        val dz = hand.z - ChamaFlowerInteraction.VASE_POSE.z
        val handToRoseDistance = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
        assertTrue("Palm and rose heads should visually overlap", handToRoseDistance < 0.18f)
        // The rose head bottom nearly touches the coffee table top (0.2725 m).
        assertEquals(0.275f, ChamaFlowerInteraction.TABLE_POSE.y - 0.075f, 0.001f)
    }

    @Test
    fun bouquetLocationsStayInsideTheSalonAndOnStableSurfaces() {
        val body = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.FLOWERS_STOP)
        val vase = ChamaFlowerInteraction.poseAt(ChamaFlowerLocation.VASE, body, 0f)
        val table = ChamaFlowerInteraction.poseAt(ChamaFlowerLocation.COFFEE_TABLE, body, 0f)
        val hand = ChamaFlowerInteraction.poseAt(ChamaFlowerLocation.IN_HAND, body, 150f)
        assertEquals(ChamaFlowerInteraction.VASE_POSE, vase)
        assertEquals(ChamaFlowerInteraction.TABLE_POSE, table)
        assertTrue(hand.isFinite)
        assertTrue(table.x in -0.65f..0.75f)
        assertTrue(table.z in -0.235f..0.435f)
    }

    @Test
    fun sweptCollisionRejectsObstacleEvenWhenSamplingWouldSkipIt() {
        val start = ChamaRoomPoint(-2.0f, -0.1f)
        val end = ChamaRoomPoint(2.0f, -0.1f)
        assertTrue(ChamaRoomCollisionMap.isPointWalkable(start))
        assertTrue(ChamaRoomCollisionMap.isPointWalkable(end))
        // A 10 m sampling interval would previously test no interior point.
        assertFalse(
            ChamaRoomCollisionMap.isSegmentWalkable(
                start = start,
                end = end,
                sampleSpacingMeters = 10f
            )
        )
    }

    @Test
    fun sweptCollisionRejectsTangentialContactWithExpandedFurniture() {
        val start = ChamaRoomPoint(-2.0f, -0.08f)
        val end = ChamaRoomPoint(2.0f, -0.08f)
        assertTrue(ChamaRoomCollisionMap.isPointWalkable(start))
        assertTrue(ChamaRoomCollisionMap.isPointWalkable(end))
        assertFalse(ChamaRoomCollisionMap.isSegmentWalkable(start, end))
    }

    @Test
    fun sweptCollisionAcceptsStationarySafePoseAndRejectsInvalidClearance() {
        val safe = ChamaRoomPoint(0f, 1.10f)
        assertTrue(ChamaRoomCollisionMap.isSegmentWalkable(safe, safe))
        assertFalse(
            ChamaRoomCollisionMap.isSegmentWalkable(
                safe, safe, avatarRadiusMeters = Float.NaN
            )
        )
        assertFalse(
            ChamaRoomCollisionMap.isSegmentWalkable(
                safe, safe, safetyMarginMeters = -0.01f
            )
        )
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

    @Test
    fun flowerReachKeepsBothArmSegmentsAtTheirDesignedLengths() {
        val pose = ChamaBodyPoseMath.reachingArm(
            shoulder = ChamaLimbPoint3D(0f, 1.70f, 0f)
        )
        fun distance(a: ChamaLimbPoint3D, b: ChamaLimbPoint3D): Float {
            val dx = b.x - a.x
            val dy = b.y - a.y
            val dz = b.z - a.z
            return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
        }
        val shoulder = ChamaLimbPoint3D(0f, 1.70f, 0f)
        assertEquals(0.42f, distance(shoulder, pose.elbow), 0.001f)
        assertEquals(0.36f, distance(pose.elbow, pose.wrist), 0.001f)
        assertEquals(0.09f, distance(pose.wrist, pose.hand), 0.001f)
        assertTrue("The reaching hand must extend forward", pose.hand.z > 0.40f)
    }

    @Test
    fun flowerReachIsAimedAtTheVaseFromTheValidatedApproach() {
        val approach = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.FLOWERS_STOP)
        assertTrue(ChamaRoomCollisionMap.isPointWalkable(approach))
        val heading = ChamaRoomNavigation.facingDegreesAt(
            ChamaAutonomyTarget.FLOWERS,
            approach
        )
        val pose = ChamaBodyPoseMath.reachingArm(
            shoulder = ChamaLimbPoint3D(0.31f, 1.70f, 0f),
            lateralAngleDegrees = -35f,
            upperForwardTiltDegrees = 55f,
            forearmForwardTiltDegrees = 60f
        )
        val radians = Math.toRadians(heading.toDouble())
        val worldX = approach.x +
            pose.hand.x * kotlin.math.cos(radians).toFloat() +
            pose.hand.z * kotlin.math.sin(radians).toFloat()
        val worldZ = approach.z -
            pose.hand.x * kotlin.math.sin(radians).toFloat() +
            pose.hand.z * kotlin.math.cos(radians).toFloat()
        val dx = 1.72f - worldX
        val dz = -0.78f - worldZ
        val horizontalGap = kotlin.math.sqrt(dx * dx + dz * dz)
        assertTrue("The extended hand should point toward the vase", horizontalGap < 0.15f)
    }

    @Test
    fun seatedLegsProjectForwardAndKeepFeetNearFloor() {
        val left = ChamaBodyPoseMath.blendLegToSeat(
            hipX = -0.15f,
            hipY = 0.78f,
            standingKnee = ChamaLimbPoint3D(-0.15f, 0.42f, 0f),
            standingAnkle = ChamaLimbPoint3D(-0.15f, 0.08f, 0f),
            blend = 1f
        )
        val right = ChamaBodyPoseMath.blendLegToSeat(
            hipX = 0.15f,
            hipY = 0.78f,
            standingKnee = ChamaLimbPoint3D(0.15f, 0.42f, 0f),
            standingAnkle = ChamaLimbPoint3D(0.15f, 0.08f, 0f),
            blend = 1f
        )
        assertEquals(0f, left.knee.z, 0.001f)
        assertEquals(0.3194f, left.ankle.z, 0.002f)
        assertEquals(0.3194f, right.ankle.z, 0.002f)
        fun distance(a: ChamaLimbPoint3D, b: ChamaLimbPoint3D): Float {
            val dx = b.x - a.x
            val dy = b.y - a.y
            val dz = b.z - a.z
            return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
        }
        val leftHip = ChamaLimbPoint3D(-0.15f, 0.78f, 0f)
        assertEquals(0.36f, distance(leftHip, left.knee), 0.001f)
        assertEquals(0.34f, distance(left.knee, left.ankle), 0.001f)
        // The rig root lowers by 0.31 m; the ankle remains at floor level.
        assertEquals(-0.006f, left.ankle.y - 0.31f, 0.015f)
        assertEquals(-0.006f, right.ankle.y - 0.31f, 0.015f)
        // Shoe front edge remains at least 5 cm before the coffee table.
        val shoeFrontZ = ChamaSeatTransitionMath.SOFA_CUSHION_CENTER.z +
            left.ankle.z + 0.09f + 0.21f
        assertTrue("Seated shoe must clear the coffee table", shoeFrontZ <= -0.285f)
        // Seated shoe bottom is flush with the floor after the root drop.
        assertEquals(0f, 0.37f - 0.31f - 0.06f, 0.001f)
    }

    @Test
    fun capsuleEulerRotationsAlignWithArmAndLegJointVectors() {
        fun assertAligned(start: ChamaLimbPoint3D, end: ChamaLimbPoint3D) {
            val rotation = ChamaBodyPoseMath.segmentRotation(start, end)
            val x = Math.toRadians(rotation.xDegrees.toDouble())
            val z = Math.toRadians(rotation.zDegrees.toDouble())
            // SceneView applies the Z rotation to local +Y, then the X rotation.
            val actual = ChamaLimbPoint3D(
                -kotlin.math.sin(z).toFloat(),
                (kotlin.math.cos(x) * kotlin.math.cos(z)).toFloat(),
                (kotlin.math.sin(x) * kotlin.math.cos(z)).toFloat()
            )
            val dx = end.x - start.x
            val dy = end.y - start.y
            val dz = end.z - start.z
            val length = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
            assertEquals(dx / length, actual.x, 0.001f)
            assertEquals(dy / length, actual.y, 0.001f)
            assertEquals(dz / length, actual.z, 0.001f)
        }

        val shoulder = ChamaLimbPoint3D(0.31f, 1.70f, 0f)
        val arm = ChamaBodyPoseMath.reachingArm(
            shoulder = shoulder,
            lateralAngleDegrees = -35f,
            upperForwardTiltDegrees = 55f,
            forearmForwardTiltDegrees = 60f
        )
        assertAligned(shoulder, arm.elbow)
        assertAligned(arm.elbow, arm.wrist)

        val hip = ChamaLimbPoint3D(-0.15f, 0.78f, 0f)
        val leg = ChamaBodyPoseMath.blendLegToSeat(
            hipX = hip.x,
            hipY = hip.y,
            standingKnee = ChamaLimbPoint3D(-0.15f, 0.42f, 0f),
            standingAnkle = ChamaLimbPoint3D(-0.15f, 0.08f, 0f),
            blend = 1f
        )
        assertAligned(hip, leg.knee)
        assertAligned(leg.knee, leg.ankle)
    }

    @Test
    fun sofaSeatTransitionStartsAtSafeApproachAndEndsOnCushion() {
        val approach = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.SOFA_SEAT)
        val start = ChamaSeatTransitionMath.pose(approach, progress = 0f)
        val middle = ChamaSeatTransitionMath.pose(approach, progress = 0.5f)
        val end = ChamaSeatTransitionMath.pose(approach, progress = 1f)

        assertEquals(approach, start.position)
        assertEquals(0f, start.rootHeight, 0.0001f)
        assertEquals(ChamaSeatTransitionMath.SOFA_CUSHION_CENTER, end.position)
        assertEquals(0f, end.rootHeight, 0.0001f)
        assertTrue("The transition arc should clear the sofa edge", middle.rootHeight > 0.11f)
        assertTrue(middle.position.x > approach.x)
        assertTrue(middle.position.z < approach.z)
    }

    @Test
    fun sofaSeatFacesIntoTheOpenRoom() {
        val approach = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.SOFA_SEAT)
        assertEquals(
            0f,
            ChamaRoomNavigation.facingDegreesAt(ChamaAutonomyTarget.SOFA, approach),
            0.001f
        )
    }

    @Test
    fun runtimeHoldsFloorNavigationDuringVisualSeatTransfer() {
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
        val safeApproach = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.SOFA_SEAT)
        val held = runtime.tick(
            ChamaAutonomyRuntimeInput(
                enabled = true,
                virtualSpaceVisible = true,
                userInteracting = false,
                lowPowerMode = false,
                energy = 0.1f,
                curiosity = 0.5f,
                nowElapsedRealtimeMs = 30_050L,
                holdMovementForSeatTransition = true
            )
        )
        assertEquals(ChamaRoomWaypoint.SOFA_SEAT, held.currentWaypoint)
        assertEquals(safeApproach, held.locomotion.position)
        assertEquals(ChamaLocomotionStatus.ARRIVED, held.locomotion.status)
        assertEquals(ChamaBodyGesture.SIT, held.bodyGesture)

        val resumed = runtime.tick(
            ChamaAutonomyRuntimeInput(
                enabled = true,
                virtualSpaceVisible = true,
                userInteracting = false,
                lowPowerMode = false,
                energy = 0.82f,
                curiosity = 0.5f,
                nowElapsedRealtimeMs = 42_100L,
                holdMovementForSeatTransition = false
            )
        )
        assertNull(resumed.routeError)
        assertTrue(
            resumed.locomotion.status == ChamaLocomotionStatus.TURNING ||
                resumed.locomotion.status == ChamaLocomotionStatus.WALKING ||
                resumed.locomotion.status == ChamaLocomotionStatus.ARRIVED
        )
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
