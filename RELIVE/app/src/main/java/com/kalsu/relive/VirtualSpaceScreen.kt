package com.kalsu.relive

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import android.os.SystemClock
import android.os.PowerManager
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalsu.relive.data.PreferenceStore
import com.kalsu.relive.avatar.ChamaBodyGesture
import com.kalsu.relive.avatar.ChamaAutonomyRuntime
import com.kalsu.relive.avatar.ChamaAutonomyRuntimeInput
import com.kalsu.relive.avatar.ChamaAutonomyNeeds
import com.kalsu.relive.avatar.ChamaRoomWaypoint
import com.kalsu.relive.avatar.ChamaLocomotionStatus
import com.kalsu.relive.avatar.ChamaRoomNavigation
import com.kalsu.relive.avatar.ChamaSeatTransitionMath
import com.kalsu.relive.avatar.ChamaFlowerLocation
import com.kalsu.relive.avatar.ChamaFlowerInteraction
import com.kalsu.relive.avatar.ChamaFlowerInteractionController
import com.kalsu.relive.voice.ChamaSpeech

private val RoomGold: Color @Composable get() = LocalRelivePalette.current.primary
private val RoomInk: Color @Composable get() = LocalRelivePalette.current.background
private val RoomCream: Color @Composable get() = LocalRelivePalette.current.text
private val RoomRose: Color @Composable get() = LocalRelivePalette.current.secondary

@Composable
fun VirtualSpaceScreen(modifier: Modifier = Modifier, onTalk: () -> Unit, onGallery: () -> Unit) {
    val context = LocalContext.current
    val palette = LocalRelivePalette.current
    val preferences = remember { PreferenceStore(context) }
    val speech = remember { ChamaSpeech(context) }
    val autonomyRuntime = remember { ChamaAutonomyRuntime() }
    val autonomyNeeds = remember { ChamaAutonomyNeeds() }
    var needsSnapshot by remember { mutableStateOf(autonomyNeeds.snapshot()) }
    val powerManager = remember(context) { context.getSystemService(PowerManager::class.java) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var screenResumed by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    var autonomyEnabled by remember { mutableStateOf(false) }
    var autonomyFrame by remember { mutableStateOf(autonomyRuntime.stop()) }
    var status by remember { mutableStateOf("Je suis heureuse de te retrouver ici.") }
    var interactionStatus by remember { mutableStateOf<String?>(null) }
    var speaking by remember { mutableStateOf(false) }
    var bodyGesture by remember { mutableStateOf(ChamaBodyGesture.IDLE) }
    var lampOn by remember { mutableStateOf(true) }
    var eveningAmbience by remember { mutableStateOf(false) }
    var sofaWelcoming by remember { mutableStateOf(false) }
    var bouquetHighlighted by remember { mutableStateOf(false) }
    val flowerInteraction = remember { ChamaFlowerInteractionController() }
    var flowerLocation by remember { mutableStateOf(flowerInteraction.location) }
    var lightMode by remember { mutableStateOf(false) }
    val seatRequested = autonomyEnabled && !lightMode &&
        autonomyFrame.currentWaypoint == ChamaRoomWaypoint.SOFA_SEAT &&
        autonomyFrame.decision.bodyGesture == ChamaBodyGesture.SIT
    val seatProgress by animateFloatAsState(
        targetValue = if (seatRequested) 1f else 0f,
        animationSpec = tween(durationMillis = 1_200),
        label = "chama_sofa_seat_transition"
    )
    val latestSeatProgress by rememberUpdatedState(seatProgress)
    val seatPose = remember(seatProgress) {
        ChamaSeatTransitionMath.pose(
            approach = ChamaRoomNavigation.pointOf(ChamaRoomWaypoint.SOFA_SEAT),
            progress = seatProgress
        )
    }
    val renderedBodyPosition = if (seatProgress > 0.001f) {
        seatPose.position
    } else {
        autonomyFrame.locomotion.position
    }
    val renderedBodyRootHeight = if (seatProgress > 0.001f) seatPose.rootHeight else 0f
    val bouquetPose = remember(
        flowerLocation,
        renderedBodyPosition,
        autonomyFrame.locomotion.headingDegrees
    ) {
        ChamaFlowerInteraction.poseAt(
            location = flowerLocation,
            bodyPosition = renderedBodyPosition,
            headingDegrees = autonomyFrame.locomotion.headingDegrees
        )
    }

    DisposableEffect(speech) {
        onDispose { speech.release() }
    }
    DisposableEffect(autonomyRuntime) {
        onDispose { autonomyRuntime.stop() }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> screenResumed = true
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> screenResumed = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun pauseAutonomyForUser() {
        val wasEnabled = autonomyEnabled
        autonomyEnabled = false
        if (flowerInteraction.dropOnTableIfHeld()) {
            flowerLocation = flowerInteraction.location
        }
        autonomyFrame = autonomyRuntime.stop()
        if (wasEnabled) {
            status = "Je m'arrête pour être attentive à toi."
        }
    }

    LaunchedEffect(autonomyEnabled, lightMode, screenResumed) {
        if (!autonomyEnabled) {
            autonomyFrame = autonomyRuntime.stop()
            autonomyNeeds.resetClock()
            return@LaunchedEffect
        }
        if (!screenResumed) {
            val now = SystemClock.elapsedRealtime()
            autonomyFrame = autonomyRuntime.tick(
                ChamaAutonomyRuntimeInput(
                    enabled = true,
                    virtualSpaceVisible = false,
                    userInteracting = false,
                    lowPowerMode = true,
                    energy = needsSnapshot.energy,
                    curiosity = needsSnapshot.curiosity,
                    nowElapsedRealtimeMs = now
                )
            )
            autonomyNeeds.resetClock()
            return@LaunchedEffect
        }
        while (isActive) {
            val now = SystemClock.elapsedRealtime()
            needsSnapshot = autonomyNeeds.advance(
                nowElapsedRealtimeMs = now,
                activity = autonomyFrame.decision.activity,
                locomotionStatus = autonomyFrame.locomotion.status
            )
            val nextFrame = autonomyRuntime.tick(
                ChamaAutonomyRuntimeInput(
                    enabled = true,
                    virtualSpaceVisible = !lightMode,
                    userInteracting = false,
                    lowPowerMode = lightMode || powerManager?.isPowerSaveMode == true,
                    energy = needsSnapshot.energy,
                    curiosity = needsSnapshot.curiosity,
                    nowElapsedRealtimeMs = now,
                    holdMovementForSeatTransition = latestSeatProgress > 0.001f
                )
            )
            autonomyFrame = nextFrame
            status = nextFrame.routeError ?: nextFrame.decision.statusFr
            delay(50L)
        }
    }

    LaunchedEffect(
        autonomyEnabled,
        screenResumed,
        lightMode,
        autonomyFrame.decision.pauseReason,
        flowerLocation
    ) {
        val shouldDropHeldBouquet = flowerLocation == ChamaFlowerLocation.IN_HAND &&
            (!autonomyEnabled || !screenResumed || lightMode ||
                autonomyFrame.decision.pauseReason != null)
        if (shouldDropHeldBouquet && flowerInteraction.dropOnTableIfHeld()) {
            flowerLocation = flowerInteraction.location
        }
    }

    LaunchedEffect(interactionStatus) {
        if (interactionStatus != null) {
            delay(3_500L)
            interactionStatus = null
        }
    }

    // When Chama reaches the vase autonomously, she briefly picks up a rose
    // and places the bouquet on the coffee table. The object remains inside
    // the salon and returns to a safe surface if the user interrupts.
    LaunchedEffect(
        autonomyEnabled,
        autonomyFrame.currentWaypoint,
        autonomyFrame.locomotion.status,
        autonomyFrame.decision.activity
    ) {
        if (!autonomyEnabled ||
            autonomyFrame.currentWaypoint != ChamaRoomWaypoint.FLOWERS_STOP ||
            autonomyFrame.locomotion.status != ChamaLocomotionStatus.ARRIVED ||
            autonomyFrame.decision.activity != com.kalsu.relive.avatar.ChamaAutonomyActivity.WALK_TO_FLOWERS ||
            flowerLocation != ChamaFlowerLocation.VASE
        ) return@LaunchedEffect

        delay(700L)
        if (autonomyEnabled &&
            autonomyFrame.currentWaypoint == ChamaRoomWaypoint.FLOWERS_STOP &&
            autonomyFrame.locomotion.status == ChamaLocomotionStatus.ARRIVED &&
            flowerLocation == ChamaFlowerLocation.VASE
        ) {
            if (flowerInteraction.pickUp()) {
                flowerLocation = flowerInteraction.location
                interactionStatus = "J'ai choisi une rose pour la regarder de plus près. 🌹"
                delay(1_200L)
                if (autonomyEnabled && flowerInteraction.placeOnTable()) {
                    flowerLocation = flowerInteraction.location
                    interactionStatus = "J'ai déposé le bouquet sur la table basse."
                }
            }
        }
    }

    fun act(line: String, gesture: ChamaBodyGesture) {
        pauseAutonomyForUser()
        status = line
        bodyGesture = gesture
        if (!preferences.voiceEnabled) {
            speaking = false
            return
        }
        speaking = true
        speech.speak(
            text = line,
            languageTag = preferences.languageTag,
            onDone = { speaking = false },
        )
    }

    Column(
        modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(palette.primary.copy(alpha = 0.38f), palette.background.copy(alpha = 0.94f), RoomInk))
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("L'ESPACE DE CHAMA", color = RoomGold, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Text("Un lieu calme pour vous retrouver", color = palette.muted, fontSize = 11.sp)
                Text("Fais glisser la pièce pour en changer l'angle", color = palette.muted, fontSize = 9.sp)
            }
            Surface(color = palette.surface, shape = CircleShape) {
                Text("KS", color = RoomGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp))
            }
        }

        Box(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.verticalGradient(listOf(palette.primary.copy(alpha = 0.36f), palette.surface, palette.background)))
        ) {
            if (!lightMode) {
                ChamaRoom3D(
                    modifier = Modifier.fillMaxSize(),
                    palette = palette,
                    lampOn = lampOn,
                    evening = eveningAmbience,
                    sofaWelcoming = sofaWelcoming || (
                        autonomyEnabled &&
                            (autonomyFrame.currentWaypoint == ChamaRoomWaypoint.SOFA_STOP ||
                                autonomyFrame.currentWaypoint == ChamaRoomWaypoint.SOFA_SEAT) &&
                            autonomyFrame.locomotion.status == ChamaLocomotionStatus.ARRIVED
                    ),
                    bouquetHighlighted = bouquetHighlighted || (
                        autonomyEnabled && autonomyFrame.currentWaypoint == ChamaRoomWaypoint.FLOWERS_STOP &&
                            autonomyFrame.locomotion.status == ChamaLocomotionStatus.ARRIVED
                    ),
                    bodyGesture = if (autonomyEnabled) {
                        if (seatProgress > 0.001f && autonomyFrame.decision.bodyGesture != ChamaBodyGesture.SIT) ChamaBodyGesture.IDLE
                        else autonomyFrame.bodyGesture
                    } else bodyGesture,
                    bodyPosition = renderedBodyPosition,
                    bodyHeadingDegrees = autonomyFrame.locomotion.headingDegrees,
                    bodyGaitPhase = autonomyFrame.locomotion.gaitPhase,
                    bodyRootHeight = renderedBodyRootHeight,
                    bouquetPose = bouquetPose,
                    onSceneInteraction = { pauseAutonomyForUser() }
                )
            } else {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(palette.primary.copy(alpha = 0.24f), palette.background, palette.surface))
                    ),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        "Mode léger : décor 3D désactivé pour économiser les ressources.",
                        color = palette.muted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(16.dp))
                Text("Bienvenue dans mon univers", color = RoomCream, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Text("ESPÉRANCE RUCHAMA KEFORA", color = RoomGold, fontSize = 11.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))

                Spacer(Modifier.weight(1f))

                Spacer(Modifier.height(12.dp))
                Text(interactionStatus ?: status, color = RoomCream, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
                if (autonomyEnabled) {
                    Text(
                        "Énergie de Chama : ${(needsSnapshot.energy * 100f).toInt()} %",
                        color = palette.muted,
                        fontSize = 10.sp
                    )
                }
                Text("Choisis une interaction", color = palette.muted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp, bottom = 8.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { pauseAutonomyForUser(); lampOn = !lampOn },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoomGold),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                    ) { Text(if (lampOn) "Éteindre lampe" else "Allumer lampe", fontSize = 10.sp) }

                    OutlinedButton(
                        onClick = { pauseAutonomyForUser(); eveningAmbience = !eveningAmbience },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoomGold),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                    ) { Text(if (eveningAmbience) "Ambiance jour" else "Ambiance soir", fontSize = 10.sp) }

                    OutlinedButton(
                        onClick = {
                            pauseAutonomyForUser()
                            sofaWelcoming = !sofaWelcoming
                            status = if (sofaWelcoming) "J'ai préparé les coussins du canapé pour un moment de détente. ❤️" else "Le salon retrouve son aménagement habituel."
                            bodyGesture = ChamaBodyGesture.LISTEN
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoomGold),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                    ) { Text(if (sofaWelcoming) "Ranger le canapé" else "Préparer le canapé", fontSize = 10.sp) }

                    if (flowerLocation == ChamaFlowerLocation.COFFEE_TABLE) {
                        OutlinedButton(
                            onClick = {
                                pauseAutonomyForUser()
                                if (flowerInteraction.returnToVase()) {
                                    flowerLocation = flowerInteraction.location
                                }
                                interactionStatus = "J'ai remis le bouquet dans son vase. 🌹"
                                bodyGesture = ChamaBodyGesture.LISTEN
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoomGold),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                        ) { Text("Remettre le bouquet", fontSize = 10.sp) }
                    }

                    OutlinedButton(
                        onClick = {
                            pauseAutonomyForUser()
                            bouquetHighlighted = !bouquetHighlighted
                            status = if (bouquetHighlighted) "J'ai mis mes roses préférées en valeur. 🌹" else "Les roses retrouvent leur teinte naturelle."
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoomGold),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                    ) { Text(if (bouquetHighlighted) "Rétablir les roses" else "Valoriser les roses", fontSize = 10.sp) }

                    OutlinedButton(
                        onClick = { pauseAutonomyForUser(); lightMode = !lightMode },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoomGold),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                    ) { Text(if (lightMode) "Activer le salon 3D" else "Mode léger", fontSize = 10.sp) }

                    OutlinedButton(
                        onClick = {
                            if (autonomyEnabled) {
                                pauseAutonomyForUser()
                                status = "Mon autonomie est arrêtée. Je t'écoute."
                            } else {
                                autonomyEnabled = true
                                status = "Je vais explorer mon salon à mon rythme."
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoomGold),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                    ) { Text(if (autonomyEnabled) "Arrêter autonomie" else "Autonomie libre", fontSize = 10.sp) }
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    FilledTonalButton(
                        onClick = { act("Coucou ! Je suis heureuse de te retrouver. ❤️", ChamaBodyGesture.WAVE) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = palette.secondary, contentColor = RoomInk),
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.WavingHand, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp)); Text("Bonjour", fontSize = 11.sp)
                    }
                    FilledTonalButton(
                        onClick = { act("Je t'écoute. Raconte-moi ce que tu as sur le cœur.", ChamaBodyGesture.LISTEN) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = palette.secondary, contentColor = RoomInk),
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp)); Text("Écouter", fontSize = 11.sp)
                    }
                    FilledTonalButton(
                        onClick = { act("Un petit moment de joie rien que pour nous !", ChamaBodyGesture.DANCE) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = palette.secondary, contentColor = RoomInk),
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp)); Text("Danser", fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { pauseAutonomyForUser(); onTalk() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = RoomGold)) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = RoomInk)
                        Spacer(Modifier.width(6.dp)); Text("Conversation", color = RoomInk, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = { pauseAutonomyForUser(); onGallery() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = RoomGold)) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(6.dp)); Text("Galerie")
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            Surface(
                modifier = Modifier.align(Alignment.CenterStart)
                    .padding(start = 10.dp)
                    .size(width = 86.dp, height = 120.dp),
                color = palette.surface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(18.dp)
            ) {
                ChamaPortrait(modifier = Modifier.fillMaxSize().padding(4.dp))
            }
        }
        Text(
            "Chama peut explorer son salon en autonomie locale, s'arrêter dès que tu interagis et économiser la batterie en mode léger. Son portrait original reste séparé, intact et non animé.",
            color = RoomRose, fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
    }
}