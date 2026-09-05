# Multi-stage build: node -> nginx serving the static SPA.
# Build context is frontend/ (docker-compose: build: ./frontend).
# NUXT_PUBLIC_API_BASE="" bakes a same-origin (relative /api) base into the
# bundle; nginx proxies /api to the backend service.
# Digest corresponds to node:24-alpine
FROM node@sha256:e67514e5d0f6c46656005e1b693b2ec9d52e80b641307de684d4a015ba7a4eaf AS build
WORKDIR /build
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ENV NUXT_PUBLIC_API_BASE=""
# nuxt generate (not nuxt build): emits .output/public/index.html for static
# hosting; the plain build only ships a node-server that renders it at runtime.
RUN npm run generate

# Unprivileged nginx: runs as uid 101 (nginx), no root anywhere in the
# runtime container. It listens on 8080, not 80 (binding <1024 needs root).
# Digest corresponds to nginxinc/nginx-unprivileged:1.27-alpine
FROM nginxinc/nginx-unprivileged@sha256:65e3e85dbaed8ba248841d9d58a899b6197106c23cb0ff1a132b7bfe0547e4c0
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/.output/public /usr/share/nginx/html
EXPOSE 8080
