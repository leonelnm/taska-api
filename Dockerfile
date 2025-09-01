# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar Maven Wrapper y dependencias
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copiar el código fuente y compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage final
FROM eclipse-temurin:21-jre-alpine-3.21
WORKDIR /app

# Copiar el jar compilado
COPY --from=builder /app/target/*.jar app.jar

# Puerto que expone tu app
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
