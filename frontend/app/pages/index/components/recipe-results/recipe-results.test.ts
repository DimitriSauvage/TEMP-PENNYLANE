import { mountSuspended } from "@nuxt/test-utils/runtime";
import { describe, expect, it } from "vitest";
import type { RecipeSearchItemDto } from "~/api/generated/client.schemas";
import RecipeResults from "./recipe-results.vue";
import type { RecipeResultsProps } from "./recipe-results.types";

function item(overrides: Partial<RecipeSearchItemDto> = {}): RecipeSearchItemDto {
  return {
    id: "r1",
    name: "Pancakes",
    totalIngredients: 3,
    matchedIngredients: 3,
    missingIngredients: [],
    readyToCook: true,
    ...overrides,
  };
}

// No explicit return type: an inferred object-literal type carries an implicit
// index signature, which mountSuspended's props option requires.
function props(overrides: Partial<RecipeResultsProps> = {}) {
  return {
    results: [] as readonly RecipeSearchItemDto[],
    totalElements: 0,
    isPending: false,
    isError: false,
    isFetching: false,
    error: null as Error | null,
    hasIngredients: true,
    readyToCookOnly: false,
    page: 0,
    ...overrides,
  };
}

describe("recipe-results", () => {
  it("prompts to add ingredients when the list is empty", async () => {
    const wrapper = await mountSuspended(RecipeResults, {
      props: props({ hasIngredients: false }),
    });
    expect(wrapper.text()).toContain("Add what you have at home to find recipes you can cook.");
  });

  it("shows a full-page skeleton grid while the first load is pending", async () => {
    const wrapper = await mountSuspended(RecipeResults, { props: props({ isPending: true }) });
    const grid = wrapper.find('[aria-busy="true"]');
    expect(grid.exists()).toBe(true);
    // One skeleton card per result slot on the page.
    expect(grid.findAll(":scope > div").length).toBe(12);
  });

  it("surfaces the error message with a retry button when the search failed", async () => {
    const wrapper = await mountSuspended(RecipeResults, {
      props: props({ isError: true, error: new Error("Provide at least one usable ingredient.") }),
    });
    expect(wrapper.find('[role="alert"]').exists()).toBe(true);
    expect(wrapper.text()).toContain("Provide at least one usable ingredient.");
    const retry = wrapper.findAll("button").find((button) => button.text() === "Try again");
    expect(retry).toBeDefined();
    await retry!.trigger("click");
    expect(wrapper.emitted("retry")).toHaveLength(1);
  });

  it("falls back to the generic message when the error carries none", async () => {
    const wrapper = await mountSuspended(RecipeResults, {
      props: props({ isError: true, error: new Error("") }),
    });
    expect(wrapper.text()).toContain("Something went wrong while loading recipes.");
  });

  it("shows the ready-to-cook-only hint when the toggle filters everything out", async () => {
    const wrapper = await mountSuspended(RecipeResults, {
      props: props({ readyToCookOnly: true }),
    });
    expect(wrapper.text()).toContain(
      "No recipe uses ALL your ingredients — try turning off 'Ready to cook only'.",
    );
  });

  it("shows the no-match state with a hint when the search returns nothing", async () => {
    const wrapper = await mountSuspended(RecipeResults, { props: props() });
    expect(wrapper.text()).toContain("No recipe matches these ingredients.");
    expect(wrapper.text()).toContain("Try removing some ingredients");
  });

  it("renders recipe cards without a paginator when results fit one page", async () => {
    const wrapper = await mountSuspended(RecipeResults, {
      props: props({ results: [item()], totalElements: 1 }),
    });
    expect(wrapper.find('a[href="/recipes/r1"]').exists()).toBe(true);
    expect(wrapper.find(".p-paginator").exists()).toBe(false);
  });

  it("emits the zero-based page when a paginator page is clicked", async () => {
    const wrapper = await mountSuspended(RecipeResults, {
      props: props({ results: [item()], totalElements: 24 }),
    });
    expect(wrapper.find(".p-paginator").exists()).toBe(true);
    const page2 = wrapper.findAll("button").find((button) => button.text() === "2");
    await page2!.trigger("click");
    expect(wrapper.emitted("page-change")).toEqual([[1]]);
  });
});
