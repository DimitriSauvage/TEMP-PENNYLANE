import type { RecipeSearchItemDto } from "~/api/generated/client.schemas";

/** Max missing ingredient names listed before truncating with "+n more". */
const MAX_SHOWN_MISSING = 3;

/**
 * Human-readable match status for one search hit.
 *
 * Returns "Ready to cook" when nothing is missing, otherwise a partial-match
 * line such as "you have 2 of 3 — missing: butter, milk". Missing names are
 * truncated to the first {@link MAX_SHOWN_MISSING} entries with an "+n more"
 * suffix.
 *
 * @param item - Ranked search hit from the backend.
 * @returns Status line displayed on the recipe card.
 */
export function formatMatchStatus(item: RecipeSearchItemDto): string {
  if (item.readyToCook) return "Ready to cook";

  const shown = item.missingIngredients.slice(0, MAX_SHOWN_MISSING);
  const rest = item.missingIngredients.length - shown.length;
  const list = rest > 0 ? `${shown.join(", ")} +${rest} more` : shown.join(", ");
  return `you have ${item.matchedIngredients} of ${item.totalIngredients} — missing: ${list}`;
}
