FROM gradle:9.5.1-jdk21 AS build
WORKDIR /workspace

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew --no-daemon dependencies

COPY src src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/platform-api-*.jar app.jar

USER 10001
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=/tmp"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
