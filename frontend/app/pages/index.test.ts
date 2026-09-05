import { mountSuspended } from "@nuxt/test-utils/runtime";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { RecipeSearchItemDto } from "~/api/generated/client.schemas";
import { searchRecipes } from "~/api/generated/recipes/recipes";
import { useIngredientList } from "~/composables/use-ingredient-list";
import IndexPage from "./index.vue";

vi.mock("~/api/generated/recipes/recipes", () => ({
  searchRecipes: vi.fn(),
}));

function okSearch(items: RecipeSearchItemDto[] = [], totalElements = items.length) {
  return {
    status: 200 as const,
    data: { page: 0, size: 12, totalElements, items },
    headers: new Headers(),
  };
}

describe("index page empty states", () => {
  beforeEach(() => {
    window.localStorage.clear();
    useIngredientList().clear();
    vi.mocked(searchRecipes).mockReset();
  });

  it("prompts to add ingredients when the list is empty", async () => {
    const wrapper = await mountSuspended(IndexPage);
    expect(wrapper.text()).toContain("Add what you have at home to find recipes you can cook.");
    expect(searchRecipes).not.toHaveBeenCalled();
  });

  it("shows the no-match state with a hint when the search returns nothing", async () => {
    useIngredientList().add("chicken");
    vi.mocked(searchRecipes).mockResolvedValue(okSearch());
    const wrapper = await mountSuspended(IndexPage);
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain("No recipe matches these ingredients.");
    });
    expect(wrapper.text()).toContain("Try removing some ingredients");
  });

  it("shows the ready-to-cook-only hint when the toggle filters everything out", async () => {
    useIngredientList().add("beef");
    vi.mocked(searchRecipes).mockResolvedValue(okSearch());
    const wrapper = await mountSuspended(IndexPage);
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain("No recipe matches these ingredients.");
    });
    await wrapper.find('input[type="checkbox"]').setValue(true);
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain(
        "No recipe uses ALL your ingredients — try turning off 'Ready to cook only'.",
      );
    });
  });

  it("renders recipe cards when the search returns results", async () => {
    useIngredientList().add("turkey");
    vi.mocked(searchRecipes).mockResolvedValue(
      okSearch([
        {
          id: "r1",
          name: "Roast Chicken",
          totalIngredients: 2,
          matchedIngredients: 2,
          missingIngredients: [],
          readyToCook: true,
        },
      ]),
    );
    const wrapper = await mountSuspended(IndexPage);
    await vi.waitFor(() => {
      expect(wrapper.text()).toContain("Roast Chicken");
    });
    expect(wrapper.find('a[href="/recipes/r1"]').exists()).toBe(true);
  });

  it("resets to the first page when an ingredient is added while paginated", async () => {
    useIngredientList().add("duck");
    // 24 hits over 12-per-page grids => 2 pages; the mock serves page 1's cards.
    const items = Array.from({ length: 12 }, (_, i) => ({
      id: `r${i}`,
      name: `Recipe ${i}`,
      totalIngredients: 2,
      matchedIngredients: 2,
      missingIngredients: [],
      readyToCook: true,
    }));
    vi.mocked(searchRecipes).mockResolvedValue(okSearch(items, 24));
    const wrapper = await mountSuspended(IndexPage);
    await vi.waitFor(() => {
      expect(wrapper.find(".p-paginator").exists()).toBe(true);
    });

    const page2 = wrapper.findAll("button").find((button) => button.text() === "2");
    expect(page2).toBeDefined();
    await page2!.trigger("click");
    await vi.waitFor(() => {
      expect(searchRecipes).toHaveBeenLastCalledWith(
        expect.objectContaining({ page: 1 }),
        expect.anything(),
      );
    });

    // Regression: adding an ingredient mutates the list via push(), which a
    // shallow ref watch misses — the page must still reset to 0.
    useIngredientList().add("milk");
    await vi.waitFor(() => {
      expect(searchRecipes).toHaveBeenLastCalledWith(
        expect.objectContaining({ page: 0, ingredients: "duck,milk" }),
        expect.anything(),
      );
    });
  });
});

describe("index page error states", () => {
  beforeEach(() => {
    window.localStorage.clear();
    useIngredientList().clear();
    vi.mocked(searchRecipes).mockReset();
  });

  it("surfaces the server error message when the search fails", async () => {
    useIngredientList().add("salt");
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
    const wrapper = await mountSuspended(IndexPage);
    // One retry with ~1s backoff: the error state lands just past the default
    // 1s waitFor timeout, so give it room.
    await vi.waitFor(
      () => {
        expect(wrapper.text()).toContain("Provide at least one usable ingredient.");
      },
      { timeout: 4000 },
    );
    expect(wrapper.text()).toContain("Try again");
  });

  it("falls back to the generic message when the error carries none", async () => {
    useIngredientList().add("salt");
    vi.mocked(searchRecipes).mockRejectedValue(new Error(""));
    const wrapper = await mountSuspended(IndexPage);
    await vi.waitFor(
      () => {
        expect(wrapper.text()).toContain("Something went wrong while loading recipes.");
      },
      { timeout: 4000 },
    );
  });
});
