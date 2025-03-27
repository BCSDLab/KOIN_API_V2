package in.koreatech.koin._common.apiloadbalancer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin._common.util.reflection.ReflectionUtils;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApiLoadBalancer<T> {

    private static final Class<ApiLoadBalance> CALL_CONTROL_ANNOTATION = ApiLoadBalance.class;
    private static final String RATIO = "ratio";

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
        childTypes.forEach(
            child -> {
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

    public T fallBack(T failedType, List<T> fallBackableTypes) {
        fallBackableTypes.remove(failedType);
        return fallBackableTypes.get(0);
    }
}
