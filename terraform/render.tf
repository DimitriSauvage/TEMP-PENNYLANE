# Spring Boot backend as a Docker web service on the Render free plan:
# 0.1 CPU / 512 MB, 750 free instance-hours per month (a 10-day window
# needs ~240). Sleeps after 15 min without traffic - the first request
# after a sleep pays the wake-up (~1 min), then it stays warm.

locals {
  # JDBC URL the backend expects (spring.datasource.url in
  # application.yml): the pooled endpoint (-pooler host, required by
  # Neon's connection limits) with SSL, as on the free plan.
  backend_jdbc_url = "jdbc:postgresql://${neon_project.recipes.database_host_pooler}/${neon_project.recipes.database_name}?sslmode=require"
}

resource "render_web_service" "backend" {
  name   = var.render_service_name
  plan   = "free" # 0.1 CPU / 512 MB
  region = var.render_region

  # Render reads the source from the GitHub repo (it needs a code
  # source), but nothing deploys automatically - pushes do NOT build.
  # The Docker build context is backend/ (so backend/.dockerignore
  # applies, excluding target/), and the Dockerfile honors the PORT and
  # JAVA_OPTS Render injects. Creating the service triggers the first
  # deploy; later deploys are manual: the `render_deploy_command`
  # output, or the dashboard's "Deploy latest commit".
  runtime_source = {
    docker = {
      repo_url        = "https://github.com/${var.github_repo}"
      branch          = var.github_branch
      auto_deploy     = false # pushes do NOT deploy - you deploy from the CLI
      context         = "backend"
      dockerfile_path = "./backend/Dockerfile"
    }
  }

  health_check_path = "/actuator/health"

  env_vars = {
    DB_URL = {
      value = local.backend_jdbc_url
    }
    DB_USER = {
      value = neon_project.recipes.database_user
    }
    DB_PASSWORD = {
      value = neon_project.recipes.database_password
    }
    # 512 MB instance: cap the JVM heap at ~50% of container RAM so
    # metaspace, thread stacks and the OS fit inside the limit.
    JAVA_OPTS = {
      value = "-XX:MaxRAMPercentage=50"
    }
  }
}
