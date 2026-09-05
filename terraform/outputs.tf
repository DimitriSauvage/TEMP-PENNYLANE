# Read after `terraform apply`:
#   terraform output app_url
#   terraform output -raw seed_command           # one-time dataset seed
#   terraform output -raw render_deploy_command  # redeploy the backend later
#   terraform output -raw db_backup_command      # dataset backup (pg_dump), if ever needed

output "app_url" {
  description = "Public URL of the SPA (open this in the browser)."
  value       = local.app_url
}

output "backend_url" {
  description = "Public URL of the Spring Boot backend on Render."
  value       = render_web_service.backend.url
}

output "backend_health_url" {
  description = "Health endpoint of the backend (the first request after a sleep wakes it up)."
  value       = "${render_web_service.backend.url}/actuator/health"
}

output "jdbc_url" {
  description = "JDBC URL of the Neon database (contains no credentials)."
  value       = local.backend_jdbc_url
}

output "db_user" {
  description = "Neon database user."
  value       = neon_project.recipes.database_user
}

output "db_backup_command" {
  description = "Dataset backup (pg_dump), run from your machine. Contains the DB password - keep it out of chat logs and screenshots."
  sensitive   = true
  value = format(
    "pg_dump \"host=%s dbname=%s user=%s password=%s sslmode=require\" > recipes-backup.sql",
    neon_project.recipes.database_host_pooler,
    neon_project.recipes.database_name,
    neon_project.recipes.database_user,
    neon_project.recipes.database_password
  )
}

output "render_deploy_command" {
  description = "Redeploy the backend from the repo's latest commit (manual, no CI/CD)."
  value       = "curl -X POST https://api.render.com/api/v1/services/${render_web_service.backend.id}/deploys -H \"Authorization: Bearer $RENDER_API_KEY\""
}

output "seed_command" {
  description = "One-time dataset seed (10,013 recipes), run from the repo root. Contains the DB password - keep it out of chat logs and screenshots."
  sensitive   = true
  value = format(
    "cd backend && DB_URL='%s' DB_USER='%s' DB_PASSWORD='%s' ./mvnw spring-boot:run -Dspring-boot.run.arguments='--app.seed.enabled=true --app.seed.file=../data/recipes-en.json'",
    local.backend_jdbc_url,
    neon_project.recipes.database_user,
    neon_project.recipes.database_password
  )
}
