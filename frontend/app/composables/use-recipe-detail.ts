import { computed, toValue } from "vue";
import type { ComputedRef, MaybeRefOrGetter } from "vue";
import { useQuery } from "@tanstack/vue-query";
import type { QueryFunctionContext, UseQueryReturnType } from "@tanstack/vue-query";
import type { RecipeDetailResponseDto } from "~/api/generated/client.schemas";
import { getRecipe } from "~/api/generated/recipes/recipes";
import { ApiError } from "~/api/client";

/** Stale time for recipe details: effectively immutable content. */
const STALE_TIME = 300_000;

/**
 * Call the generated detail endpoint and unwrap the typed result.
 *
 * @param id - Recipe id as a (numeric) string, e.g. from the route param.
 * @param signal - Abort signal from the query context; cancels the underlying
 *   fetch when the query is superseded.
 * @returns The recipe detail on success.
 * @throws {@link ApiError} with the status and the server message (fallback
 *   text when the body is unparseable or empty) on a non-200 status.
 */
export async function fetchRecipeDetail(
  id: string,
  signal?: AbortSignal,
): Promise<RecipeDetailResponseDto> {
  const response = await getRecipe(Number(id), signal ? { signal } : {});
  if (response.status !== 200) throw new ApiError(response.status, response.data);
  return response.data;
}

/** Query options produced by {@link recipeDetailQueryOptions}. */
export interface RecipeDetailQueryOptions {
  readonly queryKey: ComputedRef<readonly unknown[]>;
  readonly queryFn: (context: QueryFunctionContext) => Promise<RecipeDetailResponseDto>;
  readonly enabled: ComputedRef<boolean>;
  readonly staleTime: number;
}

/**
 * TanStack Query options for one recipe detail. The query is gated on a
 * numeric id so a hand-typed URL like `/recipes/abc` never hits the network.
 *
 * @param id - Reactive recipe id (route param).
 * @returns Query options for `useQuery`.
 */
export function recipeDetailQueryOptions(id: MaybeRefOrGetter<string>): RecipeDetailQueryOptions {
  return {
    queryKey: computed(() => ["recipes", "detail", toValue(id)]),
    queryFn: (context) => fetchRecipeDetail(toValue(id), context.signal),
    enabled: computed(() => Number.isInteger(Number(toValue(id)))),
    staleTime: STALE_TIME,
  };
}

/** Return value of {@link useRecipeDetail}. */
export type UseRecipeDetailReturn = UseQueryReturnType<
  RecipeDetailResponseDto | undefined,
  Error | null
>;

/**
 * Fetch one recipe detail by id.
 *
 * @param id - Reactive recipe id (route param).
 * @returns Query result for the recipe detail.
 */
export function useRecipeDetail(id: MaybeRefOrGetter<string>): UseRecipeDetailReturn {
  return useQuery(recipeDetailQueryOptions(id));
}
