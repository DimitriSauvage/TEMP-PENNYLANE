import type { RecipeSearchItemDto } from "~/api/generated/client.schemas";

/** Props for the recipe card component. */
export interface RecipeCardProps {
  /** Ranked search hit to display. */
  recipe: RecipeSearchItemDto;
}
