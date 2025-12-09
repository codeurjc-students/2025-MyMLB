# Step 1: Frontend Build
FROM node:20 AS build-frontend
WORKDIR /app/frontend
COPY ./frontend/ /app/frontend
RUN npm install && npm run config

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

EXPOSE 8443

ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql-container:3306/mlb
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=root

ENTRYPOINT ["java", "-jar", "app.jar"]