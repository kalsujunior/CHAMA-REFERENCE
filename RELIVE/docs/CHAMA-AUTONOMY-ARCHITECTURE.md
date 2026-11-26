# RELIVE — architecture de l'autonomie de Chama

## Objectif

Préparer une autonomie locale, progressive et contrôlable pour Chama dans son propre espace virtuel. L'autonomie doit être un système de décision, pas une boucle d'animations aléatoires.

## Première brique livrée

`avatar/ChamaAutonomyPlanner.kt` fournit un planificateur Kotlin déterministe et indépendant de l'interface :

- choisit une intention parmi déplacement vers la fenêtre, les fleurs ou le canapé, observation, danse sur place et repos ;
- prend en compte l'énergie et la curiosité, bornées entre 0 et 1 ;
- conserve une décision pendant sa durée afin d'éviter les changements incessants ;
- interrompt immédiatement la décision lorsque l'autonomie est désactivée, que l'espace virtuel n'est plus visible, que l'utilisateur interagit ou que le mode économie d'énergie est actif ;
- ne déclenche ni voix, ni microphone, ni réseau, ni action hors de l'espace virtuel.

L'option d'autonomie doit rester désactivée par défaut jusqu'à ce qu'un contrôle utilisateur et les mécanismes de déplacement soient intégrés.

## Contrat avec le moteur 3D

`ChamaAutonomyTarget` est sémantique : WINDOW, FLOWERS, SOFA et ROOM_CENTER. Le moteur 3D doit résoudre ces cibles vers des coordonnées réellement présentes dans la scène et construire un chemin navigable.

**Limite actuelle :** le planificateur ne déplace pas encore le personnage. Dans la version existante, le salon et le corps de Chama sont rendus dans deux SceneView distinctes. Il faut d'abord réunir ou synchroniser ces scènes dans un même repère spatial avant de prétendre que Chama marche physiquement jusqu'aux meubles.


## Graphe de navigation du salon

`avatar/ChamaRoomNavigation.kt` ajoute un premier graphe de points de passage au sol : centre, couloir gauche, arrêt près de la fenêtre, arrêt près du canapé, couloir droit et arrêt près des fleurs. Un calcul de plus court chemin relie ces points en contournant le canapé et la table basse. Les coordonnées sont exprimées dans le repère X/Z du salon actuel.

Les points ont été recalés à partir des dimensions et positions visibles dans `ChamaRoom3D.kt` : le couloir gauche passe à l'extérieur de l'accoudoir du canapé, le couloir droit reste devant la table basse et l'approche des fleurs évite la console. Ces marges restent des estimations géométriques du blockout ; elles ne remplacent pas une validation de collision avec le volume réel du personnage.

`avatar/ChamaLocomotionController.kt` ajoute maintenant une simulation de déplacement indépendante du rendu :

- suit une suite de points X/Z et corrige le départ si la position réelle diffère du premier point de la route ;
- limite la vitesse, applique une accélération et un freinage fondé sur la distance restante ;
- tourne progressivement vers le prochain segment avant d'avancer ;
- produit une phase de marche basée sur la distance réellement parcourue ;
- plafonne le temps simulé par mise à jour pour éviter les téléportations après un ralentissement de l'interface ;
- annule immédiatement le trajet lorsque l'autonomie est désactivée, qu'une interaction utilisateur commence ou que l'économie d'énergie est activée.

**Limite importante :** le contrôleur calcule des trames de position et d'orientation ; il n'applique pas encore ces trames aux nœuds SceneView. Le salon et le corps sont toujours deux SceneView distinctes dans `VirtualSpaceScreen.kt`. Le mouvement visible exige donc encore un repère commun, un adaptateur de rendu et une validation des collisions sur l'appareil.

## Étapes techniques suivantes

1. Créer un état de monde partagé et un repère de coordonnées unique pour le salon et le corps.
2. Remplacer les cibles symboliques par des points d'ancrage validés dans la pièce.
3. Connecter `ChamaLocomotionController` à un repère commun et à un adaptateur SceneView ; ajouter validation des volumes, collisions et évitement dynamique des meubles.
4. Ajouter les interactions de proximité : regarder les fleurs, s'approcher du canapé et s'arrêter à une distance sûre.
5. Ajouter l'assise et le lever uniquement lorsque les articulations du bassin, des genoux et des chevilles sont pilotées par le même contrôleur.
6. Ajouter la persistance des préférences d'autonomie et une commande Pause/Reprendre visible.
7. Mesurer la mémoire, la température et les images par seconde sur le téléphone cible ; suspendre les calculs lorsque l'écran est masqué ou que l'application passe en arrière-plan.

## Règles de sécurité et de contrôle

- L'utilisateur peut désactiver ou interrompre l'autonomie à tout moment.
- Une interaction directe de l'utilisateur a toujours la priorité sur une activité autonome.
- Le planificateur ne peut pas initier de conversation vocale, activer le microphone, envoyer des données ou effectuer des achats.
- Aucun déplacement ne doit sortir des limites de la pièce ni traverser les meubles.
- L'économie d'énergie suspend la locomotion autonome.
- Le portrait photographique reste un calque indépendant, statique et immuable. Aucune animation faciale, rotation de tête ou synchronisation labiale n'est autorisée.
- Les intentions du planificateur ne sont pas présentées comme des actions exécutées tant que le moteur de locomotion ne les a pas réellement terminées.

## Validation

Le planificateur, le graphe et le contrôleur de locomotion sont écrits en Kotlin pur et n'ajoutent aucune dépendance Android ou réseau. Ils n'ont pas encore été compilés dans l'application ni validés sur appareil. La compilation Android finale reste réservée à la fin de l'intégration des fonctionnalités.
