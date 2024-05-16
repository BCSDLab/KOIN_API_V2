FROM amazoncorretto:17

RUN yum install -y tzdata

RUN yum install -y wget

RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

RUN wget -O /dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'

WORKDIR /app

COPY ./build/libs/KOIN_API_V2.jar /app/app.jar

ENTRYPOINT ["sh", "-c", "java -javaagent:/dd-java-agent.jar -Ddd.service=koin-api -Ddd.env=${DD_ENV} -Ddd.version=1.0 -Ddd.agent.host=datadog-agent -Ddd.agent.port=8126 -jar /app/app.jar"]
