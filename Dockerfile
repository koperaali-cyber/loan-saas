# Stage 1: Kujenga mradi (Build stage)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Nakili pom.xml na pakua dependencies zote mapema ili kuokoa muda
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Nakili source code zote za mradi na utengeneze faili la jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Kuendesha application (Runtime stage)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Nakili faili la .jar lililojengwa kutoka stage ya kwanza
COPY --from=build /app/target/*.jar app.jar

# Weka wazi port itakayotumiwa na application (Default Spring Boot)
EXPOSE 8080

# Amri ya kuwasha application
ENTRYPOINT ["java", "-jar", "app.jar"]