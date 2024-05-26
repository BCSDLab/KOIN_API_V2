package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import in.koreatech.koin.AcceptanceTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

class CircuitBreakerApiTest extends AcceptanceTest {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp(){
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("tmoneyExpressBus");
        circuitBreaker.reset();
    }

    @Test
    @DisplayName("시외버스 서킷브레이커 테스트")
    void expressBusOpenApiCircuitBreakerTest(){
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("tmoneyExpressBus");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        doThrow(RuntimeException.class).when(tmoneyExpressBusClient).storeRemainTimeByOpenApi();

        for (int i = 0; i < 10; i++){
            try {
                tmoneyExpressBusClient.storeRemainTimeByOpenApi();
            } catch (CallNotPermittedException e){
                System.out.println("OPEN");
            } catch (RuntimeException e){
                System.out.println("CLOSED");
            }
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
