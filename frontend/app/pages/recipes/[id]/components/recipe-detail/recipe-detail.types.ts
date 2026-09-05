import type { RecipeDetailResponseDto } from "~/api/generated/client.schemas";

/** Props for the recipe detail body. */
export interface RecipeDetailProps {
  /** Recipe to display. */
  recipe: RecipeDetailResponseDto;
  /** Ingredients the user has at home; drives the missing computation. */
  userIngredients: readonly string[];
}
