package in.koreatech.koin.global.domain.callcontoller;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.global.reflection.ReflectionUtils;

public interface CallControllable<T> {

    Class<CallControlInfo> CALL_CONTROL_ANNOTATION = CallControlInfo.class;
    String RATIO = "ratio";

    T getInstanceByRatio();

    default T selectCallApi(List<T> callListByRatio) {
        int lastIndex = callListByRatio.size() - 1;
        T callApi = callListByRatio.get(lastIndex);
        callListByRatio.remove(lastIndex);
        return callApi;
    }

    default List<T> generateApiCallListByRatio(List<T> childTypes) {
        List<T> callListByRatio = new ArrayList<>();
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
        return (int)ReflectionUtils.findAnnotationValue(child, CALL_CONTROL_ANNOTATION, RATIO);
    }
}
