# CHAMA — spécification du rigging 2D

## Objectif
Préparer un avatar 2D distinct de la photographie originale de Chama, destiné à RELIVE – KAL-SU. La photo originale reste intacte et reste disponible dans la galerie. Les planches générées servent de références artistiques ; elles ne constituent pas encore des calques animables.

## Références visuelles
- Identité visuelle : jeune femme à la peau brune, longues tresses brun foncé et mèches dorées, bandeau tressé sombre.
- Tenue de référence : pull rose très clair/blanc avec grands cœurs rouges.
- Conserver les proportions du visage, la forme des yeux, le nez, les lèvres et la coiffure de la référence fournie.
- Ne pas remplacer la photographie principale par une illustration et ne pas retoucher la photographie originale.

## Structure de calques requise
Chaque élément doit être livré sur calque transparent indépendant, à la même résolution et dans le même repère :
1. arrière-plan transparent ;
2. torse et vêtement ;
3. cou ;
4. tête/base du visage ;
5. oreilles ;
6. cheveux arrière ;
7. tresses avant gauche et droite, divisées en mèches mobiles si nécessaire ;
8. bandeau ;
9. œil gauche et œil droit (blanc, iris/pupille, paupière supérieure et inférieure séparés) ;
10. sourcils gauche et droit ;
11. nez ;
12. bouche et lèvres ;
13. bras gauche et droit, avant-bras et mains séparés pour les gestes.

Prévoir des zones cachées sous les articulations afin d'éviter les trous pendant les rotations. Les calques doivent être nommés clairement et conserver exactement les mêmes dimensions.

## Formes de bouche pour la synchronisation labiale
Préparer au minimum les poses suivantes :
- repos fermé ;
- A / AH ;
- E / EH ;
- I / EE ;
- O / OH ;
- OU / OO ;
- M / B / P (lèvres fermées) ;
- F / V (lèvre inférieure contre les dents) ;
- sourire parlé.

Les formes de bouche doivent partager le même contour facial et le même point d'ancrage. Ajouter des poses intermédiaires pour éviter les changements brusques.

## États d'animation
- repos : respiration discrète, micro-mouvement de tête ;
- écoute : clignement naturel, regard léger vers l'utilisateur, inclinaison douce ;
- parole : alternance des visèmes pilotée par le moteur vocal, clignements indépendants ;
- salutation : mouvement de l'avant-bras et de la main ;
- joie : sourire, légère inclinaison de la tête ;
- danse : mouvement limité du torse et des bras, sans déformation du visage.

## Contraintes d'intégration Android
- L'avatar animé doit être un composant distinct de l'écran affichant la photo originale.
- Les animations doivent être suspendues lorsque l'écran n'est pas visible.
- Prévoir une réduction des mouvements et un mode statique pour les appareils modestes.
- Ne jamais bloquer la lecture TTS pendant une animation.
- Les données de visèmes doivent suivre les événements temporels du moteur vocal ; ne pas utiliser un simple minuteur arbitraire pour prétendre à une synchronisation exacte.
- Garder un comportement de secours : portrait original et animation de présentation si les ressources riggées ne sont pas installées.

## Critères d'acceptation
- Les calques sont séparés, transparents, alignés et exploitables par un moteur d'animation.
- Les rotations de tête ne créent pas de trous visibles.
- Les clignements et mouvements de regard restent naturels.
- Les visèmes sont synchronisés avec les événements de parole disponibles.
- Les gestes n'altèrent pas l'identité du visage.
- La version de secours existante reste fonctionnelle sans ressources additionnelles.

## État actuel
La version 0.4.0 possède une animation de présentation (flottement, respiration visuelle et inclinaison) et la synthèse vocale Android. Le rigging facial, les calques séparés, les visèmes et la synchronisation labiale réelle ne sont pas encore implémentés. Les planches de conception sont des références visuelles, pas des fichiers de rigging prêts à l'emploi.
