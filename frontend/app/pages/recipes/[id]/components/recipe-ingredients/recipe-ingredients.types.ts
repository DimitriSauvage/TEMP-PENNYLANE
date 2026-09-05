/** Props for the recipe ingredients list. */
export interface RecipeIngredientsProps {
  /** Full ingredient list of the recipe. */
  ingredients: readonly string[];
  /** Ingredients the user has at home; drives the missing computation. */
  userIngredients: readonly string[];
}
