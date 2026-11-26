# RELIVE — Activer la voix personnalisée de Chama

L'intégration Android et le point d'accès Worker sont préparés, mais la voix reste désactivée par défaut. Aucun clone vocal n'est créé par le dépôt.

## Condition indispensable : autorisation de la locutrice

N'utiliser qu'un enregistrement fourni par la personne qui parle, avec son autorisation explicite pour créer une voix synthétique et l'utiliser dans RELIVE. Conserver une trace de cette autorisation. Ne pas cloner la voix d'une autre personne à partir d'un extrait trouvé en ligne ou sans son accord.

Pour préparer un échantillon, enregistrer environ 1 à 2 minutes de parole naturelle dans une pièce calme, sans musique ni voix superposée. Conserver l'enregistrement original. Ne pas inclure de données privées.

## Créer la voix auprès du fournisseur

1. Se connecter au compte du fournisseur vocal choisi et vérifier ses conditions, ses coûts et les droits d'utilisation.
2. Créer une voix personnalisée uniquement après avoir vérifié le consentement explicite de la locutrice.
3. Enregistrer l'identifiant de voix fourni. Ne pas le mettre dans l'application Android ni dans GitHub.

## Configurer Cloudflare

Dans le dossier `RELIVE/backend/cloudflare-worker`, avec Wrangler connecté au compte Cloudflare qui héberge `relive-chama-api`, exécuter séparément :

```bash
npx wrangler secret put ELEVENLABS_API_KEY
npx wrangler secret put CHAMA_VOICE_ID
```

Wrangler demande chaque valeur de façon interactive. Ne pas inscrire les valeurs dans le dépôt, dans le code Android ou dans une conversation. Le Worker conserve également les secrets existants `RELIVE_ACCESS_TOKEN`, `GROQ_API_KEY` et `GEMINI_API_KEY`, ainsi que le binding `RATE_LIMITER`.

Le modèle vocal par défaut est `eleven_multilingual_v2`. Il peut être remplacé par la variable texte `CHAMA_VOICE_MODEL` si le compte dispose d'un autre modèle compatible.

Déployer le Worker seulement après avoir vérifié que tous les secrets requis sont présents :

```bash
npx wrangler deploy
```

## Activer dans RELIVE

1. Installer une version Android compilée depuis la branche qui contient l'intégration.
2. Ouvrir Profil.
3. Saisir le même code partagé que le secret Cloudflare `RELIVE_ACCESS_TOKEN`.
4. Choisir Français.
5. Activer « Voix personnalisée de Chama (français) » et « Activer la lecture vocale ».
6. Enregistrer les préférences.

L'option personnalisée est désactivée par défaut. Le texte des réponses lues est envoyé au Worker puis au fournisseur vocal. La voix Android locale reste le repli automatique si le réseau ou le fournisseur est indisponible. La lecture personnalisée est limitée au français ; en créole haïtien, RELIVE utilise la synthèse Android locale.

## Limites

- L'intégration ne crée pas elle-même la voix : la locutrice doit fournir un échantillon et consentir à son clonage.
- Le service distant nécessite Internet et peut entraîner des coûts ou consommer des crédits.
- Le portrait de Chama reste une image fixe, inchangée. La voix ne modifie pas les traits du visage et ne crée pas de synchronisation labiale.
- Aucune clé fournisseur ne doit être embarquée dans l'APK.
