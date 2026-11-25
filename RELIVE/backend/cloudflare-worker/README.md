# RELIVE API — Cloudflare Worker

The Worker is deployed at https://relive-chama-api.kalsujunior.workers.dev. These steps configure the deployed Worker; they do not create another Worker.

## Required configuration
1. In Cloudflare, add the provider key as a secret named `AI_API_KEY`.
2. Create a long random private access code and add it as a secret named `RELIVE_ACCESS_TOKEN`.
3. Set `AI_MODEL` to a model enabled for your provider account.
4. Optionally set `AI_API_URL` to an OpenAI-compatible Chat Completions endpoint.
5. In RELIVE > Profil, enter the exact same private access code and save preferences.
6. Keep the existing `RATE_LIMITER` binding (15 requests per IP per 60 seconds). Adjust only if needed.
7. Test POST /chat over HTTPS before enabling remote AI in RELIVE.

The Worker requires `AI_API_KEY`, `AI_MODEL`, `RELIVE_ACCESS_TOKEN` and `RATE_LIMITER`. The access code is checked before the Worker forwards any prompt to the AI provider. IP rate limiting is an additional abuse-control layer, not a replacement for user accounts. The provider key stays in Cloudflare secrets and must never be put in the Android app or Git repository.
