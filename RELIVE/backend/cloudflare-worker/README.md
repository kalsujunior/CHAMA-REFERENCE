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



## Voix personnalisée de Chama (optionnelle)

Le Worker expose aussi `POST /speech`. Cette route est protégée par le même secret `RELIVE_ACCESS_TOKEN` et le limiteur `RATE_LIMITER`. Elle accepte uniquement le français (`fr-FR`) et renvoie de l'audio MP3. Les secrets suivants sont requis pour activer le fournisseur vocal :

Secrets Cloudflare :
- `ELEVENLABS_API_KEY` : clé privée du compte vocal.
- `CHAMA_VOICE_ID` : identifiant de la voix créée avec l'autorisation explicite de sa locutrice.

Variable texte facultative :
- `CHAMA_VOICE_MODEL` : modèle vocal compatible ; valeur par défaut `eleven_multilingual_v2`.

Ne jamais intégrer ces valeurs dans Android ou dans Git. La voix personnalisée reste désactivée par défaut dans RELIVE > Profil. Lorsqu'elle est activée, le texte de réponse lu est transmis au Worker et au fournisseur vocal. Le TTS Android reste le repli si le service échoue. Le créole haïtien continue d'utiliser la synthèse Android locale.

**Consentement obligatoire :** ne créer ou utiliser une voix synthétique qu'avec l'autorisation explicite de la personne dont la voix est enregistrée. L'intégration du code ne signifie pas qu'un clone vocal a déjà été créé ou déployé. Voir `RELIVE/VOICE_SETUP.md` pour les étapes.


## Protection des requêtes

Chaque adresse IP est désormais soumise à la limite `RATE_LIMITER` avant la vérification du code partagé. Les tentatives de code erroné consomment donc elles aussi le quota et ne peuvent pas contourner la limitation pour essayer des jetons en boucle. Les requêtes restent limitées à 24 Ko déclarés et les messages à 6 000 caractères.
