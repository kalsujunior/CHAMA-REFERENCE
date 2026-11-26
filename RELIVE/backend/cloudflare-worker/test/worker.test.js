import test from "node:test";
import assert from "node:assert/strict";
import worker from "../src/index.js";

const TOKEN = "test-relive-token";

function env(overrides = {}) {
  return {
    RELIVE_ACCESS_TOKEN: TOKEN,
    RATE_LIMITER: { limit: async () => ({ success: true }) },
    ...overrides
  };
}

function request(path, body, token = TOKEN) {
  return new Request("https://relive.example" + path, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-RELIVE-ACCESS": token,
      "CF-Connecting-IP": "192.0.2.10"
    },
    body: JSON.stringify(body)
  });
}

test("speech rejects missing or invalid shared token", async () => {
  let limited = false;
  const bindings = env({
    ELEVENLABS_API_KEY: "test-provider-key",
    CHAMA_VOICE_ID: "voice-test",
    RATE_LIMITER: { limit: async () => { limited = true; return { success: true }; } }
  });
  const response = await worker.fetch(request("/speech", { text: "Bonjour" }, "wrong"), bindings);
  assert.equal(response.status, 401);
  assert.equal(limited, true);
});

test("speech accepts only French", async () => {
  const response = await worker.fetch(
    request("/speech", { text: "Bonjou", language: "ht-HT" }),
    env({ ELEVENLABS_API_KEY: "test-provider-key", CHAMA_VOICE_ID: "voice-test" })
  );
  assert.equal(response.status, 422);
});

test("speech reports missing voice configuration without contacting provider", async () => {
  const originalFetch = globalThis.fetch;
  globalThis.fetch = async () => { throw new Error("provider must not be called"); };
  try {
    const response = await worker.fetch(
      request("/speech", { text: "Bonjour", language: "fr-FR" }),
      env()
    );
    assert.equal(response.status, 503);
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("speech proxies MP3 while keeping provider key on the Worker", async () => {
  const originalFetch = globalThis.fetch;
  let providerUrl = "";
  let providerHeaders;
  let providerPayload;
  globalThis.fetch = async (url, options) => {
    providerUrl = String(url);
    providerHeaders = options.headers;
    providerPayload = JSON.parse(options.body);
    return new Response(new Uint8Array([1, 2, 3, 4]), {
      status: 200,
      headers: { "Content-Type": "audio/mpeg", "Content-Length": "4" }
    });
  };
  try {
    const response = await worker.fetch(
      request("/speech", { text: "Bonjour Chama", language: "fr-FR" }),
      env({
        ELEVENLABS_API_KEY: "provider-secret",
        CHAMA_VOICE_ID: "voice-test"
      })
    );
    assert.equal(response.status, 200);
    assert.match(response.headers.get("Content-Type"), /audio\/mpeg/);
    assert.equal(providerHeaders["xi-api-key"], "provider-secret");
    assert.match(providerUrl, /voice-test/);
    assert.equal(providerPayload.text, "Bonjour Chama");
    assert.equal(providerPayload.model_id, "eleven_multilingual_v2");
    assert.deepEqual([...new Uint8Array(await response.arrayBuffer())], [1, 2, 3, 4]);
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("chat endpoint remains available after adding speech route", async () => {
  const originalFetch = globalThis.fetch;
  let sentMessages;
  globalThis.fetch = async (_url, options) => {
    sentMessages = JSON.parse(options.body).messages;
    return new Response(JSON.stringify({
      choices: [{ message: { content: "Bonjour, je suis Chama." } }]
    }), { status: 200, headers: { "Content-Type": "application/json" } });
  };
  try {
    const response = await worker.fetch(
      request("/chat", { message: "Bonjour", language: "fr-FR", memories: [], history: [] }),
      env({ GROQ_API_KEY: "groq-test", GROQ_MODEL: "test-model" })
    );
    assert.equal(response.status, 200);
    const data = await response.json();
    assert.equal(data.reply, "Bonjour, je suis Chama.");
    assert.ok(sentMessages[0].content.includes("Espérance Ruchama Kefora"));
  } finally {
    globalThis.fetch = originalFetch;
  }
});
