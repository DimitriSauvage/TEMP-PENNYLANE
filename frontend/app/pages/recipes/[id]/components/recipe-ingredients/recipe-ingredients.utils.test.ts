import { describe, expect, it } from "vitest";
import { computeMissingIngredients } from "./recipe-ingredients.utils";

describe("computeMissingIngredients", () => {
  describe("happy path", () => {
    it("returns an empty list when the user has every ingredient", () => {
      expect(computeMissingIngredients(["butter", "milk"], ["butter", "milk"])).toEqual([]);
    });

    it("returns only the ingredients the user lacks", () => {
      expect(computeMissingIngredients(["butter", "milk", "flour"], ["flour"])).toEqual([
        "butter",
        "milk",
      ]);
    });

    it("returns every ingredient when the user list is empty", () => {
      expect(computeMissingIngredients(["butter", "milk"], [])).toEqual(["butter", "milk"]);
    });
  });

  describe("pantry staples", () => {
    it("never marks staples as missing, even when the user list is empty", () => {
      expect(
        computeMissingIngredients(["salt", "pepper", "water", "olive oil", "butter"], []),
      ).toEqual(["butter"]);
    });

    it("matches staples case-insensitively", () => {
      expect(computeMissingIngredients(["Salt", "Olive Oil", "butter"], [])).toEqual(["butter"]);
    });
  });

  describe("boundary conditions", () => {
    it("handles an empty recipe ingredient list", () => {
      expect(computeMissingIngredients([], ["butter"])).toEqual([]);
    });

    it("handles both lists empty", () => {
      expect(computeMissingIngredients([], [])).toEqual([]);
    });
  });

  describe("normalization", () => {
    it("compares ingredient names case-insensitively", () => {
      expect(computeMissingIngredients(["Butter"], ["butter"])).toEqual([]);
      expect(computeMissingIngredients(["butter"], ["BUTTER"])).toEqual([]);
    });

    it("ignores surrounding whitespace on both sides", () => {
      expect(computeMissingIngredients([" butter "], ["butter"])).toEqual([]);
      expect(computeMissingIngredients(["butter"], [" butter "])).toEqual([]);
    });
  });

  describe("output invariants", () => {
    it("preserves the recipe spelling of missing ingredients", () => {
      expect(computeMissingIngredients(["Butter", "Milk"], [])).toEqual(["Butter", "Milk"]);
    });

    it("keeps the recipe order of missing ingredients", () => {
      expect(computeMissingIngredients(["milk", "butter", "eggs"], ["eggs"])).toEqual([
        "milk",
        "butter",
      ]);
    });
  });
});
