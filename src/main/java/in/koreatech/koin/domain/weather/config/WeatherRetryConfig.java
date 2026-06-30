package in.koreatech.koin.domain.weather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

import in.koreatech.koin.domain.weather.exception.WeatherOpenApiRateLimitException;

@Configuration
public class WeatherRetryConfig {

    private static final int MAX_ATTEMPTS = 6;
    private static final long RETRY_INTERVAL_MILLIS = 5_000L;

    @Bean
    public RetryTemplate weatherRetryTemplate() {
        // 최초 호출 이후 5회 재시도해 기상청 제공 서버의 순간적인 동시 호출 제한을 흡수한다.
        return RetryTemplate.builder()
            .maxAttempts(MAX_ATTEMPTS)
            .fixedBackoff(RETRY_INTERVAL_MILLIS)
            .retryOn(WeatherOpenApiRateLimitException.class)
            .build();
    }
}
