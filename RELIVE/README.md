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
- Synthèse vocale Android et dictée vocale en français ou créole selon la langue choisie.
- Trois emplacements de galerie utilisant les photos originales choisies dans le sélecteur Android.
- Rappels locaux via WorkManager, annulation et ajout d'événement au calendrier avec confirmation.
- Consentement explicite avant tout envoi au backend IA distant.
- Code d'accès du backend conservé dans le stockage chiffré de l'application.
- Workflow GitHub Actions qui compile Android Debug et publie l'APK.

## Backend IA à deux fournisseurs

Le Worker Cloudflare est déployé à l'adresse :
https://relive-chama-api.kalsujunior.workers.dev

Le Worker essaie GroqCloud en premier et bascule automatiquement sur Google Gemini si Groq est indisponible ou atteint une limite. Un seul fournisseur configuré permet déjà de démarrer ; les deux sont nécessaires pour le secours automatique. Les deux clés API et le code privé sont conservés comme secrets Cloudflare, jamais dans l'APK ou le dépôt.

Voir `backend/cloudflare-worker/README.md` pour les étapes de configuration et les noms exacts des variables. Le même `RELIVE_ACCESS_TOKEN` doit être saisi dans RELIVE > Profil. Le consentement IA distant reste désactivé par défaut.

## Photos de Chama
Les trois photos originales ne sont pas stockées dans le dépôt. Il faut les sélectionner dans l'écran Photos. RELIVE conserve les URI et ne retouche pas les visages.

## Limites connues
- Les voix françaises et créoles disponibles dépendent des moteurs installés sur Android.
- La dictée vocale dépend du service de reconnaissance du téléphone et peut nécessiter Internet.
- Le mode local est un système de réponses de secours, pas un grand modèle d'IA.
- La mémoire locale est chiffrée sur l'appareil ; elle n'est envoyée au backend que si l'utilisateur active le consentement distant.
- Pas encore de compte utilisateur, synchronisation multi-appareils ni sauvegarde cloud.
- Le code d'accès du Worker est un secret partagé pour cette installation, pas un système complet de comptes individuels.

## Compilation locale
Prérequis : JDK 17, Gradle 8.9 et Android SDK Platform 35.

Depuis ce dossier :
```bash
gradle assembleDebug
```
APK : `app/build/outputs/apk/debug/app-debug.apk`.
