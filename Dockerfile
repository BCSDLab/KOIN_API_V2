FROM amazoncorretto:17

RUN yum install -y tzdata wget && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    wget -O /dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'

WORKDIR /app

COPY ./build/libs/KOIN_API_V2.jar /app/app.jar

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["sh", "-c", "java -javaagent:/dd-java-agent.jar -Ddd.service=koin-api -Ddd.env=${DD_ENV} -Ddd.version=1.0 -Ddd.agent.host=datadog-agent -Ddd.agent.port=8126 -jar /app/app.jar"]
