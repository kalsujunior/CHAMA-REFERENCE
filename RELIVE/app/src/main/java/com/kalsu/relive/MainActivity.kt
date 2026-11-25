package com.kalsu.relive

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalsu.relive.core.NetworkStatus
import com.kalsu.relive.data.ChatMessage
import com.kalsu.relive.data.ChatStore
import com.kalsu.relive.voice.ChamaSpeech
import com.kalsu.relive.notifications.ReminderScheduler

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
                        Triple("profile", "Profil", Icons.Default.Person),
                        Triple("reminders", "Rappels", Icons.Default.Notifications)
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
                "reminders" -> RemindersScreen(Modifier.padding(padding))
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
            Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp).background(Panel, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(112.dp).background(Color(0xFF302719), CircleShape), contentAlignment = Alignment.Center) {
                        Text("C", color = Gold, fontSize = 58.sp, fontWeight = FontWeight.Light)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("ESPÉRANCE RUCHAMA KEFORA", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Chama", color = Cream, fontSize = 25.sp, fontWeight = FontWeight.Medium)
                    Text("Portrait original à intégrer", color = Muted, fontSize = 12.sp)
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

private fun localReply(message: String): String {
    val value = message.lowercase()
    return when {
        listOf("bonjour", "salut", "coucou", "bonjou").any { value.contains(it) } ->
            "Coucou ! Je suis contente de te lire. Comment te sens-tu aujourd'hui ?"
        listOf("triste", "malheureux", "difficile", "fatigué", "fatigue").any { value.contains(it) } ->
            "Je suis là pour t'écouter. Tu peux me raconter ce qui te pèse, à ton rythme."
        listOf("merci", "mesi").any { value.contains(it) } ->
            "Avec plaisir. Je suis heureuse de pouvoir échanger avec toi."
        listOf("créole", "kreyòl", "creole").any { value.contains(it) } ->
            "Mwen la avèk ou. M ap aprann adapte konvèsasyon nou an ak lang ou prefere."
        else -> "J'ai bien reçu ton message. Le mode conversation local est actif ; la connexion au service d'intelligence artificielle distant reste à configurer."
    }
}

@Composable
private fun ChatScreen(modifier: Modifier) {
    val context = LocalContext.current
    val store = remember { ChatStore(context) }
    val speech = remember { ChamaSpeech(context) }
    var draft by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(store.load()) }
    val online = remember { mutableStateOf(NetworkStatus.isOnline(context)) }

    DisposableEffect(speech) {
        onDispose { speech.release() }
    }

    fun addMessage(author: String, text: String) {
        val saved = store.append(author, text)
        messages = (messages + saved).takeLast(1000)
    }

    Column(modifier.fillMaxSize().background(Ink)) {
        BrandHeader()
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Conversation", color = Cream, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(if (online.value) "Réseau disponible" else "Mode hors ligne",
                color = if (online.value) Gold else Muted, fontSize = 11.sp)
            IconButton(onClick = {
                store.clear()
                messages = emptyList()
            }) { Icon(Icons.Default.DeleteOutline, contentDescription = "Effacer l'historique", tint = Muted) }
        }
        if (messages.isEmpty()) {
            Text("Votre conversation commencera ici. Les messages seront conservés sur cet appareil.",
                color = Muted, modifier = Modifier.padding(20.dp))
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages, key = { it.id }) { message ->
                val fromChama = message.author == "Chama"
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (fromChama) Arrangement.Start else Arrangement.End) {
                    Surface(color = if (fromChama) Panel else Color(0xFF302719), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.widthIn(max = 300.dp).padding(12.dp)) {
                            Text(message.author, color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(message.text, color = Cream, modifier = Modifier.padding(top = 4.dp))
                            if (fromChama) {
                                TextButton(onClick = { speech.speak(message.text) }, contentPadding = PaddingValues(0.dp)) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("Écouter", color = Gold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = draft, onValueChange = { draft = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("Écris à Chama…") }, singleLine = false, maxLines = 4)
            IconButton(onClick = {
                val text = draft.trim()
                if (text.isNotEmpty()) {
                    addMessage("Toi", text)
                    addMessage("Chama", localReply(text))
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
        Text("Les trois fichiers photo originaux doivent être ajoutés aux ressources de l'application. Ils ne sont pas encore présents dans le dépôt.",
            color = Muted, modifier = Modifier.padding(20.dp))
        listOf("Photo principale — pull blanc avec cœurs rouges", "Photo 2 — galerie", "Photo 3 — galerie").forEachIndexed { index, title ->
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
        listOf("Nom : ESPÉRANCE RUCHAMA KEFORA", "Surnom : Chama", "Langues prévues : français et créole haïtien",
            "Conversation : sauvegardée localement", "Voix : synthèse vocale Android", "IA distante : backend à connecter").forEach {
            Text(it, color = Cream, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                .background(Panel, RoundedCornerShape(12.dp)).padding(16.dp))
        }
    }
}


@Composable
private fun RemindersScreen(modifier: Modifier) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("Mon rappel RELIVE") }
    var message by remember { mutableStateOf("Prends un moment pour toi.") }
    var minutes by remember { mutableStateOf("30") }
    var status by remember { mutableStateOf("Programme un rappel local sur cet appareil.") }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        status = if (granted) "Autorisation accordée." else "Notifications refusées : le rappel ne pourra pas afficher d'alerte."
    }
    Column(modifier.fillMaxSize().background(Ink)) {
        BrandHeader()
        Text("Rappels personnels", color = Cream, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
        Text("Les rappels sont planifiés localement et fonctionnent sans connexion Internet.", color = Muted, modifier = Modifier.padding(20.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titre du rappel") }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), minLines = 2)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = minutes, onValueChange = { minutes = it.filter(Char::isDigit).take(5) }, label = { Text("Dans combien de minutes ?") }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), singleLine = true)
        Spacer(Modifier.height(18.dp))
        Button(onClick = {
            val delay = minutes.toLongOrNull()
            if (title.isBlank() || message.isBlank() || delay == null || delay <= 0L) {
                status = "Vérifie les champs et indique un délai supérieur à zéro."
            } else {
                if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                try {
                    ReminderScheduler.schedule(context, title.trim(), message.trim(), delay)
                    status = "Rappel programmé dans " + delay + " minute(s)."
                } catch (error: Exception) {
                    status = "Erreur de programmation : " + (error.localizedMessage ?: "inconnue")
                }
            }
        }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
            Icon(Icons.Default.Alarm, contentDescription = null, tint = Ink)
            Spacer(Modifier.width(8.dp))
            Text("Programmer le rappel", color = Ink, fontWeight = FontWeight.Bold)
        }
        OutlinedButton(onClick = { ReminderScheduler.cancelAll(context); status = "Rappels en attente annulés." }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text("Annuler les rappels en attente", color = Gold)
        }
        Text(status, color = Gold, modifier = Modifier.padding(20.dp))
    }
}
