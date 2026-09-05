import { mountSuspended } from "@nuxt/test-utils/runtime";
import { describe, expect, it } from "vitest";
import type { RecipeDetailResponseDto } from "~/api/generated/client.schemas";
import RecipeDetail from "./recipe-detail.vue";

const recipe: RecipeDetailResponseDto = {
  id: "42",
  name: "Omelette",
  imageUrl: "https://example.com/omelette.jpg",
  ingredients: ["egg", "butter", "salt"],
  instructions: ["Beat the eggs.", "Fry in butter."],
};

// exactOptionalPropertyTypes forbids `imageUrl: undefined`, so omit the key.
const recipeWithoutImage: RecipeDetailResponseDto = {
  id: "42",
  name: "Omelette",
  ingredients: ["egg", "butter", "salt"],
  instructions: ["Beat the eggs.", "Fry in butter."],
};

describe("recipe-detail", () => {
  it("renders the recipe name as the article heading", async () => {
    const wrapper = await mountSuspended(RecipeDetail, {
      props: { recipe, userIngredients: [] },
    });
    expect(wrapper.find("article > h2").text()).toBe("Omelette");
  });

  it("renders the image with the recipe name as alt text", async () => {
    const wrapper = await mountSuspended(RecipeDetail, {
      props: { recipe, userIngredients: [] },
    });
    expect(wrapper.find("img").attributes("src")).toBe("https://example.com/omelette.jpg");
    expect(wrapper.find("img").attributes("alt")).toBe("Omelette");
  });

  it("omits the image element when the recipe has none", async () => {
    const wrapper = await mountSuspended(RecipeDetail, {
      props: { recipe: recipeWithoutImage, userIngredients: [] },
    });
    expect(wrapper.find("img").exists()).toBe(false);
  });

  it("marks ingredients the user lacks as missing", async () => {
    const wrapper = await mountSuspended(RecipeDetail, {
      props: { recipe, userIngredients: ["egg"] },
    });
    expect(wrapper.text()).toContain("butter — missing");
    expect(wrapper.text()).not.toContain("egg — missing");
  });

  it("renders the preparation steps as an ordered list", async () => {
    const wrapper = await mountSuspended(RecipeDetail, {
      props: { recipe, userIngredients: [] },
    });
    const steps = wrapper.findAll("ol li");
    expect(steps.map((step) => step.text())).toEqual(["Beat the eggs.", "Fry in butter."]);
  });

  it("shows an under-development notice instead of steps when the recipe has no instructions", async () => {
    const wrapper = await mountSuspended(RecipeDetail, {
      props: { recipe: { ...recipe, instructions: [] }, userIngredients: [] },
    });
    expect(wrapper.text()).toContain("Under development :)");
    expect(wrapper.find("ol").exists()).toBe(false);
  });
});
