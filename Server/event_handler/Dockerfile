FROM adoptopenjdk/openjdk8-openj9:alpine-slim

COPY target/*.jar /app.jar

ENTRYPOINT java -jar /app.jar
