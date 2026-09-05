import { mountSuspended } from "@nuxt/test-utils/runtime";
import { beforeEach, describe, expect, it } from "vitest";
import { useIngredientList } from "~/composables/use-ingredient-list";
import SearchFilters from "./search-filters.vue";

describe("search-filters", () => {
  beforeEach(() => {
    window.localStorage.clear();
    useIngredientList().clear();
  });

  it("renders the ingredient picker and the ready-to-cook toggle", async () => {
    const wrapper = await mountSuspended(SearchFilters, { props: { readyToCookOnly: false } });
    expect(wrapper.text()).toContain("Add an ingredient");
    expect(wrapper.text()).toContain("Ready to cook only");
  });

  it("wires the toggle label to the checkbox input via matching ids", async () => {
    const wrapper = await mountSuspended(SearchFilters, { props: { readyToCookOnly: false } });
    const input = wrapper.find('input[type="checkbox"]');
    expect(input.exists()).toBe(true);
    const label = wrapper.find(`label[for="${input.attributes("id")}"]`);
    expect(label.exists()).toBe(true);
    expect(label.text()).toContain("Ready to cook only");
  });

  it("emits the new toggle value when the switch is flipped", async () => {
    const wrapper = await mountSuspended(SearchFilters, { props: { readyToCookOnly: false } });
    await wrapper.find('input[type="checkbox"]').setValue(true);
    expect(wrapper.emitted("update:readyToCookOnly")).toEqual([[true]]);
  });
});
