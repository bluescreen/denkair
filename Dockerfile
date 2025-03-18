# syntax=docker/dockerfile:1.6

# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml ./
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

# ---- Stage 2: runtime ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd -r -u 1001 denkair
USER denkair

COPY --from=build /build/target/booking-0.0.1-SNAPSHOT.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=${PORT:-$SERVER_PORT} -jar /app/app.jar"]
