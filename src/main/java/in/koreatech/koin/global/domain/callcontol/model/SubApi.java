package in.koreatech.koin.global.domain.callcontol.model;

import java.lang.reflect.Method;

import in.koreatech.koin.global.domain.callcontol.CallControlInfo;
import in.koreatech.koin.global.model.ReflectionUtils;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubApi {

    private static final Class<CallControlInfo> CALL_CONTROL_ANNOTATION = CallControlInfo.class;

    private final Object subApiType;
    private final Method targetMethod;
    private int start;
    private int end;

    public void callMethod() {
        ReflectionUtils.callMethod(subApiType, targetMethod);
    }

    public static SubApi of(Object subApiType) {
        Method targetMethod = ReflectionUtils.findMethodByAnnotation(subApiType, CALL_CONTROL_ANNOTATION);
        return SubApi.builder()
            .subApiType(subApiType)
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
