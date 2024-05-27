package in.koreatech.koin.service;

import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class CircuitBreakerServiceTest {

    @CircuitBreaker(name = "test")
    public void testMethod() {
    }
}
