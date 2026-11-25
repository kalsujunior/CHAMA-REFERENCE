# RELIVE — KAL-SU

Application Android native Kotlin + Jetpack Compose centrée sur ESPÉRANCE RUCHAMA KEFORA (Chama).

## État réel de l'espace virtuel

- Le portrait d'origine est affiché comme une image 2D statique et indépendante.
- Le portrait original ne doit jamais être retouché, recadré, recoloré, déformé, reconstruit ou animé.
- Le composant UI s'appelle `ChamaPortrait`. Il ne s'agit pas d'un avatar 3D.
- Le décor actuellement affiché est une prévisualisation 2D dessinée avec Compose Canvas, pas une pièce 3D.
- Le corps articulé indépendant, le modèle 3D, les mouvements du corps et la pièce 3D ne sont pas encore implémentés.
- Les interactions de l'espace virtuel déclenchent des phrases vocales Android. Elles ne produisent pas encore de gestes corporels.
- La synthèse vocale ne pilote aucune animation du visage ou des lèvres.

## Protection obligatoire du portrait

Le fichier de référence intégré est `app/src/main/res/drawable-nodpi/chama_portrait.png`.

Toute future représentation animée devra garder cette image dans une couche indépendante et immuable. Les mouvements seront limités au corps, aux vêtements et aux cheveux séparés. Aucun rig facial, mouvement de tête, clignement, mouvement des yeux, de la bouche ou de la mâchoire n'est autorisé.

La spécification du rig est désormais limitée au corps :
- `app/src/main/assets/chama/rig/chama-rig-manifest.json`
- `app/src/main/assets/chama/rig/chama-motion-clips.json`
- `com.kalsu.relive.avatar.ChamaAvatarRig`

Ces fichiers sont des spécifications de conception, pas un modèle 3D ni un moteur d'animation déjà fonctionnel.

## Fonctions Android présentes

- Navigation : Accueil, Conversation, Photos, Profil, Rappels et Espace virtuel.
- Historique local des conversations (jusqu'à 1 000 messages).
- Réponses locales de secours ; elles ne constituent pas un modèle d'IA local.
- Jusqu'à 500 souvenirs, avec ajout, affichage et suppression.
- Stockage chiffré des préférences, conversations, souvenirs et URI de photos via Android Keystore.
- Migration des anciennes préférences et sauvegarde Android désactivée.
- Synthèse vocale Android en français ou créole selon les voix installées.
- Dictée vocale via le service de reconnaissance disponible sur Android.
- Portrait principal intégré et immuable ; deux emplacements secondaires de galerie utilisant les photos originales choisies dans le sélecteur Android.
- Rappels locaux via WorkManager et ajout d'événement au calendrier.
- Consentement explicite avant tout envoi au backend IA distant.
- Code d'accès du backend conservé dans le stockage chiffré de l'application.

## Backend IA

Le Worker Cloudflare est configuré à l'adresse :

https://relive-chama-api.kalsujunior.workers.dev

Le Worker prend en charge des fournisseurs IA distants. Les clés des fournisseurs et le code privé doivent rester dans les secrets Cloudflare, jamais dans l'APK ou dans le dépôt.

Le consentement IA distante reste désactivé par défaut. Les messages, une partie de l'historique et les souvenirs peuvent être envoyés au backend uniquement après activation explicite dans Profil.

Voir `backend/cloudflare-worker/README.md` pour les détails de configuration.

## Thèmes et apparence

- Le mode « Suivre la tenue » utilise actuellement la palette de la tenue principale (crème, rouge cœur et rose).
- Le mode « Thème fixe » permet de verrouiller une palette : crème/rouge, rose tendre ou noir/or.
- Le mode et la palette sont conservés dans les préférences chiffrées de l'application.
- Le choix est appliqué à l'interface principale et à l'espace virtuel.
- La détection automatique des couleurs d'une nouvelle tenue n'est pas encore implémentée ; le mode « Suivre la tenue » utilise pour le moment la palette configurée de la tenue principale.

## Photos

Les deux photos secondaires sont sélectionnées dans l'écran Photos. L'emplacement principal reste verrouillé sur le portrait intégré ; le stockage refuse toute tentative de remplacement ou de suppression de cet emplacement. RELIVE conserve les URI des photos secondaires et ne retouche pas les images.

## Limites connues et travaux restants

- Aucun avatar humain 3D ou corps articulé n'est encore livré.
- Aucun véritable environnement 3D n'est encore livré ; le décor actuel est une prévisualisation 2D.
- Aucun clonage vocal n'est implémenté. Android TTS utilise les voix installées sur le téléphone.
- Les réponses locales sont des règles de secours, pas un grand modèle d'IA.
- Pas encore de compte utilisateur, de synchronisation multi-appareils ni de sauvegarde cloud.
- Le code d'accès du Worker est un secret partagé pour cette installation, pas un système complet de comptes individuels.

## Compilation

Configuration du projet :
- Android Gradle Plugin 9.4.0
- Kotlin intégré à AGP 9.x ; plugin Compose Kotlin 2.4.20
- SceneView 4.74.0 / Filament
- compileSdk 37
- minSdk 24 (SceneView 4.x l'exige)
- Java / Kotlin JVM target 21
- Gradle 9.6.0 ou supérieur compatible avec AGP 9.4

Prérequis recommandés : JDK 21, Gradle 9.6.0 ou supérieur et Android SDK Platform 37 avec Build Tools 36.0.0.

Depuis le dossier `RELIVE` :

```bash
gradle assembleDebug
```

APK : `app/build/outputs/apk/debug/app-debug.apk`.

SceneView 4.x est ajouté au projet ; la scène 3D et ses modèles restent à intégrer. La compilation de la migration doit être vérifiée ; les commits GitHub ne constituent pas une preuve de compilation réussie.
