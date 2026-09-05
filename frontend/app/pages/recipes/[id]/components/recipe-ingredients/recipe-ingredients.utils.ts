/**
 * Pantry staples assumed present in every kitchen. Mirrors the backend ranking
 * rule (salt, pepper, water, olive oil are always counted as available).
 */
const STAPLES = new Set(["salt", "pepper", "water", "olive oil"]);

/**
 * Recipe ingredients the user does not have: neither in their stored list nor
 * among the assumed pantry staples. Comparison is case-insensitive and ignores
 * surrounding whitespace; returned names keep the recipe's original spelling
 * and order.
 *
 * @param recipeIngredients - Full ingredient list of the recipe.
 * @param userIngredients - Ingredients the user has at home.
 * @returns The subset of recipe ingredients that is missing, in recipe order.
 */
export function computeMissingIngredients(
  recipeIngredients: readonly string[],
  userIngredients: readonly string[],
): string[] {
  const owned = new Set(userIngredients.map((ingredient) => ingredient.trim().toLowerCase()));
  return recipeIngredients.filter((ingredient) => {
    const key = ingredient.trim().toLowerCase();
    return !owned.has(key) && !STAPLES.has(key);
  });
}
