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
