import { test, expect, checkPageA11y } from "../../fixtures/test.fixture";
import {
  OMELETTE_DETAIL,
  PARTIAL_OMELETTE,
  READY_PANCAKES,
  SUGGEST_CATALOG,
  searchResponse,
} from "../../fixtures/recipes.fixture";
import { mockIngredientSuggestions, mockRecipeDetail, mockRecipeSearch } from "../../mocks/api";
import { RecipeDetailPage } from "../../page-objects/recipe-detail-page";
import { SearchPage } from "../../page-objects/search-page";

test.describe("recipe detail flow", () => {
  test("flags missing ingredients, spares staples, and returns to the results", async ({
    page,
  }) => {
    const search = new SearchPage(page);
    const detail = new RecipeDetailPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearch(page, () => searchResponse([READY_PANCAKES, PARTIAL_OMELETTE]));
    await mockRecipeDetail(page, OMELETTE_DETAIL);
    await search.goto();
    await search.addIngredient("bu", "butter");
    await expect(search.resultCards).toHaveCount(2);

    await search.card("Cheesy Omelette").click();

    await detail.expectRecipe("Cheesy Omelette");
    await expect(detail.ingredient("cheese")).toContainText("— missing");
    await expect(detail.ingredient("butter")).not.toContainText("missing");
    await expect(detail.ingredient("salt")).not.toContainText("missing");

    await detail.backLink.click();

    await expect(page).toHaveURL(/\/$/);
    await expect(search.chip("butter")).toBeVisible();
    await expect(search.card("Cheesy Omelette")).toBeVisible();
  });

  test("detail page is accessible", async ({ page }) => {
    await mockRecipeDetail(page, OMELETTE_DETAIL);
    await page.goto("/recipes/2");
    await expect(page.getByRole("heading", { name: "Cheesy Omelette" })).toBeVisible();

    await checkPageA11y(page);
  });
});
