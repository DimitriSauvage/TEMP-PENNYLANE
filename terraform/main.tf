# Terraform configuration for the free-tier online deployment:
#
#   Vercel (Hobby) -> static Nuxt SPA + /api reverse proxy   (frontend)
#   Render  (Free)  -> Spring Boot backend (Docker)           (backend)
#   Neon    (Free)  -> PostgreSQL                              (database)
#
# All three accounts are free and require no credit card. Provider
# credentials are read from environment variables:
#
#   NEON_API_KEY     - Neon console -> Account settings -> API keys
#   RENDER_API_KEY   - Render dashboard -> Account Settings -> API Keys
#   RENDER_OWNER_ID  - Render workspace id ("usr-...", visible in the
#                      dashboard URL or via `curl https://api.render.com/api/v1/owners -H "Authorization: Bearer $RENDER_API_KEY"`)
#   VERCEL_API_TOKEN - https://vercel.com/account/settings/tokens
#
terraform {
  required_version = ">= 1.6.0"

  required_providers {
    # The maintained Neon provider lives under `kislerdm/neon`.
    # `neonpg/neon` does NOT exist on the Terraform registry.
    # Source repo: github.com/neondatabase/terraform-provider-neon
    neon = {
      source  = "kislerdm/neon"
      version = "~> 0.15.0"
    }
    render = {
      source  = "render-oss/render"
      version = "~> 1.9"
    }
    vercel = {
      source  = "vercel/vercel"
      version = "~> 5.15"
    }
  }

  # State is local by default (single operator, short-lived demo project).
  # terraform.tfstate* is gitignored and MUST be treated as a secret (it
  # contains the database password). For durable remote state, HCP
  # Terraform has a free tier.
}

provider "neon" {}   # api_key  <- $NEON_API_KEY
provider "render" {} # api_key  <- $RENDER_API_KEY, owner_id <- $RENDER_OWNER_ID
provider "vercel" {} # api_token <- $VERCEL_API_TOKEN (personal account: no team needed)
