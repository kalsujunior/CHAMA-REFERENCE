# RELIVE API — Cloudflare Worker

Worker déployé : https://relive-chama-api.kalsujunior.workers.dev

## Fournisseurs IA avec basculement automatique

Le Worker utilise GroqCloud en priorité, puis Google Gemini si Groq échoue (quota atteint, erreur HTTP ou indisponibilité réseau). Si Groq n'est pas configuré, Gemini est utilisé seul. Si les deux échouent, l'application reçoit une erreur et peut conserver son mode local.

Créer les clés :
- GroqCloud : https://console.groq.com/keys
- Google AI Studio : https://aistudio.google.com/apikey

Dans Cloudflare > Worker `relive-chama-api` > Settings > Variables and Secrets, configurer :

Secrets :
- `GROQ_API_KEY` : clé secrète GroqCloud.
- `GEMINI_API_KEY` : clé secrète Google AI Studio.
- `RELIVE_ACCESS_TOKEN` : code privé long choisi par le propriétaire.

Variables texte :
- `GROQ_MODEL` (facultatif) : modèle par défaut `openai/gpt-oss-20b` ; peut être remplacé par un modèle disponible.
- `GEMINI_MODEL` (facultatif) : modèle par défaut `gemini-3.8-flash` ; vérifier que ce modèle est proposé dans le quota gratuit du compte.
- `GROQ_API_URL` (facultatif) : par défaut https://api.groq.com/openai/v1/chat/completions.

Les clés des fournisseurs restent exclusivement dans les secrets Cloudflare. Ne jamais les placer dans Android, GitHub ou une conversation. Les quotas gratuits sont limités et peuvent changer selon le fournisseur, le modèle et la région.

Dans RELIVE > Profil, saisir exactement le même `RELIVE_ACCESS_TOKEN` et activer le consentement IA distant. La limite existante RATE_LIMITER (15 requêtes par IP et par 60 secondes) est conservée.

Le Worker accepte une configuration partielle : un seul fournisseur suffit pour démarrer. Le basculement automatique est actif dès que les deux fournisseurs sont configurés.


## Voix personnalisée de Chama (ElevenLabs)

L'application possède un endpoint vocal séparé : `POST /speech`. Il reçoit le texte et la langue, vérifie le même en-tête `X-RELIVE-ACCESS`, applique le limiteur de requêtes, puis relaie le texte vers ElevenLabs. La clé ElevenLabs n'est jamais intégrée à l'APK ni au dépôt.

Configurer dans Cloudflare > Worker `relive-chama-api` > Settings > Variables and Secrets :

Secrets :
- `ELEVENLABS_API_KEY` : clé API ElevenLabs.
- `CHAMA_VOICE_ID` : identifiant de la voix clonée après sa création.

Variable texte :
- `CHAMA_VOICE_MODEL` (facultatif) : modèle de synthèse, par défaut `eleven_v4`.

Le mode vocal personnalisé est désactivé par défaut dans RELIVE et doit être activé dans Profil. Le texte des réponses est alors envoyé au Worker puis au fournisseur vocal. L'application retombe automatiquement sur la synthèse Android si le service n'est pas configuré, si le réseau est indisponible ou si l'appel échoue.

Important : le client n'active actuellement la voix personnalisée que pour le français (`fr-FR`). Le créole haïtien utilise la synthèse Android jusqu'à ce qu'une prise en charge vocale de qualité soit testée et validée. La liste officielle des langues de Eleven v4 comprend le français mais ne cite pas le créole haïtien.

Tests automatiques du Worker (Node.js 22 ou supérieur) :
```bash
npm test
```

Test manuel après configuration :
```bash
curl -X POST "https://relive-chama-api.kalsujunior.workers.dev/speech" \
  -H "X-RELIVE-ACCESS: VOTRE_CODE_RELIVE" \
  -H "Content-Type: application/json" \
  -H "Accept: audio/mpeg" \
  -d '{"text":"Bonjour, je suis Chama. Heureuse de te retrouver.","language":"fr-FR"}' \
  --output chama-test.mp3
```

Ne partage jamais les clés API ni le code d'accès dans un dépôt public ou une conversation.
