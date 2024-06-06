# Amazon Corretto Alpine JDK base image 사용
FROM amazoncorretto:17-alpine-jdk

# 필요한 패키지 설치 및 설정
RUN apk add --no-cache tzdata wget curl grep && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    wget -O /dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'

# 작업 디렉토리 설정
WORKDIR /app

# 애플리케이션 JAR 파일 복사
COPY ./build/libs/KOIN_API_V2.jar /app/app.jar

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java -javaagent:/dd-java-agent.jar -Ddd.service=koin-api -Ddd.env=${DD_ENV} -Ddd.version=1.0 -Ddd.agent.host=datadog-agent -Ddd.agent.port=8126 -jar /app/app.jar"]
