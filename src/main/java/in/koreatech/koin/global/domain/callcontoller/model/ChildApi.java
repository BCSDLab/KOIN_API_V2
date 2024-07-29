package in.koreatech.koin.global.domain.callcontoller.model;

import java.lang.reflect.Method;

import in.koreatech.koin.global.domain.callcontoller.CallControlInfo;
import in.koreatech.koin.global.model.ReflectionUtils;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChildApi {

    private static final Class<CallControlInfo> CALL_CONTROL_ANNOTATION = CallControlInfo.class;

    private final Object childType;
    private final Method targetMethod;
    private int start;
    private int end;

    public void callMethod() {
        ReflectionUtils.callMethod(childType, targetMethod);
    }

    public static ChildApi of(Object childType) {
        Method targetMethod = ReflectionUtils.findMethodByAnnotation(childType, CALL_CONTROL_ANNOTATION);
        return ChildApi.builder()
            .childType(childType)
            .targetMethod(targetMethod)
            .build();
    }

    public int setRange(int start) {
        int ratio = targetMethod.getAnnotation(CALL_CONTROL_ANNOTATION).ratio();
        this.start = start;
        this.end = start + ratio;
        return end;
    }
}
