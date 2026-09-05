# PostgreSQL on the Neon free plan (0.5 GB storage, no expiry, no card).
# The project resource auto-provisions the default branch, its role and
# its database; the attributes used by the backend (pooled host, user,
# password) are read from it in render.tf.

resource "neon_project" "recipes" {
  name       = var.neon_project_name
  pg_version = 17
  region_id  = var.neon_region

  # Default branch + database + role, mirroring the local dev names.
  branch {
    name          = var.github_branch # "main"
    database_name = "recipes"
    role_name     = "recipes"
  }

  # 0.25 CU is the free-plan compute size. Scale-to-zero after 5 min of
  # inactivity is mandatory on the free plan (cannot be disabled); the
  # first connection after a sleep pays only a sub-second wake, and the
  # backend's health endpoint queries the DB, so one request to Render
  # wakes both layers at once.
  default_endpoint_settings {
    autoscaling_limit_min_cu = 0.25
    autoscaling_limit_max_cu = 0.25
    suspend_timeout_seconds  = 300
  }

  store_password = "yes"
}
