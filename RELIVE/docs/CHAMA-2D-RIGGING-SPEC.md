# CHAMA — spécification du rig corporel et du portrait protégé

## Objectif

Faire évoluer la présence visuelle de Chama dans RELIVE sans jamais modifier son visage. L'avatar animé est un assemblage hybride : le portrait original est une image indépendante, statique et immuable ; les mouvements sont produits par des éléments corporels séparés, placés derrière ou à côté du portrait.

Cette spécification remplace toute ancienne idée de rig facial, de visèmes ou de synchronisation labiale graphique. La règle de protection du portrait est prioritaire sur toute considération artistique ou technique.

## Source faciale unique

- Fichier de référence du dépôt : `shama.png`.
- Copie intégrée à l'application : `app/src/main/res/drawable-nodpi/chama_portrait.png`.
- Identifiant Git blob SHA-1 attendu pour les deux fichiers : `ad9091b7ea61919de0bab0dd0921b7a164982f92`.
- Composant Android d'affichage : `com.kalsu.relive.ChamaPortrait`.

La photo est affichée directement depuis la ressource originale. Elle ne doit jamais être recadrée, recolorée, retouchée, restaurée par IA, reconstruite, déformée, inclinée, tournée, masquée par un effet facial ou remplacée par un visage synthétique.

## Interdictions absolues

Aucun élément de rig ne doit être créé pour :
- la tête ou le visage ;
- les yeux, les paupières, les sourcils ou le regard ;
- le nez, la bouche, les lèvres, la mâchoire ou les expressions ;
- les visèmes, les blend shapes, les morph targets ou le lip-sync ;
- la rotation ou l'inclinaison de la tête.

Aucun clignement, mouvement des yeux, de la bouche ou de la mâchoire n'est autorisé. La photo doit conserver exactement ses pixels et son apparence d'origine. Ne jamais découper le visage en calques.

## Éléments corporels pouvant être animés

Les éléments suivants peuvent être modélisés séparément, à condition de ne pas recouvrir, transformer ou altérer le portrait :
1. torse et vêtements ;
2. bassin et jambes, si le cadrage les montre ;
3. épaules et bras supérieurs ;
4. avant-bras, poignets et mains ;
5. éléments de coiffure séparés, uniquement lorsqu'ils ne nécessitent aucune retouche des pixels du visage ;
6. accessoires et vêtements indépendants.

Prévoir des chevauchements suffisants entre les parties articulées pour éviter les trous visuels. Garder des points d'ancrage cohérents entre les poses. Le rig ne doit contenir que des articulations corporelles.

## États et gestes autorisés

- **Repos :** respiration discrète du torse et mouvement corporel très léger.
- **Écoute :** posture calme, bras détendus ; aucun mouvement de tête ou de regard.
- **Salutation :** mouvement d'un bras, de l'avant-bras et de la main.
- **Joie :** geste corporel positif, posture ouverte ; ne pas fabriquer de sourire sur la photo.
- **Danse :** mouvements modérés du torse, des épaules, des bras et des jambes ; le portrait reste fixe.

La synthèse vocale peut déclencher un indicateur visuel « Chama parle » ou une animation corporelle discrète. Elle ne doit pas entraîner de mouvement de bouche ni prétendre à une synchronisation labiale.

## Intégration Android

- Le portrait et le rig corporel restent des composants indépendants.
- Le portrait est rendu comme une image statique, sans transformation animée.
- Les animations corporelles sont suspendues lorsque l'espace virtuel n'est pas visible.
- Un mode léger doit désactiver les rendus et animations coûteux sur les appareils modestes.
- La lecture TTS ne doit pas dépendre du moteur d'animation.
- En cas de ressource manquante ou d'erreur 3D, afficher le portrait original et conserver les commandes de conversation disponibles.
- Les deux scènes SceneView actuelles (pièce et corps) doivent faire l'objet de mesures mémoire/GPU sur l'appareil cible avant toute fusion ou complexification.

## Critères d'acceptation obligatoires

1. Le fichier `shama.png` et la ressource Android ont exactement l'identifiant Git blob SHA-1 attendu.
2. Le manifeste maintient les verrous d'immuabilité du portrait.
3. Le visage n'est ni découpé, ni animé, ni transformé.
4. Les gestes n'agissent que sur le corps et les éléments indépendants.
5. L'indicateur de parole fonctionne sans lip-sync facial.
6. Le mode léger et le mode de secours préservent le portrait original.
7. Toute ressource qui nécessite de redessiner ou de reconstruire le visage est rejetée.

Le workflow GitHub Actions exécute `RELIVE/scripts/verify-chama-portrait.sh` avant la compilation Android. Ce contrôle bloque une modification accidentelle du portrait ou l'affaiblissement des règles d'identité dans le manifeste.

## État d'implémentation connu

La version suivie sur la branche `relive-app` comprend une pièce 3D procédurale, un blocage géométrique du corps avec des gestes simples, un portrait original indépendant et la synthèse vocale Android. Ce blocage géométrique n'est pas encore un modèle humain de production ni un rig squelettique détaillé.

Le portrait reste statique. Les mouvements corporels sont provisoires. Il n'existe pas encore de modèle GLB/FBX de production, de rig corporel final, de clonage vocal intégré ni de lip-sync — ce dernier est volontairement interdit par la règle d'identité actuelle.

**Décision produit : le visage d'Espérance Ruchama Kefora reste strictement identique à la photo de référence. Seul le corps séparé peut évoluer.**
