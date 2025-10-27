FROM eclipse-temurin:22-jdk as builder
WORKDIR /build
COPY . .
RUN ./mvnw clean package -DskipTests



FROM eclipse-temurin:22-jre
WORKDIR /app
COPY --from=builder /build/target/BankingApp-0.0.1-SNAPSHOT.jar app.jar
COPY ./src/main/resources/application-prod.properties /app/config/application-prod.properties
ENV SPRING_CONFIG_LOCATION=/app/config/application-prod.properties
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar","app.jar"]