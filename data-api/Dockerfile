# ---------- BUILD ----------
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY . .

RUN mvn clean package -pl data-api -am -DskipTests

# ---------- RUNTIME ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /workspace/data-api/target/data-api.jar app.jar

EXPOSE 9995

ENTRYPOINT ["java","-jar","app.jar"]
