# RELIVE — KAL-SU

Application Android native Kotlin + Jetpack Compose centrée sur ESPÉRANCE RUCHAMA KEFORA (Chama).

## État actuel de l'espace virtuel

- Une pièce 3D procédurale SceneView/Filament est intégrée : sol, murs, fenêtre panoramique, rideaux, canapé, coussins, tapis, table, lampe, suspension, étagère, livres, cadre mural, vase de roses et plante.
- Un blocage géométrique 3D du corps indépendant est intégré : volumes séparés pour le buste, la poitrine, la taille et le bassin, épaules, coudes, avant-bras, poignets, paumes, doigts géométriques, jambes, chaussures et détails géométriques du haut et du pantalon denim.
- Les interactions Bonjour, Écouter et Danser sélectionnent respectivement les gestes corporels WAVE, LISTEN et DANCE.
- Un contrôle dans l'interface permet d'allumer et d'éteindre visuellement la lampe du salon 3D.
- Un second contrôle bascule l'ambiance visuelle de la fenêtre panoramique entre le jour et le soir, indépendamment de la lampe.
- Un troisième contrôle prépare visuellement le canapé en modifiant la teinte des coussins ; l'action peut être annulée pour revenir à l'aménagement habituel.
- Un quatrième contrôle met en valeur le bouquet en éclaircissant visuellement les roses ; l'action peut être annulée pour retrouver leur teinte initiale.
- Un mode léger désactive le rendu 3D de la pièce pour réduire la charge graphique sur les appareils modestes ; le bloc corporel et le portrait restent affichés.
- Un médaillon mural 3D RELIVE-KAL-SU a été ajouté au mur : panneau sombre, cœur rouge en relief, bordures dorées et accents floraux. Il s'agit d'une composition géométrique indépendante inspirée de l'illustration de référence, et non de l'image raster directement plaquée sur le mur.
- Le corps possède désormais une articulation géométrique à deux segments pour chaque bras (épaule → coude → poignet) et chaque jambe (hanche → genou → cheville) ; les positions des segments sont recalculées à partir des angles articulaires pour limiter les ruptures aux jointures. Les gestes WAVE, LISTEN et DANCE utilisent des poses distinctes. Le geste DANCE ajoute une légère flexion latérale approximée par des décalages coordonnés du haut du torse, de la taille, des épaules et des détails du vêtement ; il ne s'agit pas encore d'une rotation squelettique réelle.
- Le mode léger désactive aussi les animations corporelles procédurales, en plus du décor de pièce, pour réduire le travail de rendu.
- Le portrait d'origine reste une image Compose distincte, affichée sans texture 3D, retouche ni animation.
- Le modèle corporel actuel est un blocage géométrique de développement. Ce n'est pas encore un personnage humain réaliste, un maillage final, un rig squelettique complet ni un fichier GLB/FBX.
- Les deux SceneView (pièce et corps) sont actuellement séparées. Leur coût mémoire/GPU doit être mesuré sur le téléphone cible avant validation.
- La géométrie corporelle est strictement limitée aux épaules jusqu'aux pieds : aucune tête, aucun cou, aucun visage et aucun cheveu n'est généré dans ce blocage. Les segments des membres sont orientés à partir des vecteurs entre pivots calculés ; les volumes de hanches et de chevilles renforcent la continuité visuelle des articulations. Le maillage anatomique détaillé, les vêtements finalisés, les animations avancées et l'intégration visuelle précise du portrait avec le corps restent à réaliser.

## Références corporelles vidéo de Chama

Les cinq vidéos validées par l'utilisateur servent de références qualitatives pour la posture et le mouvement : gestes des mains devant le buste, rotations du torse, changements d'appui, vues de dos et de trois-quarts, inclinaisons latérales, bras levés et mouvements de danse. Elles ne permettent pas de déduire des mensurations exactes, car les vêtements, le cadrage, la perspective et les angles de caméra varient. Les dimensions du blocage 3D restent donc des paramètres provisoires à ajuster visuellement, et non des mesures biométriques. La silhouette utilise maintenant des volumes distincts pour éviter une largeur uniforme du thorax à la taille, un bassin de transition et des détails de pantalon denim (ceinture, couture extérieure, braguette, ouvertures de poches et passants) ; ces volumes restent stylisés et ne constituent pas une reconstruction anatomique exacte. Les détails sont des primitives géométriques provisoires, pas un patron textile ni un maillage de vêtement finalisé.

La géométrie actuelle reste un blocage procédural de développement : elle n'est pas un maillage anatomique final, un système de skinning de production ni un solveur d'IK complet. Les articulations sont simulées par le recalcul des centres et orientations des segments.

## Protection obligatoire du portrait

Le fichier de référence intégré est `app/src/main/res/drawable-nodpi/chama_portrait.png`.

Son identifiant d'objet Git (blob SHA-1) de référence est :
`ad9091b7ea61919de0bab0dd0921b7a164982f92`.

Le workflow GitHub Actions vérifie automatiquement cet identifiant pour `shama.png` et pour la copie intégrée à l'application. Le contrôle `RELIVE/scripts/verify-chama-portrait.sh` valide aussi les verrous du manifeste ; la compilation échoue si l'image de référence change ou si une règle protégeant le visage est affaiblie.

Toute future représentation animée devra garder cette image dans une couche indépendante et immuable. Aucun recadrage, recoloration, déformation, reconstruction, mouvement de tête, clignement, mouvement des yeux, de la bouche ou de la mâchoire n'est autorisé. Les animations concernent exclusivement les volumes corporels séparés et ne modifient jamais la photographie.

La spécification du rig corporel est limitée aux articulations du corps :
- `app/src/main/assets/chama/rig/chama-rig-manifest.json`
- `app/src/main/assets/chama/rig/chama-motion-clips.json`
- `com.kalsu.relive.avatar.ChamaAvatarRig`

Ces fichiers restent des spécifications et ne doivent pas être présentés comme un modèle 3D livré.

## Fonctions Android présentes

- Navigation : Accueil, Conversation, Photos, Profil, Rappels et Espace virtuel.
- Historique local des conversations (jusqu'à 1 000 messages).
- Réponses locales de secours ; elles ne constituent pas un modèle d'IA local.
- Jusqu'à 500 souvenirs, avec ajout, affichage et suppression. Les demandes explicites « Souviens-toi… », « N'oublie pas… », « Mémorise… » et quelques formulations créoles sont détectées et enregistrées localement ; les autres messages ne sont pas mémorisés automatiquement.
- Stockage chiffré des préférences, conversations, souvenirs et URI de photos via Android Keystore.
- Migration des anciennes préférences et sauvegarde Android désactivée.
- Synthèse vocale Android en français ou créole selon les voix installées ; les demandes vocales faites pendant l'initialisation du moteur sont mises en attente et la dernière est prononcée lorsque le moteur est prêt.
- Dictée vocale via le service de reconnaissance disponible sur Android.
- Portrait principal intégré et immuable ; deux emplacements secondaires de galerie utilisant les photos originales choisies dans le sélecteur Android.
- Rappels locaux via WorkManager et ajout d'événement au calendrier ; les délais sont limités à 30 jours et les notifications bloquées par Android ne sont pas déclarées comme délivrées.
- Consentement explicite avant tout envoi au backend IA distant ; l'enregistrement du profil refuse l'activation distante si le code d'accès est vide. Les champs de profil sont bornés et la langue est limitée au français ou au créole haïtien.
- L'historique transmis au backend exclut le message courant du contexte précédent afin d'éviter de le répéter deux fois dans la requête. Le client refuse les URL non HTTPS, les jetons vides, limite les entrées et borne la taille de réponse.
- Identifiants de messages et de souvenirs rendus uniques même lorsque plusieurs éléments sont enregistrés dans la même milliseconde.
- Commandes de mobilier et d'ambiance regroupées sur une rangée défilante pour limiter la hauteur occupée sur les petits écrans.
- Sur Android 13 et versions ultérieures, un rappel n'est désormais planifié qu'après accord à la permission de notification ; en cas de refus, aucun rappel silencieusement inutilisable n'est créé.
- Les autorisations persistantes de lecture des anciennes photos de galerie sont libérées lorsqu'une photo est remplacée ou retirée.
- Une confirmation est demandée avant l'effacement complet de l'historique de conversation ou de tous les souvenirs, afin de réduire les suppressions accidentelles.
- La commande d'envoi de conversation libère toujours son état de chargement même si une opération locale échoue et affiche un message de reprise ; l'effacement de l'historique est désactivé pendant l'envoi.
- Code d'accès du backend conservé dans le stockage chiffré de l'application.

## Mémoire personnelle classée

Les souvenirs restent stockés localement dans les préférences chiffrées de l'application. Chaque nouvel élément peut être classé en cinq catégories : Identité, Préférences, Projets, Événements importants ou Général. Les anciens souvenirs JSON sans champ de catégorie sont lus comme « Général » et restent accessibles. La suppression d'un souvenir conserve les catégories des autres éléments.

Lorsqu'il autorise l'IA distante, l'utilisateur transmet les souvenirs pertinents avec leur catégorie afin d'améliorer le contexte conversationnel. Le transfert reste soumis au consentement IA distant existant ; aucun souvenir n'est synchronisé automatiquement vers un compte distant.

## Personnalité conversationnelle de Chama

Le Worker IA fournit désormais un cadre de personnalité cohérent pour Espérance Ruchama Kefora (Chama) : ton chaleureux, attentif, calme et naturel ; réponses adaptées au contexte et à la langue française ou créole haïtien ; honnêteté explicite sur les limites de l'IA ; absence de souvenirs ou de faits inventés ; respect de l'autonomie de l'utilisateur et absence de pression affective. Les souvenirs transmis restent du contexte utilisateur et ne remplacent jamais les règles de sécurité ni la demande actuelle.

Cette amélioration modifie le code du Worker sur la branche `relive-app`. Elle ne déploie pas automatiquement la version sur Cloudflare : le Worker doit être déployé séparément avant que l'application en production bénéficie de ce nouveau comportement. Aucun changement n'est apporté au portrait ni au rendu du visage.

## Voix personnalisée de Chama

Le code comprend une option de synthèse vocale distante en français, désactivée par défaut. Le profil propose un consentement distinct pour envoyer le texte des réponses au Worker vocal. Les identifiants du fournisseur restent exclusivement dans les secrets Cloudflare. Si le service échoue ou si le français n'est pas sélectionné, la synthèse Android locale est utilisée. Le créole haïtien reste en TTS Android.

L'activation réelle nécessite un échantillon fourni avec l'autorisation explicite de sa locutrice, la création de la voix auprès du fournisseur, la configuration des secrets Cloudflare et le déploiement du Worker. Le dépôt ne contient aucun échantillon vocal ni aucune clé fournisseur. La voix ne touche jamais au portrait et ne produit pas de synchronisation labiale.

Voir `VOICE_SETUP.md` pour les étapes et les conditions de consentement.

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
- Remplacer les volumes provisoires des tresses et les formes simplifiées des vêtements par des maillages détaillés séparés, avec des mouvements corporels mieux articulés.
- Fusionner ou optimiser les deux scènes 3D si les mesures de mémoire/GPU le nécessitent.
- Étendre les interactions avec le mobilier (assise réelle, autres objets décoratifs, éclairage complémentaire) et les déplacements dans la pièce. Le canapé et le bouquet disposent déjà de commandes visuelles simples. Le contrôle actuel du canapé ne simule pas encore une véritable animation d'assise.
- Évaluer les performances et la stabilité sur le Motorola Moto G Stylus 5G avant de considérer l'espace 3D comme validé.
- L'intégration optionnelle du fournisseur vocal est présente dans le code, mais aucun clone vocal n'est créé ni activé par défaut. Sans configuration et consentement explicite de la locutrice, Android TTS utilise les voix installées sur le téléphone.
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
- Gradle 9.9.2 (version utilisée par le workflow de compilation)

Prérequis recommandés : JDK 21, Android SDK Platform 37 et Android Build Tools 37.0.0.

Depuis le dossier `RELIVE` :

```bash
gradle assembleDebug
```

APK : `app/build/outputs/apk/debug/app-debug.apk`.

Le workflow GitHub Actions `.github/workflows/relive-android.yml` a été corrigé pour installer Android API 37, Build Tools 37.0.0 et Gradle 9.9.2, avec la création correcte du lien `android-37`.

Les modifications présentes sur GitHub ne prouvent pas à elles seules que la compilation ou le fonctionnement sur le téléphone ont été vérifiés. La prochaine validation doit couvrir la compilation, l'ouverture de l'espace virtuel, les interactions, la stabilité et la consommation de ressources.


État exact du personnage : la pièce utilise des maillages procéduraux SceneView et le corps est un blocage géométrique animé séparément. Il n'existe pas encore de modèle GLB/FBX de production ni de rig articulé réaliste. Le portrait original reste une image indépendante, statique et immuable.
