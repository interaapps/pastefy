FROM node:lts-alpine as build-stage
WORKDIR /app
COPY frontend/package*.json ./

RUN npm install
COPY frontend .
RUN npm run build --prod


FROM maven:3.6.0-jdk-8-slim AS build

WORKDIR /

COPY backend/src /home/app/src
COPY backend/pom.xml /home/app

RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:8-jre-slim
COPY --from=build /home/app/target/backend.jar /usr/local/lib/backend.jar
# COPY .env .env

EXPOSE 1337
ENTRYPOINT ["java","-jar","/usr/local/lib/backend.jar", "start"]

