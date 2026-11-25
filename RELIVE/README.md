# RELIVE — KAL-SU

Application Android native en Kotlin et Jetpack Compose, centrée sur ESPÉRANCE RUCHAMA KEFORA (Chama).

## État du dépôt

Cette branche contient une base fonctionnelle en cours de développement. Elle n'est pas encore une livraison complète et aucune compilation Android n'a été exécutée dans cet environnement.

### Déjà implémenté
- Navigation Compose : Accueil, Conversation, Galerie, Profil et Rappels.
- Identité visuelle noir et or.
- Persistance locale de l'historique (jusqu'à 1 000 messages) via SharedPreferences.
- Réponses locales de secours simples, distinctes d'une véritable IA.
- Vérification de la connectivité réseau.
- Synthèse vocale Android locale (TextToSpeech).
- Rappels locaux planifiés avec WorkManager et notifications Android.
- Écran de programmation et d'annulation des rappels, avec demande de permission sur Android 13+.
- Permissions INTERNET et ACCESS_NETWORK_STATE.

### Pas encore implémenté / dépendances nécessaires
- IA distante : nécessite un backend HTTPS et une configuration serveur sécurisée. Aucun secret d'API ne doit être placé dans l'application.
- Reconnaissance vocale et gestion du consentement.
- Mémoire structurée de long terme, préférences et export/suppression des données.
- Intégration des trois photos originales. Le portrait principal est la photo au pull blanc avec cœurs rouges ; les deux autres vont dans la galerie. Les images doivent être copiées sans retouche dans les ressources Android.
- Calendrier intégré au système, avec création d'événements après confirmation.
- Comptes, authentification, synchronisation et sauvegarde cloud.
- Paramètres complets, confidentialité, tests automatisés et accessibilité.
- Vérification de compilation avec JDK, Gradle et SDK Android compatibles.

## Photos de Chama
Ne pas régénérer, retoucher ou modifier le visage, les cheveux, les vêtements ou les proportions des photos de référence. Les fichiers originaux doivent être fournis dans les ressources du module app.

## Compilation
Depuis le dossier RELIVE, avec JDK 17 et SDK Android configuré :

    gradle assembleDebug

La compilation ne doit être déclarée validée qu'après exécution réussie de cette commande dans un environnement Android correctement configuré.
