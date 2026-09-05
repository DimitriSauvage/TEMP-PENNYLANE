import { test, expect, checkPageA11y } from "../../fixtures/test.fixture";
import {
  PARTIAL_OMELETTE,
  PARTIAL_STEW,
  READY_PANCAKES,
  STAPLE_RICE,
  SUGGEST_CATALOG,
  searchResponse,
} from "../../fixtures/recipes.fixture";
import { mockIngredientSuggestions, mockRecipeSearch } from "../../mocks/api";
import { SearchPage } from "../../page-objects/search-page";

test.describe("ranked results", () => {
  test("shows cards fewest-missing-first with match status", async ({ page }) => {
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, () =>
      searchResponse([READY_PANCAKES, PARTIAL_OMELETTE, PARTIAL_STEW]),
    );
    await search.goto();
    await search.addIngredient("bu", "butter");

    await expect(search.resultCards).toHaveCount(3);
    await expect(search.resultCards.nth(0)).toContainText("Perfect Pancakes");
    await expect(search.resultCards.nth(1)).toContainText("Cheesy Omelette");
    await expect(search.resultCards.nth(2)).toContainText("Hearty Stew");

    await expect(search.resultCards.nth(0).getByText("Ready to cook")).toBeVisible();
    await expect(search.resultCards.nth(1)).toContainText("you have 2 of 3");
    await expect(search.resultCards.nth(1)).toContainText("missing: cheese");
    await expect(search.resultCards.nth(2)).toContainText("missing: carrot, celery");
  });

  test("a recipe whose only non-user ingredient is a staple is ready to cook", async ({ page }) => {
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, () => searchResponse([STAPLE_RICE]));
    await search.goto();
    await search.addIngredient("bu", "butter");

    const card = search.card("Buttered Rice");
    await expect(card).toBeVisible();
    await expect(card).toContainText("Ready to cook");
    await expect(card).not.toContainText("missing");
  });

  test("results page is accessible", async ({ page }) => {
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, () => searchResponse([READY_PANCAKES]));
    await search.goto();
    await search.addIngredient("bu", "butter");

    await checkPageA11y(page);
  });
});
