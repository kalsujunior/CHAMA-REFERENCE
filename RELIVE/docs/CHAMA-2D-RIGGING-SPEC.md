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


## VERROU ABSOLU — IDENTITÉ DU VISAGE DE CHAMA

Cette règle est prioritaire sur toute autre consigne artistique ou technique.

1. La photographie originale fournie par l'utilisateur est l'unique référence autorisée pour le visage. Elle doit être conservée telle quelle, sans retouche, filtre, restauration générative, embellissement, remplacement ni reconstruction par IA.
2. La planche de conception du personnage générée ultérieurement est une référence pour la silhouette, les vêtements, les cheveux, les accessoires et les gestes uniquement. Elle n'est jamais une référence faciale et ne doit pas servir à redessiner les traits.
3. Interdiction de modifier la forme du visage, les proportions, les yeux, les sourcils, le nez, les lèvres, la bouche, la mâchoire, la couleur ou la texture de peau, l'expression de référence ou tout autre trait identitaire.
4. Aucun morph target, blend shape, déformation de maillage, génération d'expression, lip-sync graphique ou animation procédurale ne doit altérer le visage original.
5. Pour respecter littéralement cette contrainte, le visage reste une image source immuable. Les mouvements autorisés portent sur le corps, les bras, les mains, la posture, les vêtements et les cheveux, sans toucher aux pixels du visage. Toute animation qui nécessiterait de déformer ou de recréer le visage doit être désactivée.
6. La tête ne doit pas être tournée en 3D si cela impose de reconstruire, compléter, déformer ou inventer des parties du visage. En cas de doute, conserver le portrait frontal original et animer le corps autour de lui.
7. Si le rendu 3D réaliste ne peut pas être obtenu sans altérer le visage, il faut privilégier un avatar hybride avec le portrait original intact plutôt qu'un visage 3D approximatif.
8. Toute ressource candidate doit être comparée à la photo source avant intégration. En cas de différence faciale, la ressource est rejetée.

### Test d'acceptation obligatoire — visage inchangé

- La photo source reste accessible et inchangée dans l'application.
- Aucun outil de génération ou de retouche n'est appliqué à cette photo.
- Aucun mouvement ne déforme ni ne remplace la région faciale.
- Le mode de secours affiche la photo originale, jamais un visage synthétique.
- Si l'une de ces conditions n'est pas vérifiable, la fonctionnalité faciale reste désactivée.

**Décision produit : visage verrouillé, animation corporelle autorisée. Aucune exception sans autorisation explicite ultérieure de l'utilisateur.**
