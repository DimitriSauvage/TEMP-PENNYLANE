import { computed, onScopeDispose, ref } from "vue";
import type { ComputedRef, Ref } from "vue";
import { useQuery } from "@tanstack/vue-query";
import type { QueryFunctionContext } from "@tanstack/vue-query";
import { suggestIngredients } from "~/api/generated/ingredients/ingredients";
import { ApiError } from "~/api/client";

/** Debounce delay between the last keystroke and the suggest request. */
const DEBOUNCE_MS = 300;

/** Minimum trimmed prefix length before a request is sent. */
const MIN_LENGTH = 1;

/** Stale time for suggestions: short-lived typing aid, refetch quickly. */
const STALE_TIME = 5_000;

/**
 * Call the generated suggest endpoint and unwrap the typed result.
 *
 * @param prefix - Trimmed ingredient name prefix.
 * @param signal - Abort signal from the query context; cancels the underlying
 *   fetch when the query is superseded by a newer prefix.
 * @returns The matching catalog names on success.
 * @throws {@link ApiError} with the status and the server message (fallback
 *   text when the body is unparseable or empty) on a non-200 status.
 */
export async function fetchIngredientSuggestions(
  prefix: string,
  signal?: AbortSignal,
): Promise<string[]> {
  const response = await suggestIngredients({ q: prefix }, signal ? { signal } : {});
  if (response.status !== 200) throw new ApiError(response.status, response.data);
  return response.data.names;
}

/** Query options produced by {@link ingredientSuggestionsQueryOptions}. */
export interface IngredientSuggestionsQueryOptions {
  readonly queryKey: ComputedRef<readonly unknown[]>;
  readonly queryFn: (context: QueryFunctionContext) => Promise<string[]>;
  readonly enabled: ComputedRef<boolean>;
  readonly staleTime: number;
  readonly retry: false;
}

/**
 * TanStack Query options for the ingredient suggestions. The key embeds the
 * debounced prefix — each prefix is its own cache entry, so a late response
 * for an earlier prefix can never overwrite the current one — and the query
 * is gated on the minimum prefix length.
 *
 * @param debouncedPrefix - Reactive, already-debounced prefix.
 * @returns Query options for `useQuery`.
 */
export function ingredientSuggestionsQueryOptions(
  debouncedPrefix: Ref<string>,
): IngredientSuggestionsQueryOptions {
  return {
    queryKey: computed(() => ["ingredients", "suggest", debouncedPrefix.value]),
    queryFn: (context) => fetchIngredientSuggestions(debouncedPrefix.value, context.signal),
    enabled: computed(() => debouncedPrefix.value.length >= MIN_LENGTH),
    staleTime: STALE_TIME,
    retry: false,
  };
}

/** Return value of {@link useIngredientSuggestions}. */
export interface UseIngredientSuggestionsReturn {
  /** Catalog names matching the current prefix; empty when no match. */
  readonly suggestions: Ref<readonly string[]>;
  /** True while a suggest request is in flight. */
  readonly isSearching: Ref<boolean>;
  /**
   * Debounced prefix lookup. Short or empty prefixes clear the suggestions
   * without a request.
   *
   * @param prefix - Raw input value.
   */
  search(prefix: string): void;
  /** Drop the pending debounce and suggestions immediately. */
  clear(): void;
}

/**
 * Catalog-only ingredient suggestions for the autocomplete input: a debounced
 * prefix ref feeds a TanStack Query against the suggest endpoint. Rapid
 * keystrokes collapse into one request for the last prefix.
 */
export function useIngredientSuggestions(): UseIngredientSuggestionsReturn {
  const debouncedPrefix = ref("");
  let timer: ReturnType<typeof setTimeout> | null = null;

  function clear(): void {
    if (timer) clearTimeout(timer);
    timer = null;
    debouncedPrefix.value = "";
  }

  function search(prefix: string): void {
    if (timer) clearTimeout(timer);
    timer = null;
    const q = prefix.trim();
    if (q.length < MIN_LENGTH) {
      debouncedPrefix.value = "";
      return;
    }
    timer = setTimeout(() => {
      timer = null;
      debouncedPrefix.value = q;
    }, DEBOUNCE_MS);
  }

  const query = useQuery(ingredientSuggestionsQueryOptions(debouncedPrefix));

  const suggestions = computed(() => query.data.value ?? []);
  const isSearching = computed(() => query.isFetching.value);

  onScopeDispose(clear);

  return { suggestions, isSearching, search, clear };
}
