package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import in.koreatech.koin.acceptance.AcceptanceTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

class CircuitBreakerApiTest extends AcceptanceTest {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker circuitBreaker;

    @BeforeAll
    void setUp() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("test");
        circuitBreaker.reset();
    }

    @Test
    @DisplayName("서킷브레이커 상태 변환 테스트: CLOSED -> OPEN")
    void closedToOpenTest() {
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        doThrow(RuntimeException.class).when(testCircuitBreakerClient).testMethod();
        callMethod();
        assertThat(circuitBreaker.getState()).isNotEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("서킷브레이커 상태 변환 테스트: OPEN -> CLOSED")
    void openToClosedTest() {
        doCallRealMethod().when(testCircuitBreakerClient).testMethod();
        circuitBreaker.transitionToOpenState();
        circuitBreaker.transitionToHalfOpenState();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
        callMethod();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    private void callMethod() {
        for (int i = 0; i < 10; i++) {
            try {
                testCircuitBreakerClient.testMethod();
            } catch (CallNotPermittedException e) {
                // OPEN or HALF_OPEN
            } catch (RuntimeException e) {
                // CLOSED or Last Try
            }
        }
    }
}
