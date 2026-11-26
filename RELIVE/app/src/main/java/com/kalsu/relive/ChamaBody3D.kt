package com.kalsu.relive

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.kalsu.relive.avatar.ChamaBodyGesture
import com.kalsu.relive.avatar.ChamaBodyPoseMath
import com.kalsu.relive.avatar.ChamaLimbPoint3D
import com.kalsu.relive.avatar.ChamaRoomPoint
import io.github.sceneview.SceneScope
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size
import kotlin.math.cos
import kotlin.math.sin

private fun chamaSinDeg(degrees: Float): Float = sin(Math.toRadians(degrees.toDouble())).toFloat()
private fun chamaCosDeg(degrees: Float): Float = cos(Math.toRadians(degrees.toDouble())).toFloat()

/**
 * Corps géométrique sans tête, rendu comme enfant du salon dans le même graphe
 * SceneView. Le Node racine reçoit position et orientation calculées par
 * l'autonomie ; le portrait demeure une image Compose séparée et immuable.
 */
@Composable
internal fun SceneScope.ChamaBodyNodes(
    gesture: ChamaBodyGesture,
    rootPosition: ChamaRoomPoint,
    rootHeight: Float = 0f,
    headingDegrees: Float,
    gaitPhase: Float = 0f,
    motionEnabled: Boolean = true
) {
    val sweater = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFF7F1E8), roughness = 0.88f)
    }
    val heartRed = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFC6283D), roughness = 0.72f)
    }
    val skin = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF9B654E), roughness = 0.9f)
    }
    val trousers = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF426A91), roughness = 0.9f)
    }
    val denimSeam = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF294A6B), roughness = 0.92f)
    }
    val shoe = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF4A3433), roughness = 0.8f)
    }
    val cuff = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFE8D9D0), roughness = 0.9f)
    }
    val motion = rememberInfiniteTransition(label = "chama_body_motion")
    val sitBlend by animateFloatAsState(
        targetValue = if (gesture == ChamaBodyGesture.SIT) 1f else 0f,
        animationSpec = tween(760),
        label = "sit_stand_transition"
    )
    val postureDrop = -0.31f * sitBlend
    val reachShoulder by animateFloatAsState(
        targetValue = if (gesture == ChamaBodyGesture.REACH) 6f else 0f,
        animationSpec = tween(500),
        label = "flower_reach_transition"
    )
    val wave by motion.animateFloat(
        initialValue = -8f,
        targetValue = if (!motionEnabled) -8f else when (gesture) {
            ChamaBodyGesture.IDLE -> 6f
            ChamaBodyGesture.WALK -> 5f
            ChamaBodyGesture.WAVE -> 132f
            ChamaBodyGesture.LISTEN -> 4f
            ChamaBodyGesture.REACH -> -8f
            ChamaBodyGesture.SIT -> -8f
            ChamaBodyGesture.DANCE -> 48f
        },
        animationSpec = infiniteRepeatable(tween(1050), RepeatMode.Reverse),
        label = "right_arm_wave"
    )
    val leftWave by motion.animateFloat(
        initialValue = 7f,
        targetValue = if (!motionEnabled) 7f else when (gesture) {
            ChamaBodyGesture.IDLE -> -6f
            ChamaBodyGesture.WALK -> -5f
            ChamaBodyGesture.WAVE -> -10f
            ChamaBodyGesture.LISTEN -> -8f
            ChamaBodyGesture.REACH -> -6f
            ChamaBodyGesture.SIT -> -8f
            ChamaBodyGesture.DANCE -> -42f
        },
        animationSpec = infiniteRepeatable(tween(1250), RepeatMode.Reverse),
        label = "left_arm_motion"
    )
    val breathe by motion.animateFloat(
        initialValue = 0.0f,
        targetValue = if (!motionEnabled) 0f else if (gesture == ChamaBodyGesture.DANCE) 0.032f else 0.018f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "body_breathing"
    )
    val sway by motion.animateFloat(
        initialValue = -0.012f,
        targetValue = if (!motionEnabled) -0.012f else if (gesture == ChamaBodyGesture.DANCE) 0.045f else 0.012f,
        animationSpec = infiniteRepeatable(tween(1450), RepeatMode.Reverse),
        label = "body_sway"
    )
    val glance by motion.animateFloat(
        initialValue = -7f,
        targetValue = if (motionEnabled && gesture == ChamaBodyGesture.LISTEN) 7f else -7f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "gentle_look_around"
    )
    val step by motion.animateFloat(
        initialValue = 0f,
        targetValue = if (motionEnabled && gesture == ChamaBodyGesture.DANCE) 0.075f else 0f,
        animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
        label = "body_dance_step"
    )
    // Small lateral torso flex is represented as coordinated layer offsets.
    // It is a blockout approximation, not a production skeletal bend.
    val torsoLean by motion.animateFloat(
        initialValue = 0f,
        targetValue = if (!motionEnabled) 0f else if (gesture == ChamaBodyGesture.DANCE) 0.055f else 0.008f,
        animationSpec = infiniteRepeatable(tween(1750), RepeatMode.Reverse),
        label = "torso_lateral_flex"
    )

    // Wrist articulation adds a small independent palm rotation. The motion
    // is intentionally restrained so the hands remain attached to the forearms.
    val rightWristFlex by motion.animateFloat(
        initialValue = 0f,
        targetValue = if (!motionEnabled) 0f else when (gesture) {
            ChamaBodyGesture.IDLE -> 2f
            ChamaBodyGesture.WALK -> 2f
            ChamaBodyGesture.WAVE -> 24f
            ChamaBodyGesture.LISTEN -> 7f
            ChamaBodyGesture.REACH -> 8f
            ChamaBodyGesture.SIT -> 0f
            ChamaBodyGesture.DANCE -> 18f
        },
        animationSpec = infiniteRepeatable(tween(680), RepeatMode.Reverse),
        label = "right_wrist_flex"
    )
    val leftWristFlex by motion.animateFloat(
        initialValue = 0f,
        targetValue = if (!motionEnabled) 0f else when (gesture) {
            ChamaBodyGesture.IDLE -> -2f
            ChamaBodyGesture.WALK -> -4f
            ChamaBodyGesture.WAVE -> -5f
            ChamaBodyGesture.LISTEN -> -9f
            ChamaBodyGesture.REACH -> -2f
            ChamaBodyGesture.SIT -> -2f
            ChamaBodyGesture.DANCE -> -16f
        },
        animationSpec = infiniteRepeatable(tween(820), RepeatMode.Reverse),
        label = "left_wrist_flex"
    )

    // Two-link arm kinematics: each forearm starts at the calculated elbow,
    // and each hand follows the calculated wrist. Angles are measured from
    // the downward vertical axis; the portrait/head are not part of this rig.
    val shoulderY = 1.70f + breathe
    val upperArmLength = 0.42f
    val forearmLength = 0.36f
    val rightShoulderAngle = if (gesture == ChamaBodyGesture.REACH) reachShoulder else wave
    val leftShoulderAngle = leftWave
    val rightElbowBend = when (gesture) {
        ChamaBodyGesture.WAVE -> -112f
        ChamaBodyGesture.DANCE -> -28f
        ChamaBodyGesture.LISTEN -> 8f
        ChamaBodyGesture.REACH -> 0f
        ChamaBodyGesture.SIT -> 12f
        ChamaBodyGesture.IDLE -> 5f
        ChamaBodyGesture.WALK -> 4f
    }
    val leftElbowBend = when (gesture) {
        ChamaBodyGesture.WAVE -> 8f
        ChamaBodyGesture.DANCE -> 30f
        ChamaBodyGesture.LISTEN -> -5f
        ChamaBodyGesture.REACH -> 5f
        ChamaBodyGesture.SIT -> 12f
        ChamaBodyGesture.IDLE -> 5f
        ChamaBodyGesture.WALK -> 6f
    }
    val rightForearmAngle = rightShoulderAngle + rightElbowBend
    val leftForearmAngle = leftShoulderAngle + leftElbowBend

    val bodyCenterX = sway
    val upperTorsoX = bodyCenterX + torsoLean
    val waistCenterX = bodyCenterX + torsoLean * 0.55f
    val rightShoulderX = upperTorsoX + 0.31f
    val leftShoulderX = upperTorsoX - 0.31f
    val reaching = gesture == ChamaBodyGesture.REACH
    val sitting = gesture == ChamaBodyGesture.SIT
    // Forward reach is solved in the body-local Y/Z plane so the hand travels
    // toward the flowers rather than sideways across the chest.
    val rightReachPose = if (reaching) {
        ChamaBodyPoseMath.reachingArm(
            shoulder = ChamaLimbPoint3D(rightShoulderX, shoulderY, 0f),
            upperArmLength = upperArmLength,
            forearmLength = forearmLength,
            lateralAngleDegrees = -35f,
            upperForwardTiltDegrees = 55f,
            forearmForwardTiltDegrees = 60f
        )
    } else null
    val rightElbowZ = rightReachPose?.elbow?.z ?: if (sitting) 0.12f * sitBlend else 0f
    val rightWristZ = rightReachPose?.wrist?.z ?: if (sitting) 0.22f * sitBlend else 0f
    val leftElbowZ = if (sitting) 0.12f * sitBlend else 0f
    val leftWristZ = if (sitting) 0.30f * sitBlend else 0f
    val rightElbowX = rightReachPose?.elbow?.x ?: rightShoulderX + chamaSinDeg(rightShoulderAngle) * upperArmLength
    val rightElbowY = rightReachPose?.elbow?.y ?: shoulderY - chamaCosDeg(rightShoulderAngle) * upperArmLength
    val leftElbowX = leftShoulderX + chamaSinDeg(leftShoulderAngle) * upperArmLength
    val leftElbowY = shoulderY - chamaCosDeg(leftShoulderAngle) * upperArmLength
    val rightWristX = rightReachPose?.wrist?.x ?: rightElbowX + chamaSinDeg(rightForearmAngle) * forearmLength
    val rightWristY = rightReachPose?.wrist?.y ?: rightElbowY - chamaCosDeg(rightForearmAngle) * forearmLength
    val leftWristX = leftElbowX + chamaSinDeg(leftForearmAngle) * forearmLength
    val leftWristY = leftElbowY - chamaCosDeg(leftForearmAngle) * forearmLength
    val rightHandAngle = rightForearmAngle + rightWristFlex
    val leftHandAngle = leftForearmAngle + leftWristFlex
    val rightHandX = rightReachPose?.hand?.x ?: rightWristX + chamaSinDeg(rightHandAngle) * 0.09f
    val rightHandY = rightReachPose?.hand?.y ?: rightWristY - chamaCosDeg(rightHandAngle) * 0.09f
    val leftHandX = leftWristX + chamaSinDeg(leftHandAngle) * 0.09f
    val leftHandY = leftWristY - chamaCosDeg(leftHandAngle) * 0.09f
    val rightHandZ = rightReachPose?.hand?.z ?: rightWristZ + if (sitting) 0.035f * sitBlend else 0f
    val leftHandZ = leftWristZ + if (sitting) 0.035f * sitBlend else 0f

    // Hip -> knee -> ankle chain. The dance step shifts the two legs in
    // opposite phases while preserving a continuous thigh/calf connection.
    val hipY = 0.78f
    val thighLength = 0.36f
    val calfLength = 0.34f
    // During walking, leg phase follows the distance-driven locomotion clock.
    val walkStep = chamaSinDeg(gaitPhase.coerceIn(0f, 1f) * 360f) * 0.12f
    val leftThighAngle = when (gesture) {
        ChamaBodyGesture.DANCE -> step * 95f
        ChamaBodyGesture.WALK -> walkStep * 75f
        else -> 0f
    }
    val rightThighAngle = when (gesture) {
        ChamaBodyGesture.DANCE -> -step * 95f
        ChamaBodyGesture.WALK -> -walkStep * 75f
        else -> 0f
    }
    val leftKneeBend = when (gesture) {
        ChamaBodyGesture.DANCE, ChamaBodyGesture.WALK -> -8f
        ChamaBodyGesture.SIT -> -6f
        else -> 0f
    }
    val rightKneeBend = when (gesture) {
        ChamaBodyGesture.DANCE, ChamaBodyGesture.WALK -> 8f
        ChamaBodyGesture.SIT -> 6f
        else -> 0f
    }
    val leftHipX = bodyCenterX - 0.15f
    val rightHipX = bodyCenterX + 0.15f
    val sitThighTilt = 55f * sitBlend
    val sitCalfTilt = 45f * sitBlend
    val leftStandingKneeX = leftHipX + chamaSinDeg(leftThighAngle) * thighLength
    val leftStandingKneeY = hipY - chamaCosDeg(leftThighAngle) * thighLength
    val rightStandingKneeX = rightHipX + chamaSinDeg(rightThighAngle) * thighLength
    val rightStandingKneeY = hipY - chamaCosDeg(rightThighAngle) * thighLength
    val leftCalfAngle = leftThighAngle + leftKneeBend
    val rightCalfAngle = rightThighAngle + rightKneeBend
    val leftStandingAnkleX = leftStandingKneeX + chamaSinDeg(leftCalfAngle) * calfLength
    val leftStandingAnkleY = leftStandingKneeY - chamaCosDeg(leftCalfAngle) * calfLength
    val rightStandingAnkleX = rightStandingKneeX + chamaSinDeg(rightCalfAngle) * calfLength
    val rightStandingAnkleY = rightStandingKneeY - chamaCosDeg(rightCalfAngle) * calfLength
    val leftLegPose = ChamaBodyPoseMath.blendLegToSeat(
        leftHipX, hipY,
        ChamaLimbPoint3D(leftStandingKneeX, leftStandingKneeY, 0f),
        ChamaLimbPoint3D(leftStandingAnkleX, leftStandingAnkleY, 0f),
        sitBlend, thighLength, calfLength
    )
    val rightLegPose = ChamaBodyPoseMath.blendLegToSeat(
        rightHipX, hipY,
        ChamaLimbPoint3D(rightStandingKneeX, rightStandingKneeY, 0f),
        ChamaLimbPoint3D(rightStandingAnkleX, rightStandingAnkleY, 0f),
        sitBlend, thighLength, calfLength
    )
    val leftKneeX = leftLegPose.knee.x
    val leftKneeY = leftLegPose.knee.y
    val leftKneeZ = leftLegPose.knee.z
    val rightKneeX = rightLegPose.knee.x
    val rightKneeY = rightLegPose.knee.y
    val rightKneeZ = rightLegPose.knee.z
    val leftAnkleX = leftLegPose.ankle.x
    val leftAnkleY = leftLegPose.ankle.y
    val leftAnkleZ = leftLegPose.ankle.z
    val rightAnkleX = rightLegPose.ankle.x
    val rightAnkleY = rightLegPose.ankle.y
    val rightAnkleZ = rightLegPose.ankle.z


    Node(
        position = Position(rootPosition.x, rootHeight, rootPosition.z),
        rotation = Rotation(y = headingDegrees + if (gesture == ChamaBodyGesture.LISTEN) glance else 0f)
    ) {
        Node(position = Position(0f, postureDrop, 0f)) {
    // Layered torso silhouette: upper chest, tapered waist and pelvis
        // are separate volumes so the waist does not keep the chest width.
        // The body starts below the absent neck; no head or neck is generated.
        CapsuleNode(radius = 0.255f, height = 0.48f,
            materialInstance = sweater,
            position = Position(upperTorsoX, 1.57f + breathe, 0f))
        CapsuleNode(radius = 0.205f, height = 0.43f,
            materialInstance = sweater,
            position = Position(waistCenterX, 1.25f + breathe, 0f))
        CapsuleNode(radius = 0.135f, height = 0.56f,
            materialInstance = sweater,
            position = Position(upperTorsoX - 0.245f, 1.56f + breathe, 0f),
            rotation = Rotation(z = 90f))
        CapsuleNode(radius = 0.135f, height = 0.56f,
            materialInstance = sweater,
            position = Position(upperTorsoX + 0.245f, 1.56f + breathe, 0f),
            rotation = Rotation(z = 90f))
        // Pelvis volume bridges the fitted top and both trouser legs.
        CapsuleNode(radius = 0.255f, height = 0.34f,
            materialInstance = trousers,
            position = Position(bodyCenterX, 0.88f, 0f))

        // Small red heart-like sweater details are geometric decorations,
        // not a texture of the protected portrait.
        SphereNode(radius = 0.055f, materialInstance = heartRed,
            position = Position(upperTorsoX - 0.12f, 1.58f + breathe, 0.255f))
        SphereNode(radius = 0.055f, materialInstance = heartRed,
            position = Position(upperTorsoX - 0.055f, 1.58f + breathe, 0.255f))
        SphereNode(radius = 0.055f, materialInstance = heartRed,
            position = Position(upperTorsoX + 0.08f, 1.42f + breathe, 0.255f))
        SphereNode(radius = 0.055f, materialInstance = heartRed,
            position = Position(upperTorsoX + 0.145f, 1.42f + breathe, 0.255f))

        // Shoulder, elbow and wrist joints are placed from a two-link chain.
        SphereNode(radius = 0.13f, materialInstance = sweater,
            position = Position(leftShoulderX, shoulderY, 0f))
        SphereNode(radius = 0.13f, materialInstance = sweater,
            position = Position(rightShoulderX, shoulderY, 0f))
        CapsuleNode(radius = 0.105f, height = upperArmLength,
            materialInstance = sweater,
            position = Position((leftShoulderX + leftElbowX) / 2f, (shoulderY + leftElbowY) / 2f, leftElbowZ / 2f),
            rotation = Rotation(x = if (sitting) -16f else 0f, z = leftShoulderAngle + 180f))
        CapsuleNode(radius = 0.105f, height = upperArmLength,
            materialInstance = sweater,
            position = Position((rightShoulderX + rightElbowX) / 2f, (shoulderY + rightElbowY) / 2f, rightElbowZ / 2f),
            rotation = Rotation(
                x = if (reaching) -60f else if (sitting) -16f else 0f,
                z = if (reaching) 161f else rightShoulderAngle + 180f
            ))
        SphereNode(radius = 0.092f, materialInstance = sweater,
            position = Position(leftElbowX, leftElbowY, leftElbowZ))
        SphereNode(radius = 0.092f, materialInstance = sweater,
            position = Position(rightElbowX, rightElbowY, rightElbowZ))
        CapsuleNode(radius = 0.085f, height = forearmLength,
            materialInstance = sweater,
            position = Position((leftElbowX + leftWristX) / 2f, (leftElbowY + leftWristY) / 2f, (leftElbowZ + leftWristZ) / 2f),
            rotation = Rotation(x = if (sitting) -16f else 0f, z = leftForearmAngle + 180f))
        CapsuleNode(radius = 0.085f, height = forearmLength,
            materialInstance = sweater,
            position = Position((rightElbowX + rightWristX) / 2f, (rightElbowY + rightWristY) / 2f, (rightElbowZ + rightWristZ) / 2f),
            rotation = Rotation(
                x = if (reaching) -62f else if (sitting) -16f else 0f,
                z = if (reaching) 163f else rightForearmAngle + 180f
            ))
        SphereNode(radius = 0.075f, materialInstance = cuff,
            position = Position(leftWristX, leftWristY, leftWristZ))
        SphereNode(radius = 0.075f, materialInstance = cuff,
            position = Position(rightWristX, rightWristY, rightWristZ))
        SphereNode(radius = 0.105f, materialInstance = skin,
            position = Position(leftHandX, leftHandY, leftHandZ))
        SphereNode(radius = 0.105f, materialInstance = skin,
            position = Position(rightHandX, rightHandY, rightHandZ))

        // Four simplified finger capsules per palm. Their spread axis and
        // direction rotate with the wrist; this remains a stylized blockout.
        val leftFingerOffsets = listOf(-0.075f, -0.025f, 0.025f, 0.075f)
        leftFingerOffsets.forEachIndexed { index, spread ->
            val angleOffset = (index - 1.5f) * 5f
            val fingerX = leftHandX + chamaCosDeg(leftHandAngle) * spread +
                chamaSinDeg(leftHandAngle) * 0.055f
            val fingerY = leftHandY + chamaSinDeg(leftHandAngle) * spread -
                chamaCosDeg(leftHandAngle) * 0.055f
            CapsuleNode(radius = 0.024f, height = if (index == 0 || index == 3) 0.11f else 0.12f,
                materialInstance = skin, position = Position(fingerX, fingerY, leftHandZ + 0.03f),
                rotation = Rotation(z = leftHandAngle + angleOffset))
        }
        val rightFingerOffsets = listOf(-0.075f, -0.025f, 0.025f, 0.075f)
        rightFingerOffsets.forEachIndexed { index, spread ->
            val angleOffset = (index - 1.5f) * 5f
            val fingerX = rightHandX + chamaCosDeg(rightHandAngle) * spread +
                chamaSinDeg(rightHandAngle) * 0.055f
            val fingerY = rightHandY + chamaSinDeg(rightHandAngle) * spread -
                chamaCosDeg(rightHandAngle) * 0.055f
            CapsuleNode(radius = 0.024f, height = if (index == 0 || index == 3) 0.11f else 0.12f,
                materialInstance = skin, position = Position(fingerX, fingerY, rightHandZ + 0.03f),
                rotation = Rotation(z = rightHandAngle + angleOffset))
        }

        // Sweater hem and subtle seams are separate geometry, not portrait pixels.
        CubeNode(size = Size(0.48f, 0.045f, 0.43f), materialInstance = cuff,
            position = Position(waistCenterX, 1.02f + breathe, 0f))
        CubeNode(size = Size(0.34f, 0.025f, 0.018f), materialInstance = heartRed,
            position = Position(upperTorsoX, 1.50f + breathe, 0.286f))
        CubeNode(size = Size(0.40f, 0.022f, 0.018f), materialInstance = cuff,
            position = Position(waistCenterX, 1.30f + breathe, 0.285f))
        CubeNode(size = Size(0.42f, 0.035f, 0.035f), materialInstance = cuff,
            position = Position(waistCenterX, 0.99f + breathe, 0.22f))

        // Hip pivots and leg segments follow the calculated hip-knee-ankle chain.
        SphereNode(radius = 0.14f, materialInstance = trousers,
            position = Position(leftHipX, hipY, 0f))
        SphereNode(radius = 0.14f, materialInstance = trousers,
            position = Position(rightHipX, hipY, 0f))
        CapsuleNode(radius = 0.135f, height = thighLength, materialInstance = trousers,
            position = Position((leftHipX + leftKneeX) / 2f, (hipY + leftKneeY) / 2f, leftKneeZ / 2f),
            rotation = Rotation(x = -sitThighTilt, z = (leftThighAngle + 180f) * (1f - sitBlend)))
        CapsuleNode(radius = 0.135f, height = thighLength, materialInstance = trousers,
            position = Position((rightHipX + rightKneeX) / 2f, (hipY + rightKneeY) / 2f, rightKneeZ / 2f),
            rotation = Rotation(x = -sitThighTilt, z = (rightThighAngle + 180f) * (1f - sitBlend)))
        SphereNode(radius = 0.105f, materialInstance = trousers,
            position = Position(leftKneeX, leftKneeY, leftKneeZ))
        SphereNode(radius = 0.105f, materialInstance = trousers,
            position = Position(rightKneeX, rightKneeY, rightKneeZ))
        CapsuleNode(radius = 0.115f, height = calfLength, materialInstance = trousers,
            position = Position((leftKneeX + leftAnkleX) / 2f, (leftKneeY + leftAnkleY) / 2f, (leftKneeZ + leftAnkleZ) / 2f),
            rotation = Rotation(x = -sitCalfTilt, z = (leftCalfAngle + 180f) * (1f - sitBlend)))
        CapsuleNode(radius = 0.115f, height = calfLength, materialInstance = trousers,
            position = Position((rightKneeX + rightAnkleX) / 2f, (rightKneeY + rightAnkleY) / 2f, (rightKneeZ + rightAnkleZ) / 2f),
            rotation = Rotation(x = -sitCalfTilt, z = (rightCalfAngle + 180f) * (1f - sitBlend)))
        SphereNode(radius = 0.105f, materialInstance = trousers,
            position = Position(leftAnkleX, leftAnkleY, leftAnkleZ))
        SphereNode(radius = 0.105f, materialInstance = trousers,
            position = Position(rightAnkleX, rightAnkleY, rightAnkleZ))
        // Denim waistband and outer seams clarify the pelvis and leg silhouette.
        CubeNode(size = Size(0.48f, 0.055f, 0.34f), materialInstance = denimSeam,
            position = Position(bodyCenterX, 1.055f, 0.015f))
        // Front fly, pocket openings and belt loops add restrained denim construction detail.
        CapsuleNode(radius = 0.009f, height = 0.13f, materialInstance = denimSeam,
            position = Position(bodyCenterX, 0.925f, 0.192f))
        CapsuleNode(radius = 0.009f, height = 0.17f, materialInstance = denimSeam,
            position = Position(bodyCenterX - 0.145f, 0.925f, 0.19f),
            rotation = Rotation(z = 32f))
        CapsuleNode(radius = 0.009f, height = 0.17f, materialInstance = denimSeam,
            position = Position(bodyCenterX + 0.145f, 0.925f, 0.19f),
            rotation = Rotation(z = -32f))
        CubeNode(size = Size(0.035f, 0.085f, 0.025f), materialInstance = denimSeam,
            position = Position(bodyCenterX - 0.16f, 1.055f, 0.19f))
        CubeNode(size = Size(0.035f, 0.085f, 0.025f), materialInstance = denimSeam,
            position = Position(bodyCenterX + 0.16f, 1.055f, 0.19f))
        CapsuleNode(radius = 0.018f, height = 0.62f, materialInstance = denimSeam,
            position = Position(leftHipX - 0.105f, 0.49f, 0.13f))
        CapsuleNode(radius = 0.018f, height = 0.62f, materialInstance = denimSeam,
            position = Position(rightHipX + 0.105f, 0.49f, 0.13f))
        CubeNode(size = Size(0.30f, 0.12f, 0.42f), materialInstance = shoe,
            position = Position(leftAnkleX, leftAnkleY - 0.055f, leftAnkleZ + 0.09f))
        CubeNode(size = Size(0.30f, 0.12f, 0.42f), materialInstance = shoe,
            position = Position(rightAnkleX, rightAnkleY - 0.055f, rightAnkleZ + 0.09f))

        }
    }
}
