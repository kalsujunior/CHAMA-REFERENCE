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
import coil.compose.AsyncImage
import com.kalsu.relive.data.PhotoStore
import com.kalsu.relive.data.PreferenceStore
import com.kalsu.relive.voice.ChamaSpeech
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val RoomGold = Color(0xFFD7B56D)
private val RoomInk = Color(0xFF090909)
private val RoomCream = Color(0xFFF5EBDD)

@Composable
fun VirtualSpaceScreen(modifier: Modifier = Modifier, onTalk: () -> Unit, onGallery: () -> Unit) {
    val context = LocalContext.current
    val portrait = remember { PhotoStore(context).get(0) }
    val preferences = remember { PreferenceStore(context) }
    val speech = remember { ChamaSpeech(context) }
    val scope = rememberCoroutineScope()
    var gesture by remember { mutableStateOf("idle") }
    var status by remember { mutableStateOf("Je suis heureuse de te retrouver ici.") }
    var speaking by remember { mutableStateOf(false) }

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
        speech.speak(line, preferences.languageTag)
        scope.launch {
            delay(2200)
            speaking = false
            gesture = "idle"
        }
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
                    if (portrait != null) {
                        AsyncImage(
                            model = portrait,
                            contentDescription = "Photo originale de Chama, conservée sans retouche",
                            modifier = Modifier.fillMaxSize().graphicsLayer {
                                scaleX = breathing
                                scaleY = breathing
                                translationY = floating
                                rotationZ = gestureTilt
                            },
                            contentScale = ContentScale.Fit
                        )
                        if (gesture == "wave" || gesture == "dance" || gesture == "listen") {
                            Surface(
                                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                                color = Color(0xDD211B2B),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = when (gesture) { "wave" -> "👋"; "dance" -> "♪"; else -> "♡" },
                                    fontSize = 30.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Text("♡", color = RoomGold, fontSize = 64.sp)
                            Text("Ajoute la photo principale de Chama", color = RoomCream, fontWeight = FontWeight.SemiBold)
                            Text("Photos → Portrait principal", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                    if (speaking) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                            color = Color(0xDD332A19), shape = RoundedCornerShape(50)
                        ) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = RoomGold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(7.dp))
                                Text("Chama parle…", color = RoomCream, fontSize = 12.sp)
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
            "La photo reste originale : les mouvements sont des animations de présentation, pas une animation réelle du visage ou du corps.",
            color = Color.LightGray, fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
    }
}
