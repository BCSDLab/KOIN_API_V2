FROM amazoncorretto:17

RUN yum install -y tzdata

RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

WORKDIR /app

COPY ./build/libs/KOIN_API_V2.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
