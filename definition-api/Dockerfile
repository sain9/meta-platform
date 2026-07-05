FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY . .

RUN mvn clean package -pl definition-api -am -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /workspace/definition-api/target/definition-api.jar app.jar

EXPOSE 9996

ENTRYPOINT ["java","-jar","app.jar"]
