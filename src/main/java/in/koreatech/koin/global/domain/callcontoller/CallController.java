package in.koreatech.koin.global.domain.callcontoller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.reflection.ReflectionUtils;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallController<T> {

    private static final Class<CallControlInfo> CALL_CONTROL_ANNOTATION = CallControlInfo.class;
    private static final String RATIO = "ratio";

    private final FallBack<T> fallBack;

    public T getInstanceByRatio(List<T> clientTypes, List<T> apiCallListByRatio) {
        if (apiCallListByRatio.isEmpty()) {
            apiCallListByRatio.addAll(generateApiCallListByRatio(clientTypes));
        }
        return selectCallApi(apiCallListByRatio);
    }

    private T selectCallApi(List<T> callListByRatio) {
        int lastIndex = callListByRatio.size() - 1;
        T callApi = callListByRatio.get(lastIndex);
        callListByRatio.remove(lastIndex);
        return callApi;
    }

    private List<T> generateApiCallListByRatio(List<T> childTypes) {
        List<T> callListByRatio = new ArrayList<>();
        childTypes.sort(Comparator.comparingInt(this::getChildRatio).reversed());
        childTypes.stream().forEach(child -> {
            System.out.println("타입순서: " + child);
        });
        childTypes.forEach(
            child -> {
                System.out.println(child + " 삽입!");
                int ratio = getChildRatio(child);
                for (int i = 0; i < ratio; i++) {
                    callListByRatio.add(child);
                }
            }
        );
        return callListByRatio;
    }

    private int getChildRatio(T child) {
        return (int)ReflectionUtils.getAnnotationValue(child, CALL_CONTROL_ANNOTATION, RATIO);
    }

    public T fallBack(boolean hasCircuitBreaker, T failedType, List<T> fallBackableTypes) {
        return fallBack.getOtherType(hasCircuitBreaker, failedType, fallBackableTypes);
    }
}
