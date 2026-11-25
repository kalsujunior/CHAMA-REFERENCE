const json = (value, status = 200, headers = {}) =>
  new Response(JSON.stringify(value), {
    status,
    headers: { "Content-Type": "application/json; charset=utf-8", "Cache-Control": "no-store", ...headers }
  });

export default {
  async fetch(request, env) {
    const origin = request.headers.get("Origin");
    const allowedOrigin = (env.ALLOWED_ORIGIN || "").trim();
    const cors = origin && allowedOrigin && origin === allowedOrigin
      ? { "Access-Control-Allow-Origin": allowedOrigin, "Vary": "Origin" }
      : {};

    if (request.method === "OPTIONS") {
      if (!origin || !allowedOrigin || origin !== allowedOrigin) return new Response(null, { status: 403 });
      return new Response(null, {
        status: 204,
        headers: { ...cors, "Access-Control-Allow-Methods": "POST, OPTIONS", "Access-Control-Allow-Headers": "Content-Type", "Access-Control-Max-Age": "86400" }
      });
    }
    if (request.method !== "POST") return json({ error: "Method not allowed" }, 405, cors);
    if (!env.AI_API_KEY || !env.AI_MODEL || !env.RATE_LIMITER) {
      return json({ error: "Backend is not fully configured" }, 503, cors);
    }

    const ip = request.headers.get("CF-Connecting-IP") || "unknown";
    try {
      const result = await env.RATE_LIMITER.limit({ key: ip });
      if (!result.success) return json({ error: "Rate limit exceeded" }, 429, cors);
    } catch (_) {
      return json({ error: "Rate limiter unavailable" }, 503, cors);
    }

    const length = Number(request.headers.get("Content-Length") || "0");
    if (length > 24000) return json({ error: "Request too large" }, 413, cors);

    let body;
    try { body = await request.json(); }
    catch (_) { return json({ error: "Invalid JSON" }, 400, cors); }

    const message = typeof body.message === "string" ? body.message.trim() : "";
    if (!message || message.length > 6000) return json({ error: "Message is empty or too long" }, 400, cors);

    const language = typeof body.language === "string" ? body.language.slice(0, 16) : "fr-FR";
    const memories = Array.isArray(body.memories)
      ? body.memories.filter(value => typeof value === "string").slice(-20).map(value => value.slice(0, 500))
      : [];
    const history = Array.isArray(body.history)
      ? body.history.slice(-20).filter(item => item && typeof item.text === "string")
          .map(item => ({ role: item.author === "Chama" ? "assistant" : "user", content: item.text.slice(0, 2000) }))
      : [];

    const system = [
      "Tu es Chama, compagne virtuelle de RELIVE. Tu es chaleureuse, respectueuse, honnête et claire.",
      "Tu ne prétends pas être une personne humaine ni accéder à des données qui ne sont pas fournies.",
      "Réponds dans la langue demandée : " + language + ".",
      "Utilise les souvenirs comme contexte sans inventer ni transformer une supposition en fait.",
      memories.length
        ? "Souvenirs explicitement enregistrés par l'utilisateur :\n" + memories.map((item, index) => (index + 1) + ". " + item).join("\n")
        : "Aucun souvenir personnel n'a été fourni."
    ].join("\n\n");

    const endpoint = env.AI_API_URL || "https://api.openai.com/v1/chat/completions";
    let upstream;
    try {
      upstream = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Authorization": "Bearer " + env.AI_API_KEY,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          model: env.AI_MODEL,
          messages: [{ role: "system", content: system }, ...history, { role: "user", content: message }],
          temperature: 0.7,
          max_tokens: 700
        })
      });
    } catch (_) {
      return json({ error: "AI provider unavailable" }, 502, cors);
    }
    if (!upstream.ok) return json({ error: "AI provider request failed" }, 502, cors);

    let response;
    try { response = await upstream.json(); }
    catch (_) { return json({ error: "Invalid AI provider response" }, 502, cors); }
    const reply = response && response.choices && response.choices[0] && response.choices[0].message
      ? response.choices[0].message.content
      : null;
    if (typeof reply !== "string" || !reply.trim()) return json({ error: "AI provider returned no reply" }, 502, cors);
    return json({ reply: reply.trim() }, 200, cors);
  }
};
