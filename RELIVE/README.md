# RELIVE — KAL-SU

Application Android native Kotlin + Jetpack Compose centrée sur ESPÉRANCE RUCHAMA KEFORA (Chama).

## Fonctions de la version 0.2.0
- Navigation : Accueil, Conversation, Photos, Profil et Rappels.
- Interface sombre noir et or.
- Historique local des conversations (jusqu'à 1 000 messages).
- Réponses locales de secours clairement identifiées comme non-IA.
- Les souvenirs enregistrés sont consultables dans la conversation : demander « Que te rappelles-tu de moi ? ».
- Jusqu'à 500 souvenirs personnels, avec ajout, affichage et suppression.
- Conversations, souvenirs, préférences et URI de photos stockés dans des préférences chiffrées AES-256 via Android Keystore.
- Migration automatique des anciennes préférences locales vers le stockage chiffré.
- Sauvegarde Android désactivée pour éviter de restaurer des données chiffrées sans la clé Keystore.
- Synthèse vocale Android et dictée vocale via le service de reconnaissance installé.
- Trois emplacements de galerie utilisant les photos originales choisies dans le sélecteur Android.
- Rappels locaux via WorkManager, annulation et ajout d'événement au calendrier avec confirmation.
- Consentement explicite avant tout envoi au backend IA distant.
- Workflow GitHub Actions qui compile Android Debug et publie l'APK.

## Backend IA (à déployer par le propriétaire)
Le client Android attend un endpoint HTTPS POST /chat qui accepte un message, la langue, jusqu'à 20 souvenirs et les 20 derniers échanges. La réponse attendue est un JSON avec le champ reply.

Un modèle de backend Cloudflare Worker est fourni dans backend/cloudflare-worker. Il appelle une API compatible OpenAI Chat Completions. La clé fournisseur doit être ajoutée comme secret Cloudflare AI_API_KEY, jamais dans le dépôt ou l'APK. Il faut configurer AI_MODEL et le binding de limitation de débit RATE_LIMITER. Le Worker refuse les requêtes si le binding ou la clé API manque.

Pour compiler avec l'URL du Worker :
```bash
gradle assembleDebug -PreliveApiBaseUrl=https://TON-DOMAINE-WORKER
```
L'URL doit être HTTPS et ne doit pas finir par /chat.

**Important :** le backend n'est pas déployé automatiquement. Il nécessite un compte Cloudflare, une clé API fournisseur et la configuration des secrets/bindings. Sans URL valide, le mode local continue. L'IA distante est désactivée par défaut et nécessite un accord dans Profil.

## Photos de Chama
Les trois photos originales ne sont pas stockées dans le dépôt. Il faut les sélectionner dans l'écran Photos. RELIVE conserve les URI et ne retouche pas les visages.

## Limites connues
- Les voix françaises et créoles disponibles dépendent des moteurs installés sur Android.
- La dictée vocale dépend du service de reconnaissance du téléphone et peut nécessiter Internet.
- Le mode local est un système de réponses de secours, pas un grand modèle d'IA.
- La mémoire locale peut être lue par l'application et n'est envoyée au backend que si l'utilisateur active le consentement distant.
- Pas encore de compte utilisateur, authentification personnelle, synchronisation multi-appareils ni sauvegarde cloud.
- Le backend fourni est un modèle de déploiement, pas un service déjà hébergé.

## Compilation locale
Prérequis : JDK 17, Gradle 8.9 et Android SDK Platform 35.

Depuis ce dossier :
```bash
gradle assembleDebug
```
APK : app/build/outputs/apk/debug/app-debug.apk.
