# Hatua ya 1: Kujenga application (Build stage)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Nakili pom.xml na pakua dependencies kwanza (kuharakisha build zijazo)
COPY pom.xml .
RUN mvc dependency:go-offline -B

# Nakili source code zote na jenga faili la jar
COPY src ./src
RUN mvn clean package -DskipTests

# Hatua ya 2: Kuendesha application (Runtime stage)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Nakili faili la .jar lililojengwa kutoka hatua ya kwanza
COPY --from=build /app/target/*.jar app.jar

# Fungua port 8080 (kawaida ya Spring Boot)
EXPOSE 8080

# Amri ya kuendesha application
ENTRYPOINT ["java", "-jar", "app.jar"]