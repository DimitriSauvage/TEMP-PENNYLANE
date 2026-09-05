import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import AppTitle from "./app-title.vue";

describe("app-title", () => {
  it("renders the app title", () => {
    const wrapper = mount(AppTitle);
    expect(wrapper.text()).toContain("Recipe Finder");
  });
});
