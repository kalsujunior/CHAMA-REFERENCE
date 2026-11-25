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

## Backend IA
Le Worker Cloudflare est déployé à l'adresse HTTPS suivante :

https://relive-chama-api.kalsujunior.workers.dev

Le client Android est préconfiguré avec cette adresse. Le Worker applique une limite de 15 requêtes par adresse IP et exige un code d'accès privé avant de contacter le fournisseur IA.

**Le serveur est déployé, mais l'IA distante reste en attente de configuration des secrets.** Le propriétaire doit ajouter dans Cloudflare :
- `AI_API_KEY` : clé API du fournisseur IA, stockée comme secret Cloudflare.
- `AI_MODEL` : nom d'un modèle disponible sur le compte fournisseur.
- `RELIVE_ACCESS_TOKEN` : long code privé choisi par le propriétaire.

Le même `RELIVE_ACCESS_TOKEN` doit être saisi dans RELIVE > Profil > Code d'accès privé du serveur. L'application le stocke chiffré et ne l'envoie qu'au Worker HTTPS. Ne jamais mettre la clé fournisseur dans l'APK ou dans le dépôt.

Le code source du Worker est dans `backend/cloudflare-worker`. Voir son README pour les étapes de configuration. Tant que les secrets ne sont pas configurés, RELIVE continue de fonctionner en mode local. L'IA distante est désactivée par défaut et nécessite un accord dans Profil.

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
