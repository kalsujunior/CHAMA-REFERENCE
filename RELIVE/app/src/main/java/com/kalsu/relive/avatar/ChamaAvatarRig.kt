package com.kalsu.relive.avatar

/**
 * Body-only rig specification for a future Chama renderer.
 *
 * The face and head are deliberately absent: the original portrait remains a
 * separate, immutable 2D layer. No facial joints, expressions, visemes,
 * morph targets, eye motion, jaw motion or head rotation are defined here.
 *
 * This file describes planned body controls; it is not itself a 3D model.
 */
enum class ChamaBodyJoint {
    ROOT,
    TORSO,
    SHOULDER_LEFT, ELBOW_LEFT, WRIST_LEFT, HAND_LEFT,
    SHOULDER_RIGHT, ELBOW_RIGHT, WRIST_RIGHT, HAND_RIGHT,
    BRAID_LEFT, BRAID_RIGHT
}

enum class ChamaBodyGesture {
    IDLE,
    WAVE,
    LISTEN,
    DANCE
}

data class ChamaBodyPose(
    val gesture: ChamaBodyGesture = ChamaBodyGesture.IDLE,
    val breathingAmount: Float = 0f,
    val leftArmAmount: Float = 0f,
    val rightArmAmount: Float = 0f,
    val braidSwingAmount: Float = 0f
)
