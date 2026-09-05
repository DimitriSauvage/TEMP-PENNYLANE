import { test, expect, checkPageA11y } from "../../fixtures/test.fixture";
import { SUGGEST_CATALOG, searchResponse } from "../../fixtures/recipes.fixture";
import { mockIngredientSuggestions, mockRecipeSearch } from "../../mocks/api";
import { SearchPage } from "../../page-objects/search-page";

test.describe("first visit with no ingredients", () => {
  test("prompts for ingredients and never calls the search API", async ({ page }) => {
    const search = new SearchPage(page);
    const searchMock = await mockRecipeSearch(page, () => searchResponse([]));
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);

    await search.goto();

    await expect(search.emptyPrompt).toBeVisible();
    await expect(search.staplesHint).toBeVisible();
    await expect(search.chips).toHaveCount(0);
    expect(searchMock.calls).toHaveLength(0);
  });

  test("empty home page is accessible", async ({ page }) => {
    await page.goto("/");
    await checkPageA11y(page);
  });
});
