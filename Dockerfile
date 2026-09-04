FROM maven:3.9.11-eclipse-temurin-21

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src

# A porta é definida em tempo de execução por SERVER_PORT no arquivo .env.
CMD ["./mvnw", "spring-boot:run"]
