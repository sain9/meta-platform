FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY . .

RUN mvn clean package -pl gateway-api -am -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /workspace/gateway-api/target/gateway-api.jar app.jar

EXPOSE 9999

ENTRYPOINT ["java","-jar","app.jar"]
