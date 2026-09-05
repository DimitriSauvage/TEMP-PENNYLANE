import { readonly, ref } from "vue";
import type { Ref } from "vue";

/** localStorage key holding the persisted ingredient list. */
const STORAGE_KEY = "recipe-finder.ingredients";

/** Parse the persisted value defensively: corrupted or malformed data yields an empty list. */
function load(): string[] {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed: unknown = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed.filter((entry): entry is string => typeof entry === "string");
  } catch {
    return [];
  }
}

function save(ingredients: readonly string[]): void {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(ingredients));
  } catch {
    // Quota exceeded or storage disabled by browser policy: the in-memory
    // list still works for the session, only persistence is lost.
  }
}

// Module-level singleton: every caller (pages, components) shares one list.
const ingredients = ref<string[]>(load());

/** Return value of {@link useIngredientList}. */
export interface UseIngredientListReturn {
  /** Ingredients the user has at home, persisted to localStorage. */
  readonly ingredients: Readonly<Ref<readonly string[]>>;
  /**
   * Add an ingredient.
   *
   * @param name - Ingredient name; trimmed before storing.
   * @returns true when added, false when empty or a case-insensitive duplicate.
   */
  add(name: string): boolean;
  /**
   * Remove one ingredient.
   *
   * @param name - Ingredient to remove, matched case-insensitively.
   */
  remove(name: string): void;
  /** Remove every ingredient. */
  clear(): void;
}

/**
 * Shared ingredient list persisted to localStorage. Loads the persisted list
 * on first use and saves on every mutation, so the list survives reloads.
 */
export function useIngredientList(): UseIngredientListReturn {
  function add(name: string): boolean {
    const trimmed = name.trim();
    if (!trimmed) return false;
    const exists = ingredients.value.some(
      (ingredient) => ingredient.toLowerCase() === trimmed.toLowerCase(),
    );
    if (exists) return false;
    ingredients.value.push(trimmed);
    save(ingredients.value);
    return true;
  }

  function remove(name: string): void {
    const key = name.trim().toLowerCase();
    ingredients.value = ingredients.value.filter((ingredient) => ingredient.toLowerCase() !== key);
    save(ingredients.value);
  }

  function clear(): void {
    ingredients.value = [];
    save(ingredients.value);
  }

  return { ingredients: readonly(ingredients), add, remove, clear };
}
