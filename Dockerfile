FROM node:20.13-alpine as frontend

WORKDIR /
COPY frontend/package*.json ./app/

RUN npm --prefix app install
COPY frontend app
RUN npm run --prefix app build


FROM maven:3.8.3-eclipse-temurin-17 AS build

WORKDIR /

COPY backend/src /home/app/src
COPY backend/pom.xml /home/app
COPY --from=frontend backend/src/main/resources/static /home/app/src/main/resources/static
RUN mvn -f /home/app/pom.xml clean package

FROM eclipse-temurin:17
COPY --from=build /home/app/target/backend.jar /usr/local/lib/backend.jar
# COPY .env .env

EXPOSE 1337
ENTRYPOINT ["java","-jar","/usr/local/lib/backend.jar", "start"]

