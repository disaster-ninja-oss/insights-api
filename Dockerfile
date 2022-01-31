FROM nexus.kontur.io:8084/openjdk:17-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY target/*.jar ./
ENTRYPOINT ["java","-jar","/insights-api-0.0.1-SNAPSHOT.jar"]