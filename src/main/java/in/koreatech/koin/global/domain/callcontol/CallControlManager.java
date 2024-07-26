package in.koreatech.koin.global.domain.callcontol;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.domain.random.model.RandomGenerator;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Component
public class CallControlManager {

    private final CallControlRegistry callControlRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final List<ApiInfo> callableApis = new ArrayList<>();
    private CallControl callControl;

    public CallControlManager(
        CallControlRegistry callControlRegistry,
        CircuitBreakerRegistry circuitBreakerRegistry
    ) {
        this.callControlRegistry = callControlRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public void callApi(String className) {
        callControl = callControlRegistry.getCallControl(className);
        callableApis.addAll(callControl.apiInfos());
        int randomNum = RandomGenerator.createNumber(0, callControl.totalRatio());
        ApiInfo selectedApi = selectApi(randomNum);
        try {
            selectedApi.targetMethod().invoke(selectedApi.targetClass());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Api 호출에 실패하였습니다. message: " + e.getMessage());
        }
    }

    public ApiInfo selectApi(int randomNum) {
        return callControl.apiInfos().stream()
            .filter(apiRange -> apiRange.start() <= randomNum && apiRange.end() > randomNum)
            .map(this::verifyCircuitBreakerStatus)
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("범위를 벗어난 숫자입니다. number : " + randomNum));
    }

    public ApiInfo verifyCircuitBreakerStatus(ApiInfo selectedApi) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(selectedApi.name());
        boolean isClosed = circuitBreaker.getState().equals(CircuitBreaker.State.CLOSED);
        return isClosed ? selectedApi : findOtherCallableApi(selectedApi);
    }

    public ApiInfo findOtherCallableApi(ApiInfo selectedApi) {
        try {
            callableApis.remove(selectedApi);
            return verifyCircuitBreakerStatus(callableApis.get(0));
        } catch (Exception e) {
            throw new IllegalArgumentException("모든 CircuitBreaker가 OPEN 되어 호출 가능한 Api가 없습니다.");
        }
    }
}

