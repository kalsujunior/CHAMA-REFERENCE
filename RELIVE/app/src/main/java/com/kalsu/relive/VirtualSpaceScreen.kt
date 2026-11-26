package com.kalsu.relive

import androidx.compose.foundation.background
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
    var status by remember { mutableStateOf("Je suis heureuse de te retrouver ici.") }
    var speaking by remember { mutableStateOf(false) }
    var bodyGesture by remember { mutableStateOf(ChamaBodyGesture.IDLE) }
    var lampOn by remember { mutableStateOf(true) }

    DisposableEffect(speech) {
        onDispose { speech.release() }
    }

    fun act(line: String, gesture: ChamaBodyGesture) {
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
            ChamaRoom3D(modifier = Modifier.fillMaxSize(), palette = palette, lampOn = lampOn)
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(16.dp))
                Text("Bienvenue dans mon univers", color = RoomCream, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Text("ESPÉRANCE RUCHAMA KEFORA", color = RoomGold, fontSize = 11.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))

                Box(
                    Modifier.weight(1f).fillMaxWidth().padding(horizontal = 18.dp)
                        .clip(RoundedCornerShape(26.dp)).background(palette.surface.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1.15f).fillMaxWidth()) {
                            ChamaBody3D(modifier = Modifier.fillMaxSize(), gesture = bodyGesture)
                        }
                        Divider(color = palette.secondary.copy(alpha = 0.55f))
                        Box(Modifier.weight(0.85f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            ChamaPortrait(modifier = Modifier.fillMaxSize())
                        }
                    }
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
                OutlinedButton(
                    onClick = { lampOn = !lampOn },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoomGold),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(if (lampOn) "Éteindre la lampe" else "Allumer la lampe", fontSize = 11.sp)
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
                    Button(onClick = onTalk, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = RoomGold)) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = RoomInk)
                        Spacer(Modifier.width(6.dp)); Text("Conversation", color = RoomInk, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onGallery, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = RoomGold)) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(6.dp)); Text("Galerie")
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
        }
        Text(
            "Salon 3D manipulable, éclairage interactif et corps animé. Le portrait original reste indépendant, intact et non animé.",
            color = RoomRose, fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
    }
}