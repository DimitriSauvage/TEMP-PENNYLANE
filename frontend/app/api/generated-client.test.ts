import { describe, expect, it } from "vitest";
import * as ingredients from "./generated/ingredients/ingredients";
import * as recipes from "./generated/recipes/recipes";

describe("generated API client", () => {
  it("exports the three endpoint functions", () => {
    expect(typeof recipes.searchRecipes).toBe("function");
    expect(typeof recipes.getRecipe).toBe("function");
    expect(typeof ingredients.suggestIngredients).toBe("function");
  });
});
