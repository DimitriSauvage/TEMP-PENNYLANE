import { mountSuspended } from "@nuxt/test-utils/runtime";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { RecipeDetailResponseDto } from "~/api/generated/client.schemas";
import { getRecipe } from "~/api/generated/recipes/recipes";
import { useIngredientList } from "~/composables/use-ingredient-list";
import RecipeDetailPage from "./[id].vue";

vi.mock("~/api/generated/recipes/recipes", () => ({
  getRecipe: vi.fn(),
}));

const detail: RecipeDetailResponseDto = {
  id: "42",
  name: "Omelette",
  imageUrl: "https://example.com/omelette.jpg",
  ingredients: ["egg", "butter", "salt"],
  instructions: ["Beat the eggs.", "Fry in butter."],
};

describe("recipe detail page", () => {
  beforeEach(() => {
    window.localStorage.clear();
    useIngredientList().clear();
    vi.mocked(getRecipe).mockReset();
  });

  it("renders the recipe with missing ingredients highlighted", async () => {
    useIngredientList().add("egg");
    vi.mocked(getRecipe).mockResolvedValue({
      status: 200,
      data: detail,
      headers: new Headers(),
    });
    const wrapper = await mountSuspended(RecipeDetailPage, { route: "/recipes/42" });
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain("Omelette");
    });
    expect(wrapper.find("img").attributes("alt")).toBe("Omelette");
    expect(wrapper.text()).toContain("Beat the eggs.");
    // 'egg' is in the user list, 'salt' is a staple: only 'butter' is missing.
    expect(wrapper.text()).toContain("butter — missing");
    expect(wrapper.text()).not.toContain("egg — missing");
    expect(wrapper.text()).not.toContain("salt — missing");
    expect(wrapper.find("button").text()).toContain("Back");
  });

  it("surfaces the server error message with a retry when the lookup fails", async () => {
    vi.mocked(getRecipe).mockResolvedValue({
      status: 404,
      data: {
        timestamp: "2026-09-04T10:00:00Z",
        status: 404,
        error: "Not Found",
        path: "/api/recipes/999",
        code: "RECIPE_NOT_FOUND",
        message: "Recipe not found",
      },
      headers: new Headers(),
    });
    const wrapper = await mountSuspended(RecipeDetailPage, { route: "/recipes/999" });
    // One retry with ~1s backoff: the error state lands just past the default
    // 1s waitFor timeout, so give it room.
    await vi.waitFor(
      () => {
        expect(wrapper.text()).toContain("Recipe not found");
      },
      { timeout: 4000 },
    );
    const retry = wrapper.findAll("button").find((button) => button.text() === "Try again");
    expect(retry).toBeDefined();
  });

  it("falls back to the generic message when the error carries none", async () => {
    vi.mocked(getRecipe).mockRejectedValue(new Error(""));
    const wrapper = await mountSuspended(RecipeDetailPage, { route: "/recipes/999" });
    await vi.waitFor(
      () => {
        expect(wrapper.text()).toContain("Could not load this recipe.");
      },
      { timeout: 4000 },
    );
  });

  it("shows the error state without hitting the network for a non-numeric id", async () => {
    const wrapper = await mountSuspended(RecipeDetailPage, { route: "/recipes/abc" });
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain("Could not load this recipe.");
    });
    expect(getRecipe).not.toHaveBeenCalled();
    // Retrying a gated query would refetch /api/recipes/NaN — no button.
    const retry = wrapper.findAll("button").find((button) => button.text() === "Try again");
    expect(retry).toBeUndefined();
  });
});
