package com.kalsu.relive

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalsu.relive.data.PreferenceStore
import com.kalsu.relive.voice.ChamaSpeech

private val RoomGold: Color @Composable get() = LocalRelivePalette.current.primary
private val RoomInk: Color @Composable get() = LocalRelivePalette.current.background
private val RoomCream: Color @Composable get() = LocalRelivePalette.current.text
private val RoomRose: Color @Composable get() = LocalRelivePalette.current.secondary

@Composable
private fun RoomEnvironment(modifier: Modifier = Modifier, palette: RelivePalette) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.verticalGradient(
                listOf(palette.primary.copy(alpha = 0.42f), palette.background.copy(alpha = 0.88f), palette.background),
                endY = h * 0.82f
            ),
            size = Size(w, h)
        )
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(palette.surface.copy(alpha = 0.60f), palette.secondary.copy(alpha = 0.28f)),
                start = Offset(w * 0.07f, h * 0.12f),
                end = Offset(w * 0.37f, h * 0.43f)
            ),
            topLeft = Offset(w * 0.07f, h * 0.12f),
            size = Size(w * 0.30f, h * 0.31f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
        )
        drawLine(palette.primary.copy(alpha = 0.60f), Offset(w * 0.22f, h * 0.12f), Offset(w * 0.22f, h * 0.43f), 3f)
        drawLine(palette.primary.copy(alpha = 0.60f), Offset(w * 0.07f, h * 0.275f), Offset(w * 0.37f, h * 0.275f), 3f)
        drawRoundRect(palette.surface.copy(alpha = 0.72f), Offset(w * 0.78f, h * 0.20f), Size(w * 0.15f, h * 0.42f), androidx.compose.ui.geometry.CornerRadius(12f, 12f))
        for (i in 1..3) {
            val y = h * (0.20f + i * 0.105f)
            drawLine(palette.primary.copy(alpha = 0.65f), Offset(w * 0.80f, y), Offset(w * 0.91f, y), 3f)
        }
        val floor = Path().apply {
            moveTo(0f, h * 0.76f); lineTo(w, h * 0.76f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(floor, Brush.verticalGradient(listOf(palette.secondary.copy(alpha = 0.70f), palette.background), h * 0.76f, h))
        drawLine(palette.primary.copy(alpha = 0.48f), Offset(0f, h * 0.76f), Offset(w, h * 0.76f), 2f)
        drawRoundRect(palette.secondary, Offset(w * 0.04f, h * 0.62f), Size(w * 0.34f, h * 0.15f), androidx.compose.ui.geometry.CornerRadius(26f, 26f))
        drawRoundRect(palette.surface, Offset(w * 0.07f, h * 0.59f), Size(w * 0.28f, h * 0.12f), androidx.compose.ui.geometry.CornerRadius(22f, 22f))
        drawLine(palette.primary, Offset(w * 0.69f, h * 0.44f), Offset(w * 0.69f, h * 0.75f), 5f)
        drawRoundRect(palette.primary, Offset(w * 0.64f, h * 0.40f), Size(w * 0.10f, h * 0.055f), androidx.compose.ui.geometry.CornerRadius(12f, 12f))
        drawCircle(palette.primary.copy(alpha = 0.14f), radius = w * 0.18f, center = Offset(w * 0.69f, h * 0.49f))
        drawRoundRect(palette.secondary.copy(alpha = 0.85f), Offset(w * 0.88f, h * 0.70f), Size(w * 0.07f, h * 0.09f), androidx.compose.ui.geometry.CornerRadius(8f, 8f))
        for (i in 0..4) {
            val x = w * (0.915f + (i - 2) * 0.018f)
            val y = h * (0.58f + kotlin.math.abs(i - 2) * 0.018f)
            drawLine(palette.secondary.copy(alpha = 0.85f), Offset(w * 0.915f, h * 0.72f), Offset(x, y), 5f)
            drawCircle(palette.secondary.copy(alpha = 0.85f), radius = 7f, center = Offset(x, y))
        }
    }
}

@Composable
fun VirtualSpaceScreen(modifier: Modifier = Modifier, onTalk: () -> Unit, onGallery: () -> Unit) {
    val context = LocalContext.current
    val palette = LocalRelivePalette.current
    val preferences = remember { PreferenceStore(context) }
    val speech = remember { ChamaSpeech(context) }
    var status by remember { mutableStateOf("Je suis heureuse de te retrouver ici.") }
    var speaking by remember { mutableStateOf(false) }

    DisposableEffect(speech) {
        onDispose { speech.release() }
    }

    fun act(line: String) {
        status = line
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
            ChamaRoom3D(modifier = Modifier.fillMaxSize(), palette = palette)
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(16.dp))
                Text("Bienvenue dans mon univers", color = RoomCream, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Text("ESPÉRANCE RUCHAMA KEFORA", color = RoomGold, fontSize = 11.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))

                Box(
                    Modifier.weight(1f).fillMaxWidth().padding(horizontal = 18.dp)
                        .clip(RoundedCornerShape(26.dp)).background(palette.surface.copy(alpha = 0.90f)),
                    contentAlignment = Alignment.Center
                ) {
                    ChamaPortrait(modifier = Modifier.fillMaxSize())
                    if (speaking) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                            color = palette.secondary.copy(alpha = 0.88f), shape = RoundedCornerShape(50)
                        ) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = RoomGold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(7.dp))
                                Text("Chama parle", color = RoomInk, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(status, color = RoomCream, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
                Text("Choisis une interaction", color = palette.muted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp, bottom = 8.dp))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    FilledTonalButton(
                        onClick = { act("Coucou ! Je suis heureuse de te retrouver. ❤️") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = palette.secondary, contentColor = RoomInk),
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.WavingHand, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp)); Text("Bonjour", fontSize = 11.sp)
                    }
                    FilledTonalButton(
                        onClick = { act("Je t'écoute. Raconte-moi ce que tu as sur le cœur.") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = palette.secondary, contentColor = RoomInk),
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp)); Text("Écouter", fontSize = 11.sp)
                    }
                    FilledTonalButton(
                        onClick = { act("Un petit moment de joie rien que pour nous !") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = palette.secondary, contentColor = RoomInk),
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp)); Text("Partager", fontSize = 11.sp)
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
            "Portrait original protégé — visage inchangé. Première pièce 3D procédurale ; corps indépendant encore en développement.",
            color = RoomRose, fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
    }
}