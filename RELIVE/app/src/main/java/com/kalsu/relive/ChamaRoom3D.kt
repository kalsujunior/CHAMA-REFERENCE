package com.kalsu.relive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.sceneview.SceneView
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size
import com.kalsu.relive.avatar.ChamaBodyGesture
import com.kalsu.relive.avatar.ChamaRoomPoint

/**
 * First real-time 3D room blockout for RELIVE.
 * All furniture and architecture are independent Filament meshes.
 * Chama's protected portrait is not loaded, mapped, or modified here.
 */
@Composable
internal fun ChamaRoom3D(modifier: Modifier = Modifier, palette: RelivePalette, lampOn: Boolean = true, evening: Boolean = false, sofaWelcoming: Boolean = false, bouquetHighlighted: Boolean = false, bodyGesture: ChamaBodyGesture = ChamaBodyGesture.IDLE, bodyPosition: ChamaRoomPoint = ChamaRoomPoint(0f, 1.10f), bodyHeadingDegrees: Float = 0f, onSceneInteraction: () -> Unit = {}) {
    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
    val latestSceneInteraction = rememberUpdatedState(onSceneInteraction)

    val wall = remember(materialLoader, palette.background) {
        materialLoader.createColorInstance(palette.background)
    }
    val floor = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFE9DED2))
    }
    val sofa = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFF0DDE0))
    }
    val sofaAccent = remember(materialLoader, sofaWelcoming) {
        materialLoader.createColorInstance(
            if (sofaWelcoming) Color(0xFFE56B83) else Color(0xFFB94A5B),
            roughness = 0.88f
        )
    }
    val wood = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF68443B))
    }
    val gold = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFD4AF37), metallic = 0.72f, roughness = 0.28f)
    }
    val glass = remember(materialLoader, evening) {
        materialLoader.createColorInstance(
            if (evening) Color(0xFF263B62) else Color(0xFF8FB9C9),
            roughness = 0.18f
        )
    }
    val leaf = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF466B4D))
    }
    val curtain = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFB86A78), roughness = 0.96f)
    }
    val rug = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFB94A5B), roughness = 0.98f)
    }
    val paper = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFE8D7C5), roughness = 0.9f)
    }
    val rose = remember(materialLoader, bouquetHighlighted) {
        materialLoader.createColorInstance(
            if (bouquetHighlighted) Color(0xFFFF5278) else Color(0xFFB52C4A),
            roughness = 0.72f
        )
    }
    val lampMaterial = remember(materialLoader, lampOn) {
        materialLoader.createColorInstance(
            if (lampOn) Color(0xFFFFD98A) else Color(0xFF786C69),
            metallic = if (lampOn) 0.08f else 0.0f,
            roughness = 0.42f
        )
    }

    // Palette du médaillon mural RELIVE-KAL-SU, inspiré de l'illustration de référence.
    val muralPanel = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFF5A302B), roughness = 0.82f)
    }
    val muralGold = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFE2B84F), metallic = 0.78f, roughness = 0.24f)
    }
    val muralRed = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFB51232), metallic = 0.18f, roughness = 0.28f)
    }
    val muralPink = remember(materialLoader) {
        materialLoader.createColorInstance(Color(0xFFE99AA8), roughness = 0.55f)
    }

    SceneView(
        modifier = modifier,
        engine = engine,
        materialLoader = materialLoader,
        cameraManipulator = rememberCameraManipulator(
            orbitHomePosition = Position(0f, 1.35f, 5.6f),
            targetPosition = Position(0f, 0.75f, 0f)
        ),
        onTouchEvent = { _, _ ->
            latestSceneInteraction.value()
            false
        }
    ) {
        // Sol et murs de la pièce.
        PlaneNode(
            size = Size(5.2f, 5.2f),
            normal = Direction(0f, 1f, 0f),
            materialInstance = floor,
            position = Position(0f, -0.03f, 0f)
        )
        PlaneNode(
            size = Size(5.2f, 3.2f),
            normal = Direction(0f, 0f, 1f),
            materialInstance = wall,
            position = Position(0f, 1.55f, -2.35f)
        )
        PlaneNode(
            size = Size(4.7f, 3.0f),
            normal = Direction(1f, 0f, 0f),
            materialInstance = wall,
            position = Position(-2.55f, 1.45f, 0f)
        )

        // Grande fenêtre panoramique et ses montants.
        CubeNode(
            size = Size(1.72f, 1.28f, 0.035f),
            materialInstance = glass,
            position = Position(-1.15f, 1.78f, -2.29f)
        )
        CubeNode(size = Size(1.82f, 0.055f, 0.08f), materialInstance = gold,
            position = Position(-1.15f, 2.44f, -2.22f))
        CubeNode(size = Size(1.82f, 0.055f, 0.08f), materialInstance = gold,
            position = Position(-1.15f, 1.12f, -2.22f))
        CubeNode(size = Size(0.055f, 1.36f, 0.08f), materialInstance = gold,
            position = Position(-2.06f, 1.78f, -2.22f))
        CubeNode(size = Size(0.055f, 1.36f, 0.08f), materialInstance = gold,
            position = Position(-0.24f, 1.78f, -2.22f))
        CubeNode(size = Size(0.035f, 1.28f, 0.06f), materialInstance = gold,
            position = Position(-1.15f, 1.78f, -2.20f))

        // Rideaux encadrant la baie panoramique.
        CubeNode(size = Size(0.22f, 1.48f, 0.10f), materialInstance = curtain,
            position = Position(-2.13f, 1.78f, -2.12f))
        CubeNode(size = Size(0.22f, 1.48f, 0.10f), materialInstance = curtain,
            position = Position(-0.17f, 1.78f, -2.12f))
        CylinderNode(radius = 0.025f, height = 2.08f, materialInstance = gold,
            position = Position(-1.15f, 2.55f, -2.10f), rotation = io.github.sceneview.math.Rotation(z = 90f))

        // Tapis rouge profond au centre du salon.
        CubeNode(size = Size(2.70f, 0.025f, 1.55f), materialInstance = rug,
            position = Position(0.0f, 0.005f, 0.35f))
        CubeNode(size = Size(2.48f, 0.018f, 1.34f), materialInstance = sofa,
            position = Position(0.0f, 0.025f, 0.35f))

        // Canapé crème/rose, coussins et accoudoirs.
        CubeNode(size = Size(2.05f, 0.42f, 0.78f), materialInstance = sofa,
            position = Position(0.05f, 0.36f, -0.92f))
        CubeNode(size = Size(2.05f, 0.78f, 0.22f), materialInstance = sofa,
            position = Position(0.05f, 0.78f, -1.25f))
        CubeNode(size = Size(0.24f, 0.55f, 0.78f), materialInstance = sofaAccent,
            position = Position(-1.10f, 0.48f, -0.92f))
        CubeNode(size = Size(0.24f, 0.55f, 0.78f), materialInstance = sofaAccent,
            position = Position(1.20f, 0.48f, -0.92f))
        CubeNode(size = Size(0.48f, 0.40f, 0.12f), materialInstance = sofaAccent,
            position = Position(-0.48f, 0.70f, -0.88f))
        CubeNode(size = Size(0.48f, 0.40f, 0.12f), materialInstance = sofaAccent,
            position = Position(0.45f, 0.70f, -0.88f))

        // Table basse avec plateau doré.
        CubeNode(size = Size(1.35f, 0.08f, 0.62f), materialInstance = wood,
            position = Position(0.05f, 0.20f, 0.10f))
        CubeNode(size = Size(1.40f, 0.035f, 0.67f), materialInstance = gold,
            position = Position(0.05f, 0.255f, 0.10f))

        // Petite console latérale et lampe.
        CubeNode(size = Size(0.48f, 0.48f, 0.42f), materialInstance = wood,
            position = Position(1.72f, 0.25f, -0.78f))
        CylinderNode(radius = 0.035f, height = 0.72f, materialInstance = gold,
            position = Position(1.72f, 0.83f, -0.78f))
        SphereNode(radius = 0.22f, materialInstance = lampMaterial,
            position = Position(1.72f, 1.28f, -0.78f))
        if (lampOn) {
            SphereNode(radius = 0.075f, materialInstance = paper,
                position = Position(1.72f, 1.24f, -0.76f))
        }

        // Étagère murale et livres décoratifs.
        CubeNode(size = Size(0.72f, 0.08f, 0.24f), materialInstance = wood,
            position = Position(-2.12f, 1.10f, -1.30f))
        CubeNode(size = Size(0.13f, 0.27f, 0.16f), materialInstance = rose,
            position = Position(-2.32f, 1.27f, -1.31f), rotation = io.github.sceneview.math.Rotation(z = -7f))
        CubeNode(size = Size(0.13f, 0.31f, 0.16f), materialInstance = paper,
            position = Position(-2.16f, 1.29f, -1.31f))
        CubeNode(size = Size(0.13f, 0.24f, 0.16f), materialInstance = gold,
            position = Position(-2.00f, 1.25f, -1.31f))

        // Médaillon mural sculptural RELIVE-KAL-SU : panneau, cœur rouge et accents floraux.
        CubeNode(size = Size(1.22f, 0.92f, 0.075f), materialInstance = muralGold,
            position = Position(1.42f, 1.88f, -2.285f))
        CubeNode(size = Size(1.14f, 0.84f, 0.045f), materialInstance = muralPanel,
            position = Position(1.42f, 1.88f, -2.235f))
        // Cœur stylisé en relief : deux lobes arrondis et une pointe en losange.
        CubeNode(size = Size(0.48f, 0.48f, 0.085f), materialInstance = muralGold,
            position = Position(1.42f, 1.91f, -2.185f),
            rotation = io.github.sceneview.math.Rotation(z = 45f))
        CubeNode(size = Size(0.42f, 0.42f, 0.095f), materialInstance = muralRed,
            position = Position(1.42f, 1.91f, -2.125f),
            rotation = io.github.sceneview.math.Rotation(z = 45f))
        SphereNode(radius = 0.205f, materialInstance = muralRed,
            position = Position(1.28f, 2.075f, -2.12f))
        SphereNode(radius = 0.205f, materialInstance = muralRed,
            position = Position(1.56f, 2.075f, -2.12f))
        // Petites roses et ornements dorés de chaque côté.
        SphereNode(radius = 0.105f, materialInstance = rose,
            position = Position(0.98f, 1.88f, -2.13f))
        SphereNode(radius = 0.105f, materialInstance = muralPink,
            position = Position(1.86f, 1.88f, -2.13f))
        SphereNode(radius = 0.055f, materialInstance = muralGold,
            position = Position(1.05f, 2.18f, -2.13f))
        SphereNode(radius = 0.055f, materialInstance = muralGold,
            position = Position(1.79f, 2.18f, -2.13f))
        CubeNode(size = Size(0.025f, 0.42f, 0.035f), materialInstance = muralGold,
            position = Position(1.42f, 1.88f, -2.105f))

        // Suspension et pampille lumineuse décorative.
        CylinderNode(radius = 0.025f, height = 0.58f, materialInstance = gold,
            position = Position(0.55f, 2.55f, -0.45f))
        SphereNode(radius = 0.16f, materialInstance = gold,
            position = Position(0.55f, 2.25f, -0.45f))
        SphereNode(radius = 0.10f, materialInstance = paper,
            position = Position(0.55f, 2.20f, -0.45f))

        // Vase de roses sur la console.
        CylinderNode(radius = 0.11f, height = 0.22f, materialInstance = gold,
            position = Position(1.72f, 0.60f, -0.78f))
        SphereNode(radius = 0.075f, materialInstance = rose,
            position = Position(1.65f, 0.82f, -0.78f))
        SphereNode(radius = 0.075f, materialInstance = rose,
            position = Position(1.78f, 0.84f, -0.78f))
        SphereNode(radius = 0.07f, materialInstance = leaf,
            position = Position(1.72f, 0.91f, -0.78f))

        // Plante décorative à droite.
        CylinderNode(radius = 0.18f, height = 0.34f, materialInstance = wood,
            position = Position(2.02f, 0.17f, -1.65f))
        CylinderNode(radius = 0.025f, height = 0.72f, materialInstance = leaf,
            position = Position(2.02f, 0.68f, -1.65f))
        SphereNode(radius = 0.24f, materialInstance = leaf,
            position = Position(1.83f, 1.02f, -1.65f))
        SphereNode(radius = 0.22f, materialInstance = leaf,
            position = Position(2.20f, 1.12f, -1.65f))
        SphereNode(radius = 0.20f, materialInstance = leaf,
            position = Position(2.02f, 1.34f, -1.65f))
        // Chama partage le même graphe 3D que le salon : son Node racine
        // reçoit la position et le cap de la simulation de locomotion.
        ChamaBodyNodes(
            gesture = bodyGesture,
            rootPosition = bodyPosition,
            headingDegrees = bodyHeadingDegrees
        )
    }
}
