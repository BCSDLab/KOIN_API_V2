package in.koreatech.koin.global.domain.callcontoller;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.global.model.ReflectionUtils;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CallApiList<T> {

    private static final Class<CallControlInfo> CALL_CONTROL_ANNOTATION = CallControlInfo.class;
    private static final String RATIO = "ratio";

    private List<T> callApiList;

    public CallApiList(List<T> children) {
        this.callApiList = generateCallApiList(children);
    }

    private List<T> generateCallApiList(List<T> children) {
        List<T> callControlList = new ArrayList<>();
        children.forEach(
            child -> {
                int ratio = getApiRatio(child);
                for (int i = 0; i < ratio; i++) {
                    callControlList.add(child);
                }
            }
        );
        return callControlList;
    }

    private int getApiRatio(T child) {
        return (int)ReflectionUtils.findAnnotationValue(child, CALL_CONTROL_ANNOTATION, RATIO);
    }
}
