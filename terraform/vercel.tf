# Nuxt SPA on the Vercel Hobby plan. Terraform provisions the project and
# its build-time env var; the deploy itself is CLI-driven from the
# frontend/ directory (`npx vercel --prod`), so nothing runs on GitHub.
# The /api/* rewrites live in frontend/vercel.json and proxy same-origin
# /api calls to the Render backend - the browser only ever talks to one
# origin.

locals {
  # Vercel serves the project at https://<project-name>.vercel.app.
  app_url = "https://${var.vercel_project_name}.vercel.app"
}

resource "vercel_project" "frontend" {
  name             = var.vercel_project_name
  build_command    = "npm run generate" # static SPA export (also regenerates the typed API client)
  output_directory = ".output/public"
  node_version     = "22.x" # same major as the local toolchain
}

# The SPA must call /api on its own origin (vercel.json proxies it to
# Render); without this override the http://localhost:8080 default is
# baked into the static bundle and every API call fails in production.
# `vercel deploy` pulls project env vars into the local build, so this
# must exist BEFORE the first `npx vercel --prod` (terraform apply first).
resource "vercel_project_environment_variable" "api_base" {
  project_id = vercel_project.frontend.id
  key        = "NUXT_PUBLIC_API_BASE"
  value      = local.app_url
  target     = ["production", "preview"]
  sensitive  = false
}
