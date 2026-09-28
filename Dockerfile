FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY infras ./infras
COPY services ./services

ARG MODULE_PATH
RUN mvn -pl ${MODULE_PATH} -am package -DskipTests
RUN JAR_FILE=$(find /workspace/${MODULE_PATH}/target -maxdepth 1 -type f -name "*.jar" ! -name "original-*.jar" | head -n 1) && \
    cp "$JAR_FILE" /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080 8081 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
