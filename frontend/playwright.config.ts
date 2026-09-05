import { defineConfig, devices } from "@playwright/test";

/**
 * Hermetic E2E: the backend API is mocked at the network layer by each spec
 * (see e2e/mocks/api.ts); Playwright boots the Nuxt dev server itself.
 *
 * NUXT_PUBLIC_API_BASE points the app at its own origin so API calls are
 * same-origin; the mocks also send permissive CORS headers so they keep
 * working against an already-running dev server started with the default
 * http://localhost:8080 base.
 */
export default defineConfig({
  testDir: "./e2e/tests",
  fullyParallel: true,
  retries: process.env.CI ? 2 : 0,
  reporter: [["html"], ["github"]],
  use: {
    baseURL: "http://localhost:3000",
    trace: "on-first-retry",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  webServer: {
    command: "npm run dev",
    url: "http://localhost:3000",
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    env: { ...process.env, NUXT_PUBLIC_API_BASE: "http://localhost:3000" },
  },
});
