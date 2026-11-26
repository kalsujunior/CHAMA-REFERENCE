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

L'option d'autonomie reste désactivée par défaut. Le bouton « Autonomie libre » permet de l'activer explicitement dans l'espace de Chama.

## Contrat avec le moteur 3D

`ChamaAutonomyTarget` est sémantique : WINDOW, FLOWERS, SOFA et ROOM_CENTER. Le moteur 3D doit résoudre ces cibles vers des coordonnées réellement présentes dans la scène et construire un chemin navigable.

`ChamaBody3D.kt` expose maintenant `SceneScope.ChamaBodyNodes`. Le corps sans tête est placé sous un `Node` racine dont la position X/Z et le cap Y proviennent du contrôleur. `ChamaRoom3D.kt` déclare ce corps dans le même bloc `SceneView` que les murs et les meubles : le salon et le personnage partagent donc désormais le même graphe de scène et le même repère spatial. Le portrait original demeure un calque Compose indépendant.


## Graphe de navigation du salon

`avatar/ChamaRoomNavigation.kt` ajoute un premier graphe de points de passage au sol : centre, couloir gauche, arrêt près de la fenêtre, arrêt près du canapé, couloir droit et arrêt près des fleurs. Un calcul de plus court chemin relie ces points en contournant le canapé et la table basse. Les coordonnées sont exprimées dans le repère X/Z du salon actuel. Chaque arête proposée est maintenant filtrée par la carte de collision statique.

`avatar/ChamaRoomCollisionMap.kt` décrit les empreintes du canapé, de la table basse, de la console et du pot de plante. Elle agrandit ces obstacles du rayon estimé du personnage (0,38 m) et d'une marge supplémentaire (0,07 m), puis échantillonne les segments de trajet tous les 5 cm. Le coordinateur vérifie également le segment entre la position courante et le premier point du nouvel itinéraire avant de lancer la simulation, puis contrôle le déplacement entre la pose précédente et chaque nouvelle trame. Si un segment est refusé, le contrôleur revient à la dernière pose sûre et interrompt le trajet. Les points ont été recalés à partir des dimensions et positions visibles dans `ChamaRoom3D.kt` : le couloir gauche passe à l'extérieur de l'accoudoir du canapé, le couloir droit reste devant la table basse et l'approche des fleurs évite la console. Une vérification géométrique des cinq segments du graphe confirme qu'ils restent dans la zone marchable modélisée. Ces marges restent des estimations du blockout ; elles ne remplacent pas une validation de collision continue avec le volume réel du personnage.

`avatar/ChamaLocomotionController.kt` ajoute maintenant une simulation de déplacement indépendante du rendu :

- suit une suite de points X/Z et corrige le départ si la position réelle diffère du premier point de la route ;
- limite la vitesse, applique une accélération et un freinage fondé sur la distance restante ;
- tourne progressivement vers le prochain segment avant d'avancer ;
- produit une phase de marche basée sur la distance réellement parcourue ;
- plafonne le temps simulé par mise à jour pour éviter les téléportations après un ralentissement de l'interface ;
- annule immédiatement le trajet lorsque l'autonomie est désactivée, qu'une interaction utilisateur commence ou que l'économie d'énergie est activée.

`avatar/ChamaAutonomyRuntime.kt` relie le planificateur, le graphe et le contrôleur : une décision devient un itinéraire, l'arrivée met à jour le dernier point de passage connu, et une interruption annule le trajet. `avatar/ChamaAutonomyNeeds.kt` fait évoluer localement l'énergie et la curiosité : la marche et la danse consomment de l'énergie, le repos au canapé en récupère, et l'exploration entretient la curiosité. `VirtualSpaceScreen.kt` appelle le runtime à 20 Hz lorsque l'autonomie est activée, puis transmet la position et l'orientation au `Node` racine du corps. Le geste `WALK` anime les bras et les jambes pendant le déplacement. À l'arrivée, Chama oriente son corps vers la fenêtre, le vase de fleurs ou le canapé ; le salon met en valeur les fleurs ou les coussins lorsqu'elle s'y arrête. L'utilisateur peut arrêter l'autonomie par le bouton dédié, par une interaction avec la scène ou par les commandes manuelles. Le mode économie d'énergie Android suspend aussi la locomotion. L'indicateur d'énergie est visible pendant l'autonomie. L'autonomie reste désactivée à l'ouverture de l'écran.

**Limites importantes :** la carte de collision est un préfiltre statique et le contrôleur suit des segments géométriquement validés ; il n'existe pas encore de physique dynamique ni de détection continue des volumes articulés du personnage à chaque frame. Le corps reste un blockout de primitives, pas un squelette skinné. La danse sur place est disponible, mais s'asseoir, se lever et manipuler physiquement les objets ne sont pas encore implémentés. La validation visuelle et les collisions réelles restent à effectuer sur téléphone.

## Étapes techniques suivantes

1. Valider visuellement sur téléphone les proportions du personnage, les distances de marche et le cadrage de la caméra ; recalibrer les empreintes du mobilier si nécessaire.
2. Ajouter une détection continue de collision tenant compte du volume du corps et des bras, puis des obstacles dynamiques.
3. Ajouter les interactions de proximité : s'arrêter devant la fenêtre, regarder les fleurs et s'approcher du canapé sans le traverser.
4. Ajouter l'assise et le lever lorsque le bassin, les genoux et les chevilles seront pilotés par un rig cohérent.
5. Ajouter des activités autonomes supplémentaires et une mémoire locale des préférences/activités, sans autoriser d'action hors du salon.
6. Mesurer la mémoire, la température et les images par seconde sur le Mhorse S22 Max ; adapter la fréquence et suspendre le travail lorsque l'écran est masqué ou l'application en arrière-plan.

## Règles de sécurité et de contrôle

- L'utilisateur peut désactiver ou interrompre l'autonomie à tout moment.
- Une interaction directe de l'utilisateur a toujours la priorité sur une activité autonome.
- Le planificateur ne peut pas initier de conversation vocale, activer le microphone, envoyer des données ou effectuer des achats.
- Aucun déplacement ne doit sortir des limites de la pièce ni traverser les meubles.
- L'économie d'énergie suspend la locomotion autonome.
- Le portrait photographique reste un calque indépendant, statique et immuable. Aucune animation faciale, rotation de tête ou synchronisation labiale n'est autorisée.
- Les intentions du planificateur ne sont pas présentées comme des actions exécutées tant que le moteur de locomotion ne les a pas réellement terminées.

## Validation

Le planificateur, les besoins, le graphe, la carte de collision, le contrôleur et le coordinateur restent en Kotlin pur. `ChamaAutonomyTest.kt` ajoute dix tests JUnit couvrant les itinéraires, les obstacles, le point de reprise, l'arrivée, l'orientation vers les objets, l'interruption utilisateur, la replanification et l'évolution de l'énergie. Ces tests ont été ajoutés mais ne sont pas encore exécutés. Aucune compilation Android ni validation sur appareil n'a été lancée ; la validation finale devra vérifier la scène partagée, les performances et les collisions sur le téléphone.
