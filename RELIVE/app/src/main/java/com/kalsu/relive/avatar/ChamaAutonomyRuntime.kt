package com.kalsu.relive.avatar

/**
 * Coordinates planning, salon routing and time-based locomotion.
 *
 * VirtualSpaceScreen applies each locomotion frame to the body root Node in
 * the shared salon SceneView. Static and per-frame route guards run here; the
 * articulated body still needs final on-device collision and visual validation.
 */
data class ChamaAutonomyRuntimeInput(
    val enabled: Boolean,
    val virtualSpaceVisible: Boolean,
    val userInteracting: Boolean,
    val lowPowerMode: Boolean,
    val energy: Float,
    val curiosity: Float,
    val nowElapsedRealtimeMs: Long
)

data class ChamaAutonomyRuntimeFrame(
    val decision: ChamaAutonomyDecision,
    val locomotion: ChamaLocomotionFrame,
    val currentWaypoint: ChamaRoomWaypoint,
    val routeError: String? = null
) {
    val activity: ChamaAutonomyActivity? get() = decision.activity
    val bodyGesture: ChamaBodyGesture
        get() = when (locomotion.status) {
            ChamaLocomotionStatus.WALKING -> ChamaBodyGesture.WALK
            ChamaLocomotionStatus.TURNING -> ChamaBodyGesture.IDLE
            else -> decision.bodyGesture
        }
}

/**
 * Pure Kotlin coordinator. It never accesses Android, sensors, voice, network,
 * storage or SceneView. User interruption always wins over autonomous motion.
 */
class ChamaAutonomyRuntime(
    initialWaypoint: ChamaRoomWaypoint = ChamaRoomWaypoint.ROOM_CENTER,
    initialHeadingDegrees: Float = 0f,
    private val planner: ChamaAutonomyPlanner = ChamaAutonomyPlanner(),
    private val locomotion: ChamaLocomotionController = ChamaLocomotionController()
) {
    private var currentWaypoint = initialWaypoint
    private var currentDecision: ChamaAutonomyDecision? = null
    private var destinationWaypoint: ChamaRoomWaypoint? = null
    private var destinationTarget: ChamaAutonomyTarget? = null
    private var lastFrame = ChamaLocomotionFrame(
        position = ChamaRoomNavigation.pointOf(initialWaypoint),
        headingDegrees = initialHeadingDegrees,
        speedMetersPerSecond = 0f,
        gaitPhase = 0f,
        status = ChamaLocomotionStatus.IDLE
    )

    @Synchronized
    fun tick(input: ChamaAutonomyRuntimeInput): ChamaAutonomyRuntimeFrame {
        val decision = planner.decide(
            ChamaAutonomyInput(
                enabled = input.enabled,
                virtualSpaceVisible = input.virtualSpaceVisible,
                userInteracting = input.userInteracting,
                lowPowerMode = input.lowPowerMode,
                energy = input.energy,
                curiosity = input.curiosity,
                nowElapsedRealtimeMs = input.nowElapsedRealtimeMs
            )
        )

        if (decision.isPaused) {
            lastFrame = locomotion.update(
                nowElapsedRealtimeMs = input.nowElapsedRealtimeMs,
                enabled = input.enabled,
                virtualSpaceVisible = input.virtualSpaceVisible,
                userInteracting = input.userInteracting,
                lowPowerMode = input.lowPowerMode
            )
            destinationWaypoint = null
            destinationTarget = null
            ChamaRoomNavigation.nearestReachableWaypoint(lastFrame.position)?.let {
                currentWaypoint = it
            }
            currentDecision = decision
            return ChamaAutonomyRuntimeFrame(decision, lastFrame, currentWaypoint)
        }

        var routeError: String? = null
        if (decision != currentDecision) {
            val target = decision.target
            if (target != null) {
                val route = ChamaRoomNavigation.findRoute(currentWaypoint, target)
                val startConnectorIsSafe = route != null &&
                    ChamaRoomCollisionMap.isSegmentWalkable(lastFrame.position, route.points.first())
                if (route == null || !ChamaRoomCollisionMap.validateRoute(route) || !startConnectorIsSafe) {
                    routeError = "Aucun itinéraire entièrement validé n'est disponible vers $target."
                    lastFrame = locomotion.cancel()
                    destinationWaypoint = null
                    destinationTarget = null
                } else {
                    destinationWaypoint = ChamaRoomNavigation.waypointOf(target)
                    destinationTarget = target
                    lastFrame = locomotion.begin(
                        route = route,
                        currentPosition = lastFrame.position,
                        currentHeadingDegrees = lastFrame.headingDegrees,
                        nowElapsedRealtimeMs = input.nowElapsedRealtimeMs
                    )
                }
            } else {
                lastFrame = locomotion.cancel()
                destinationWaypoint = null
                destinationTarget = null
            }
            currentDecision = decision
        }

        val lastSafePose = lastFrame
        val candidateFrame = locomotion.update(
            nowElapsedRealtimeMs = input.nowElapsedRealtimeMs,
            enabled = input.enabled,
            virtualSpaceVisible = input.virtualSpaceVisible,
            userInteracting = input.userInteracting,
            lowPowerMode = input.lowPowerMode
        )
        if (!ChamaRoomCollisionMap.isSegmentWalkable(lastSafePose.position, candidateFrame.position)) {
            lastFrame = locomotion.holdAt(lastSafePose.position, lastSafePose.headingDegrees)
            destinationWaypoint = null
            destinationTarget = null
            ChamaRoomNavigation.nearestReachableWaypoint(lastSafePose.position)?.let {
                currentWaypoint = it
            }
            routeError = "Déplacement interrompu : la vérification de sécurité a refusé le segment."
        } else {
            lastFrame = candidateFrame
        }
        if (lastFrame.status == ChamaLocomotionStatus.ARRIVED) {
            destinationWaypoint?.let { currentWaypoint = it }
            destinationTarget?.let { target ->
                lastFrame = locomotion.orientAtArrival(
                    ChamaRoomNavigation.facingDegreesAt(target, lastFrame.position)
                )
            }
            destinationWaypoint = null
            destinationTarget = null
        }
        return ChamaAutonomyRuntimeFrame(decision, lastFrame, currentWaypoint, routeError)
    }

    @Synchronized
    fun stop(): ChamaAutonomyRuntimeFrame {
        planner.reset()
        currentDecision = null
        destinationWaypoint = null
        destinationTarget = null
        lastFrame = locomotion.cancel()
        ChamaRoomNavigation.nearestReachableWaypoint(lastFrame.position)?.let {
            currentWaypoint = it
        }
        return ChamaAutonomyRuntimeFrame(
            decision = ChamaAutonomyDecision(
                activity = null,
                target = null,
                pauseReason = ChamaAutonomyPauseReason.DISABLED,
                statusFr = "L'autonomie est arrêtée."
            ),
            locomotion = lastFrame,
            currentWaypoint = currentWaypoint
        )
    }
}
