import { beforeEach, describe, expect, it, vi } from "vitest";

const STORAGE_KEY = "recipe-finder.ingredients";

/**
 * The ingredient list is a module-level singleton, so each test re-imports the
 * module (vi.resetModules) to simulate a fresh page load reading localStorage.
 */
async function freshList() {
  const mod = await import("./use-ingredient-list");
  return mod.useIngredientList();
}

describe("useIngredientList", () => {
  beforeEach(() => {
    window.localStorage.clear();
    vi.restoreAllMocks();
    vi.resetModules();
  });

  describe("add", () => {
    it("adds a new ingredient and reports success", async () => {
      const list = await freshList();
      expect(list.add("egg")).toBe(true);
      expect(list.ingredients.value).toEqual(["egg"]);
    });

    it("trims surrounding whitespace before storing", async () => {
      const list = await freshList();
      expect(list.add("  egg  ")).toBe(true);
      expect(list.ingredients.value).toEqual(["egg"]);
    });

    it("rejects an empty name", async () => {
      const list = await freshList();
      expect(list.add("")).toBe(false);
      expect(list.add("   ")).toBe(false);
      expect(list.ingredients.value).toEqual([]);
    });

    it("rejects a duplicate, case-insensitively", async () => {
      const list = await freshList();
      list.add("egg");
      expect(list.add("Egg")).toBe(false);
      expect(list.add(" egg ")).toBe(false);
      expect(list.ingredients.value).toEqual(["egg"]);
    });
  });

  describe("remove", () => {
    it("removes a single ingredient, case-insensitively", async () => {
      const list = await freshList();
      list.add("egg");
      list.add("milk");
      list.remove("EGG");
      expect(list.ingredients.value).toEqual(["milk"]);
    });

    it("is a no-op for an unknown ingredient", async () => {
      const list = await freshList();
      list.add("egg");
      list.remove("caviar");
      expect(list.ingredients.value).toEqual(["egg"]);
    });
  });

  describe("clear", () => {
    it("empties the list", async () => {
      const list = await freshList();
      list.add("egg");
      list.add("milk");
      list.clear();
      expect(list.ingredients.value).toEqual([]);
    });
  });

  describe("localStorage persistence", () => {
    it("saves the list on every mutation", async () => {
      const list = await freshList();
      list.add("egg");
      list.add("milk");
      expect(window.localStorage.getItem(STORAGE_KEY)).toBe('["egg","milk"]');
      list.remove("egg");
      expect(window.localStorage.getItem(STORAGE_KEY)).toBe('["milk"]');
      list.clear();
      expect(window.localStorage.getItem(STORAGE_KEY)).toBe("[]");
    });

    it("reloads the persisted list on re-instantiation (page reload)", async () => {
      window.localStorage.setItem(STORAGE_KEY, '["flour","butter"]');
      const list = await freshList();
      expect(list.ingredients.value).toEqual(["flour", "butter"]);
    });

    it("starts empty when nothing was persisted", async () => {
      const list = await freshList();
      expect(list.ingredients.value).toEqual([]);
    });

    it("starts empty on corrupted JSON instead of throwing", async () => {
      window.localStorage.setItem(STORAGE_KEY, '{"unterminated');
      const list = await freshList();
      expect(list.ingredients.value).toEqual([]);
    });

    it("starts empty when the persisted value is not an array", async () => {
      window.localStorage.setItem(STORAGE_KEY, '"eggs"');
      const list = await freshList();
      expect(list.ingredients.value).toEqual([]);
    });

    it("drops non-string entries from a corrupted array", async () => {
      window.localStorage.setItem(STORAGE_KEY, '["egg",5,null]');
      const list = await freshList();
      expect(list.ingredients.value).toEqual(["egg"]);
    });
  });

  describe("defensive persistence", () => {
    it("keeps the in-memory list working when localStorage writes throw", async () => {
      const list = await freshList();
      // Spy on the instance: this environment's localStorage shadows the
      // Storage prototype methods with own properties, so a prototype spy
      // does not intercept writes.
      vi.spyOn(window.localStorage, "setItem").mockImplementation(() => {
        throw new DOMException("Quota exceeded", "QuotaExceededError");
      });
      expect(() => list.add("egg")).not.toThrow();
      expect(list.ingredients.value).toEqual(["egg"]);
      expect(() => list.remove("egg")).not.toThrow();
      expect(list.ingredients.value).toEqual([]);
    });
  });
});
