FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY infras ./infras
COPY services ./services

ARG MODULE_PATH
RUN mvn -pl ${MODULE_PATH} -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
ARG MODULE_PATH
COPY --from=build /workspace/${MODULE_PATH}/target/*.jar app.jar

EXPOSE 8080 8081 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
