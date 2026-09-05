import { ref } from "vue";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { RecipeSearchResponseDto } from "~/api/generated/client.schemas";
import { ApiError } from "~/api/client";
import { searchRecipes } from "~/api/generated/recipes/recipes";
import { SEARCH_PAGE_SIZE, fetchRecipeSearch, recipeSearchQueryOptions } from "./use-recipe-search";

vi.mock("~/api/generated/recipes/recipes", () => ({
  searchRecipes: vi.fn(),
}));

function okSearchResponse(items: RecipeSearchResponseDto["items"] = []): RecipeSearchResponseDto {
  return { page: 0, size: SEARCH_PAGE_SIZE, totalElements: 0, items };
}

describe("recipeSearchQueryOptions", () => {
  describe("query key", () => {
    it('embeds normalized params under ["recipes","search"]', () => {
      const params = ref({
        ingredients: ["butter", "egg"],
        readyToCookOnly: false,
        page: 2,
      });
      const options = recipeSearchQueryOptions(params);
      expect(options.queryKey.value).toEqual([
        "recipes",
        "search",
        {
          ingredients: ["butter", "egg"],
          readyToCookOnly: false,
          page: 2,
          size: SEARCH_PAGE_SIZE,
        },
      ]);
    });

    it("sorts ingredients in the key so add order does not split the cache", () => {
      const params = ref({
        ingredients: ["milk", "butter", "egg"],
        readyToCookOnly: false,
        page: 0,
      });
      const options = recipeSearchQueryOptions(params);
      expect(options.queryKey.value[2]).toMatchObject({
        ingredients: ["butter", "egg", "milk"],
      });
    });

    it("reacts to param changes", () => {
      const params = ref({ ingredients: ["egg"], readyToCookOnly: false, page: 0 });
      const options = recipeSearchQueryOptions(params);
      params.value = { ingredients: ["egg", "milk"], readyToCookOnly: true, page: 1 };
      expect(options.queryKey.value[2]).toEqual({
        ingredients: ["egg", "milk"],
        readyToCookOnly: true,
        page: 1,
        size: SEARCH_PAGE_SIZE,
      });
    });
  });

  describe("enabled gating", () => {
    it("is disabled when the ingredient list is empty", () => {
      const params = ref({ ingredients: [], readyToCookOnly: false, page: 0 });
      const options = recipeSearchQueryOptions(params);
      expect(options.enabled.value).toBe(false);
    });

    it("is enabled once at least one ingredient is present", () => {
      const params = ref({ ingredients: ["egg"], readyToCookOnly: false, page: 0 });
      const options = recipeSearchQueryOptions(params);
      expect(options.enabled.value).toBe(true);
    });

    it("flips from disabled to enabled reactively", () => {
      const params = ref({ ingredients: [] as string[], readyToCookOnly: false, page: 0 });
      const options = recipeSearchQueryOptions(params);
      expect(options.enabled.value).toBe(false);
      params.value = { ...params.value, ingredients: ["egg"] };
      expect(options.enabled.value).toBe(true);
    });
  });
});

describe("fetchRecipeSearch", () => {
  beforeEach(() => {
    vi.mocked(searchRecipes).mockReset();
  });

  it("calls the generated client with joined, sorted ingredients and pagination", async () => {
    vi.mocked(searchRecipes).mockResolvedValue({
      status: 200,
      data: okSearchResponse(),
      headers: new Headers(),
    });
    const data = await fetchRecipeSearch({
      // Sorted, as the query-key normalizer passes it.
      ingredients: ["egg", "milk"],
      readyToCookOnly: true,
      page: 3,
      size: SEARCH_PAGE_SIZE,
    });
    expect(searchRecipes).toHaveBeenCalledWith(
      {
        ingredients: "egg,milk",
        readyToCookOnly: true,
        page: 3,
        size: SEARCH_PAGE_SIZE,
      },
      {},
    );
    expect(data).toEqual(okSearchResponse());
  });

  it("returns the payload data on success", async () => {
    const response: RecipeSearchResponseDto = {
      page: 0,
      size: 12,
      totalElements: 1,
      items: [
        {
          id: "r1",
          name: "Omelette",
          totalIngredients: 2,
          matchedIngredients: 2,
          missingIngredients: [],
          readyToCook: true,
        },
      ],
    };
    vi.mocked(searchRecipes).mockResolvedValue({
      status: 200,
      data: response,
      headers: new Headers(),
    });
    const data = await fetchRecipeSearch({
      ingredients: ["egg"],
      readyToCookOnly: false,
      page: 0,
      size: 12,
    });
    expect(data).toBe(response);
  });

  it("throws an ApiError carrying the status and server message on an HTTP error", async () => {
    vi.mocked(searchRecipes).mockResolvedValue({
      status: 422,
      data: {
        timestamp: "2026-09-04T10:00:00Z",
        status: 422,
        error: "Unprocessable Entity",
        path: "/api/recipes/search",
        code: "NO_USABLE_INGREDIENTS",
        message: "Provide at least one usable ingredient.",
      },
      headers: new Headers(),
    });
    const error: unknown = await fetchRecipeSearch({
      ingredients: ["egg"],
      readyToCookOnly: false,
      page: 0,
      size: 12,
    }).catch((thrown: unknown) => thrown);
    expect(error).toBeInstanceOf(ApiError);
    expect(error).toHaveProperty("status", 422);
    expect(error).toHaveProperty("message", "Provide at least one usable ingredient.");
  });

  it("forwards the abort signal to the generated client", async () => {
    vi.mocked(searchRecipes).mockResolvedValue({
      status: 200,
      data: okSearchResponse(),
      headers: new Headers(),
    });
    const controller = new AbortController();
    await fetchRecipeSearch(
      {
        ingredients: ["egg"],
        readyToCookOnly: false,
        page: 0,
        size: SEARCH_PAGE_SIZE,
      },
      controller.signal,
    );
    expect(searchRecipes).toHaveBeenCalledWith(
      {
        ingredients: "egg",
        readyToCookOnly: false,
        page: 0,
        size: SEARCH_PAGE_SIZE,
      },
      { signal: controller.signal },
    );
  });
});
