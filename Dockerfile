FROM amazoncorretto:17
WORKDIR /app
VOLUME /tmp
ADD ./KOIN_API_V2.jar /app/app.jar
# ENV JAVA_OPTS=""
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
