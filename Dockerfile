FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -B dependency:go-offline -DskipTests || true
COPY src ./src
RUN mvn -q -B package -DskipTests && cp target/applicant-tracking-system-*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN addgroup -S ats && adduser -S ats -G ats && mkdir -p /app/data/cvs && chown -R ats:ats /app/data
COPY --from=build /workspace/app.jar app.jar
USER ats
ENV ATS_CV_DIRECTORY=/app/data/cvs
VOLUME /app/data

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
