FROM openjdk:21-jdk-slim
WORKDIR /app

COPY build/libs/DsMemeBot-*-all.jar /app/DsMemeBot.jar

CMD ["java", "-jar", "DsMemeBot.jar"]