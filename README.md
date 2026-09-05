# Problem statement

### _It's dinner time!_ Create an application that helps users find the most relevant recipes that they can prepare with the ingredients that they have at home

## Objective

Deliver a prototype web application to answer the above problem statement.

__✅ Must have's__

- A back-end with Ruby on Rails (If you don't know Ruby on Rails, refer to the FAQ)
- A PostgreSQL relational database
- A well-thought user experience

__🚫 Don'ts__

- Excessive effort in styling
- Features which don't directly answer the above statement
- Over-engineer your prototype

## Deliverable

- The codebase should be pushed on the current GitHub private repository.
- 2 or 3 user stories that address the statement in your repo's `README.md`.
- The application accessible online (a personal server, fly.io or something else). If you can't deploy your app online, refer to the FAQ)
- Submission of the above via [this form](https://forms.gle/siH7Rezuq2V1mUJGA).
- If you're on Mac, make sure your browser has [permission to share the screen](https://support.apple.com/en-al/guide/mac-help/mchld6aa7d23/mac).


## Data

Please start from the following dataset to perform the assignment:
[english-language recipes](https://pennylane-interviewing-assets-20220328.s3.eu-west-1.amazonaws.com/recipes-en.json.gz) scraped from www.allrecipes.com with [recipe-scrapers](https://github.com/hhursev/recipe-scrapers)

Download it with this command if the above link doesn't work:
```sh textWrap
wget https://pennylane-interviewing-assets-20220328.s3.eu-west-1.amazonaws.com/recipes-en.json.gz && gzip -dc recipes-en.json.gz > recipes-en.json
```

## FAQ

<details>
<summary><i>I'm a back-end developer or don't know React, what do I do?</i></summary>

Just make the simplest UI, style isn't important and server rendered HTML pages will do!
</details>

<details>
<summary><i>Can I have a time extension for the test?</i></summary>

No worries, we know that unforeseen events happen, simply reach out to the recruiter you've been
talking with to discuss this.
</details>

<details>
<summary><i>Can I transform the dataset before seeding it in the DB</i></summary>

Absolutely, feel free to post-process the dataset as needed to fit your needs.
</details>

<details>
<summary><i>Should I rather implement option X or option Y</i></summary>

That decision is up to you and part of the challenge. Please document your choice
to be able to explain your reflexion and choice to your interviewer for the
challenge debrief.
</details>

<details>
<summary><i>Do I really have to deploy the application online?</i></summary>
Deploying the application does ensure a smooth interview experience by allowing interviewers to test your code live. However, you should not overinvest time (or money) on this if you really can't figure it. You can alternatively provide demo videos as a worst case option, as interviewers won't checkout and run the application to cover for missing demo or online version. In case you don't have an online application, please make sure everything is working smoothly
locally before your debrief interview.
  
</details>

<details>
<summary><i>I don't know <b>Ruby on Rails</b></i></summary>

That probably means you're applying for a managerial position, so it's fine to
pick another language of your choice to perform this task.
</details>

<details>


<summary><i>Can I use AI tools?</b></i></summary>

You are free to use AI tools to assist you in completing this case study. To maintain transparency, please document which AI tools you used during the assignment.

For each tool, briefly explain:
- The main tasks or problems for which you used it.
- How you validated and refined any AI-generated code.

Note: While AI can be a valuable assistant, interviewers will assess your ability to understand the entire codebase, explain key technical choices, and effectively answer technical questions about improvements. We expect candidates to use AI as a supportive tool rather than having it generate the complete solution. AI should supplement your coding process, not replace your critical thinking and hands-on development work.
</details>

---

# Solution

## What the app does

You type the ingredients you have at home. The app ranks the whole recipe catalog by how close you are to cooking each recipe: the ones you can cook right now come first, then the ones missing one ingredient, then two, and so on. Pick a result and you get the full recipe, with the ingredients you still need highlighted.

The codebase splits into `backend/` (Kotlin, Spring Boot) and `frontend/` (Nuxt 4 single-page app). The dataset provided with the brief is post-processed and seeded into PostgreSQL.

## User stories

The five stories below cover the product end to end. US1, US2 and US4 are the heart of the application; US3 and US5 round out the experience without adding features beyond the problem statement.

### US1: Manage my available ingredients

**As a home cook, I want to list the ingredients I have at home so the app can find recipes I can actually prepare.**

- I can add an ingredient by picking it from suggestions drawn from the real recipe catalog.
- I can remove a single ingredient, or clear the whole list in one action.
- Adding an ingredient I already have changes nothing; duplicates are ignored.
- My ingredient list is still there when I come back to the app in the same browser.
- A visible hint tells me that salt, pepper, water and olive oil are assumed present, so I don't need to add them.
- I cannot invent ingredients: if what I type matches nothing in the catalog, nothing is added.

The catalog is the single source of truth for ingredient names, which keeps matching reliable and the suggestion list meaningful.

### US2: Find the most relevant recipes for my kitchen

**As a home cook, I want recipes ordered by how close I am to being able to prepare them so the best options come first.**

- Recipes needing nothing I lack appear first, then recipes missing one ingredient, then two, and so on.
- When two recipes miss the same number of ingredients, the one using more of what I have ranks first.
- Every result shows its match status: a "Ready to cook" badge, or a line reading "you have X of Y" with the names of the missing ingredients.
- Staple ingredients (salt, pepper, water, olive oil) never count as missing.
- Long result lists are paginated, and moving between pages keeps my ingredient list intact.

Ranking happens on the server for every search, so the order is identical no matter which browser or device I use.

### US3: See only recipes I can cook right now

**As a home cook short on time, I want to hide recipes that need ingredients I don't have so every result is something I can start immediately.**

- A toggle hides every recipe with at least one missing ingredient.
- Turning the toggle off brings the full ranked list back.
- When the toggle is on and nothing matches fully, I get a clear message explaining why and suggesting I turn it off.

### US4: Read a recipe in detail

**As a home cook, I want the full recipe once I pick a result so I can actually cook it.**

- I see the picture, the name and the complete ingredient list.
- The ingredients I am missing are clearly highlighted; staples are never flagged as missing.
- I can go back to my search results without losing my ingredient list or my place in them.

### US5: Never get stuck

**As a home cook, I want the app to tell me what to do next so I am never stuck on a dead screen.**

- With no ingredients yet, I am invited to add what I have at home.
- When no recipe matches my ingredients, I see a friendly message and a hint to remove or add some.
- When something goes wrong loading data, I see an error message with a retry option.

## Technical choices

| Choice | Why |
| --- | --- |
| Backend: Kotlin 2.2 / Java 21 / Spring Boot 3.5 | Mature, typed, fast to build on. The FAQ allows a language of choice when Rails isn't the candidate's stack. |
| PostgreSQL + Spring Data JPA + Liquibase | A relational database is a must-have of the brief. Typed schema, version-controlled migrations. |
| springdoc-openapi | Full OpenAPI documentation of every endpoint, consumed by the frontend codegen. |
| Frontend: Nuxt 4 (SPA mode) + Vue 3 + PrimeVue 4 (Aura) | A component-rich UI kit keeps the styling effort low (the brief says no excessive styling). |
| Tailwind 4 | Layout and spacing tokens on top of the PrimeVue theme. |
| TanStack Query | Server state: caching, loading and error states without hand-rolled plumbing. |
| Orval | Generates the typed API client from the backend's OpenAPI spec, so the spec is the single source of truth for the API contract. |
| Clean architecture | Domain / application use cases / adapters / config, enforced by ArchUnit tests. All expected failures are typed errors (kotlin-result), never exceptions. |
| TDD throughout | Backend: 41 unit tests + 19 Testcontainers integration tests (real PostgreSQL, zero mocks in the integration tests). Frontend: 83 Vitest tests. |

## Documented choices and trade-offs

The FAQ asks candidates to document their decisions, so here they are:

- Matching is by ingredient presence, not quantity. Scraped quantities ("1 pinch") are too unreliable to reason about.
- Salt, pepper, water and olive oil are assumed present in every kitchen, and the UI says so. This matches how home cooks actually think.
- The dataset contains no step-by-step instructions, so the detail view focuses on ingredients, with the missing ones highlighted. Instructions would be the first extension.
- Ingredient names are canonicalized (lowercase, punctuation stripped, naive plural folding) so "Chicken Breasts" and "chicken breast" match.
- Search loads the catalog and ranks it in the domain layer. Simple and correct for ~10k recipes. Moving the matching into SQL is the upgrade path if the catalog grows.
- No authentication, per prototype scope.

## Run the app with Docker

Prerequisites: Docker only.

```sh
docker compose up --build
```

Then open http://localhost:3000. One command starts the whole stack:

- postgres (PostgreSQL 17): seeded automatically on backend startup. When the catalog is empty the backend downloads the gzipped dataset from S3 and seeds it (Liquibase runs at boot).
- backend (Kotlin / Spring Boot): also exposed directly on http://localhost:8080 (API at `/api/**`, docs at /swagger-ui.html, spec at /v3/api-docs).
- frontend (Nuxt 4 SPA served by nginx): http://localhost:3000. It is built from `frontend/` by `frontend/Dockerfile` (nginx config in `frontend/nginx/default.conf`), and nginx proxies `/api/**` to the backend, so the browser only ever talks to one origin.

`docker compose down -v` stops the stack and wipes the database volume.

## Run locally (for working on the code)

Prerequisites: Java 21, Docker, Node 20+ with npm.

```sh
# 1. Database (postgres service of the root docker-compose.yml)
docker compose up -d postgres

# 2. Backend (seeds itself on first boot: downloads the dataset from S3
#    when the catalog is empty)
./mvnw spring-boot:run
# (seeded: 10,013 recipes / 18,951 ingredients; later boots skip seeding)

# 3. Frontend (new terminal)
cd frontend && npm install && npm run dev
# open http://localhost:3000
```

The API is served at http://localhost:8080/api/**, with interactive docs at /swagger-ui.html and the machine spec at /v3/api-docs.

Seeding is automatic: on every boot the app checks the catalog and seeds it only when it is empty, downloading the dataset (`app.seed.url`, the S3 gzipped JSON by default). If the download fails on an empty catalog the app refuses to start.

## Running the tests

- Backend unit tests (no Docker needed): `cd backend && ./mvnw test`
- Backend integration tests (Docker needed): `cd backend && ./mvnw verify`
- Frontend: `cd frontend && npm run test` / `npm run typecheck` / `npm run lint` / `npm run build`

## AI tools used

The brief requires this section, so here it is, plainly and specifically.

- Tools: OpenCode coding agents (GLM model family), orchestrated by a lead agent; Plannotator for plan review.
- What they were used for: project scaffolding, TDD test and implementation generation per wave, dataset post-processing, and a conventions review pass.
- How the output was validated: every increment had to pass the executable gates before being committed (`./mvnw test` / `verify`, `npm run test` / `build` / `lint` / `typecheck`). The orchestrator re-ran all gates itself and reviewed every diff. A dedicated review agent then audited the code against written convention standards. That review caught one real bug (a Vue watcher that missed an in-place array mutation), which was fixed and locked with a regression test.

## How we worked

Domain-driven design with clean architecture. A pure-Kotlin domain (ingredient canonicalization, staples, missing-ingredient computation, ranking) sits at the center. Application use cases are the only door into it. Adapters (REST, JPA persistence) live outside, and config wires everything. ArchUnit tests enforce the layering and naming rules so the structure cannot rot.

One rule governs the testing: domain logic is tested through application use cases, never by direct domain unit tests. Tests assert observable outcomes, so the domain internals could be refactored freely.

Every wave went TDD: RED (failing test written first), GREEN (minimal implementation), then refactor, one use case at a time.

Sub-agent delegation: the lead agent acted as architect and reviewer only. Specialist sub-agents implemented each wave, with a library of written convention skills injected into their context so every agent followed the same standards (Kotlin idioms, DDD modeling, error handling, REST conventions, Vue/PrimeVue/Tailwind patterns, accessibility). The lead verified every result by re-running the gates and reading the diffs before committing.

A final review gate audited the whole codebase against all convention standards before delivery. Its findings, including one real functional bug, were fixed and locked with regression tests.

Commits are atomic conventional commits, one per verified increment (one per use case on the backend).
