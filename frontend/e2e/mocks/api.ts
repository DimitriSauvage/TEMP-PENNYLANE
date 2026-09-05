import type { Page, Route } from "@playwright/test";
import type {
  IngredientSuggestionsResponseDto,
  RecipeDetailResponseDto,
  RecipeSearchResponseDto,
} from "../../app/api/generated/client.schemas";

/** Parsed /api/recipes/search query, as sent by the Orval client. */
export interface SearchRequest {
  readonly ingredients: string;
  readonly readyToCookOnly: boolean;
  readonly page: number;
  readonly size: number;
}

/** Handle on recorded /api/recipes/search traffic. */
export interface SearchMock {
  readonly calls: readonly SearchRequest[];
}

/** Handle on recorded /api/ingredients/suggest traffic. */
export interface SuggestMock {
  readonly queries: readonly string[];
}

// The e2e dev server points the API at its own origin, but responses carry
// permissive CORS headers so the mocks also work against a reused dev server
// started with the default http://localhost:8080 base.
const JSON_HEADERS: Record<string, string> = {
  "content-type": "application/json",
  "access-control-allow-origin": "*",
};

function fulfillJson(route: Route, body: unknown): Promise<void> {
  return route.fulfill({ status: 200, headers: JSON_HEADERS, body: JSON.stringify(body) });
}

/** Mock GET /api/recipes/search; `respond` receives the parsed query. */
export async function mockRecipeSearch(
  page: Page,
  respond: (request: SearchRequest) => RecipeSearchResponseDto,
): Promise<SearchMock> {
  const calls: SearchRequest[] = [];
  await page.route("**/api/recipes/search**", (route) => {
    const params = new URL(route.request().url()).searchParams;
    const request: SearchRequest = {
      ingredients: params.get("ingredients") ?? "",
      readyToCookOnly: params.get("readyToCookOnly") === "true",
      page: Number(params.get("page") ?? 0),
      size: Number(params.get("size") ?? 0),
    };
    calls.push(request);
    return fulfillJson(route, respond(request));
  });
  return { calls };
}

/** Mock GET /api/ingredients/suggest with a prefix-filtered catalog. */
export async function mockIngredientSuggestions(
  page: Page,
  catalog: readonly string[],
): Promise<SuggestMock> {
  const queries: string[] = [];
  await page.route("**/api/ingredients/suggest**", (route) => {
    const q = new URL(route.request().url()).searchParams.get("q") ?? "";
    queries.push(q);
    const body: IngredientSuggestionsResponseDto = {
      names: catalog.filter((name) => name.startsWith(q)),
    };
    return fulfillJson(route, body);
  });
  return { queries };
}

/** Mock GET /api/recipes/{id} for one recipe detail. */
export async function mockRecipeDetail(page: Page, detail: RecipeDetailResponseDto): Promise<void> {
  await page.route(`**/api/recipes/${detail.id}`, (route) => fulfillJson(route, detail));
}

/** Backend error envelope, as serialized by the application error handler. */
function errorBody(status: number, message: string, path: string): Record<string, unknown> {
  return {
    timestamp: "2026-01-01T12:00:00Z",
    status,
    error: "Error",
    path,
    code: "TEST",
    message,
  };
}

/** Mock GET /api/recipes/search with an error status and server message. */
export async function mockRecipeSearchError(
  page: Page,
  status: number,
  message: string,
): Promise<void> {
  await page.route("**/api/recipes/search**", (route) =>
    route.fulfill({
      status,
      headers: JSON_HEADERS,
      body: JSON.stringify(errorBody(status, message, "/api/recipes/search")),
    }),
  );
}

/** Mock GET /api/recipes/{id} with an error status and server message. */
export async function mockRecipeDetailError(
  page: Page,
  id: string,
  status: number,
  message: string,
): Promise<void> {
  await page.route(`**/api/recipes/${id}`, (route) =>
    route.fulfill({
      status,
      headers: JSON_HEADERS,
      body: JSON.stringify(errorBody(status, message, `/api/recipes/${id}`)),
    }),
  );
}
