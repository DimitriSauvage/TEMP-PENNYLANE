import { test, expect, checkPageA11y } from "../../fixtures/test.fixture";
import { SUGGEST_CATALOG } from "../../fixtures/recipes.fixture";
import {
  mockIngredientSuggestions,
  mockRecipeDetailError,
  mockRecipeSearchError,
} from "../../mocks/api";
import { SearchPage } from "../../page-objects/search-page";

test.describe("error states", () => {
  test("search surfaces the server error message", async ({ page }) => {
    page.allowResourceError(422);
    const search = new SearchPage(page);
    await mockIngredientSuggestions(page, SUGGEST_CATALOG);
    await mockRecipeSearchError(page, 422, "Provide at least one usable ingredient.");

    await search.goto();
    await search.addIngredient("bu", "butter");

    const alert = page.getByRole("alert");
    await expect(alert).toContainText("Provide at least one usable ingredient.");
    await expect(alert.getByRole("button", { name: "Try again" })).toBeVisible();
    await checkPageA11y(page);
  });

  test("detail surfaces the server error message with a retry", async ({ page }) => {
    page.allowResourceError(404);
    await mockRecipeDetailError(page, "999", 404, "Recipe not found");

    await page.goto("/recipes/999");

    const alert = page.getByRole("alert");
    await expect(alert).toContainText("Recipe not found");
    await expect(alert.getByRole("button", { name: "Try again" })).toBeVisible();
    await checkPageA11y(page);
  });

  test("detail shows the error state without a network call for a non-numeric id", async ({
    page,
  }) => {
    // Canary: any recipe-API call on /recipes/abc means the NaN gate broke.
    let recipeApiCalls = 0;
    await page.route("**/api/recipes/**", (route) => {
      recipeApiCalls += 1;
      return route.fulfill({ status: 200, body: "{}" });
    });

    await page.goto("/recipes/abc");

    const alert = page.getByRole("alert");
    await expect(alert).toContainText("Could not load this recipe.");
    await expect(alert.getByRole("button", { name: "Try again" })).toHaveCount(0);
    expect(recipeApiCalls).toBe(0);
  });
});
