import { test, expect, checkPageA11y } from "../../fixtures/test.fixture";
import { READY_PANCAKES, SUGGEST_CATALOG, searchResponse } from "../../fixtures/recipes.fixture";
import { mockIngredientSuggestions, mockRecipeSearch } from "../../mocks/api";
import { SearchPage } from "../../page-objects/search-page";

test.describe("ingredient persistence", () => {
  test("keeps the ingredient chips and results across a reload", async ({ page }) => {
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, () => searchResponse([READY_PANCAKES]));
    await search.goto();
    await search.addIngredient("bu", "butter");
    await expect(search.card("Perfect Pancakes")).toBeVisible();

    await page.reload();

    await expect(search.chip("butter")).toBeVisible();
    await expect(search.card("Perfect Pancakes")).toBeVisible();
  });

  test("reloaded results page is accessible", async ({ page }) => {
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, () => searchResponse([READY_PANCAKES]));
    await search.goto();
    await search.addIngredient("bu", "butter");
    await page.reload();
    await expect(search.card("Perfect Pancakes")).toBeVisible();

    await checkPageA11y(page);
  });
});
