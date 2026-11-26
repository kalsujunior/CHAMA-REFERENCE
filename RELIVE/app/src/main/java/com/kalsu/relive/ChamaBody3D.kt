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
    val braidHair = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF35231F), roughness = 0.92f)
    }

    val motion = rememberInfiniteTransition(label = "chama_body_motion")
    val wave by motion.animateFloat(
        initialValue = -8f,
        targetValue = if (!motionEnabled) -8f else when (gesture) {
            ChamaBodyGesture.IDLE -> 2f
            ChamaBodyGesture.WAVE -> 38f
            ChamaBodyGesture.LISTEN -> -12f
            ChamaBodyGesture.DANCE -> 27f
        },
        animationSpec = infiniteRepeatable(tween(1050), RepeatMode.Reverse),
        label = "right_arm_wave"
    )
    val leftWave by motion.animateFloat(
        initialValue = 7f,
        targetValue = if (!motionEnabled) 7f else when (gesture) {
            ChamaBodyGesture.IDLE -> 3f
            ChamaBodyGesture.WAVE -> -8f
            ChamaBodyGesture.LISTEN -> 14f
            ChamaBodyGesture.DANCE -> -30f
        },
        animationSpec = infiniteRepeatable(tween(1250), RepeatMode.Reverse),
        label = "left_arm_motion"
    )
    val braidSwing by motion.animateFloat(
        initialValue = -4f,
        targetValue = if (!motionEnabled) -4f else when (gesture) {
            ChamaBodyGesture.IDLE -> 4f
            ChamaBodyGesture.WAVE -> 8f
            ChamaBodyGesture.LISTEN -> -2f
            ChamaBodyGesture.DANCE -> 15f
        },
        animationSpec = infiniteRepeatable(tween(1650), RepeatMode.Reverse),
        label = "braid_swing"
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

        // Two independent hair braids: they are separate body accessories,
        // not a head mesh and never touch or alter the protected portrait.
        CapsuleNode(radius = 0.065f, height = 0.78f, materialInstance = braidHair,
            position = Position(-0.39f, 1.76f, -0.13f), rotation = Rotation(z = -7f + braidSwing))
        CapsuleNode(radius = 0.065f, height = 0.78f, materialInstance = braidHair,
            position = Position(0.39f, 1.76f, -0.13f), rotation = Rotation(z = 7f - braidSwing))
        SphereNode(radius = 0.052f, materialInstance = braidHair,
            position = Position(-0.39f, 1.99f, -0.13f))
        SphereNode(radius = 0.052f, materialInstance = braidHair,
            position = Position(0.39f, 1.99f, -0.13f))
        SphereNode(radius = 0.048f, materialInstance = braidHair,
            position = Position(-0.42f, 1.66f, -0.13f))
        SphereNode(radius = 0.048f, materialInstance = braidHair,
            position = Position(-0.36f, 1.55f, -0.13f))
        SphereNode(radius = 0.048f, materialInstance = braidHair,
            position = Position(-0.42f, 1.44f, -0.13f))
        SphereNode(radius = 0.048f, materialInstance = braidHair,
            position = Position(-0.36f, 1.33f, -0.13f))
        SphereNode(radius = 0.048f, materialInstance = braidHair,
            position = Position(0.42f, 1.66f, -0.13f))
        SphereNode(radius = 0.048f, materialInstance = braidHair,
            position = Position(0.36f, 1.55f, -0.13f))
        SphereNode(radius = 0.048f, materialInstance = braidHair,
            position = Position(0.42f, 1.44f, -0.13f))
        SphereNode(radius = 0.048f, materialInstance = braidHair,
            position = Position(0.36f, 1.33f, -0.13f))

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

        // Shoulder joints and arms. The upper arm rotates independently.
        SphereNode(radius = 0.13f, materialInstance = sweater,
            position = Position(-0.31f, 1.70f + breathe, 0f))
        SphereNode(radius = 0.13f, materialInstance = sweater,
            position = Position(0.31f, 1.70f + breathe, 0f))
        CapsuleNode(
            radius = 0.105f, height = 0.48f,
            materialInstance = sweater,
            position = Position(-0.43f, 1.40f + breathe, 0f),
            rotation = Rotation(z = leftWave)
        )
        CapsuleNode(
            radius = 0.105f, height = 0.48f,
            materialInstance = sweater,
            position = Position(0.43f, 1.40f + breathe, 0f),
            rotation = Rotation(z = wave)
        )
        CapsuleNode(
            radius = 0.085f, height = 0.42f,
            materialInstance = sweater,
            position = Position(-0.49f + sway, 1.04f, 0f),
            rotation = Rotation(z = leftWave * 0.55f)
        )
        CapsuleNode(
            radius = 0.085f, height = 0.42f,
            materialInstance = sweater,
            position = Position(0.49f + sway, 1.05f, 0f),
            rotation = Rotation(z = wave * 0.72f)
        )
        SphereNode(radius = 0.105f, materialInstance = sweater,
            position = Position(-0.49f + sway, 1.17f, 0f))
        SphereNode(radius = 0.105f, materialInstance = sweater,
            position = Position(0.49f + sway, 1.17f, 0f))
        SphereNode(radius = 0.105f, materialInstance = skin,
            position = Position(-0.53f + sway + leftWave * 0.0015f, 0.79f + leftWave * 0.002f, 0f))
        SphereNode(radius = 0.105f, materialInstance = skin,
            position = Position(0.54f + sway + wave * 0.0015f, 0.80f + wave * 0.002f, 0f))

        // Cuffs, palms and simple fingers make the hands readable at phone scale.
        SphereNode(radius = 0.075f, materialInstance = cuff,
            position = Position(-0.50f + sway + leftWave * 0.0015f, 0.88f + leftWave * 0.002f, 0f))
        SphereNode(radius = 0.075f, materialInstance = cuff,
            position = Position(0.51f + sway + wave * 0.0015f, 0.89f + wave * 0.002f, 0f))
        CapsuleNode(radius = 0.026f, height = 0.12f, materialInstance = skin,
            position = Position(-0.63f + sway + leftWave * 0.0015f, 0.69f + leftWave * 0.002f, 0.015f), rotation = Rotation(z = -8f))
        CapsuleNode(radius = 0.026f, height = 0.12f, materialInstance = skin,
            position = Position(-0.59f + sway + leftWave * 0.0015f, 0.68f + leftWave * 0.002f, 0.025f))
        CapsuleNode(radius = 0.026f, height = 0.12f, materialInstance = skin,
            position = Position(-0.55f + sway + leftWave * 0.0015f, 0.68f + leftWave * 0.002f, 0.025f))
        CapsuleNode(radius = 0.026f, height = 0.11f, materialInstance = skin,
            position = Position(-0.51f + sway + leftWave * 0.0015f, 0.69f + leftWave * 0.002f, 0.015f), rotation = Rotation(z = 8f))
        CapsuleNode(radius = 0.026f, height = 0.12f, materialInstance = skin,
            position = Position(0.49f + sway + wave * 0.0015f, 0.69f + wave * 0.002f, 0.015f), rotation = Rotation(z = -8f))
        CapsuleNode(radius = 0.026f, height = 0.12f, materialInstance = skin,
            position = Position(0.53f + sway + wave * 0.0015f, 0.68f + wave * 0.002f, 0.025f))
        CapsuleNode(radius = 0.026f, height = 0.12f, materialInstance = skin,
            position = Position(0.57f + sway + wave * 0.0015f, 0.68f + wave * 0.002f, 0.025f))
        CapsuleNode(radius = 0.026f, height = 0.11f, materialInstance = skin,
            position = Position(0.61f + sway + wave * 0.0015f, 0.69f + wave * 0.002f, 0.015f), rotation = Rotation(z = 8f))

        // Sweater hem and subtle seams are separate geometry, not portrait pixels.
        CubeNode(size = Size(0.48f, 0.045f, 0.43f), materialInstance = cuff,
            position = Position(0f, 1.02f + breathe, 0f))
        CubeNode(size = Size(0.34f, 0.025f, 0.018f), materialInstance = heartRed,
            position = Position(0f, 1.50f + breathe, 0.286f))
        CubeNode(size = Size(0.40f, 0.022f, 0.018f), materialInstance = cuff,
            position = Position(0f, 1.30f + breathe, 0.285f))
        CubeNode(size = Size(0.42f, 0.035f, 0.035f), materialInstance = cuff,
            position = Position(0f, 0.99f + breathe, 0.22f))

        // Legs and shoes complete the independent body silhouette.
        CapsuleNode(radius = 0.13f, height = 0.68f, materialInstance = trousers,
            position = Position(-0.16f, 0.36f + step, 0f), rotation = Rotation(z = if (gesture == ChamaBodyGesture.DANCE) 5f else 0f))
        CapsuleNode(radius = 0.13f, height = 0.68f, materialInstance = trousers,
            position = Position(0.16f, 0.36f - step, 0f), rotation = Rotation(z = if (gesture == ChamaBodyGesture.DANCE) -5f else 0f))
        CubeNode(size = Size(0.30f, 0.12f, 0.42f), materialInstance = shoe,
            position = Position(-0.16f, 0.025f + step, 0.09f))
        CubeNode(size = Size(0.30f, 0.12f, 0.42f), materialInstance = shoe,
            position = Position(0.16f, 0.025f - step, 0.09f))
    }
}
