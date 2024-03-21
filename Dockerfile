FROM eclipse-temurin:8u402-b06-jre-alpine

ADD target/*.jar /opt/app.jar
CMD ["java", "-jar", "/opt/app.jar"]