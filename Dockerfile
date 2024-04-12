FROM eclipse-temurin:8u402-b06-jre-windowsservercore-ltsc2022

ADD target/*.jar /opt/app.jar
CMD ["java", "-jar", "/opt/app.jar"]