package com.kalsu.relive.avatar

/**
 * Coordinates planning, salon routing and time-based locomotion.
 *
 * This is still a simulation boundary: callers must apply its frames to a
 * shared 3D world only after renderer integration and collision validation.
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
        get() = if (locomotion.status == ChamaLocomotionStatus.WALKING ||
            locomotion.status == ChamaLocomotionStatus.TURNING) {
            ChamaBodyGesture.IDLE
        } else {
            decision.bodyGesture
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
                } else {
                    destinationWaypoint = ChamaRoomNavigation.waypointOf(target)
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
            }
            currentDecision = decision
        }

        lastFrame = locomotion.update(
            nowElapsedRealtimeMs = input.nowElapsedRealtimeMs,
            enabled = input.enabled,
            virtualSpaceVisible = input.virtualSpaceVisible,
            userInteracting = input.userInteracting,
            lowPowerMode = input.lowPowerMode
        )
        if (lastFrame.status == ChamaLocomotionStatus.ARRIVED) {
            destinationWaypoint?.let { currentWaypoint = it }
            destinationWaypoint = null
        }
        return ChamaAutonomyRuntimeFrame(decision, lastFrame, currentWaypoint, routeError)
    }

    @Synchronized
    fun stop(): ChamaAutonomyRuntimeFrame {
        planner.reset()
        currentDecision = null
        destinationWaypoint = null
        lastFrame = locomotion.cancel()
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
