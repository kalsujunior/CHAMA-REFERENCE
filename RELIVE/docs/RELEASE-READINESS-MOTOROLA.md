# RELIVE — readiness de livraison sur Motorola

Dernière vérification : 26 novembre 2026.

## Compatibilité Android

- Appareil cible déclaré : Android 12 et versions ultérieures.
- `minSdk = 24` : Android 12 est compatible avec la version minimale déclarée.
- `targetSdk = 35`, `compileSdk = 37`.
- Plug-in Android Gradle : 9.4.0.
- Le plug-in Android Gradle 9.4.0 exige Gradle 9.6.0 au minimum et JDK 17 au minimum. Le module configure la compilation Java en version 21 ; utiliser un JDK 21 pour reproduire cette configuration.
- Aucun Gradle Wrapper (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar` ou `gradle-wrapper.properties`) n'a été trouvé dans le dépôt lors de cet audit. Une compilation en terminal nécessite donc une installation Gradle compatible, ou la génération préalable du Wrapper.

Référence officielle : https://developer.android.com/build/releases/agp-9-4-0-release-notes

### Préparer une compilation locale

Le dépôt ne contient pas encore le Wrapper. Depuis le dossier `RELIVE`, avec un JDK 21, Android SDK Platform 37, Build Tools 36.0.0 et une installation Gradle disponible :

```bash
cd RELIVE
gradle wrapper --gradle-version 9.6.0
./gradlew --version
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

La commande de test et la compilation doivent toutes deux réussir avant de récupérer l'APK dans `app/build/outputs/apk/debug/app-debug.apk`. Si la commande `gradle wrapper` échoue parce que Gradle ou le JDK installé est trop ancien, mettre d'abord l'environnement de compilation à niveau ; ne pas modifier les versions Android du projet au hasard.

## Fonctions présentes dans l'application

- Accueil, conversation locale, galerie, profil, rappels et espace 3D.
- Conversation distante facultative, désactivée par défaut, via le Worker RELIVE.
- Synthèse vocale Android disponible.
- Autonomie locale dans le salon : marche vers des points d'intérêt, observation, danse et repos logique. Elle reste désactivée par défaut et s'arrête lors d'une interaction, en mode économie d'énergie ou lorsque l'écran quitte l'état actif.
- Portrait principal intégré séparément, sans modification.

## État du serveur RELIVE

- Le Worker `relive-chama-api` est présent dans le compte Cloudflare.
- Le code déployé prend en charge le traitement du chat (`/chat`) avec contrôle d'accès, limitation de débit et fournisseurs IA configurés.
- Le code actuellement déployé ne fournit pas de route audio `/speech`. Le client Android a donc été ajusté pour utiliser la synthèse Android et ne plus envoyer le texte des réponses à un endpoint audio inexistant.
- L'audit a relevé que des valeurs de configuration sensibles sont encore stockées comme variables texte ordinaires plutôt que comme secrets Worker. Migrer ces valeurs vers des secrets protégés et vérifier le déploiement avant d'activer des services distants supplémentaires. Aucune valeur sensible ne doit être copiée dans ce document.

## Vérifications et éléments bloquants

- Onze tests JUnit d'autonomie sont présents dans `ChamaAutonomyTest.kt`, mais ils n'ont pas encore été exécutés.
- Aucune compilation Android n'a été exécutée sur cette branche pendant cet audit.
- Aucun APK n'a encore été installé ni testé sur le Motorola.
- La collision dynamique du corps articulé, le rig 3D skinné, l'assise réelle et les manipulations physiques d'objets restent à réaliser.
- Les performances, le cadrage, les collisions et la température doivent être mesurés sur le téléphone réel.

## Contrôles de protection

- Le portrait original `chama_portrait.png` conserve son empreinte Git attendue : `ad9091b7ea61919de0bab0dd0921b7a164982f92`.
- La branche de travail reste séparée de `main`.
- Les commits de développement utilisent `[skip ci]` ; aucun workflow n'a été déclenché par les derniers commits contrôlés.

## Critère de livraison

RELIVE ne doit être annoncée comme opérationnelle qu'après :
1. compilation réussie de l'APK debug ;
2. exécution réussie des tests unitaires ;
3. installation et lancement sur le Motorola ;
4. vérification manuelle de la conversation, de la galerie, de la voix Android, des rappels et du salon 3D ;
5. vérification de l'arrêt immédiat de l'autonomie lors des interactions et de l'absence de traversée des meubles.
