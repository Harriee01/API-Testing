# ──────────────────────────────────────────────────────────────────────────────
# Dockerfile – Multi-stage build for the JSONPlaceholder API test suite
#
# Stage 1 (builder): downloads dependencies, compiles Java, runs the tests.
# Stage 2 (runner):  a lightweight JRE-only image that copies compiled
#                    artefacts and can re-run tests without Maven caches.
#
# Usage:
#   docker build -t api-tests .
#   docker run --rm -v "$(pwd)/target:/app/target" api-tests
# ──────────────────────────────────────────────────────────────────────────────

# ── Stage 1: build & test ─────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

LABEL maintainer="QA Automation Team"
LABEL description="REST Assured API automation suite – build & test stage"

# Install Maven (Alpine package manager keeps the image small)
RUN apk add --no-cache maven

# Set the working directory inside the container
WORKDIR /app

# ── Dependency layer (Docker cache optimisation) ──────────────────────────────
# Copy ONLY pom.xml first so Docker caches the dependency download step.
# As long as pom.xml doesn't change, 'mvn dependency:go-offline' is skipped
# on subsequent builds, saving minutes of network time.
COPY pom.xml .
RUN mvn --batch-mode dependency:go-offline -q

# ── Source layer ──────────────────────────────────────────────────────────────
# Now copy the full source tree (changes here don't invalidate the dep cache)
COPY src/ src/

# Run the tests. Results are written to /app/target/allure-results/
# --batch-mode     → disables colour output (cleaner CI logs)
# --no-transfer-progress → removes Maven download progress bars
RUN mvn --batch-mode --no-transfer-progress clean test \
    || true
    # NOTE: '|| true' ensures the image layer succeeds even if tests fail,
    # so the results can still be extracted via 'docker cp' or volume mounts.

# ── Stage 2: lightweight runtime ─────────────────────────────────────────────
# Use JRE (not JDK) – smaller footprint, no compiler needed for re-runs.
FROM eclipse-temurin:21-jre-alpine AS runner

LABEL description="REST Assured API automation suite – runtime stage"

# Install Maven in the JRE image so 'mvn test' can be re-executed if needed
RUN apk add --no-cache maven

WORKDIR /app

# Copy compiled artefacts and source from the builder stage
COPY --from=builder /app /app
# Also bring the local Maven cache to avoid re-downloading dependencies
COPY --from=builder /root/.m2 /root/.m2

# Default command: run all tests and leave results in /app/target/allure-results
# Override with: docker run ... api-tests mvn test -Dtest=PostTests
CMD ["mvn", "--batch-mode", "--no-transfer-progress", "clean", "test"]
