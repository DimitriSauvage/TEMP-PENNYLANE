import { test, expect, checkPageA11y } from "../../fixtures/test.fixture";
import { SUGGEST_CATALOG, pagedSearchResponse } from "../../fixtures/recipes.fixture";
import { mockIngredientSuggestions, mockRecipeSearch } from "../../mocks/api";
import { SearchPage } from "../../page-objects/search-page";

test.describe("pagination", () => {
  test("shows the paginator and page 2 holds different results", async ({ page }) => {
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, pagedSearchResponse);
    await search.goto();
    await search.addIngredient("bu", "butter");

    await expect(search.resultCards).toHaveCount(12);
    await expect(search.card("Paged Recipe 01")).toBeVisible();
    await expect(search.card("Paged Recipe 13")).toHaveCount(0);

    const page2 = search.resultsRegion.getByRole("button", { name: "Page 2" });
    await expect(page2).toBeVisible();
    await page2.click();

    await expect(search.card("Paged Recipe 13")).toBeVisible();
    await expect(search.card("Paged Recipe 01")).toHaveCount(0);
    await expect(search.resultCards).toHaveCount(2);
  });

  test("paginated results page is accessible", async ({ page }) => {
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, pagedSearchResponse);
    await search.goto();
    await search.addIngredient("bu", "butter");
    await expect(search.resultCards).toHaveCount(12);

    await checkPageA11y(page);
  });
});
