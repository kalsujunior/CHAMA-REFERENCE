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

## Étapes techniques suivantes

1. Créer un état de monde partagé et un repère de coordonnées unique pour le salon et le corps.
2. Remplacer les cibles symboliques par des points d'ancrage validés dans la pièce.
3. Ajouter un contrôleur de locomotion : vitesse de marche, accélération, rotation du corps, arrêt, évitement des meubles et interpolation des pas.
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

Cette première brique est du Kotlin pur et n'ajoute aucune dépendance Android ou réseau. Elle n'a pas encore été compilée dans l'application ni validée sur appareil. La compilation Android finale reste réservée à la fin de l'intégration des fonctionnalités.
