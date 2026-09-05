import { test, expect, checkPageA11y } from "../../fixtures/test.fixture";
import {
  PARTIAL_OMELETTE,
  PARTIAL_STEW,
  READY_PANCAKES,
  SUGGEST_CATALOG,
  searchResponse,
} from "../../fixtures/recipes.fixture";
import { mockIngredientSuggestions, mockRecipeSearch } from "../../mocks/api";
import { SearchPage } from "../../page-objects/search-page";

test.describe("ready-to-cook-only toggle", () => {
  test("hides partial matches when toggled on", async ({ page }) => {
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, (request) =>
      request.readyToCookOnly
        ? searchResponse([READY_PANCAKES])
        : searchResponse([READY_PANCAKES, PARTIAL_OMELETTE, PARTIAL_STEW]),
    );
    await search.goto();
    await search.addIngredient("bu", "butter");
    await expect(search.resultCards).toHaveCount(3);

    await search.readyToCookToggle.click();

    await expect(search.resultCards).toHaveCount(1);
    await expect(search.resultCards.nth(0)).toContainText("Perfect Pancakes");
    await expect(search.card("Cheesy Omelette")).toHaveCount(0);
  });

  test("shows the turn-it-off message when nothing matches fully", async ({ page }) => {
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, (request) =>
      request.readyToCookOnly ? searchResponse([]) : searchResponse([PARTIAL_OMELETTE]),
    );
    await search.goto();
    await search.addIngredient("bu", "butter");
    await expect(search.resultCards).toHaveCount(1);

    await search.readyToCookToggle.click();

    await expect(
      search.resultsRegion.getByText(
        "No recipe uses ALL your ingredients — try turning off 'Ready to cook only'.",
      ),
    ).toBeVisible();
    await expect(search.resultCards).toHaveCount(0);
  });

  test("toggled results page is accessible", async ({ page }) => {
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, (request) =>
      request.readyToCookOnly
        ? searchResponse([READY_PANCAKES])
        : searchResponse([READY_PANCAKES, PARTIAL_OMELETTE]),
    );
    await search.goto();
    await search.addIngredient("bu", "butter");
    await search.readyToCookToggle.click();
    await expect(search.resultCards).toHaveCount(1);

    await checkPageA11y(page);
  });
});
