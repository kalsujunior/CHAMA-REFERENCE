# RELIVE — readiness de livraison sur Motorola

Dernière vérification : 26 novembre 2026.

## Environnement Motorola connu

- Développement effectué dans Termux sur le Motorola.
- Copie locale du projet : `$HOME/RELIVE`.
- Dépôt cloné : `$HOME/CHAMA-REFERENCE`.
- La compilation reste volontairement différée jusqu'à l'intégration de toutes les fonctionnalités prévues ; le script de build est préparé pour la validation finale et ne doit pas être lancé prématurément.

## Compatibilité Android

- Appareil cible déclaré : Android 12 et versions ultérieures.
- `minSdk = 24` : Android 12 est compatible avec la version minimale déclarée.
- `targetSdk = 35`, `compileSdk = 37`.
- Plug-in Android Gradle : 9.4.0.
- Le plug-in Android Gradle 9.4.0 exige Gradle 9.6.0 au minimum et JDK 17 au minimum. Le module configure la compilation Java en version 21 ; utiliser un JDK 21 pour reproduire cette configuration.
- Aucun Gradle Wrapper (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar` ou `gradle-wrapper.properties`) n'a été trouvé dans le dépôt lors de cet audit. Une compilation en terminal nécessite donc une installation Gradle compatible, ou la génération préalable du Wrapper.

Référence officielle : https://developer.android.com/build/releases/agp-9-4-0-release-notes

### Préparer une compilation locale

Le dépôt ne contient pas encore le Wrapper. Avec un JDK 21, Android SDK Platform 37, Build Tools 36.0.0 et Gradle disponible, le script fourni automatise la génération locale du Wrapper 9.6.0, les tests, la compilation et la copie de l'APK :

```bash
cd RELIVE
bash scripts/build-motorola.sh
```

L'APK est copié vers `/storage/emulated/0/Download/RELIVE-MOTOROLA-debug.apk` lorsque le stockage Android est accessible ; sinon, le script affiche son emplacement dans le dossier de build. Si l'environnement Gradle/JDK/SDK est trop ancien ou incomplet, le script s'arrête avec un message d'erreur ; ne pas modifier les versions Android du projet au hasard.

## Fonctions présentes dans l'application

- Accueil, conversation locale, galerie, profil, rappels et espace 3D.
- Conversation distante facultative, désactivée par défaut, via le Worker RELIVE.
- Synthèse vocale Android disponible.
- Autonomie locale dans le salon : marche vers des points d'intérêt, observation, danse, posture assise simplifiée et geste de bras tendu vers les fleurs. Elle reste désactivée par défaut et s'arrête lors d'une interaction, en mode économie d'énergie ou lorsque l'écran quitte l'état actif.
- Portrait principal intégré séparément, sans modification.

## État du serveur RELIVE

- Le Worker `relive-chama-api` est présent dans le compte Cloudflare.
- Le code déployé prend en charge le traitement du chat (`/chat`) avec contrôle d'accès, limitation de débit et fournisseurs IA configurés.
- Le code actuellement déployé ne fournit pas de route audio `/speech`. Le client Android a donc été ajusté pour utiliser la synthèse Android et ne plus envoyer le texte des réponses à un endpoint audio inexistant.
- L'audit a relevé que des valeurs de configuration sensibles sont encore stockées comme variables texte ordinaires plutôt que comme secrets Worker. Migrer ces valeurs vers des secrets protégés et vérifier le déploiement avant d'activer des services distants supplémentaires. Aucune valeur sensible ne doit être copiée dans ce document.

## Vérifications et éléments bloquants

- Seize tests JUnit d'autonomie et de cinématique du corps sont présents dans `ChamaAutonomyTest.kt`, mais ils n'ont pas encore été exécutés.
- Le module pur Kotlin `ChamaBodyPoseMath.kt` a été compilé séparément avec Kotlin/JVM 1.9 et vérifié sur les longueurs des bras, l'avancée des jambes et la hauteur des pieds. Cette vérification ciblée ne constitue ni l'exécution des tests JUnit ni une compilation Android.
- Aucune compilation Android n'a été exécutée sur cette branche pendant cet audit.
- Aucun APK n'a encore été installé ni testé sur le Motorola.
- La collision dynamique du corps articulé, le rig 3D skinné, l'assise directement sur le coussin du canapé et les manipulations physiques d'objets restent à réaliser. Les poses assises et de portée sont des cinématiques géométriques testables, mais pas un rig squelettique.
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
