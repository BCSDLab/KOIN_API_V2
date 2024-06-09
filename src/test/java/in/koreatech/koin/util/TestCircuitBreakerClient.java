package in.koreatech.koin.util;

import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
public class TestCircuitBreakerClient {

    @CircuitBreaker(name = "test")
    public void testMethod() {
    }
}
