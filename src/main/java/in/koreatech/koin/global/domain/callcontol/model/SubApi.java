package in.koreatech.koin.global.domain.callcontol.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.aop.framework.AopProxyUtils;

import in.koreatech.koin.global.domain.callcontol.CallControlInfo;
import in.koreatech.koin.global.domain.callcontol.exception.MethodNotFoundException;
import lombok.Builder;

@Builder
public record SubApi(
    Object subApiType,
    Method targetMethod,
    int start,
    int end
) {

    private static final Class<CallControlInfo> CALL_CONTROL_ANNOTATION = CallControlInfo.class;

    public void callMethod() {
        try {
            this.targetMethod.invoke(subApiType);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Api 호출에 실패하였습니다. message: " + e.getMessage());
        }
    }

    public static SubApi of(Object subApiType, int startRange) {
        Method method = getMethodWithCallControlAnnotation(subApiType);
        int ratio = method.getAnnotation(CALL_CONTROL_ANNOTATION).ratio();
        return SubApi.builder()
            .subApiType(subApiType)
            .targetMethod(method)
            .start(startRange).end(startRange + ratio)
            .build();
    }

    private static Method getMethodWithCallControlAnnotation(Object subApiType) {
        Class<?> subClass = AopProxyUtils.ultimateTargetClass(subApiType);
        return Arrays.stream(subClass.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(CALL_CONTROL_ANNOTATION))
            .findAny()
            .orElseThrow(() -> new MethodNotFoundException("클래스 내에 해당 어노테이션을 가지는 메서드가 없습니다."));
    }
}
