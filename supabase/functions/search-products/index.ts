import { createSearchHandler } from "./handler.ts";

Deno.serve(createSearchHandler({ getApiKey: () => Deno.env.get("TINYFISH_API_KEY") }));
