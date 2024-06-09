package in.koreatech.koin.config;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

import java.time.Duration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@TestConfiguration
public class TestResilience4jConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    TestResilience4jConfig(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Bean
    public CircuitBreaker testCircuitBreaker() {
        CircuitBreakerConfig circuitBreakerConfig = getTestConfig();
        return circuitBreakerRegistry.circuitBreaker("test", circuitBreakerConfig);
    }

    private static CircuitBreakerConfig getTestConfig() {
        return CircuitBreakerConfig.custom()
            // 최소 호출횟수
            .minimumNumberOfCalls(2)
            // 실패율 임계값(100% 실패)
            .failureRateThreshold(100)
            // 실패율 집계 기준(호출횟수, 호출시간)
            .slidingWindowType(COUNT_BASED)
            // 호출 횟수
            .slidingWindowSize(2)
            // OPEN 상태에서의 대기시간
            .waitDurationInOpenState(Duration.ofMillis(1))
            // Open에서 Half Open으로 자동으로 넘어갈 지 여부
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            // Half Open 상태에서 최대 호출횟수
            .permittedNumberOfCallsInHalfOpenState(2)
            .build();
    }
}
