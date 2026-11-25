# RELIVE — KAL-SU

Application Android native Kotlin + Jetpack Compose centrée sur ESPÉRANCE RUCHAMA KEFORA (Chama).

## Fonctions présentes dans la branche relive-app
- Navigation : Accueil, Conversation, Photos, Profil et Rappels.
- Interface sombre noir et or.
- Historique local des conversations (SharedPreferences, limite de 1 000 messages).
- Réponses locales de secours clairement identifiées comme non-IA.
- Vérification de la connectivité réseau.
- Synthèse vocale Android et dictée vocale via le service de reconnaissance installé sur le téléphone.
- Trois emplacements de galerie avec sélection de photos originales via le sélecteur de documents Android ; les URI et autorisations de lecture sont conservées.
- Le portrait du premier emplacement est affiché sur l'écran d'accueil.
- Préférences persistantes pour le nom affiché, la langue de synthèse vocale et l'activation de la lecture.
- Souvenirs personnels structurés : ajout, affichage et suppression locale (jusqu'à 500 entrées).
- Rappels locaux programmés via WorkManager, avec annulation des rappels RELIVE en attente.
- Création d'événements dans l'application calendrier Android, avec confirmation par l'utilisateur.
- Workflow GitHub Actions pour compiler Android Debug et publier l'APK en artefact.

## Limites et éléments encore nécessaires
- Le client IA hybride est présent : si une URL de backend est configurée et que le réseau est disponible, l'application envoie message, langue, 20 derniers échanges et souvenirs au endpoint HTTPS /chat ; en cas d'absence de backend ou d'échec réseau, elle revient aux réponses locales. Le serveur doit renvoyer un JSON avec le champ reply. Le modèle IA n'est pas encore déployé.
- URL du backend configurable à la compilation avec la propriété Gradle reliveApiBaseUrl. La connexion distante exige HTTPS et un consentement explicite dans Profil. La clé du fournisseur IA doit rester côté serveur, jamais dans l'APK.
- Les trois photos originales ne sont pas stockées dans le dépôt : il faut les choisir depuis le téléphone dans l'écran Photos. Aucun visage n'est retouché par RELIVE.
- La dictée dépend d'un service de reconnaissance vocale installé sur Android et peut nécessiter une connexion.
- Les conversations, souvenirs et préférences sont actuellement stockés localement sans chiffrement applicatif dédié. Le chiffrement, les contrôles d'export et la politique de conservation doivent être finalisés avant un usage avec des informations sensibles.
- La synthèse vocale créole dépend des voix installées sur le téléphone ; Android peut utiliser une voix de remplacement si elle n'existe pas.
- Mémoire sémantique intégrée aux réponses de l'IA, calendrier avancé avec gestion interne des événements récurrents, comptes, authentification, synchronisation cloud, sauvegarde, administration, confidentialité avancée et tests fonctionnels restent à développer.
- L'état de compilation doit être confirmé par un workflow GitHub Actions réussi sur le dernier commit.

## Compilation locale
Depuis ce dossier, avec JDK 17, Gradle 8.9 et Android SDK Platform 35 installés :

    gradle assembleDebug

Le fichier attendu est app/build/outputs/apk/debug/app-debug.apk.
