# RELIVE — KAL-SU

Base de l'application Android native RELIVE, en Kotlin et Jetpack Compose, centrée sur la compagne virtuelle Chama.

## Inclus dans cette première base
- Navigation Accueil / Conversation / Galerie / Profil.
- Identité visuelle noir et or.
- Emplacement réservé au portrait principal.
- Écrans de démonstration en français.

## Photos de Chama
La photo principale choisie est la première photo envoyée dans la conversation (pull blanc avec cœurs rouges). Les deux autres photos sont destinées à la galerie. Les fichiers originaux devront être copiés dans les ressources Android avant de remplacer les emplacements de démonstration. Aucun visage ne doit être retouché ou régénéré.

## Compilation
Ouvrir ce dossier dans Android Studio ou Code On The Go avec le SDK Android installé, synchroniser Gradle puis lancer `assembleDebug`.

## À développer
1. Intégrer les trois fichiers photo originaux.
2. Persistance locale des conversations et préférences.
3. Comptes et authentification sécurisée avec backend.
4. IA conversationnelle avec service distant et gestion des clés côté serveur.
5. Notifications, rendez-vous, carte/localisation et fonctions multimédias avec autorisations explicites.
6. Tableau d'administration protégé.
7. Tests, sécurité, accessibilité et publication.

Les réponses de Chama dans cette version sont des démonstrations locales ; aucune connexion IA réelle n'est configurée.
