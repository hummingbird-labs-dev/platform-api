FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy pre-built JAR from build job
COPY build/libs/platform-api-*.jar app.jar

USER 10001
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=/tmp"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
