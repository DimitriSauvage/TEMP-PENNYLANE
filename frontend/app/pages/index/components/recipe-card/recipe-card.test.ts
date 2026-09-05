import { mountSuspended } from "@nuxt/test-utils/runtime";
import { describe, expect, it } from "vitest";
import type { RecipeSearchItemDto } from "~/api/generated/client.schemas";
import RecipeCard from "./recipe-card.vue";

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

describe("recipe-card", () => {
  it("shows a Ready to cook badge when nothing is missing", async () => {
    const wrapper = await mountSuspended(RecipeCard, { props: { recipe: item() } });
    expect(wrapper.text()).toContain("Ready to cook");
    expect(wrapper.text()).not.toContain("missing");
  });

  it("shows the match status line for a partial match", async () => {
    const wrapper = await mountSuspended(RecipeCard, {
      props: {
        recipe: item({
          matchedIngredients: 2,
          missingIngredients: ["butter", "milk"],
          readyToCook: false,
        }),
      },
    });
    expect(wrapper.text()).toContain("you have 2 of 3 — missing: butter, milk");
    expect(wrapper.text()).not.toContain("Ready to cook");
  });

  it("links to the recipe detail page", async () => {
    const wrapper = await mountSuspended(RecipeCard, { props: { recipe: item() } });
    expect(wrapper.find("a").attributes("href")).toBe("/recipes/r1");
  });

  it("uses the recipe name as the image alt text", async () => {
    const wrapper = await mountSuspended(RecipeCard, {
      props: { recipe: item({ imageUrl: "https://example.com/pancakes.jpg" }) },
    });
    expect(wrapper.find("img").attributes("alt")).toBe("Pancakes");
  });

  it("loads grid images lazily and decodes them off the main thread", async () => {
    const wrapper = await mountSuspended(RecipeCard, {
      props: { recipe: item({ imageUrl: "https://example.com/pancakes.jpg" }) },
    });
    expect(wrapper.find("img").attributes("loading")).toBe("lazy");
    expect(wrapper.find("img").attributes("decoding")).toBe("async");
  });

  it("renders a placeholder instead of a broken image when the recipe has none", async () => {
    const wrapper = await mountSuspended(RecipeCard, { props: { recipe: item() } });
    expect(wrapper.find("img").exists()).toBe(false);
  });
});
