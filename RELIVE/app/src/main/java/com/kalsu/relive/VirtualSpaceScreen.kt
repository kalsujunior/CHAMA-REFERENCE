package com.kalsu.relive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalsu.relive.data.PreferenceStore
import com.kalsu.relive.avatar.ChamaViseme
import com.kalsu.relive.voice.ChamaSpeech

private val RoomGold = Color(0xFFD7B56D)
private val RoomInk = Color(0xFF090909)
private val RoomCream = Color(0xFFF5EBDD)

@Composable
private fun RoomEnvironment(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF40334A), Color(0xFF211D2B), Color(0xFF17151D)),
                endY = h * 0.82f
            ),
            size = Size(w, h)
        )
        // Warm window and its golden frame.
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Color(0x77E5C27B), Color(0x223D3450)),
                start = Offset(w * 0.07f, h * 0.12f),
                end = Offset(w * 0.37f, h * 0.43f)
            ),
            topLeft = Offset(w * 0.07f, h * 0.12f),
            size = Size(w * 0.30f, h * 0.31f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
        )
        drawLine(Color(0x99D9B979), Offset(w * 0.22f, h * 0.12f), Offset(w * 0.22f, h * 0.43f), 3f)
        drawLine(Color(0x99D9B979), Offset(w * 0.07f, h * 0.275f), Offset(w * 0.37f, h * 0.275f), 3f)
        // Bookcase on the right.
        drawRoundRect(Color(0xAA17131C), Offset(w * 0.78f, h * 0.20f), Size(w * 0.15f, h * 0.42f), androidx.compose.ui.geometry.CornerRadius(12f, 12f))
        for (i in 1..3) {
            val y = h * (0.20f + i * 0.105f)
            drawLine(Color(0x99C49D59), Offset(w * 0.80f, y), Offset(w * 0.91f, y), 3f)
        }
        // Perspective floor.
        val floor = Path().apply {
            moveTo(0f, h * 0.76f); lineTo(w, h * 0.76f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(floor, Brush.verticalGradient(listOf(Color(0xFF302735), Color(0xFF100F14)), h * 0.76f, h))
        drawLine(Color(0x77D7B56D), Offset(0f, h * 0.76f), Offset(w, h * 0.76f), 2f)
        // Sofa silhouette.
        drawRoundRect(Color(0xDD302638), Offset(w * 0.04f, h * 0.62f), Size(w * 0.34f, h * 0.15f), androidx.compose.ui.geometry.CornerRadius(26f, 26f))
        drawRoundRect(Color(0xFF49384A), Offset(w * 0.07f, h * 0.59f), Size(w * 0.28f, h * 0.12f), androidx.compose.ui.geometry.CornerRadius(22f, 22f))
        // Floor lamp with warm glow.
        drawLine(Color(0xFFD7B56D), Offset(w * 0.69f, h * 0.44f), Offset(w * 0.69f, h * 0.75f), 5f)
        drawRoundRect(Color(0xFFE0BD75), Offset(w * 0.64f, h * 0.40f), Size(w * 0.10f, h * 0.055f), androidx.compose.ui.geometry.CornerRadius(12f, 12f))
        drawCircle(Color(0x22E8C77F), radius = w * 0.18f, center = Offset(w * 0.69f, h * 0.49f))
        // Decorative indoor plant.
        drawRoundRect(Color(0xFF8A6847), Offset(w * 0.88f, h * 0.70f), Size(w * 0.07f, h * 0.09f), androidx.compose.ui.geometry.CornerRadius(8f, 8f))
        for (i in 0..4) {
            val x = w * (0.915f + (i - 2) * 0.018f)
            val y = h * (0.58f + kotlin.math.abs(i - 2) * 0.018f)
            drawLine(Color(0xFF78906B), Offset(w * 0.915f, h * 0.72f), Offset(x, y), 5f)
            drawCircle(Color(0xFF78906B), radius = 7f, center = Offset(x, y))
        }
    }
}

@Composable
fun VirtualSpaceScreen(modifier: Modifier = Modifier, onTalk: () -> Unit, onGallery: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember { PreferenceStore(context) }
    val speech = remember { ChamaSpeech(context) }
    var gesture by remember { mutableStateOf("idle") }
    var status by remember { mutableStateOf("Je suis heureuse de te retrouver ici.") }
    var speaking by remember { mutableStateOf(false) }
    var currentViseme by remember { mutableStateOf(ChamaViseme.REST) }

    DisposableEffect(speech) {
        onDispose { speech.release() }
    }

    val movement = rememberInfiniteTransition(label = "chama-idle")
    val breathing by movement.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(tween(2100), RepeatMode.Reverse),
        label = "chama-breathing"
    )
    val floating by movement.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
        label = "chama-floating"
    )
    val gestureTilt by animateFloatAsState(
        targetValue = when (gesture) {
            "wave" -> -3.5f
            "dance" -> 3.5f
            "listen" -> 2f
            else -> 0f
        },
        animationSpec = tween(350),
        label = "chama-gesture-tilt"
    )

    fun act(kind: String, line: String) {
        gesture = kind
        status = line
        speaking = true
        speech.speak(
            text = line,
            languageTag = preferences.languageTag,
            onDone = {
                speaking = false
                gesture = "idle"
                currentViseme = ChamaViseme.REST
            },
            onViseme = { viseme -> currentViseme = viseme }
        )
    }

    Column(
        modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF211B2B), Color(0xFF101014), RoomInk))
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("L'ESPACE DE CHAMA", color = RoomGold, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Text("Un lieu calme pour vous retrouver", color = Color.LightGray, fontSize = 11.sp)
            }
            Surface(color = Color(0xFF332A19), shape = CircleShape) {
                Text("KS", color = RoomGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp))
            }
        }

        Box(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF46354A), Color(0xFF201D2C), Color(0xFF111116))))
        ) {
            RoomEnvironment(modifier = Modifier.fillMaxSize())
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(16.dp))
                Text("Bienvenue dans mon univers", color = RoomCream, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Text("ESPÉRANCE RUCHAMA KEFORA", color = RoomGold, fontSize = 11.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))

                Box(
                    Modifier.weight(1f).fillMaxWidth().padding(horizontal = 18.dp)
                        .clip(RoundedCornerShape(26.dp)).background(Color(0xFF17151C)),
                    contentAlignment = Alignment.Center
                ) {
                    Chama3DAvatar(
                        gesture = gesture,
                        speaking = speaking,
                        viseme = currentViseme.name,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (speaking) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                            color = Color(0xDD332A19), shape = RoundedCornerShape(50)
                        ) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = RoomGold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(7.dp))
                                Text("Chama parle • bouche ${currentViseme.label}", color = RoomCream, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(status, color = RoomCream, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
                Text("Choisis une interaction", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp, bottom = 8.dp))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    FilledTonalButton(
                        onClick = { act("wave", "Coucou ! Je te fais un grand signe de la main. ❤️") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF332A19), contentColor = RoomGold),
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.WavingHand, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp)); Text("Saluer", fontSize = 11.sp)
                    }
                    FilledTonalButton(
                        onClick = { act("listen", "Je t'écoute. Raconte-moi ce que tu as sur le cœur.") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF332A19), contentColor = RoomGold),
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp)); Text("Écouter", fontSize = 11.sp)
                    }
                    FilledTonalButton(
                        onClick = { act("dance", "Un petit moment de joie rien que pour nous !") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF332A19), contentColor = RoomGold),
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp)); Text("Danser", fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onTalk, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = RoomGold)) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = RoomInk)
                        Spacer(Modifier.width(6.dp)); Text("Conversation", color = RoomInk, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onGallery, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = RoomGold)) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null)
                        Spacer(Modifier.width(6.dp)); Text("Galerie")
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
        }
        Text(
            "Avatar 3D procédural animé : expressions et mouvements évolutifs. Le modèle sera progressivement affiné pour se rapprocher davantage de la référence de Chama.",
            color = Color.LightGray, fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
    }
}
