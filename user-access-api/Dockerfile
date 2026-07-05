FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY . .

RUN mvn clean package -pl user-access-api -am -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /workspace/user-access-api/target/user-access-api.jar app.jar

EXPOSE 9997

ENTRYPOINT ["java","-jar","app.jar"]
