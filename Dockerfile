# Image unique pour tous les modules : docker build --build-arg MODULE=mcp-server .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
ARG MODULE
RUN mvn -B -q -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app
RUN useradd --system --create-home app
COPY --from=build /workspace/${MODULE}/target/*.jar app.jar
USER app
ENTRYPOINT ["java", "-jar", "app.jar"]
