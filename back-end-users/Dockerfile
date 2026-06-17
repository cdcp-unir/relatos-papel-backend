FROM maven:3.9.12-eclipse-temurin-25 AS build
COPY . .
RUN ls -la
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre
EXPOSE 8080
COPY --from=build /target/users-service-0.0.1-SNAPSHOT.jar app.jar

ENV DB_URL=${DB_URL};
ENV DB_DRIVER=${DB_DRIVER};
ENV DB_USER=${DB_USER};
ENV DB_PASSWORD=${DB_PASSWORD};
ENV EUREKA_URL=${EUREKA_URL};
ENV USERS_URL=${USERS_URL};

ENTRYPOINT ["java", "-jar", "/app.jar"]