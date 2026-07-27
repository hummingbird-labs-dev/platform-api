FROM gradle:9.5.1-jdk21 AS build
WORKDIR /workspace

# Install CA certificates for SSL/TLS verification
RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

# Download gradle wrapper distribution and cache dependencies
# Using --mount=type=cache to persist gradle cache between builds
RUN --mount=type=cache,target=/root/.gradle \
  ./gradlew --no-daemon dependencies

COPY src src
RUN --mount=type=cache,target=/root/.gradle \
  ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/platform-api-*.jar app.jar

USER 10001
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=/tmp"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
