# 사용할 Java 버전 지정
FROM openjdk:17-jdk-slim
WORKDIR /app
VOLUME /tmp
ADD ./build/libs/KOIN_API_V2.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
