FROM eclipse-temurin:21-jre-alpine

VOLUME /tmp

ARG JAR_FILE=stats-service/stats-server/target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]