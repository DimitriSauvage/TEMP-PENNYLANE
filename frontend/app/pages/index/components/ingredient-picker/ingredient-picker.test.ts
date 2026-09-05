import { flushPromises } from "@vue/test-utils";
import type { DOMWrapper } from "@vue/test-utils";
import { mountSuspended } from "@nuxt/test-utils/runtime";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { suggestIngredients } from "~/api/generated/ingredients/ingredients";
import { useIngredientList } from "~/composables/use-ingredient-list";
import IngredientPicker from "./ingredient-picker.vue";

vi.mock("~/api/generated/ingredients/ingredients", () => ({
  suggestIngredients: vi.fn(),
}));

const STORAGE_KEY = "recipe-finder.ingredients";

function ok(names: string[]) {
  return { status: 200 as const, data: { names }, headers: new Headers() };
}

/** Type into the autocomplete input and let the debounce fire. */
async function type(wrapper: Awaited<ReturnType<typeof mountSuspended>>, value: string) {
  vi.useFakeTimers();
  await wrapper.find("input").setValue(value);
  await vi.advanceTimersByTimeAsync(300);
  vi.useRealTimers();
  await flushPromises();
}

function visibleOptions(): string[] {
  return [...document.querySelectorAll('[role="option"]')].map(
    (option) => option.textContent?.trim() ?? "",
  );
}

// mountSuspended leaves the app mounted; unmount the previous wrapper so its
// teleported overlay does not leak into document-level assertions.
let activeWrapper: Awaited<ReturnType<typeof mountSuspended>> | undefined;

async function mountPicker() {
  await activeWrapper?.unmount();
  activeWrapper = await mountSuspended(IngredientPicker);
  return activeWrapper;
}

describe("ingredient-picker", () => {
  beforeEach(() => {
    window.localStorage.clear();
    useIngredientList().clear();
    vi.mocked(suggestIngredients).mockReset();
  });

  afterEach(async () => {
    await activeWrapper?.unmount();
    activeWrapper = undefined;
  });

  it("shows the staples hint", async () => {
    const wrapper = await mountPicker();
    expect(wrapper.text()).toContain(
      "Salt, pepper, water and olive oil are assumed in your kitchen — no need to add them.",
    );
  });

  it("shows catalog suggestions while typing", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(ok(["egg", "eggs"]));
    const wrapper = await mountPicker();
    await type(wrapper, "eg");
    expect(suggestIngredients).toHaveBeenCalledWith({ q: "eg" }, expect.anything());
    expect(visibleOptions()).toEqual(["egg", "eggs"]);
  });

  it("shows no catalog suggestion when the prefix matches nothing", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(ok([]));
    const wrapper = await mountPicker();
    await type(wrapper, "xyz");
    // PrimeVue renders its built-in "No results found" message; no catalog name is offered.
    expect(visibleOptions()).toEqual(["No results found"]);
  });

  it("adds nothing when the prefix matches nothing", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(ok([]));
    const wrapper = await mountPicker();
    await type(wrapper, "xyz");
    expect(window.localStorage.getItem(STORAGE_KEY)).toBe("[]");
    expect(wrapper.text()).not.toContain("Your ingredients");
  });

  it("adds the selected suggestion and clears the input", async () => {
    vi.mocked(suggestIngredients).mockResolvedValue(ok(["egg"]));
    const wrapper = await mountPicker();
    await type(wrapper, "e");
    document.querySelector<HTMLElement>('[role="option"]')?.click();
    await flushPromises();
    expect(window.localStorage.getItem(STORAGE_KEY)).toBe('["egg"]');
    expect(wrapper.text()).toContain("Your ingredients");
    expect((wrapper.find("input").element as HTMLInputElement).value).toBe("");
  });

  it("does not offer already-added ingredients as suggestions", async () => {
    useIngredientList().add("egg");
    vi.mocked(suggestIngredients).mockResolvedValue(ok(["egg", "eggs"]));
    const wrapper = await mountPicker();
    await type(wrapper, "egg");
    expect(visibleOptions()).toEqual(["eggs"]);
  });

  it("removes a single ingredient from its chip", async () => {
    useIngredientList().add("egg");
    useIngredientList().add("milk");
    const wrapper = await mountPicker();
    await wrapper.find('button[aria-label="Remove egg"]').trigger("click");
    expect(window.localStorage.getItem(STORAGE_KEY)).toBe('["milk"]');
  });

  it("clears every ingredient", async () => {
    useIngredientList().add("egg");
    useIngredientList().add("milk");
    const wrapper = await mountPicker();
    const clearAll = wrapper
      .findAll("button")
      .find((button: DOMWrapper<Element>) => button.text() === "Clear all");
    expect(clearAll).toBeDefined();
    await clearAll?.trigger("click");
    expect(window.localStorage.getItem(STORAGE_KEY)).toBe("[]");
    expect(wrapper.text()).not.toContain("Your ingredients");
  });
});
