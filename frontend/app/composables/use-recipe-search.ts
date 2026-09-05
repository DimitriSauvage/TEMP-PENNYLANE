import { computed, ref } from "vue";
import type { ComputedRef, Ref } from "vue";
import { keepPreviousData, useQuery } from "@tanstack/vue-query";
import type { QueryFunctionContext, UseQueryReturnType } from "@tanstack/vue-query";
import type { RecipeSearchResponseDto } from "~/api/generated/client.schemas";
import { searchRecipes } from "~/api/generated/recipes/recipes";
import { ApiError } from "~/api/client";

/** Page size used by the results grid. */
export const SEARCH_PAGE_SIZE = 12;

/** Stale time for search results: standard list churn. */
const STALE_TIME = 30_000;

/** Search filters owned by the caller; the composable owns pagination. */
export interface RecipeSearchFilters {
  readonly ingredients: readonly string[];
  readonly readyToCookOnly: boolean;
}

/** Complete search params, including the composable-owned page index. */
export interface RecipeSearchParams extends RecipeSearchFilters {
  readonly page: number;
}

/** Normalized params embedded in the query key. */
export interface RecipeSearchKeyParams {
  readonly ingredients: readonly string[];
  readonly readyToCookOnly: boolean;
  readonly page: number;
  readonly size: number;
}

/** Query options produced by {@link recipeSearchQueryOptions}. */
export interface RecipeSearchQueryOptions {
  readonly queryKey: ComputedRef<readonly unknown[]>;
  readonly queryFn: (context: QueryFunctionContext) => Promise<RecipeSearchResponseDto>;
  readonly enabled: ComputedRef<boolean>;
  readonly placeholderData: typeof keepPreviousData;
  readonly staleTime: number;
}

/**
 * Call the generated search endpoint and unwrap the typed result.
 *
 * @param params - Normalized search params; ingredients are joined, sorted.
 * @param signal - Abort signal from the query context; cancels the underlying
 *   fetch when the query is superseded (page/filter change).
 * @returns The page payload on success.
 * @throws {@link ApiError} with the status and the server message (fallback
 *   text when the body is unparseable or empty) on a non-200 status.
 */
export async function fetchRecipeSearch(
  params: RecipeSearchKeyParams,
  signal?: AbortSignal,
): Promise<RecipeSearchResponseDto> {
  const response = await searchRecipes(
    {
      ingredients: params.ingredients.join(","),
      readyToCookOnly: params.readyToCookOnly,
      page: params.page,
      size: params.size,
    },
    signal ? { signal } : {},
  );
  if (response.status !== 200) throw new ApiError(response.status, response.data);
  return response.data;
}

/**
 * TanStack Query options for the recipe search. The key embeds normalized
 * params (ingredients sorted, so add order never splits the cache) and the
 * query is gated on a non-empty ingredient list.
 *
 * @param params - Reactive search params.
 * @returns Query options for `useQuery`.
 */
export function recipeSearchQueryOptions(
  params: Ref<RecipeSearchParams>,
): RecipeSearchQueryOptions {
  const keyParams = computed<RecipeSearchKeyParams>(() => ({
    ingredients: [...params.value.ingredients].sort(),
    readyToCookOnly: params.value.readyToCookOnly,
    page: params.value.page,
    size: SEARCH_PAGE_SIZE,
  }));

  return {
    queryKey: computed(() => ["recipes", "search", keyParams.value]),
    queryFn: (context) => fetchRecipeSearch(keyParams.value, context.signal),
    enabled: computed(() => params.value.ingredients.length > 0),
    placeholderData: keepPreviousData,
    staleTime: STALE_TIME,
  };
}

/** Return value of {@link useRecipeSearch}. */
export interface UseRecipeSearchReturn {
  /** The TanStack Query result for the current search. */
  readonly query: UseQueryReturnType<RecipeSearchResponseDto, Error>;
  /** Zero-based current page index. */
  readonly page: Ref<number>;
  /**
   * Jump to a page.
   *
   * @param page - Zero-based page index.
   */
  setPage(page: number): void;
}

/**
 * Ranked recipe search over the user's ingredient list. The query only runs
 * once at least one ingredient is present; `keepPreviousData` keeps the
 * current grid on screen while the next page or filter change loads.
 *
 * @param filters - Reactive search filters (ingredients + ready-to-cook toggle).
 * @returns Query result plus the page state.
 */
export function useRecipeSearch(filters: Ref<RecipeSearchFilters>): UseRecipeSearchReturn {
  const page = ref(0);
  const params = computed<RecipeSearchParams>(() => ({ ...filters.value, page: page.value }));
  const query = useQuery(recipeSearchQueryOptions(params));

  function setPage(next: number): void {
    page.value = next;
  }

  return { query, page, setPage };
}
