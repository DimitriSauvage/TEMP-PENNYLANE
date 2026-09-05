import { describe, expect, it } from "vitest";
import type { RecipeSearchItemDto } from "~/api/generated/client.schemas";
import { formatMatchStatus } from "./recipe-card.utils";

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

describe("formatMatchStatus", () => {
  describe("happy path", () => {
    it("returns the ready-to-cook label when nothing is missing", () => {
      expect(formatMatchStatus(item())).toBe("Ready to cook");
    });

    it("describes a partial match with matched counts and missing names", () => {
      expect(
        formatMatchStatus(
          item({
            matchedIngredients: 2,
            missingIngredients: ["butter", "milk"],
            readyToCook: false,
          }),
        ),
      ).toBe("you have 2 of 3 — missing: butter, milk");
    });

    it("lists a single missing ingredient without truncation", () => {
      expect(
        formatMatchStatus(
          item({
            matchedIngredients: 2,
            missingIngredients: ["butter"],
            readyToCook: false,
          }),
        ),
      ).toBe("you have 2 of 3 — missing: butter");
    });

    it("lists up to three missing ingredients without truncation", () => {
      expect(
        formatMatchStatus(
          item({
            matchedIngredients: 0,
            missingIngredients: ["butter", "milk", "eggs"],
            readyToCook: false,
          }),
        ),
      ).toBe("you have 0 of 3 — missing: butter, milk, eggs");
    });
  });

  describe("truncation", () => {
    it("appends +1 more for four missing ingredients", () => {
      expect(
        formatMatchStatus(
          item({
            matchedIngredients: 0,
            missingIngredients: ["butter", "milk", "eggs", "flour"],
            readyToCook: false,
          }),
        ),
      ).toBe("you have 0 of 3 — missing: butter, milk, eggs +1 more");
    });

    it("appends +n more beyond the first three missing ingredients", () => {
      expect(
        formatMatchStatus(
          item({
            totalIngredients: 5,
            matchedIngredients: 0,
            missingIngredients: ["a", "b", "c", "d", "e"],
            readyToCook: false,
          }),
        ),
      ).toBe("you have 0 of 5 — missing: a, b, c +2 more");
    });
  });

  describe("type invariants", () => {
    it("always returns a non-empty string", () => {
      const ready = formatMatchStatus(item());
      const partial = formatMatchStatus(
        item({ matchedIngredients: 1, missingIngredients: ["x"], readyToCook: false }),
      );
      expect(ready.length).toBeGreaterThan(0);
      expect(partial.length).toBeGreaterThan(0);
      expect(typeof ready).toBe("string");
      expect(typeof partial).toBe("string");
    });
  });
});
