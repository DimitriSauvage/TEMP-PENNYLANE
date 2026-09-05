import type {
  RecipeDetailResponseDto,
  RecipeSearchItemDto,
  RecipeSearchResponseDto,
} from "../../app/api/generated/client.schemas";

/** Page size the UI requests (mirrors SEARCH_PAGE_SIZE in use-recipe-search). */
const SEARCH_PAGE_SIZE = 12;

/** 1x1 transparent GIF: exercises <img> rendering without any network request. */
export const PIXEL_IMAGE_URL =
  "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";

/** Ingredient catalog served by the mocked suggest endpoint. */
export const SUGGEST_CATALOG = ["butter", "chicken", "chickpeas"];

/** Ready to cook: the user has everything (0 missing). */
export const READY_PANCAKES: RecipeSearchItemDto = {
  id: "1",
  name: "Perfect Pancakes",
  totalIngredients: 3,
  matchedIngredients: 3,
  missingIngredients: [],
  readyToCook: true,
};

/** Partial match: 1 missing ingredient (cheese). */
export const PARTIAL_OMELETTE: RecipeSearchItemDto = {
  id: "2",
  name: "Cheesy Omelette",
  totalIngredients: 3,
  matchedIngredients: 2,
  missingIngredients: ["cheese"],
  readyToCook: false,
};

/** Partial match: 2 missing ingredients (carrot, celery). */
export const PARTIAL_STEW: RecipeSearchItemDto = {
  id: "3",
  name: "Hearty Stew",
  totalIngredients: 4,
  matchedIngredients: 2,
  missingIngredients: ["carrot", "celery"],
  readyToCook: false,
};

/** Only non-user ingredient is salt — a staple, so nothing is missing. */
export const STAPLE_RICE: RecipeSearchItemDto = {
  id: "4",
  name: "Buttered Rice",
  totalIngredients: 2,
  matchedIngredients: 2,
  missingIngredients: [],
  readyToCook: true,
};

/** Detail of PARTIAL_OMELETTE: cheese missing, salt a staple. */
export const OMELETTE_DETAIL: RecipeDetailResponseDto = {
  id: "2",
  name: "Cheesy Omelette",
  imageUrl: PIXEL_IMAGE_URL,
  ingredients: ["butter", "egg", "cheese", "salt"],
  instructions: ["Melt the butter.", "Whisk the eggs.", "Fold in the cheese."],
};

/** 14 ready recipes — more than one page of 12. */
export const PAGED_RESULTS: RecipeSearchItemDto[] = Array.from({ length: 14 }, (_, index) => ({
  id: String(index + 1),
  name: `Paged Recipe ${String(index + 1).padStart(2, "0")}`,
  totalIngredients: 1,
  matchedIngredients: 1,
  missingIngredients: [],
  readyToCook: true,
}));

/** Build a one-page search response over `items`. */
export function searchResponse(
  items: readonly RecipeSearchItemDto[],
  totalElements = items.length,
  page = 0,
): RecipeSearchResponseDto {
  return { page, size: SEARCH_PAGE_SIZE, totalElements, items: [...items] };
}

/** Slice PAGED_RESULTS into the page the UI requested. */
export function pagedSearchResponse(request: { page: number }): RecipeSearchResponseDto {
  const start = request.page * SEARCH_PAGE_SIZE;
  return searchResponse(
    PAGED_RESULTS.slice(start, start + SEARCH_PAGE_SIZE),
    PAGED_RESULTS.length,
    request.page,
  );
}
