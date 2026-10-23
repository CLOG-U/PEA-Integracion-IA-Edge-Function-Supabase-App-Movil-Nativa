// Adaptado del codelab de epanchanaf; la clave se utiliza solo en el servidor.
type Dependencies = {
  getApiKey: () => string | undefined;
  fetcher?: typeof fetch;
  now?: () => number;
  timeoutMs?: number;
};
const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Content-Type": "application/json; charset=utf-8",
  "Cache-Control": "no-store",
};
function json(body: unknown, status = 200, extraHeaders = {}) {
  return new Response(JSON.stringify(body), { status, headers: { ...corsHeaders, ...extraHeaders } });
}
function record(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}
function text(value: unknown, fallback: string, maxLength = 2000): string {
  return typeof value === "string" && value.trim() ? value.trim().slice(0, maxLength) : fallback;
}
export function offerFromResult(result: unknown, position: number, query: string) {
  if (!record(result) || typeof result.url !== "string") return null;
  let url: URL;
  try { url = new URL(result.url); } catch { return null; }
  if (!["http:", "https:"].includes(url.protocol) || !url.hostname || url.username || url.password) return null;
  return {
    id: `${position}-${url.href}`,
    store: text(result.site_name, url.hostname.replace(/^www\./, ""), 120),
    productName: text(result.title, query, 300),
    price: "Ver en fuente", originalPrice: null,
    shipping: "Consulta en tienda", delivery: "Disponibilidad por confirmar", rating: "—",
    sourceUrl: url.href, summary: text(result.snippet, "Resultado encontrado en la web."),
    tags: ["Resultado web"], isBestValue: false,
  };
}
async function readJson(request: Request): Promise<unknown> {
  const reader = request.body?.getReader();
  if (!reader) throw new Error("Empty body");
  const chunks: Uint8Array[] = [];
  let size = 0;
  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      size += value.byteLength;
      if (size > 4096) { await reader.cancel(); throw new Error("Body too large"); }
      chunks.push(value);
    }
  } finally { reader.releaseLock(); }
  const bytes = new Uint8Array(size);
  let offset = 0;
  for (const chunk of chunks) { bytes.set(chunk, offset); offset += chunk.byteLength; }
  return JSON.parse(new TextDecoder().decode(bytes));
}
export function createSearchHandler(dependencies: Dependencies) {
  const fetcher = dependencies.fetcher ?? fetch;
  const now = dependencies.now ?? Date.now;
  // Contador didáctico por instancia. No sustituye un límite distribuido ni una identidad.
  const requests = new Map<string, number[]>();
  return async (request: Request): Promise<Response> => {
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: corsHeaders });
    if (request.method !== "POST") return json({ error: "Usa una solicitud POST para buscar." }, 405, { Allow: "POST, OPTIONS" });
    const timestamp = now();
    for (const [key, times] of requests) {
      if (!times.some((time) => time > timestamp - 60_000)) requests.delete(key);
    }
    const ip = request.headers.get("x-forwarded-for")?.split(",")[0]?.trim() || "anonymous";
    const key = requests.has(ip) || requests.size < 1000 ? ip : "overflow";
    const recent = (requests.get(key) ?? []).filter((time) => time > timestamp - 60_000);
    if (recent.length >= 20) return json({ error: "Demasiadas búsquedas. Inténtalo en un minuto." }, 429, { "Retry-After": "60" });
    recent.push(timestamp);
    requests.set(key, recent);
    let query: string;
    try {
      const body = await readJson(request);
      query = record(body) && typeof body.query === "string" ? body.query.trim() : "";
    } catch {
      return json({ error: "El cuerpo debe ser JSON válido y no superar 4096 bytes." }, 400);
    }
    if (query.length < 2 || query.length > 200) return json({ error: "La búsqueda debe tener entre 2 y 200 caracteres." }, 400);
    const apiKey = dependencies.getApiKey();
    if (!apiKey) return json({ code: "TINYFISH_NOT_CONFIGURED", error: "La búsqueda en vivo aún no está configurada." }, 503);
    const searchUrl = new URL("https://api.search.tinyfish.ai");
    searchUrl.searchParams.set("query", query);
    searchUrl.searchParams.set("purpose", "Encontrar páginas de compra y ofertas relevantes.");
    searchUrl.searchParams.set("location", "EC");
    searchUrl.searchParams.set("language", "es");
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), dependencies.timeoutMs ?? 15_000);
    try {
      const upstream = await fetcher(searchUrl, { headers: { "X-API-Key": apiKey, Accept: "application/json" }, signal: controller.signal });
      if (!upstream.ok) {
        console.error("TinyFish search failed", { status: upstream.status });
        return json({ error: "No fue posible consultar las fuentes web ahora." }, 502);
      }
      const payload: unknown = await upstream.json();
      if (!record(payload) || !Array.isArray(payload.results)) return json({ error: "La fuente de búsqueda devolvió una respuesta no válida." }, 502);
      const offers = payload.results.map((result, index) => offerFromResult(result, index + 1, query)).filter((offer) => offer !== null).slice(0, 10);
      return json({ query, offers, searchedAt: new Date(now()).toISOString() });
    } catch {
      // Los errores del proveedor pueden contener datos privados: no se registran.
      console.error("TinyFish request failed or timed out");
      return json({ error: "La búsqueda web no está disponible en este momento." }, 502);
    } finally { clearTimeout(timer); }
  };
}
