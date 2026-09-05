import { expect, type Locator, type Page } from "@playwright/test";

/** Page object for `/`: ingredient picker, ready-to-cook toggle, ranked results. */
export class SearchPage {
  constructor(private readonly page: Page) {}

  async goto(): Promise<void> {
    await this.page.goto("/");
  }

  /** Empty-state prompt shown before the first ingredient. */
  get emptyPrompt(): Locator {
    return this.page.getByText("Add what you have at home to find recipes you can cook.");
  }

  /** Pantry staples hint under the autocomplete. */
  get staplesHint(): Locator {
    return this.page.getByText("Salt, pepper, water and olive oil are assumed in your kitchen");
  }

  get readyToCookToggle(): Locator {
    return this.page.getByRole("switch", { name: "Ready to cook only" });
  }

  /** Results landmark (section labelled by the "Recipes" heading). */
  get resultsRegion(): Locator {
    return this.page.getByRole("region", { name: "Recipes" });
  }

  /** Recipe cards, in ranked order. */
  get resultCards(): Locator {
    return this.resultsRegion.getByRole("link");
  }

  card(name: string): Locator {
    return this.resultCards.filter({ hasText: name });
  }

  /** Remove button of one ingredient chip. */
  chip(name: string): Locator {
    return this.page.getByRole("button", { name: `Remove ${name}` });
  }

  /** All ingredient chip remove buttons. */
  get chips(): Locator {
    return this.page.getByRole("button", { name: /^Remove / });
  }

  /** Type a prefix and pick `name` from the catalog suggestions. */
  async addIngredient(prefix: string, name: string): Promise<void> {
    await this.page.getByLabel("Add an ingredient").fill(prefix);
    await this.page.getByRole("option", { name, exact: true }).click();
    await expect(this.chip(name)).toBeVisible();
  }
}
