import type { RecipeSearchItemDto } from "~/api/generated/client.schemas";

/** Props for the recipe results section. */
export interface RecipeResultsProps {
  /** Ranked search hits for the current page. */
  results: readonly RecipeSearchItemDto[];
  /** Total number of hits across all pages. */
  totalElements: number;
  /** True while the first load of the current search is in flight. */
  isPending: boolean;
  /** True when the search query failed. */
  isError: boolean;
  /** True while a background refetch is in flight. */
  isFetching: boolean;
  /** Error thrown by the query, if any. */
  error: Error | null;
  /** True when the user has at least one ingredient. */
  hasIngredients: boolean;
  /** Current value of the ready-to-cook-only filter. */
  readyToCookOnly: boolean;
  /** Zero-based current page index. */
  page: number;
}

/** Emits of the recipe results section. */
export interface RecipeResultsEmits {
  /** Retry the failed search. */
  (e: "retry"): void;
  /** Jump to the given zero-based page. */
  (e: "page-change", page: number): void;
}
