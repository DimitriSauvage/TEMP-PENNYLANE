import { defineComponent, h, ref } from "vue";
import { mount } from "@vue/test-utils";
import type { VueWrapper } from "@vue/test-utils";
import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError } from "~/api/client";
import { suggestIngredients } from "~/api/generated/ingredients/ingredients";
import {
  fetchIngredientSuggestions,
  ingredientSuggestionsQueryOptions,
  useIngredientSuggestions,
} from "./use-ingredient-suggestions";
import type { UseIngredientSuggestionsReturn } from "./use-ingredient-suggestions";

vi.mock("~/api/generated/ingredients/ingredients", () => ({
  suggestIngredients: vi.fn(),
}));

function ok(names: string[]) {
  return { status: 200 as const, data: { names }, headers: new Headers() };
}

function errorStatus() {
  return {
    status: 500 as const,
    data: {
      timestamp: "2026-09-04T10:00:00Z",
      status: 500,
      error: "Internal Server Error",
      path: "/api/ingredients/suggest",
      code: "INTERNAL",
      message: "boom",
    },
    headers: new Headers(),
  };
}

// useQuery needs a QueryClient from the app context, so the composable runs
// inside a minimal host component with a fresh QueryClient per test.
const hosts: VueWrapper[] = [];

function setup(): UseIngredientSuggestionsReturn {
  const queryClient = new QueryClient();
  let list!: UseIngredientSuggestionsReturn;
  const host = mount(
    defineComponent({
      setup() {
        list = useIngredientSuggestions();
        return () => h("div");
      },
    }),
    { global: { plugins: [[VueQueryPlugin, { queryClient }]] } },
  );
  hosts.push(host);
  return list;
}

describe("useIngredientSuggestions", () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.mocked(suggestIngredients).mockReset();
  });

  afterEach(async () => {
    while (hosts.length) await hosts.pop()?.unmount();
    vi.useRealTimers();
  });

  it("does not fetch for an empty prefix", () => {
    const list = setup();
    list.search("");
    expect(list.suggestions.value).toEqual([]);
    expect(suggestIngredients).not.toHaveBeenCalled();
  });

  it("does not fetch for a whitespace-only prefix", () => {
    const list = setup();
    list.search("   ");
    expect(suggestIngredients).not.toHaveBeenCalled();
  });

  it("waits for the debounce delay before fetching", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(ok(["egg"]));
    const list = setup();
    list.search("e");
    expect(suggestIngredients).not.toHaveBeenCalled();
    await vi.advanceTimersByTimeAsync(300);
    expect(suggestIngredients).toHaveBeenCalledWith({ q: "e" }, expect.anything());
    expect(list.suggestions.value).toEqual(["egg"]);
  });

  it("trims the prefix before sending it", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(ok(["egg"]));
    const list = setup();
    list.search("  e ");
    await vi.advanceTimersByTimeAsync(300);
    expect(suggestIngredients).toHaveBeenCalledWith({ q: "e" }, expect.anything());
  });

  it("collapses rapid keystrokes into a single request for the last prefix", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(ok(["eggs"]));
    const list = setup();
    list.search("e");
    list.search("eg");
    list.search("egg");
    await vi.advanceTimersByTimeAsync(300);
    expect(suggestIngredients).toHaveBeenCalledTimes(1);
    expect(suggestIngredients).toHaveBeenCalledWith({ q: "egg" }, expect.anything());
    expect(list.suggestions.value).toEqual(["eggs"]);
  });

  it("clears suggestions when the prefix becomes too short again", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(ok(["egg"]));
    const list = setup();
    list.search("egg");
    await vi.advanceTimersByTimeAsync(300);
    expect(list.suggestions.value).toEqual(["egg"]);
    list.search("");
    await vi.advanceTimersByTimeAsync(0);
    expect(list.suggestions.value).toEqual([]);
  });

  it("shows no suggestion when the catalog returns no match", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(ok([]));
    const list = setup();
    list.search("xyz");
    await vi.advanceTimersByTimeAsync(300);
    expect(list.suggestions.value).toEqual([]);
  });

  it("shows no suggestion on an HTTP error status", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(errorStatus());
    const list = setup();
    list.search("e");
    await vi.advanceTimersByTimeAsync(300);
    expect(list.suggestions.value).toEqual([]);
  });

  it("clear() drops a pending debounced request", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(ok(["egg"]));
    const list = setup();
    list.search("e");
    await vi.advanceTimersByTimeAsync(150);
    list.clear();
    await vi.advanceTimersByTimeAsync(300);
    expect(suggestIngredients).not.toHaveBeenCalled();
    expect(list.suggestions.value).toEqual([]);
  });

  it("ignores a late response for an earlier prefix", async () => {
    let resolveFirst!: (value: ReturnType<typeof ok>) => void;
    const firstPromise = new Promise<ReturnType<typeof ok>>((resolve) => {
      resolveFirst = resolve;
    });
    vi.mocked(suggestIngredients).mockImplementation((params) =>
      params.q === "a" ? firstPromise : Promise.resolve(ok(["butter"])),
    );
    const list = setup();

    list.search("a");
    await vi.advanceTimersByTimeAsync(300); // 'a' request in flight
    list.search("b");
    await vi.advanceTimersByTimeAsync(300); // 'b' resolves first
    expect(list.suggestions.value).toEqual(["butter"]);

    resolveFirst(ok(["avocado"])); // stale 'a' response lands late
    await vi.advanceTimersByTimeAsync(0);
    expect(list.suggestions.value).toEqual(["butter"]);
  });

  it("stops searching while a request is in flight", async () => {
    let resolve!: (value: ReturnType<typeof ok>) => void;
    vi.mocked(suggestIngredients).mockReturnValue(
      new Promise<ReturnType<typeof ok>>((r) => {
        resolve = r;
      }),
    );
    const list = setup();
    expect(list.isSearching.value).toBe(false);
    list.search("e");
    await vi.advanceTimersByTimeAsync(300);
    expect(list.isSearching.value).toBe(true);
    resolve(ok(["egg"]));
    await vi.advanceTimersByTimeAsync(0);
    expect(list.isSearching.value).toBe(false);
  });
});

describe("fetchIngredientSuggestions", () => {
  beforeEach(() => {
    vi.mocked(suggestIngredients).mockReset();
  });

  it("returns the catalog names on success", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(ok(["egg", "eggs"]));
    await expect(fetchIngredientSuggestions("eg")).resolves.toEqual(["egg", "eggs"]);
  });

  it("throws an ApiError carrying the status and server message on an HTTP error", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(errorStatus());
    const error: unknown = await fetchIngredientSuggestions("e").catch((thrown: unknown) => thrown);
    expect(error).toBeInstanceOf(ApiError);
    expect(error).toHaveProperty("status", 500);
    expect(error).toHaveProperty("message", "boom");
  });

  it("forwards the abort signal to the generated client", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(ok(["egg"]));
    const controller = new AbortController();
    await fetchIngredientSuggestions("e", controller.signal);
    expect(suggestIngredients).toHaveBeenCalledWith({ q: "e" }, { signal: controller.signal });
  });
});

describe("ingredientSuggestionsQueryOptions", () => {
  it('embeds the debounced prefix under ["ingredients","suggest"]', () => {
    const prefix = ref("eg");
    const options = ingredientSuggestionsQueryOptions(prefix);
    expect(options.queryKey.value).toEqual(["ingredients", "suggest", "eg"]);
  });

  it("is disabled while the prefix is empty", () => {
    const prefix = ref("");
    const options = ingredientSuggestionsQueryOptions(prefix);
    expect(options.enabled.value).toBe(false);
  });

  it("is enabled once the prefix is long enough", () => {
    const prefix = ref("e");
    const options = ingredientSuggestionsQueryOptions(prefix);
    expect(options.enabled.value).toBe(true);
  });

  it("does not retry failed requests", () => {
    const prefix = ref("e");
    const options = ingredientSuggestionsQueryOptions(prefix);
    expect(options.retry).toBe(false);
  });
});
