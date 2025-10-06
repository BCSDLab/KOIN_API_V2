package in.koreatech.koin.config;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import in.koreatech.koin.global.exception.CustomException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class Resilience4jConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Bean
    public CircuitBreaker tmoneyExpressBusCircuitBreaker() {
        CircuitBreakerConfig circuitBreakerConfig = getBusConfig();
        return circuitBreakerRegistry.circuitBreaker("tmoneyExpressBus", circuitBreakerConfig);
    }

    @Bean
    public CircuitBreaker publicExpressBusCircuitBreaker() {
        CircuitBreakerConfig circuitBreakerConfig = getBusConfig();
        return circuitBreakerRegistry.circuitBreaker("publicExpressBus", circuitBreakerConfig);
    }

    @Bean
    public CircuitBreaker cityBusCircuitBreaker() {
        CircuitBreakerConfig circuitBreakerConfig = getBusConfig();
        return circuitBreakerRegistry.circuitBreaker("cityBus", circuitBreakerConfig);
    }

    @Bean
    public CircuitBreaker cityBusRouteCircuitBreaker() {
        CircuitBreakerConfig circuitBreakerConfig = getBusConfig();
        return circuitBreakerRegistry.circuitBreaker("cityBusRoute", circuitBreakerConfig);
    }

    @Bean
    public CircuitBreaker roadNameAddressCircuitBreaker() {
        CircuitBreakerConfig circuitBreakerConfig = getRoadNameAddressConfig();
        return circuitBreakerRegistry.circuitBreaker("roadNameAddress", circuitBreakerConfig);
    }

    private static CircuitBreakerConfig getBusConfig() {
        return CircuitBreakerConfig.custom()
            // 최소 호출횟수
            .minimumNumberOfCalls(2)
            // 실패율 임계값(100% 실패)
            .failureRateThreshold(100)
            // 실패율 집계 기준(호출횟수, 호출시간)
            .slidingWindowType(COUNT_BASED)
            // 호출 횟수
            .slidingWindowSize(2)
            // Half Open 상태로 넘어가기 전 대기시간
            .waitDurationInOpenState(Duration.ofMillis(3_600_000))
            // Open에서 Half Open으로 자동으로 넘어갈 지 여부
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            // Half Open 상태에서 최대 호출횟수
            .permittedNumberOfCallsInHalfOpenState(2)
            .build();
    }

    private static CircuitBreakerConfig getRoadNameAddressConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(70)
            .slowCallDurationThreshold(Duration.ofSeconds(5))
            .slowCallRateThreshold(90)
            .slidingWindowType(COUNT_BASED)
            .minimumNumberOfCalls(5)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .permittedNumberOfCallsInHalfOpenState(2)
            .ignoreException(throwable -> {
                if (throwable instanceof CustomException e) {
                    return e.getErrorCode().getHttpStatus().is4xxClientError();
                }
                return false;
            })
            .build();
    }
}
