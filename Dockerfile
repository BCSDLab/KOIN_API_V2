FROM amazoncorretto:17

WORKDIR /app

COPY ./build/libs/KOIN_API_V2.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
