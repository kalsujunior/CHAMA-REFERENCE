# RELIVE API — Cloudflare Worker

This is a deployment template. Committing it does not deploy a server.

## Configuration
1. Create a Cloudflare Worker and deploy this folder.
2. Configure a rate-limit binding named RATE_LIMITER. The included Wrangler example is 15 requests per IP per 60 seconds; adjust it to your needs.
3. Add the provider key as a Cloudflare secret: `npx wrangler secret put AI_API_KEY`.
4. Create a long random private access code and add it as a Cloudflare secret: `npx wrangler secret put RELIVE_ACCESS_TOKEN`. Enter the same code in RELIVE > Profil. The Android client stores it encrypted and sends it only to this HTTPS Worker.
5. Set AI_MODEL to a model enabled for your provider account.
5. Optionally set AI_API_URL to an OpenAI-compatible Chat Completions endpoint.
6. Set ALLOWED_ORIGIN to the exact web origin only if a browser frontend calls the Worker. Native Android requests do not send an Origin header.
7. Test POST /chat over HTTPS before enabling remote AI in RELIVE.

The Worker requires AI_API_KEY, AI_MODEL and RATE_LIMITER. It limits request size and returns sanitized provider errors. IP rate limiting is basic abuse protection, not user authentication. Do not treat this template as a complete production account system.
