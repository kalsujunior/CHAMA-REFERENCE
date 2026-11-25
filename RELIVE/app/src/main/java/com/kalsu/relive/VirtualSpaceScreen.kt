package com.kalsu.relive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import com.kalsu.relive.data.PhotoStore

private val RoomGold = Color(0xFFD7B56D)
private val RoomInk = Color(0xFF090909)
private val RoomCream = Color(0xFFF5EBDD)

@Composable
fun VirtualSpaceScreen(modifier: Modifier = Modifier, onTalk: () -> Unit, onGallery: () -> Unit) {
    val context = LocalContext.current
    val portrait = remember { PhotoStore(context).get(0) }
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
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF46354A), Color(0xFF201D2C), Color(0xFF111116)))
                )
        ) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(20.dp))
                Text("Bienvenue dans mon univers", color = RoomCream, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Text("ESPÉRANCE RUCHAMA KEFORA", color = RoomGold, fontSize = 11.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(18.dp))
                Box(
                    Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF17151C)),
                    contentAlignment = Alignment.Center
                ) {
                    if (portrait != null) {
                        AsyncImage(
                            model = portrait,
                            contentDescription = "Avatar original de Chama, sans retouche",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Text("♡", color = RoomGold, fontSize = 64.sp)
                            Text("Ajoute la photo principale de Chama", color = RoomCream, fontWeight = FontWeight.SemiBold)
                            Text("Photos → Portrait principal", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Je suis heureuse de te retrouver ici.", color = RoomCream, fontSize = 14.sp)
                Text("Notre petit espace, à notre rythme.", color = Color.LightGray, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onTalk, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = RoomGold)) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = RoomInk)
                        Spacer(Modifier.width(6.dp))
                        Text("Parler", color = RoomInk, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onGallery, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = RoomGold)) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Galerie")
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
        Text(
            "Portrait affiché depuis ta photo originale. RELIVE ne modifie pas son visage.",
            color = Color.LightGray, fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth().padding(14.dp)
        )
    }
}
