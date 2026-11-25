package com.kalsu.relive

import android.os.Bundle
import android.content.Intent
import android.speech.RecognizerIntent
import android.net.Uri
import android.provider.CalendarContract
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalsu.relive.core.NetworkStatus
import com.kalsu.relive.ai.AiGateway
import com.kalsu.relive.data.ChatMessage
import com.kalsu.relive.data.ChatStore
import com.kalsu.relive.data.PhotoStore
import com.kalsu.relive.data.PreferenceStore
import com.kalsu.relive.data.MemoryStore
import com.kalsu.relive.voice.ChamaSpeech
import com.kalsu.relive.notifications.ReminderScheduler
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val mainPhoto = remember { PhotoStore(context).get(0) }
    val displayName = remember { PreferenceStore(context).displayName.ifBlank { "Chama" } }
    Column(modifier.fillMaxSize().background(Ink)) {
        BrandHeader()
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Heureuse de te retrouver", color = Cream, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text("Je suis $displayName. Je suis là pour toi.", color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))
            Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp).background(Panel, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center) {
                if (mainPhoto != null) {
                    AsyncImage(model = mainPhoto, contentDescription = "Portrait original de Chama",
                        modifier = Modifier.fillMaxSize())
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(112.dp).background(Color(0xFF302719), CircleShape), contentAlignment = Alignment.Center) {
                            Text("C", color = Gold, fontSize = 58.sp, fontWeight = FontWeight.Light)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("ESPÉRANCE RUCHAMA KEFORA", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Chama", color = Cream, fontSize = 25.sp, fontWeight = FontWeight.Medium)
                        Text("Ajoute son portrait dans Photos", color = Muted, fontSize = 12.sp)
                    }
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

private fun localReply(message: String, memories: List<String>, languageTag: String): String {
    val normalized = message.lowercase()
    val creole = languageTag == "ht-HT"
    if (listOf("que sais-tu de moi", "que sais tu de moi", "que te rappelles-tu", "que te rappelles tu", "qu'est-ce que tu sais", "sa ou sonje", "kisa ou sonje").any { normalized.contains(it) }) {
        return if (memories.isEmpty()) {
            if (creole) "Mwen poko gen okenn souvni ou te anrejistre. Ou ka ajoute yo nan Profil, nan seksyon Souvenirs à conserver."
            else "Je n'ai pas encore de souvenir enregistré. Tu peux m'en confier dans Profil, rubrique Souvenirs à conserver."
        } else {
            val heading = if (creole) "Men souvni ou te konfye m yo :" else "Voici les souvenirs que tu m'as confiés :"
            heading + "\n" + memories.takeLast(20).mapIndexed { index, item -> (index + 1).toString() + ". " + item }.joinToString("\n")
        }
    }
    if (listOf("souviens-toi", "souviens toi", "sonje sa", "sonje sa pou mwen").any { normalized.contains(it) }) {
        return if (creole) "Mwen ka konsève detay sa a. Ajoute li nan Profil, seksyon « Souvenirs à conserver », pou l rete anrejistre sou telefòn sa a."
        else "Je peux conserver ce détail. Ajoute-le dans Profil, rubrique « Souvenirs à conserver », pour qu'il reste enregistré sur cet appareil."
    }
    val value = message.lowercase()
    return when {
        listOf("bonjour", "salut", "coucou", "bonjou").any { value.contains(it) } ->
            if (creole) "Bonjou ! Mwen kontan ou ekri m. Kijan ou santi w jodi a ?"
            else "Coucou ! Je suis contente de te lire. Comment te sens-tu aujourd'hui ?"
        listOf("triste", "malheureux", "difficile", "fatigué", "fatigue", "tris").any { value.contains(it) } ->
            if (creole) "Mwen la pou koute w. Ou ka rakonte m sa k ap boulvèse w, san prese."
            else "Je suis là pour t'écouter. Tu peux me raconter ce qui te pèse, à ton rythme."
        listOf("merci", "mesi").any { value.contains(it) } ->
            if (creole) "Se ak tout plezi mwen. Mwen kontan pale avè w."
            else "Avec plaisir. Je suis heureuse de pouvoir échanger avec toi."
        listOf("créole", "kreyòl", "creole").any { value.contains(it) } ->
            "Mwen la avèk ou. M ap aprann adapte konvèsasyon nou an ak lang ou prefere."
        else -> if (creole) "Mwen resevwa mesaj ou a. Mòd konvèsasyon lokal la aktif; sèvis entèlijans atifisyèl la poko konekte."
            else "J'ai bien reçu ton message. Le mode conversation local est actif ; la connexion au service d'intelligence artificielle distant reste à configurer."
    }
}

@Composable
private fun ChatScreen(modifier: Modifier) {
    val context = LocalContext.current
    val store = remember { ChatStore(context) }
    val preferences = remember { PreferenceStore(context) }
    val memoryStore = remember { MemoryStore(context) }
    val scope = rememberCoroutineScope()
    val speech = remember { ChamaSpeech(context) }
    var sending by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(store.load()) }
    val online = remember { mutableStateOf(NetworkStatus.isOnline(context)) }
    val speechInput = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val recognized = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!recognized.isNullOrBlank()) draft = recognized
    }

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
                            if (fromChama && preferences.voiceEnabled) {
                                TextButton(onClick = { speech.speak(message.text, preferences.languageTag) }, contentPadding = PaddingValues(0.dp)) {
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
            IconButton(onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, preferences.languageTag)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Parle à Chama")
                }
                try { speechInput.launch(intent) } catch (_: Exception) { }
            }) { Icon(Icons.Default.Mic, contentDescription = "Dicter un message", tint = Gold) }
            OutlinedTextField(value = draft, onValueChange = { draft = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("Écris à Chama…") }, singleLine = false, maxLines = 4)
            IconButton(enabled = !sending, onClick = {
                val text = draft.trim()
                if (text.isNotEmpty() && !sending) {
                    addMessage("Toi", text)
                    draft = ""
                    sending = true
                    scope.launch {
                        val memories = memoryStore.load().map { it.text }
                        online.value = NetworkStatus.isOnline(context)
                        val history = store.load().takeLast(20).map { it.author to it.text }
                        val answer = if (online.value && preferences.remoteAiEnabled) AiGateway.reply(text, preferences.languageTag, memories, history) else null
                        addMessage("Chama", answer ?: localReply(text, memories, preferences.languageTag))
                        sending = false
                    }
                }
            }) {
                if (sending) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Gold, strokeWidth = 2.dp)
                else Icon(Icons.Default.Send, contentDescription = "Envoyer", tint = Gold)
            }
        }
    }
}

@Composable
private fun GalleryScreen(modifier: Modifier) {
    val context = LocalContext.current
    val store = remember { PhotoStore(context) }
    val slots = remember { mutableStateListOf<Uri?>(store.get(0), store.get(1), store.get(2)) }
    var pendingSlot by remember { mutableIntStateOf(0) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: SecurityException) { }
            store.set(pendingSlot, uri)
            slots[pendingSlot] = uri
        }
    }
    val labels = listOf("Portrait principal — pull blanc avec cœurs rouges", "Photo de galerie 2", "Photo de galerie 3")
    Column(modifier.fillMaxSize().background(Ink)) {
        BrandHeader()
        Text("Galerie de Chama", color = Cream, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
        Text("Sélectionne les photos originales depuis ton téléphone. RELIVE les conserve sans retoucher leur apparence.", color = Muted, modifier = Modifier.padding(20.dp))
        slots.forEachIndexed { index, uri ->
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 7.dp).background(Panel, RoundedCornerShape(14.dp)).padding(12.dp)) {
                if (uri != null) {
                    AsyncImage(model = uri, contentDescription = labels[index], modifier = Modifier.fillMaxWidth().height(190.dp))
                } else {
                    Box(Modifier.fillMaxWidth().height(90.dp).background(Color(0xFF242424), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Gold, modifier = Modifier.size(34.dp))
                    }
                }
                Text(labels[index], color = Cream, modifier = Modifier.padding(top = 10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { pendingSlot = index; picker.launch(arrayOf("image/*")) }) { Text(if (uri == null) "Choisir une photo" else "Remplacer", color = Gold) }
                    if (uri != null) TextButton(onClick = { store.clear(index); slots[index] = null }) { Text("Retirer", color = Muted) }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(modifier: Modifier) {
    val context = LocalContext.current
    val preferences = remember { PreferenceStore(context) }
    val memoryStore = remember { MemoryStore(context) }
    var memories by remember { mutableStateOf(memoryStore.load()) }
    var memoryDraft by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf(preferences.displayName) }
    var language by remember { mutableStateOf(preferences.languageTag) }
    var voiceEnabled by remember { mutableStateOf(preferences.voiceEnabled) }
    var remoteAiEnabled by remember { mutableStateOf(preferences.remoteAiEnabled) }
    var savedMessage by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState())) {
        BrandHeader()
        Text("Profil et préférences", color = Cream, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp))
        OutlinedTextField(value = displayName, onValueChange = { displayName = it },
            label = { Text("Nom affiché de Chama") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), singleLine = true)
        Text("Langue de la voix", color = Gold, modifier = Modifier.padding(start = 20.dp, top = 18.dp))
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            FilterChip(selected = language == "fr-FR", onClick = { language = "fr-FR" }, label = { Text("Français") })
            Spacer(Modifier.width(10.dp))
            FilterChip(selected = language == "ht-HT", onClick = { language = "ht-HT" }, label = { Text("Kreyòl ayisyen") })
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Activer la lecture vocale", color = Cream, modifier = Modifier.weight(1f))
            Switch(checked = voiceEnabled, onCheckedChange = { voiceEnabled = it })
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Autoriser l'IA distante", color = Cream)
                Text("Envoie ton message, les 20 derniers échanges et jusqu’à 20 souvenirs au serveur HTTPS configuré. Active uniquement si tu acceptes ce transfert.", color = Muted, fontSize = 11.sp)
            }
            Switch(checked = remoteAiEnabled, onCheckedChange = { remoteAiEnabled = it })
        }
        Button(onClick = {
            if (displayName.isBlank()) {
                savedMessage = "Le nom ne peut pas être vide."
            } else {
                preferences.displayName = displayName
                preferences.languageTag = language
                preferences.voiceEnabled = voiceEnabled
                preferences.remoteAiEnabled = remoteAiEnabled
                savedMessage = "Préférences enregistrées sur cet appareil."
            }
        }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
            Text("Enregistrer les préférences", color = Ink, fontWeight = FontWeight.Bold)
        }
        Text(savedMessage, color = Gold, modifier = Modifier.padding(20.dp))
        Text("Souvenirs à conserver (${memories.size}/500)", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        OutlinedTextField(value = memoryDraft, onValueChange = { memoryDraft = it },
            label = { Text("Un détail important à mémoriser") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), minLines = 2)
        Button(onClick = {
            if (memoryDraft.isNotBlank()) {
                memoryStore.add(memoryDraft)
                memories = memoryStore.load()
                memoryDraft = ""
            }
        }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
            Text("Enregistrer ce souvenir", color = Ink)
        }
        if (memories.isEmpty()) {
            Text("Aucun souvenir enregistré.", color = Muted, modifier = Modifier.padding(20.dp))
        } else {
            memories.forEach { memory ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 5.dp)
                    .background(Panel, RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(memory.text, color = Cream, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        memoryStore.delete(memory.id)
                        memories = memoryStore.load()
                    }) { Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer ce souvenir", tint = Muted) }
                }
            }
        }
        listOf("Identité : ESPÉRANCE RUCHAMA KEFORA", "Conversation : sauvegardée localement",
            "Photos : stockage local des originaux", "IA distante : backend sécurisé à connecter").forEach {
            Text(it, color = Cream, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)
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
        OutlinedButton(onClick = {
            val begin = System.currentTimeMillis() + 60L * 60L * 1000L
            val end = begin + 30L * 60L * 1000L
            val calendarIntent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title.ifBlank { "Rendez-vous RELIVE" })
                .putExtra(CalendarContract.Events.DESCRIPTION, message)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
            try {
                context.startActivity(calendarIntent)
                status = "Ouvre ton calendrier pour confirmer et enregistrer l'événement."
            } catch (_: Exception) {
                status = "Aucune application de calendrier compatible n'a été trouvée."
            }
        }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Gold)
            Spacer(Modifier.width(8.dp))
            Text("Ajouter au calendrier", color = Gold)
        }
        OutlinedButton(onClick = { ReminderScheduler.cancelAll(context); status = "Rappels en attente annulés." }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text("Annuler les rappels en attente", color = Gold)
        }
        Text(status, color = Gold, modifier = Modifier.padding(20.dp))
    }
}
