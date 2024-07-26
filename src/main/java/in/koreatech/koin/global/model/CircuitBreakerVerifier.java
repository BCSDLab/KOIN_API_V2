package in.koreatech.koin.global.model;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.aop.framework.AopProxyUtils;

import in.koreatech.koin.global.domain.callcontol.exception.MethodNotFoundException;
import in.koreatech.koin.global.domain.callcontol.model.SubApi;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CircuitBreakerVerifier {

    private static final Class<CircuitBreaker> CIRCUIT_BREAKER = CircuitBreaker.class;

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    // private static Method getHasCircuitBreaker(SubApi subApi) {
    //     Class<?> subClass = AopProxyUtils.ultimateTargetClass(subApi.getSubApiType());
    //
    //     return Arrays.stream(subClass.getDeclaredMethods())
    //         .filter(method -> CIRCUIT_BREAKER.equals(method.getAnnotation(CIRCUIT_BREAKER).annotationType()))
    //         .findAny()
    //         .orElseThrow(() -> new MethodNotFoundException("클래스 내에 해당 어노테이션을 가지는 메서드가 없습니다."));
    // }
    //
    // private boolean verifyCircuitBreakerStatus(String name) {
    //     io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
    //     return circuitBreaker.getState().equals(State.CLOSED);
    // }
    //
    // private SubApi findOtherCallableApi(SubApi selectedApi) {
    //     try {
    //         callableApis.remove(selectedApi);
    //         return verifyCircuitBreakerStatus(callableApis.get(0));
    //     } catch (Exception e) {
    //         throw new IllegalArgumentException("모든 CircuitBreaker가 OPEN 되어 호출 가능한 Api가 없습니다.");
    //     }
    // }
}
