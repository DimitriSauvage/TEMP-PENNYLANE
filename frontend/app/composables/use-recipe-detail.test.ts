import { beforeEach, describe, expect, it, vi } from "vitest";
import type { RecipeDetailResponseDto } from "~/api/generated/client.schemas";
import { ApiError } from "~/api/client";
import { getRecipe } from "~/api/generated/recipes/recipes";
import { fetchRecipeDetail, recipeDetailQueryOptions } from "./use-recipe-detail";

vi.mock("~/api/generated/recipes/recipes", () => ({
  getRecipe: vi.fn(),
}));

const detail: RecipeDetailResponseDto = {
  id: "42",
  name: "Omelette",
  ingredients: ["egg", "butter"],
  instructions: ["Beat the eggs.", "Fry in butter."],
};

describe("fetchRecipeDetail", () => {
  beforeEach(() => {
    vi.mocked(getRecipe).mockReset();
  });

  it("calls the generated client with the numeric recipe id", async () => {
    vi.mocked(getRecipe).mockResolvedValue({
      status: 200,
      data: detail,
      headers: new Headers(),
    });
    const data = await fetchRecipeDetail("42");
    expect(getRecipe).toHaveBeenCalledWith(42, {});
    expect(data).toBe(detail);
  });

  it("throws an ApiError carrying the status and server message on a 404", async () => {
    vi.mocked(getRecipe).mockResolvedValue({
      status: 404,
      data: {
        timestamp: "2026-09-04T10:00:00Z",
        status: 404,
        error: "Not Found",
        path: "/api/recipes/999",
        code: "RECIPE_NOT_FOUND",
        message: "Recipe 999 does not exist.",
      },
      headers: new Headers(),
    });
    const error: unknown = await fetchRecipeDetail("999").catch((thrown: unknown) => thrown);
    expect(error).toBeInstanceOf(ApiError);
    expect(error).toHaveProperty("status", 404);
    expect(error).toHaveProperty("message", "Recipe 999 does not exist.");
  });

  it("forwards the abort signal to the generated client", async () => {
    vi.mocked(getRecipe).mockResolvedValue({
      status: 200,
      data: detail,
      headers: new Headers(),
    });
    const controller = new AbortController();
    await fetchRecipeDetail("42", controller.signal);
    expect(getRecipe).toHaveBeenCalledWith(42, { signal: controller.signal });
  });
});

describe("recipeDetailQueryOptions", () => {
  it("is disabled for a non-numeric route id so /recipes/abc never hits the network", () => {
    const options = recipeDetailQueryOptions("abc");
    expect(options.enabled.value).toBe(false);
  });

  it("is disabled for a non-integer id", () => {
    const options = recipeDetailQueryOptions("12.5");
    expect(options.enabled.value).toBe(false);
  });

  it("is enabled for a numeric id", () => {
    const options = recipeDetailQueryOptions("42");
    expect(options.enabled.value).toBe(true);
  });
});
