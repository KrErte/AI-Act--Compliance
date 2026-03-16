# === Development stage ===
FROM maven:3.9-eclipse-temurin-21-alpine AS development
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:resolve -B
COPY src/ src/
CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"]

# === Build stage ===
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:resolve -B
COPY src/ src/
RUN mvn package -DskipTests -B

# === Production stage ===
FROM eclipse-temurin:21-jre-alpine AS production
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
