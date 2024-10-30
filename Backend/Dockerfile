FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} expenses-app.jar
ENTRYPOINT ["java","-jar","/expenses-app.jar"]