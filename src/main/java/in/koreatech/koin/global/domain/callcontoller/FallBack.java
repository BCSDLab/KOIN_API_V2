package in.koreatech.koin.global.domain.callcontoller;

import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.reflection.ReflectionUtils;
import in.koreatech.koin.global.reflection.exception.ClassNotFoundException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FallBack<T> {

    private static final Class<CircuitBreaker> CIRCUIT_BREAKER = CircuitBreaker.class;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public T getOtherType(boolean hasCircuitBreaker, T failedType, List<T> fallBackableTypes) {
        fallBackableTypes.remove(failedType);
        if (hasCircuitBreaker) {
            return getOtherTypeByCircuitBreaker(fallBackableTypes);
        }
        return fallBackableTypes.get(0);
    }

    private T getOtherTypeByCircuitBreaker(List<T> childTypes) {
        return childTypes.stream()
            .filter(this::isCircuitBreakerClosed)
            .findFirst()
            .orElseThrow();
    }

    private boolean isCircuitBreakerClosed(T type) {
        try {
            String name = (String)ReflectionUtils.getAnnotationValue(type, CIRCUIT_BREAKER, "name");
            return circuitBreakerRegistry.circuitBreaker(name).getState().equals(CLOSED);
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            throw new IllegalArgumentException("리플렉션 중 문제가 발생했습니다.");
        }
    }
}
