package com.kalsu.relive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF090909)
private val Panel = Color(0xFF171717)
private val Gold = Color(0xFFD7B56D)
private val Cream = Color(0xFFF5EBDD)
private val Muted = Color(0xFFAAA29A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ReliveApp() }
    }
}

@Composable
private fun ReliveApp() {
    var page by remember { mutableStateOf("home") }
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Gold, background = Ink, surface = Panel,
        onPrimary = Ink, onBackground = Cream, onSurface = Cream
    )) {
        Scaffold(
            containerColor = Ink,
            bottomBar = {
                NavigationBar(containerColor = Panel) {
                    val tabs = listOf(
                        Triple("home", "Accueil", Icons.Default.Home),
                        Triple("chat", "Parler", Icons.Default.Chat),
                        Triple("gallery", "Photos", Icons.Default.PhotoLibrary),
                        Triple("profile", "Profil", Icons.Default.Person)
                    )
                    tabs.forEach { (key, label, icon) ->
                        NavigationBarItem(
                            selected = page == key,
                            onClick = { page = key },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Gold, selectedTextColor = Gold,
                                indicatorColor = Color(0xFF332A19),
                                unselectedIconColor = Muted, unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            when (page) {
                "chat" -> ChatScreen(Modifier.padding(padding))
                "gallery" -> GalleryScreen(Modifier.padding(padding))
                "profile" -> ProfileScreen(Modifier.padding(padding))
                else -> HomeScreen(Modifier.padding(padding), { page = "chat" }, { page = "gallery" })
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("RELIVE", color = Gold, fontSize = 23.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Text("KAL-SU • COMPAGNE VIRTUELLE", color = Muted, fontSize = 9.sp, letterSpacing = 1.2.sp)
        }
        Box(Modifier.size(42.dp).background(Color(0xFF302719), CircleShape), contentAlignment = Alignment.Center) {
            Text("KS", color = Gold, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier, onTalk: () -> Unit, onGallery: () -> Unit) {
    Column(modifier.fillMaxSize().background(Ink)) {
        BrandHeader()
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Heureuse de te retrouver", color = Cream, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text("Je suis Chama. Je suis là pour toi.", color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))
            // Emplacement réservé à la photo originale principale, à intégrer sans retouche.
            Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp).background(Panel, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(112.dp).background(Color(0xFF302719), CircleShape), contentAlignment = Alignment.Center) {
                        Text("C", color = Gold, fontSize = 58.sp, fontWeight = FontWeight.Light)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("ESPÉRANCE RUCHAMA KEFORA", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Chama", color = Cream, fontSize = 25.sp, fontWeight = FontWeight.Medium)
                    Text("Photo principale à intégrer", color = Muted, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(18.dp))
            Button(onClick = onTalk, modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Forum, contentDescription = null)
                Spacer(Modifier.width(9.dp))
                Text("Parler avec Chama", color = Ink, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onGallery) { Text("Voir sa galerie", color = Gold) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChatScreen(modifier: Modifier) {
    var draft by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf("Chama : Coucou ! Comment s'est passée ta journée ?") }
    Column(modifier.fillMaxSize().background(Ink)) {
        BrandHeader()
        Text("Conversation", color = Cream, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages) { message ->
                Surface(color = Panel, shape = RoundedCornerShape(16.dp)) {
                    Text(message, color = Cream, modifier = Modifier.padding(14.dp))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = draft, onValueChange = { draft = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("Écris à Chama…") }, singleLine = true)
            IconButton(onClick = {
                if (draft.isNotBlank()) {
                    messages.add("Toi : $draft")
                    messages.add("Chama : Je t'écoute. Raconte-moi davantage. (Réponse de démonstration)")
                    draft = ""
                }
            }) { Icon(Icons.Default.Send, contentDescription = "Envoyer", tint = Gold) }
        }
    }
}

@Composable
private fun GalleryScreen(modifier: Modifier) {
    Column(modifier.fillMaxSize().background(Ink)) {
        BrandHeader()
        Text("Galerie de Chama", color = Cream, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
        Text("Les trois photos originales seront placées ici sans retouche.", color = Muted, modifier = Modifier.padding(20.dp))
        listOf("Photo principale — pull blanc avec cœurs rouges", "Photo 2 — maillot bleu", "Photo 3 — robe rose").forEachIndexed { index, title ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 7.dp).background(Panel, RoundedCornerShape(14.dp)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).background(Color(0xFF302719), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Text("${index + 1}", color = Gold, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Text(title, color = Cream, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ProfileScreen(modifier: Modifier) {
    Column(modifier.fillMaxSize().background(Ink)) {
        BrandHeader()
        Text("Profil et préférences", color = Cream, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp))
        listOf("Langue : Français", "Créole haïtien : prévu", "Mémoire : stockage local à configurer",
            "Notifications : à configurer", "Compte : mode démonstration").forEach {
            Text(it, color = Cream, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                .background(Panel, RoundedCornerShape(12.dp)).padding(16.dp))
        }
    }
}
