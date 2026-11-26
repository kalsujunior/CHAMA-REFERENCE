package com.kalsu.relive

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import com.kalsu.relive.avatar.ChamaBodyGesture
import kotlin.math.cos
import kotlin.math.sin

private fun chamaSinDeg(degrees: Float): Float = sin(Math.toRadians(degrees.toDouble())).toFloat()
private fun chamaCosDeg(degrees: Float): Float = cos(Math.toRadians(degrees.toDouble())).toFloat()

/**
 * Body-only procedural character blockout.
 *
 * The head and face are intentionally omitted. The immutable source portrait
 * remains a separate Compose image and is never used as a texture or mesh.
 */
@Composable
internal fun ChamaBody3D(
    modifier: Modifier = Modifier,
    gesture: ChamaBodyGesture = ChamaBodyGesture.IDLE,
    motionEnabled: Boolean = true
) {
    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
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
        materialLoader.createColorInstance(Color(0xFF332C32), roughness = 0.9f)
    }
    val shoe = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF4A3433), roughness = 0.8f)
    }
    val cuff = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFE8D9D0), roughness = 0.9f)
    }
    val motion = rememberInfiniteTransition(label = "chama_body_motion")
    val wave by motion.animateFloat(
        initialValue = -8f,
        targetValue = if (!motionEnabled) -8f else when (gesture) {
            ChamaBodyGesture.IDLE -> 6f
            ChamaBodyGesture.WAVE -> 132f
            ChamaBodyGesture.LISTEN -> 4f
            ChamaBodyGesture.DANCE -> 48f
        },
        animationSpec = infiniteRepeatable(tween(1050), RepeatMode.Reverse),
        label = "right_arm_wave"
    )
    val leftWave by motion.animateFloat(
        initialValue = 7f,
        targetValue = if (!motionEnabled) 7f else when (gesture) {
            ChamaBodyGesture.IDLE -> -6f
            ChamaBodyGesture.WAVE -> -10f
            ChamaBodyGesture.LISTEN -> -8f
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
    val step by motion.animateFloat(
        initialValue = -0.035f,
        targetValue = if (!motionEnabled) -0.035f else if (gesture == ChamaBodyGesture.DANCE) 0.075f else 0.012f,
        animationSpec = infiniteRepeatable(tween(if (gesture == ChamaBodyGesture.DANCE) 520 else 1800), RepeatMode.Reverse),
        label = "body_step"
    )

    // Two-link arm kinematics: each forearm starts at the calculated elbow,
    // and each hand follows the calculated wrist. Angles are measured from
    // the downward vertical axis; the portrait/head are not part of this rig.
    val shoulderY = 1.70f + breathe
    val upperArmLength = 0.42f
    val forearmLength = 0.36f
    val rightShoulderAngle = wave
    val leftShoulderAngle = leftWave
    val rightElbowBend = when (gesture) {
        ChamaBodyGesture.WAVE -> -112f
        ChamaBodyGesture.DANCE -> -28f
        ChamaBodyGesture.LISTEN -> 8f
        ChamaBodyGesture.IDLE -> 5f
    }
    val leftElbowBend = when (gesture) {
        ChamaBodyGesture.WAVE -> 8f
        ChamaBodyGesture.DANCE -> 30f
        ChamaBodyGesture.LISTEN -> -5f
        ChamaBodyGesture.IDLE -> 5f
    }
    val rightForearmAngle = rightShoulderAngle + rightElbowBend
    val leftForearmAngle = leftShoulderAngle + leftElbowBend

    val rightShoulderX = 0.31f
    val leftShoulderX = -0.31f
    val rightElbowX = rightShoulderX + chamaSinDeg(rightShoulderAngle) * upperArmLength
    val rightElbowY = shoulderY - chamaCosDeg(rightShoulderAngle) * upperArmLength
    val leftElbowX = leftShoulderX + chamaSinDeg(leftShoulderAngle) * upperArmLength
    val leftElbowY = shoulderY - chamaCosDeg(leftShoulderAngle) * upperArmLength
    val rightWristX = rightElbowX + chamaSinDeg(rightForearmAngle) * forearmLength
    val rightWristY = rightElbowY - chamaCosDeg(rightForearmAngle) * forearmLength
    val leftWristX = leftElbowX + chamaSinDeg(leftForearmAngle) * forearmLength
    val leftWristY = leftElbowY - chamaCosDeg(leftForearmAngle) * forearmLength
    val rightHandX = rightWristX + chamaSinDeg(rightForearmAngle) * 0.09f
    val rightHandY = rightWristY - chamaCosDeg(rightForearmAngle) * 0.09f
    val leftHandX = leftWristX + chamaSinDeg(leftForearmAngle) * 0.09f
    val leftHandY = leftWristY - chamaCosDeg(leftForearmAngle) * 0.09f

    // Hip -> knee -> ankle chain. The dance step shifts the two legs in
    // opposite phases while preserving a continuous thigh/calf connection.
    val hipY = 0.78f
    val thighLength = 0.36f
    val calfLength = 0.34f
    val leftThighAngle = if (gesture == ChamaBodyGesture.DANCE) step * 95f else 0f
    val rightThighAngle = if (gesture == ChamaBodyGesture.DANCE) -step * 95f else 0f
    val leftKneeBend = if (gesture == ChamaBodyGesture.DANCE) -8f else 0f
    val rightKneeBend = if (gesture == ChamaBodyGesture.DANCE) 8f else 0f
    val leftKneeX = -0.15f + chamaSinDeg(leftThighAngle) * thighLength
    val leftKneeY = hipY - chamaCosDeg(leftThighAngle) * thighLength
    val rightKneeX = 0.15f + chamaSinDeg(rightThighAngle) * thighLength
    val rightKneeY = hipY - chamaCosDeg(rightThighAngle) * thighLength
    val leftCalfAngle = leftThighAngle + leftKneeBend
    val rightCalfAngle = rightThighAngle + rightKneeBend
    val leftAnkleX = leftKneeX + chamaSinDeg(leftCalfAngle) * calfLength
    val leftAnkleY = leftKneeY - chamaCosDeg(leftCalfAngle) * calfLength
    val rightAnkleX = rightKneeX + chamaSinDeg(rightCalfAngle) * calfLength
    val rightAnkleY = rightKneeY - chamaCosDeg(rightCalfAngle) * calfLength

    SceneView(
        modifier = modifier,
        engine = engine,
        materialLoader = materialLoader,
        cameraManipulator = rememberCameraManipulator(
            orbitHomePosition = Position(0f, 1.15f, 3.9f),
            targetPosition = Position(0f, 1.0f, 0f)
        )
    ) {
        // Torso and hips: the head/face are deliberately not modelled.
        CapsuleNode(
            radius = 0.29f, height = 0.88f,
            materialInstance = sweater,
            position = Position(sway, 1.42f + breathe, 0f)
        )
        CapsuleNode(
            radius = 0.27f, height = 0.30f,
            materialInstance = trousers,
            position = Position(sway, 0.88f, 0f)
        )

        // Small red heart-like sweater details are geometric decorations,
        // not a texture of the protected portrait.
        SphereNode(radius = 0.055f, materialInstance = heartRed,
            position = Position(-0.12f, 1.58f + breathe, 0.255f))
        SphereNode(radius = 0.055f, materialInstance = heartRed,
            position = Position(-0.055f, 1.58f + breathe, 0.255f))
        SphereNode(radius = 0.055f, materialInstance = heartRed,
            position = Position(0.08f, 1.42f + breathe, 0.255f))
        SphereNode(radius = 0.055f, materialInstance = heartRed,
            position = Position(0.145f, 1.42f + breathe, 0.255f))

        // Shoulder, elbow and wrist joints are placed from a two-link chain.
        SphereNode(radius = 0.13f, materialInstance = sweater,
            position = Position(leftShoulderX, shoulderY, 0f))
        SphereNode(radius = 0.13f, materialInstance = sweater,
            position = Position(rightShoulderX, shoulderY, 0f))
        CapsuleNode(radius = 0.105f, height = upperArmLength,
            materialInstance = sweater,
            position = Position((leftShoulderX + leftElbowX) / 2f, (shoulderY + leftElbowY) / 2f, 0f),
            rotation = Rotation(z = leftShoulderAngle + 180f))
        CapsuleNode(radius = 0.105f, height = upperArmLength,
            materialInstance = sweater,
            position = Position((rightShoulderX + rightElbowX) / 2f, (shoulderY + rightElbowY) / 2f, 0f),
            rotation = Rotation(z = rightShoulderAngle + 180f))
        SphereNode(radius = 0.092f, materialInstance = sweater,
            position = Position(leftElbowX, leftElbowY, 0f))
        SphereNode(radius = 0.092f, materialInstance = sweater,
            position = Position(rightElbowX, rightElbowY, 0f))
        CapsuleNode(radius = 0.085f, height = forearmLength,
            materialInstance = sweater,
            position = Position((leftElbowX + leftWristX) / 2f, (leftElbowY + leftWristY) / 2f, 0f),
            rotation = Rotation(z = leftForearmAngle + 180f))
        CapsuleNode(radius = 0.085f, height = forearmLength,
            materialInstance = sweater,
            position = Position((rightElbowX + rightWristX) / 2f, (rightElbowY + rightWristY) / 2f, 0f),
            rotation = Rotation(z = rightForearmAngle + 180f))
        SphereNode(radius = 0.075f, materialInstance = cuff,
            position = Position(leftWristX, leftWristY, 0f))
        SphereNode(radius = 0.075f, materialInstance = cuff,
            position = Position(rightWristX, rightWristY, 0f))
        SphereNode(radius = 0.105f, materialInstance = skin,
            position = Position(leftHandX, leftHandY, 0.015f))
        SphereNode(radius = 0.105f, materialInstance = skin,
            position = Position(rightHandX, rightHandY, 0.015f))

        // Fingers follow the palms instead of remaining fixed at the hips.
        CapsuleNode(radius = 0.024f, height = 0.12f, materialInstance = skin,
            position = Position(leftHandX - 0.075f, leftHandY - 0.095f, 0.025f), rotation = Rotation(z = leftForearmAngle - 8f))
        CapsuleNode(radius = 0.024f, height = 0.12f, materialInstance = skin,
            position = Position(leftHandX - 0.025f, leftHandY - 0.105f, 0.03f), rotation = Rotation(z = leftForearmAngle - 3f))
        CapsuleNode(radius = 0.024f, height = 0.12f, materialInstance = skin,
            position = Position(leftHandX + 0.025f, leftHandY - 0.105f, 0.03f), rotation = Rotation(z = leftForearmAngle + 3f))
        CapsuleNode(radius = 0.024f, height = 0.11f, materialInstance = skin,
            position = Position(leftHandX + 0.075f, leftHandY - 0.095f, 0.025f), rotation = Rotation(z = leftForearmAngle + 8f))
        CapsuleNode(radius = 0.024f, height = 0.12f, materialInstance = skin,
            position = Position(rightHandX - 0.075f, rightHandY - 0.095f, 0.025f), rotation = Rotation(z = rightForearmAngle - 8f))
        CapsuleNode(radius = 0.024f, height = 0.12f, materialInstance = skin,
            position = Position(rightHandX - 0.025f, rightHandY - 0.105f, 0.03f), rotation = Rotation(z = rightForearmAngle - 3f))
        CapsuleNode(radius = 0.024f, height = 0.12f, materialInstance = skin,
            position = Position(rightHandX + 0.025f, rightHandY - 0.105f, 0.03f), rotation = Rotation(z = rightForearmAngle + 3f))
        CapsuleNode(radius = 0.024f, height = 0.11f, materialInstance = skin,
            position = Position(rightHandX + 0.075f, rightHandY - 0.095f, 0.025f), rotation = Rotation(z = rightForearmAngle + 8f))

        // Sweater hem and subtle seams are separate geometry, not portrait pixels.
        CubeNode(size = Size(0.48f, 0.045f, 0.43f), materialInstance = cuff,
            position = Position(0f, 1.02f + breathe, 0f))
        CubeNode(size = Size(0.34f, 0.025f, 0.018f), materialInstance = heartRed,
            position = Position(0f, 1.50f + breathe, 0.286f))
        CubeNode(size = Size(0.40f, 0.022f, 0.018f), materialInstance = cuff,
            position = Position(0f, 1.30f + breathe, 0.285f))
        CubeNode(size = Size(0.42f, 0.035f, 0.035f), materialInstance = cuff,
            position = Position(0f, 0.99f + breathe, 0.22f))

        // Hip pivots and leg segments follow the calculated hip-knee-ankle chain.
        SphereNode(radius = 0.14f, materialInstance = trousers,
            position = Position(-0.15f, hipY, 0f))
        SphereNode(radius = 0.14f, materialInstance = trousers,
            position = Position(0.15f, hipY, 0f))
        CapsuleNode(radius = 0.135f, height = thighLength, materialInstance = trousers,
            position = Position((-0.15f + leftKneeX) / 2f, (hipY + leftKneeY) / 2f, 0f),
            rotation = Rotation(z = leftThighAngle + 180f))
        CapsuleNode(radius = 0.135f, height = thighLength, materialInstance = trousers,
            position = Position((0.15f + rightKneeX) / 2f, (hipY + rightKneeY) / 2f, 0f),
            rotation = Rotation(z = rightThighAngle + 180f))
        SphereNode(radius = 0.105f, materialInstance = trousers,
            position = Position(leftKneeX, leftKneeY, 0f))
        SphereNode(radius = 0.105f, materialInstance = trousers,
            position = Position(rightKneeX, rightKneeY, 0f))
        CapsuleNode(radius = 0.115f, height = calfLength, materialInstance = trousers,
            position = Position((leftKneeX + leftAnkleX) / 2f, (leftKneeY + leftAnkleY) / 2f, 0f),
            rotation = Rotation(z = leftCalfAngle + 180f))
        CapsuleNode(radius = 0.115f, height = calfLength, materialInstance = trousers,
            position = Position((rightKneeX + rightAnkleX) / 2f, (rightKneeY + rightAnkleY) / 2f, 0f),
            rotation = Rotation(z = rightCalfAngle + 180f))
        SphereNode(radius = 0.105f, materialInstance = trousers,
            position = Position(leftAnkleX, leftAnkleY, 0f))
        SphereNode(radius = 0.105f, materialInstance = trousers,
            position = Position(rightAnkleX, rightAnkleY, 0f))
        CubeNode(size = Size(0.30f, 0.12f, 0.42f), materialInstance = shoe,
            position = Position(leftAnkleX, leftAnkleY - 0.055f, 0.09f))
        CubeNode(size = Size(0.30f, 0.12f, 0.42f), materialInstance = shoe,
            position = Position(rightAnkleX, rightAnkleY - 0.055f, 0.09f))
    }
}
