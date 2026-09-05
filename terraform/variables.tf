# --- Git -----------------------------------------------------------------

variable "github_repo" {
  description = "GitHub repository hosting this project (owner/name). Render builds the backend from it."
  type        = string
  default     = "pennylane-hiring/DimitriSauvage"
}

variable "github_branch" {
  description = "Branch Render builds the backend from (also the Neon default branch name)."
  type        = string
  default     = "main"
}

# --- Neon (PostgreSQL) ----------------------------------------------------

variable "neon_project_name" {
  description = "Neon project name (unique within your Neon org)."
  type        = string
  default     = "dimitri-recipes"
}

variable "neon_region" {
  description = "Neon region id. aws-eu-central-1 (Frankfurt) keeps the DB close to the Render backend."
  type        = string
  default     = "aws-eu-central-1"
}

# --- Render (backend) -----------------------------------------------------

variable "render_service_name" {
  description = "Render web service name. Must be unique across all of onrender.com. MUST stay in sync with the /api rewrite destination in frontend/vercel.json (Vercel rewrites cannot read env vars)."
  type        = string
  default     = "dimitri-recipes-api"
}

variable "render_region" {
  description = "Render region for the backend instance."
  type        = string
  default     = "frankfurt"
  validation {
    condition     = contains(["frankfurt", "ohio", "oregon", "singapore", "virginia"], var.render_region)
    error_message = "render_region must be one of: frankfurt, ohio, oregon, singapore, virginia."
  }
}

# --- Vercel (frontend) ----------------------------------------------------

variable "vercel_project_name" {
  description = "Vercel project name; the app is served at https://<name>.vercel.app. Must be unique across vercel.app."
  type        = string
  default     = "dimitri-recipes"
}
