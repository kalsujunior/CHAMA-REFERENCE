# RELIVE — Finaliser la voix de Chama

Ce guide décrit les seules étapes qui nécessitent encore l'enregistrement de la locutrice et les accès du propriétaire.

## 1. Préparer l'échantillon vocal

Demander à la locutrice un enregistrement continu d'environ 1 à 2 minutes :
- une seule personne parle, sans musique ni autre voix ;
- pièce calme, téléphone à environ 15 à 25 cm de la bouche ;
- débit naturel, volume régulier et ton habituel ;
- phrases variées, avec des pauses naturelles ;
- éviter les cris, chuchotements, rires prolongés et bruits de fond ;
- privilégier WAV ou MP3 propre ; conserver l'original sans filtre agressif.

L'autorisation de la personne doit couvrir explicitement la création et l'utilisation de sa voix synthétique dans RELIVE. Ne pas enregistrer de mots de passe, données bancaires ou informations privées.

## 2. Créer la voix chez ElevenLabs

1. Ouvrir https://elevenlabs.io/app/voice-lab.
2. Se connecter et choisir Instant Voice Cloning si cette option est disponible sur le compte.
3. Nommer la voix « Chama — Espérance Ruchama Kefora ».
4. Importer l'échantillon propre et confirmer que la locutrice a autorisé le clonage.
5. Enregistrer la voix et copier son identifiant `voice_id`.
6. Créer une clé API depuis les paramètres développeur du compte.

La création effective du clone dépend du compte et des conditions du fournisseur ; elle n'est pas exécutée par le code Android.

## 3. Configurer Cloudflare

Depuis le dossier `RELIVE/backend/cloudflare-worker`, utiliser Wrangler authentifié sur le compte qui héberge `relive-chama-api` :

```bash
npx wrangler secret put ELEVENLABS_API_KEY
npx wrangler secret put CHAMA_VOICE_ID
npx wrangler deploy
```

À chaque commande `secret put`, Wrangler demande la valeur de manière interactive. Ne pas coller les clés dans le dépôt, dans le code Android ou dans un chat.

Le Worker conserve aussi le secret existant `RELIVE_ACCESS_TOKEN`, les clés IA déjà configurées et le limiteur `RATE_LIMITER`. Vérifier qu'ils sont toujours présents après le déploiement.

## 4. Activer dans RELIVE

1. Installer la version Android construite depuis la branche contenant l'intégration.
2. Ouvrir Profil et saisir le même code `RELIVE_ACCESS_TOKEN` déjà configuré côté Worker.
3. Activer « Voix personnalisée de Chama (français) ».
4. Activer « Activer la lecture vocale ».
5. Envoyer une phrase courte pour vérifier le résultat.
6. Si le service n'est pas disponible, RELIVE doit reprendre la voix Android.

## 5. Langues et limites

- Français : endpoint ElevenLabs activé, une fois la voix créée et le Worker configuré.
- Créole haïtien : synthèse Android locale pour le moment. La langue n'apparaît pas dans la liste officielle des langues Eleven v4 consultée lors de l'intégration ; ne pas promettre une imitation fidèle avant un test dédié.
- Synchronisation labiale : l'avatar actuel utilise une photo 2D fixe. Une vraie animation de bouche synchronisée nécessite un avatar animé/riggé ou une technologie dédiée ; cette intégration audio ne la crée pas.
- La voix distante nécessite Internet et peut consommer les crédits du compte fournisseur.

## 6. Vérification

Dans le dossier Worker :

```bash
npm test
```

Puis compiler Android depuis le dossier `RELIVE` :

```bash
gradle assembleDebug
```

APK attendu : `app/build/outputs/apk/debug/app-debug.apk`.
