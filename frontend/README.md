# Recipe Finder — Frontend

Nuxt 4 SPA (`ssr: false`). English UI, no auth, no i18n framework.

## Commands

| Command                           | Purpose                                                                                                                                    |
| --------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| `npm install`                     | Install dependencies                                                                                                                       |
| `npm run gen:api`                 | Regenerate the typed API client from `openapi/api.json` (Orval → `app/api/generated/`, gitignored, read-only)                              |
| `npm run dev`                     | Dev server on http://localhost:3000                                                                                                        |
| `npm run test`                    | Vitest (`@nuxt/test-utils` nuxt environment + happy-dom); regenerates the API client first                                                 |
| `npm run lint` / `npm run format` | ESLint / Prettier                                                                                                                          |
| `npm run build`                   | SPA build (regenerates the API client first) → `.output/public/`                                                                           |
| `npm run generate`                | Static SPA build for static hosting (regenerates the API client first) → `.output/public/`; the build path `frontend/Dockerfile` ships |

API base URL: public runtime config `apiBase` (default `http://localhost:8080`), override with
`NUXT_PUBLIC_API_BASE`.
