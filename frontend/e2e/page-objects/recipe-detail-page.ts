import { expect, type Locator, type Page } from "@playwright/test";

/** Page object for `/recipes/:id`. */
export class RecipeDetailPage {
  constructor(private readonly page: Page) {}

  get backLink(): Locator {
    return this.page.getByRole("button", { name: "Back" });
  }

  /** Ingredients landmark (section labelled by the "Ingredients" heading). */
  get ingredientsRegion(): Locator {
    return this.page.getByRole("region", { name: "Ingredients" });
  }

  ingredient(name: string): Locator {
    return this.ingredientsRegion.getByRole("listitem").filter({ hasText: name });
  }

  /** Assert the detail view of `name` is rendered at /recipes/:id. */
  async expectRecipe(name: string): Promise<void> {
    await expect(this.page).toHaveURL(/\/recipes\/\d+$/);
    await expect(this.page.getByRole("heading", { name })).toBeVisible();
    await expect(this.page.getByRole("img", { name })).toBeVisible();
  }
}
