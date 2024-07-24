package in.koreatech.koin.global.domain.callcontol;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.domain.random.model.RandomGenerator;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Component
public class CallControlManager {

    private final CallControl callControl;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final List<ApiInfo> apiInfos = new ArrayList<>();

    public CallControlManager(CallControl callControl, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.callControl = callControl;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public void callApi() {
        apiInfos.addAll(callControl.apiInfos());
        int randomNum = RandomGenerator.createNumber(0, callControl.totalRatio());
        System.out.println("======================================");
        System.out.println("정보");
        System.out.println("- 선택된 난수 : " + randomNum + "\n");
        ApiInfo selectedApi = selectApi(randomNum);
        try {
            System.out.println("최종 호출할 Api");
            System.out.println(String.format("""
                    - 선택된 클래스 : %s,
                    - 선택된 메서드 : %s,
                    """,
                selectedApi.name(),
                selectedApi.targetMethod().getName()
            ));
            System.out.println("======================================");
            selectedApi.targetMethod().invoke(selectedApi.targetClass());
        } catch (Exception e) {
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
        // 테스트용 출력코드
        System.out.println(String.format("""
                - 선택된 클래스 : %s,
                - 해당 클래스의 범위 : %d ~ %d,
                - 서킷브레이커(%s)의 상태 : %s
                - 선택된 메서드 : %s
                """,
            selectedApi.name(),
            selectedApi.start(), selectedApi.end(),
            circuitBreaker.getName(), circuitBreaker.getState(),
            selectedApi.targetMethod().getName()
        ));
        boolean isClosed = circuitBreaker.getState().equals(CircuitBreaker.State.CLOSED);
        return isClosed ? selectedApi : anotherCall(selectedApi);
    }

    public ApiInfo anotherCall(ApiInfo selectedInfo) {
        try {
            System.out.println("  => error: " + selectedInfo.name() + "의 CircuitBreaker가 OPEN 상태 이므로 다른 Api를 탐색합니다.\n");
            apiInfos.remove(selectedInfo);
            return verifyCircuitBreakerStatus(apiInfos.get(0));
        } catch (Exception e) {
            throw new IllegalArgumentException("호출 가능한 Api 없음 - 모든 Api의 CircuitBreaker가 OPEN 상태입니다.");
        }
    }
}

