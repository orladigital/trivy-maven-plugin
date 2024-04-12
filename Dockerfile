FROM amazoncorretto:latest

ADD target/*.jar /opt/app.jar
CMD ["java", "-jar", "/opt/app.jar"]