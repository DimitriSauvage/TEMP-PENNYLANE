# ---------------------------------------------------------------------------
# Stage 1 (build): compile the Spring Boot app with Maven + JDK 21.
# Only pom.xml is copied first so Docker caches the dependency download
# layer; it is invalidated only when pom.xml changes, not on every src edit.
# ---------------------------------------------------------------------------
# Digest corresponds to maven:3.9-eclipse-temurin-21
FROM maven@sha256:8f6ac126f7810bb5549c4cd122d2bf0e9cda5bdeb0838aa928f09e779fd8bef8 AS build
WORKDIR /build
COPY pom.xml ./pom.xml
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

# ---------------------------------------------------------------------------
# Stage 2 (runtime): slim JRE-only image containing just the built jar.
# No Maven/JDK/source code ships in the final image (Sonar S6554: avoid
# shipping build dependencies in production images).
# ---------------------------------------------------------------------------
# Digest corresponds to eclipse-temurin:21-jre
FROM eclipse-temurin@sha256:7a65df4b22d2de92d4e04056e884f3b9122d70b21e2847fd66084278bd0ce037
WORKDIR /app
COPY --from=build /build/target/recipes-backend-0.0.1-SNAPSHOT.jar app.jar
# The temurin image ships a stock non-root user `ubuntu` (uid 1000); use it
# instead of creating one — useradd with uid 1000 fails (not unique).
USER ubuntu
EXPOSE 8080
# The JRE image has no curl/wget; bash's /dev/tcp opens a TCP connection to
# the app port and fails if it is not accepting traffic (/dev/tcp is
# host/port only — no URL path, so this is a TCP-connect check).
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD ["bash", "-c", ": < /dev/tcp/127.0.0.1/8080 || exit 1"]
# Shell form so container hosts can inject JAVA_OPTS and PORT; exec keeps
# java as PID 1 so shutdown signals reach it. The HEALTHCHECK above still
# probes 8080 (Render ignores Docker healthchecks and uses its Health Check
# Path setting; the compose service overrides `command`).
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar --server.port=${PORT:-8080}"]
