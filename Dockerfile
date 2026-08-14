FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon dependencies
COPY src ./src
RUN ./gradlew --no-daemon shadowJar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/weather-bot-*.jar app.jar
VOLUME ["/app/data"]
ENTRYPOINT ["java", "-jar", "app.jar"]
