import { test, expect, checkPageA11y } from "../../fixtures/test.fixture";
import { SUGGEST_CATALOG, searchResponse } from "../../fixtures/recipes.fixture";
import { mockIngredientSuggestions, mockRecipeSearch } from "../../mocks/api";
import { SearchPage } from "../../page-objects/search-page";

test.describe("guided ingredient entry", () => {
  test("typing a prefix shows catalog suggestions and picking one adds a chip", async ({
    page,
  }) => {
    const search = new SearchPage(page);
    const suggestMock = await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, () => searchResponse([]));
    await search.goto();

    await page.getByLabel("Add an ingredient").fill("chick");
    await expect.poll(() => suggestMock.queries).toContain("chick");
    await expect(page.getByRole("option", { name: "chicken", exact: true })).toBeVisible();
    await expect(page.getByRole("option", { name: "chickpeas", exact: true })).toBeVisible();

    await page.getByRole("option", { name: "chicken", exact: true }).click();

    await expect(search.chip("chicken")).toBeVisible();
    await expect(search.chips).toHaveCount(1);
  });

  test("a prefix with no catalog match adds nothing", async ({ page }) => {
    const search = new SearchPage(page);
    const suggestMock = await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, () => searchResponse([]));
    await search.goto();

    await page.getByLabel("Add an ingredient").fill("zzz");
    await expect.poll(() => suggestMock.queries).toContain("zzz");
    await expect(search.chips).toHaveCount(0);
  });

  test("adding the same ingredient twice leaves a single chip", async ({ page }) => {
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, () => searchResponse([]));
    await search.goto();

    await search.addIngredient("chick", "chicken");

    // Retyping the prefix must not offer the ingredient already in the list.
    await page.getByLabel("Add an ingredient").fill("chick");
    await expect(page.getByRole("option", { name: "chickpeas", exact: true })).toBeVisible();
    await expect(page.getByRole("option", { name: "chicken", exact: true })).toHaveCount(0);
    await expect(search.chips).toHaveCount(1);
  });

  test("picker with a chip is accessible", async ({ page }) => {
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, () => searchResponse([]));
    await search.goto();
    await search.addIngredient("chick", "chicken");

    await checkPageA11y(page);
  });
});
