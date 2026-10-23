import { test } from "node:test";
import assert from "node:assert/strict";
import { createSearchHandler, offerFromResult } from "./handler.ts";

const request = (body: unknown = { query: "Switch OLED" }) => new Request("https://example.com/search-products", {
  method: "POST", body: JSON.stringify(body), headers: { "Content-Type": "application/json" },
});
const result = { title: "Nintendo Switch OLED", snippet: "Consola", url: "https://store.example/switch" };
const make = (options = {}) => createSearchHandler({
  getApiKey: () => "test-only-key", fetcher: async () => Response.json({ results: [result] }), ...options,
});
test("transforma resultados sin inventar precios y conserva la fuente", async () => {
  let calledUrl = "";
  const handler = make({ fetcher: async (url: URL, init: RequestInit) => {
    calledUrl = String(url);
    assert.equal(new Headers(init.headers).get("X-API-Key"), "test-only-key");
    return Response.json({ results: [result] });
  } });
  const response = await handler(request({ query: "  Switch OLED  " }));
  assert.equal(response.status, 200);
  const payload = await response.json();
  assert.equal(payload.query, "Switch OLED");
  assert.equal(payload.offers[0].sourceUrl, result.url);
  assert.equal(payload.offers[0].store, "store.example");
  assert.equal(payload.offers[0].price, "Ver en fuente");
  assert.equal(new URL(calledUrl).searchParams.get("location"), "EC");
  assert.ok(!JSON.stringify(payload).includes("test-only-key"));
});
test("rechaza consultas inválidas sin consultar TinyFish", async () => {
  const handler = make({ fetcher: async () => { throw new Error("No debe llamar"); } });
  for (const body of [null, [], {}, { query: 1 }, { query: "x" }, { query: " " }, { query: "a".repeat(201) }]) {
    assert.equal((await handler(request(body))).status, 400);
  }
});
test("rechaza JSON malformado y cuerpos grandes", async () => {
  for (const body of ["{", JSON.stringify({ query: "a".repeat(5000) })]) {
    assert.equal((await make()(new Request("https://example.com", { method: "POST", body }))).status, 400);
  }
});
test("responde a OPTIONS y rechaza GET", async () => {
  const handler = make();
  assert.equal((await handler(new Request("https://example.com", { method: "OPTIONS" }))).status, 204);
  assert.equal((await handler(new Request("https://example.com"))).status, 405);
});
test("informa cuando falta el secreto", async () => {
  const response = await make({ getApiKey: () => undefined })(request());
  assert.equal(response.status, 503);
  assert.equal((await response.json()).code, "TINYFISH_NOT_CONFIGURED");
});
test("maneja fallos HTTP, JSON inválido y contrato inválido", async () => {
  for (const response of [new Response("private-detail", { status: 401 }), new Response("no-json"), Response.json({ unexpected: [] })]) {
    const reply = await make({ fetcher: async () => response })(request());
    assert.equal(reply.status, 502);
    assert.ok(!(await reply.text()).includes("private-detail"));
  }
});
test("filtra URLs peligrosas, credenciales y resultados malformados", () => {
  for (const url of ["javascript:alert(1)", "file:///secret", "https://user:pass@store.example", "bad-url"]) {
    assert.equal(offerFromResult({ ...result, url }, 1, "Switch"), null);
  }
  assert.equal(offerFromResult(null, 1, "Switch"), null);
});
test("limita a diez resultados y admite lista vacía", async () => {
  for (const count of [0, 15]) {
    const reply = await make({ fetcher: async () => Response.json({ results: Array(count).fill(result) }) })(request());
    assert.equal((await reply.json()).offers.length, Math.min(count, 10));
  }
});
test("la cuota se restablece pasado un minuto", async () => {
  let time = 100_000;
  const handler = make({ now: () => time });
  for (let i = 0; i < 20; i++) assert.equal((await handler(request())).status, 200);
  const limited = await handler(request());
  assert.equal(limited.status, 429);
  assert.equal(limited.headers.get("Retry-After"), "60");
  time += 60_001;
  assert.equal((await handler(request())).status, 200);
});
test("cancela al proveedor al agotar el tiempo", async () => {
  const handler = make({ timeoutMs: 10, fetcher: (_url: URL, init: RequestInit) => new Promise((_resolve, reject) => {
    init.signal?.addEventListener("abort", () => reject(new Error("timeout")), { once: true });
  }) });
  assert.equal((await handler(request())).status, 502);
});
