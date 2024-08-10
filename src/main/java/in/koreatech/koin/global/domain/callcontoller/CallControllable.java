package in.koreatech.koin.global.domain.callcontoller;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.global.reflection.ReflectionUtils;

public interface CallControllable<T> {

    Class<CallControlInfo> CALL_CONTROL_ANNOTATION = CallControlInfo.class;
    String RATIO = "ratio";

    T getInstanceByRatio();

    default T selectCallApi(List<T> callApiList) {
        int lastIndex = callApiList.size() - 1;
        T callApi = callApiList.get(lastIndex);
        callApiList.remove(lastIndex);
        return callApi;
    }

    default List<T> generateCallApiList(List<T> children) {
        List<T> callApiList = new ArrayList<>();
        children.forEach(
            child -> {
                int ratio = getApiRatio(child);
                for (int i = 0; i < ratio; i++) {
                    callApiList.add(child);
                }
            }
        );
        return callApiList;
    }

    private int getApiRatio(T child) {
        return (int)ReflectionUtils.findAnnotationValue(child, CALL_CONTROL_ANNOTATION, RATIO);
    }
}
