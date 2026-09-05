import { mountSuspended } from "@nuxt/test-utils/runtime";
import { describe, expect, it } from "vitest";
import RecipeIngredients from "./recipe-ingredients.vue";

describe("recipe-ingredients", () => {
  it("highlights missing ingredients and leaves owned ones plain", async () => {
    const wrapper = await mountSuspended(RecipeIngredients, {
      props: { ingredients: ["butter", "flour", "salt"], userIngredients: ["flour"] },
    });
    const items = wrapper.findAll("li");
    expect(items).toHaveLength(3);
    expect(items.at(0)?.text()).toContain("butter");
    expect(items.at(0)?.text()).toContain("missing");
    expect(items.at(1)?.text()).toContain("flour");
    expect(items.at(1)?.text()).not.toContain("missing");
  });

  it("never highlights pantry staples", async () => {
    const wrapper = await mountSuspended(RecipeIngredients, {
      props: { ingredients: ["salt", "olive oil"], userIngredients: [] },
    });
    expect(wrapper.text()).not.toContain("missing");
  });

  it("marks nothing missing when the user has everything", async () => {
    const wrapper = await mountSuspended(RecipeIngredients, {
      props: { ingredients: ["butter", "milk"], userIngredients: ["butter", "milk"] },
    });
    expect(wrapper.text()).not.toContain("missing");
  });
});
