const json = (value, status = 200, headers = {}) =>
  new Response(JSON.stringify(value), {
    status,
    headers: { "Content-Type": "application/json; charset=utf-8", "Cache-Control": "no-store", ...headers }
  });

const readGroqReply = (data) => {
  const value = data?.choices?.[0]?.message?.content;
  return typeof value === "string" ? value.trim() : "";
};

const readGeminiReply = (data) => {
  const parts = data?.candidates?.[0]?.content?.parts;
  return Array.isArray(parts)
    ? parts.map(part => typeof part.text === "string" ? part.text : "").join("").trim()
    : "";
};

async function askGroq(env, messages) {
  const response = await fetch(env.GROQ_API_URL || "https://api.groq.com/openai/v1/chat/completions", {
    method: "POST",
    headers: {
      "Authorization": "Bearer " + env.GROQ_API_KEY,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      model: env.GROQ_MODEL,
      messages,
      temperature: 0.7,
      max_tokens: 700
    })
  });
  if (!response.ok) throw new Error("Groq HTTP " + response.status);
  const data = await response.json();
  const reply = readGroqReply(data);
  if (!reply) throw new Error("Groq returned an empty reply");
  return reply;
}

async function askGemini(env, system, history, message) {
  const model = encodeURIComponent(env.GEMINI_MODEL);
  const endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" +
    model + ":generateContent?key=" + encodeURIComponent(env.GEMINI_API_KEY);
  const contents = [
    ...history.map(item => ({
      role: item.role === "assistant" ? "model" : "user",
      parts: [{ text: item.content }]
    })),
    { role: "user", parts: [{ text: message }] }
  ];
  const response = await fetch(endpoint, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      systemInstruction: { parts: [{ text: system }] },
      contents,
      generationConfig: { temperature: 0.7, maxOutputTokens: 700 }
    })
  });
  if (!response.ok) throw new Error("Gemini HTTP " + response.status);
  const data = await response.json();
  const reply = readGeminiReply(data);
  if (!reply) throw new Error("Gemini returned an empty reply");
  return reply;
}

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
        headers: { ...cors, "Access-Control-Allow-Methods": "POST, OPTIONS", "Access-Control-Allow-Headers": "Content-Type, X-RELIVE-ACCESS", "Access-Control-Max-Age": "86400" }
      });
    }
    if (request.method !== "POST") return json({ error: "Method not allowed" }, 405, cors);

    const hasGroq = Boolean(env.GROQ_API_KEY && env.GROQ_MODEL);
    const hasGemini = Boolean(env.GEMINI_API_KEY && env.GEMINI_MODEL);
    if ((!hasGroq && !hasGemini) || !env.RELIVE_ACCESS_TOKEN || !env.RATE_LIMITER) {
      return json({ error: "Backend is not fully configured" }, 503, cors);
    }
    if (request.headers.get("X-RELIVE-ACCESS") !== env.RELIVE_ACCESS_TOKEN) {
      return json({ error: "Unauthorized" }, 401, cors);
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

    const providers = [];
    if (hasGroq) providers.push({ name: "groq", run: () => askGroq(env, [{ role: "system", content: system }, ...history, { role: "user", content: message }]) });
    if (hasGemini) providers.push({ name: "gemini", run: () => askGemini(env, system, history, message) });

    const failures = [];
    for (const provider of providers) {
      try {
        const reply = await provider.run();
        return json({ reply, provider: provider.name }, 200, cors);
      } catch (_) {
        failures.push(provider.name);
      }
    }

    return json({
      error: "AI providers unavailable",
      providersAttempted: failures
    }, 502, cors);
  }
};
