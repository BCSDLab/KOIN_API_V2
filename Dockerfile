FROM amazoncorretto:17

RUN if ! rpm -q tzdata; then yum install -y tzdata; fi

RUN if ! command -v wget >/dev/null 2>&1; then yum install -y wget; fi

RUN if [ "$(readlink /etc/localtime)" != "/usr/share/zoneinfo/Asia/Seoul" ]; then ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime; fi

RUN if [ ! -f /dd-java-agent.jar ]; then wget -O /dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'; fi

WORKDIR /app

COPY ./build/libs/KOIN_API_V2.jar /app/app.jar

ENTRYPOINT ["sh", "-c", "java -javaagent:/dd-java-agent.jar -Ddd.service=koin-api -Ddd.env=${DD_ENV} -Ddd.version=1.0 -Ddd.agent.host=datadog-agent -Ddd.agent.port=8126 -jar /app/app.jar"]
