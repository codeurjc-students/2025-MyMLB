# Step 1: Frontend Build
FROM node:20 AS build-frontend
WORKDIR /app/frontend
COPY ./frontend/ /app/frontend
RUN npx ng build --configuration production

# Step 2: Backend Build
FROM maven:3.9.6-eclipse-temurin-21 AS build-backend
WORKDIR /app/backend
COPY ./backend/ /app/backend
RUN mvn clean package -DskipTests

# Step 3: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy backend JAR
COPY --from=build-backend /app/backend/target/*.jar app.jar

# Copy keystore
COPY ./backend/src/main/resources/keystore.p12 /app/keystore.p12

# Copy frontend build
COPY --from=build-frontend /app/frontend/dist/frontend /app/static

EXPOSE 8080

ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=docker-prod
ENV SPRING_DATASOURCE_URL=
ARG SPRING_DATASOURCE_USERNAME=
ARG SPRING_DATASOURCE_PASSWORD=

ARG CLOUDINARY_CLOUD_NAME=
ARG CLOUDINARY_API_KEY=
ARG CLOUDINARY_API_SECRET=

ARG SPRING_MAIL_HOST=
ENV SPRING_MAIL_PORT=587
ARG SPRING_MAIL_USERNAME=
ARG SPRING_MAIL_PASSWORD=

ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_CONNECTIONTIMEOUT=10000
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_TIMEOUT=10000
ENV SPRING_MAIL_PROPERTIES_MAIL_SMTP_WRITETIMEOUT=10000

ENTRYPOINT ["java", "-jar", "app.jar"]