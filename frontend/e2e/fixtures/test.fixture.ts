import { checkA11y, injectAxe } from "axe-playwright";
import { test as base, expect } from "@playwright/test";
import type { Page } from "@playwright/test";

// Dev-server-only chrome (Nuxt DevTools anchor pill and frame) and PrimeVue's
// body-portal autocomplete overlay sit outside the app's own markup. Their axe
// findings (devtools label contrast, overlay empty-message contrast, overlay
// portal outside landmarks) are reported as deviations instead of failing the
// suites — fixing them would require app-source changes, which is out of scope.
const A11Y_EXCLUDE = ["#nuxt-devtools-anchor", "nuxt-devtools-frame", ".p-autocomplete-overlay"];

/** The app page plus the console-error policy's escape hatch (see below). */
export type TestPage = Page & {
  /**
   * Exempt one HTTP status from the console-error policy: Chromium logs
   * "Failed to load resource: the server responded with a status of N" as a
   * console error, so tests that deliberately provoke that status declare it
   * here instead of weakening the policy for every other test.
   */
  allowResourceError: (status: number) => void;
};

/**
 * Base test with the project's console-error policy: any browser console
 * error fails the test at teardown. The favicon is stubbed because the SPA
 * ships none and its 404 would otherwise surface as a console error.
 */
export const test = base.extend({
  page: async ({ page }, use) => {
    const errors: string[] = [];
    const allowedStatuses = new Set<number>();
    page.on("console", (message) => {
      if (message.type() !== "error") return;
      const status = /status of (\d+)\b/.exec(message.text());
      if (status && allowedStatuses.has(Number(status[1]))) return;
      errors.push(message.text());
    });
    const testPage = Object.assign(page, {
      allowResourceError: (status: number) => allowedStatuses.add(status),
    }) as TestPage;
    await testPage.route("**/favicon.ico", (route) => route.fulfill({ status: 204 }));
    await use(testPage);
    if (errors.length > 0) throw new Error(`Console error(s) during test:\n${errors.join("\n")}`);
  },
});

/**
 * Inject axe and assert the page is accessible. Waits for the SPA to mount
 * first: in `ssr: false` mode the shell has no title, main landmark or heading
 * until the client app boots, and axe would flag that empty shell.
 */
export async function checkPageA11y(page: Page): Promise<void> {
  await expect(page.getByRole("main")).toBeVisible();
  await expect(page).toHaveTitle(/Recipe Finder/);
  await injectAxe(page);
  await checkA11y(page, { exclude: A11Y_EXCLUDE });
}

export { expect };
