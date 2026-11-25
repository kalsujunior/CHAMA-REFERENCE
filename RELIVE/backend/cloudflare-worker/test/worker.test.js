import test from "node:test";
import assert from "node:assert/strict";
import worker from "../src/index.js";

const baseEnv = () => ({
  RELIVE_ACCESS_TOKEN: "test-relive-token",
  ELEVENLABS_API_KEY: "test-elevenlabs-key",
  CHAMA_VOICE_ID: "test-voice-id",
  CHAMA_VOICE_MODEL: "eleven_v4",
  RATE_LIMITER: { limit: async () => ({ success: true }) }
});

const request = (body, headers = {}) => new Request("https://relive.test/speech", {
  method: "POST",
  headers: { "Content-Type": "application/json", ...headers },
  body: JSON.stringify(body)
});

test("speech endpoint rejects requests without RELIVE access token", async () => {
  const response = await worker.fetch(request({ text: "Bonjour", language: "fr-FR" }), baseEnv());
  assert.equal(response.status, 401);
});

test("speech endpoint rejects languages not yet enabled", async () => {
  const response = await worker.fetch(
    request({ text: "Bonjou", language: "ht-HT" }, { "X-RELIVE-ACCESS": "test-relive-token" }),
    baseEnv()
  );
  assert.equal(response.status, 422);
});

test("speech endpoint returns provider audio and keeps the API key server-side", async () => {
  const originalFetch = globalThis.fetch;
  let providerRequest;
  globalThis.fetch = async (url, init) => {
    providerRequest = { url: String(url), headers: init.headers, body: JSON.parse(init.body) };
    return new Response(new Uint8Array([1, 2, 3, 4]), {
      status: 200,
      headers: { "Content-Type": "audio/mpeg" }
    });
  };

  try {
    const response = await worker.fetch(
      request({ text: "Bonjour, je suis Chama.", language: "fr-FR" }, { "X-RELIVE-ACCESS": "test-relive-token" }),
      baseEnv()
    );
    assert.equal(response.status, 200);
    assert.equal(response.headers.get("Content-Type"), "audio/mpeg");
    assert.deepEqual(Array.from(new Uint8Array(await response.arrayBuffer())), [1, 2, 3, 4]);
    assert.match(providerRequest.url, /text-to-speech\/test-voice-id/);
    assert.equal(providerRequest.headers["xi-api-key"], "test-elevenlabs-key");
    assert.equal(providerRequest.body.model_id, "eleven_v4");
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("speech endpoint reports missing provider configuration without exposing secrets", async () => {
  const env = baseEnv();
  delete env.ELEVENLABS_API_KEY;
  const response = await worker.fetch(
    request({ text: "Bonjour", language: "fr-FR" }, { "X-RELIVE-ACCESS": "test-relive-token" }),
    env
  );
  assert.equal(response.status, 503);
  assert.deepEqual(await response.json(), { error: "Personalized voice is not configured" });
});
