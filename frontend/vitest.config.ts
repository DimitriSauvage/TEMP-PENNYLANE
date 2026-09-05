import { configDefaults } from "vitest/config";
import { defineVitestConfig } from "@nuxt/test-utils/config";

export default defineVitestConfig({
  test: {
    environment: "nuxt",
    environmentOptions: {
      nuxt: {
        domEnvironment: "happy-dom",
      },
    },
    // Playwright owns e2e/**/*.spec.ts; keep them out of the Vitest run.
    exclude: [...configDefaults.exclude, "e2e/**"],
  },
});
