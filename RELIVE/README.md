# RELIVE — KAL-SU

Application Android native Kotlin + Jetpack Compose centrée sur ESPÉRANCE RUCHAMA KEFORA (Chama).

## État actuel de l'espace virtuel

- Une pièce 3D procédurale SceneView/Filament est intégrée : sol, murs, fenêtre panoramique, rideaux, canapé, coussins, tapis, table, lampe, suspension, étagère, livres, cadre mural, vase de roses et plante.
- Un premier blocage géométrique 3D du corps indépendant est intégré : torse, bassin, épaules, coudes, avant-bras, poignets, paumes, doigts géométriques, jambes, chaussures et détails géométriques du pull.
- Les interactions Bonjour, Écouter et Danser sélectionnent respectivement les gestes corporels WAVE, LISTEN et DANCE.
- Un contrôle dans l'interface permet d'allumer et d'éteindre visuellement la lampe du salon 3D.
- Un second contrôle bascule l'ambiance visuelle de la fenêtre panoramique entre le jour et le soir, indépendamment de la lampe.
- Un troisième contrôle prépare visuellement le canapé en modifiant la teinte des coussins ; l'action peut être annulée pour revenir à l'aménagement habituel.
- Un quatrième contrôle met en valeur le bouquet en éclaircissant visuellement les roses ; l'action peut être annulée pour retrouver leur teinte initiale.
- Un médaillon mural 3D RELIVE-KAL-SU a été ajouté au mur : panneau sombre, cœur rouge en relief, bordures dorées et accents floraux. Il s'agit d'une composition géométrique indépendante inspirée de l'illustration de référence, et non de l'image raster directement plaquée sur le mur.
- Le corps possède des animations procédurales simples : mouvement indépendant des bras, respiration, balancement du torse et oscillation des deux tresses géométriques.
- Le portrait d'origine reste une image Compose distincte, affichée sans texture 3D, retouche ni animation.
- Le modèle corporel actuel est un blocage géométrique de développement. Ce n'est pas encore un personnage humain réaliste, un maillage final, un rig squelettique complet ni un fichier GLB/FBX.
- Les deux SceneView (pièce et corps) sont actuellement séparées. Leur coût mémoire/GPU doit être mesuré sur le téléphone cible avant validation.
- Les tresses actuelles sont des volumes géométriques provisoires ; leur maillage détaillé, les vêtements finalisés, les animations avancées, l'intégration visuelle du portrait avec le corps et les interactions complètes de la pièce restent à réaliser.

## Protection obligatoire du portrait

Le fichier de référence intégré est `app/src/main/res/drawable-nodpi/chama_portrait.png`.

Son SHA-256 de référence est :
`ad9091b7ea61919de0bab0dd0921b7a164982f92`.

Toute future représentation animée devra garder cette image dans une couche indépendante et immuable. Aucun recadrage, recoloration, déformation, reconstruction, mouvement de tête, clignement, mouvement des yeux, de la bouche ou de la mâchoire n'est autorisé. Les animations concernent exclusivement le corps et les éléments séparés qui ne modifient pas la photographie.

La spécification du rig corporel est limitée aux articulations du corps :
- `app/src/main/assets/chama/rig/chama-rig-manifest.json`
- `app/src/main/assets/chama/rig/chama-motion-clips.json`
- `com.kalsu.relive.avatar.ChamaAvatarRig`

Ces fichiers restent des spécifications et ne doivent pas être présentés comme un modèle 3D livré.

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
- La détection automatique des couleurs d'une nouvelle tenue n'est pas encore implémentée.

## Limites et prochaines intégrations

- Remplacer progressivement le blocage géométrique par un corps 3D détaillé et réellement articulé, sans toucher au portrait.
- Ajouter les tresses et les vêtements comme éléments séparés, puis leurs mouvements corporels.
- Fusionner ou optimiser les deux scènes 3D si les mesures de mémoire/GPU le nécessitent.
- Étendre les interactions avec le mobilier (assise réelle, autres objets décoratifs, éclairage complémentaire) et les déplacements dans la pièce. Le canapé et le bouquet disposent déjà de commandes visuelles simples. Le contrôle actuel du canapé ne simule pas encore une véritable animation d'assise.
- Évaluer les performances et la stabilité sur le Motorola Moto G Stylus 5G avant de considérer l'espace 3D comme validé.
- Aucun clonage vocal n'est implémenté. Android TTS utilise les voix installées sur le téléphone.
- Pas encore de compte utilisateur, de synchronisation multi-appareils ni de sauvegarde cloud.
- Le code d'accès du Worker est un secret partagé pour cette installation, pas un système complet de comptes individuels.

## Compilation

Configuration du projet :
- Android Gradle Plugin 9.4.0
- Kotlin intégré à AGP 9.x ; plugin Compose Kotlin 2.4.20
- SceneView 4.74.0 / Filament
- compileSdk 37
- minSdk 24
- Java / Kotlin JVM target 21
- Gradle 9.6.0 ou supérieur compatible avec AGP 9.4

Prérequis recommandés : JDK 21, Android SDK Platform 37 et Build Tools 36.0.0.

Depuis le dossier `RELIVE` :

```bash
gradle assembleDebug
```

APK : `app/build/outputs/apk/debug/app-debug.apk`.

Les modifications présentes sur GitHub ne prouvent pas à elles seules que la compilation ou le fonctionnement sur le téléphone ont été vérifiés. La prochaine validation doit couvrir la compilation, l'ouverture de l'espace virtuel, les interactions, la stabilité et la consommation de ressources.
